# Scalability Demo – DOMDataBroker Transaction Chain Writes

This demo fires 1_000 independent write transactions against the MD‑SAL DOMDataBroker and measures how many commits succeed, how much fail,
and how long the burst takes.After analysing the high conflict rate caused by optimistic locking in **`dom-databroker`**,
**`dom-databroker-txchain`** switches to a MergingTransactionChain solution that achieves 100% success with no code blocking.

The code lives in **`mdsal/docs/examples/dom-databroker-txchain/`** and is self-contained –
it spins up an in-memory broker, loads the example YANG model, and performs the test.

---

## 1. Use-case description

Applications interacting with a YANG-modeled datastore might execute many small write transactions in rapid succession.
In this situation, each event might result in a new write transaction that:

1. Writes a single data node,
2. Commits immediately.

This test emulates that pattern by writing 1,000 `counter` values (1 to 1000) to the operational datastore using separate transactions.


### YANG model used
```yang
module test {
  namespace "urn:test";
  prefix test;

  revision 2025-05-13;

  leaf counter {
    type int32;
    config false;
  }
}
```
---

## 2. Building & Running the Demo

**Prerequisites:**

- JDK 21
- Maven 3.9+

### Build

```sh
# From the repository root
cd docs/examples/dom-databroker-txchain
mvn clean install -Pq
```

### Run
```sh
# From the repository root
cd docs/examples/dom-databroker-txchain/target
java -jar dom-databroker-txchain-x.x.x-SNAPSHOT.jar
```

## 3. Root cause of the original failures

### 3.1 Snapshot‑based optimistic locking

`DOMDataBroker.newWriteOnlyTransaction()` returns a snapshot view of the datastore –
see the [github](https://github.com/opendaylight/mdsal/blob/5b3ef3e5d00a43502b330ecbef0417528ed1de12/dom/mdsal-dom-api/src/main/java/org/opendaylight/mdsal/dom/api/DOMTransactionFactory.java#L69).
The snapshot captures the datastore generation at that moment. All writes are staged on top of that frozen view.
At `commit()` the broker validates that the head generation is unchanged; if not, it throws `OptimisticLockFailedException`.

### 3.2 What happened in the previous approach
````
for (int i = 0; i < 1_000; ++i) {
final var tx = broker.newWriteOnlyTransaction();   // all share initial generation
tx.put(OPERATIONAL, path, value(i));
tx.commit();
}
````
All 1_000 transactions start from the same initial generation. The first commit moves the head to newer generation
the remaining 999 commits now see a stale generation and fail.
One in three commits “wins the race”, hence the ~33% success rate.

## 4. Fix – Merging Transaction Chain

### 4.1 Concept

A `DOMTransactionChain` serialises transactions: the broker starts transaction N+1 only after commit N finishes.
A merging chain allows each transaction to write to the same subtree without conflicts while keeping the API asynchronous.

Key points:

All commits run on one thread inside the chain → no concurrent writes to the same path.

Because each transaction’s snapshot is taken after the previous commit, the generation is always current.

No blocking in user code – commits still complete via callbacks.

### 4.2 Implementation excerpt

````
final var chain = broker.createMergingTransactionChain();
for (int i = 0; i < 1_000; ++i) {
    final var tx = chain.newWriteOnlyTransaction();
    tx.put(OPERATIONAL, path, ImmutableNodes.leafNode(COUNTER_QNAME, i));
    tx.commit();
}
````

## 5 How Merging Transaction Chain works under the hood

* `createMergingTransactionChain()` returns a chain bound to a single worker thread.
* `newWriteOnlyTransaction()` captures the current datastore generation, not a stale one, because the previous commit has already completed.
* The broker queues each commit in order; if commits touch the same subtree it auto‑merges compatible modifications.
* Because the chain always operates on the latest generation, the head‑check inside `canCommit()` succeeds, so no `OptimisticLockFailedException` is thrown.

