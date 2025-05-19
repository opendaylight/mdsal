# Scalability Demo – DOMDataBroker Transaction Chain Writes

This demo fires 1_000 independent write transactions against the MD‑SAL DOMDataBroker and measures how many commits
succeed, how much fail, and how long the burst takes. After analysing the high conflict rate caused by optimistic
locking in **`dom-databroker`**,**`dom-databroker-txchain`** switches to a MergingTransactionChain solution that
achieves 100% success with no code blocking.

The code lives in **`mdsal/docs/examples/dom-databroker-txchain/`** and is self-contained –
it spins up an in-memory broker, loads the example YANG model, and performs the test.

---

## 1. Use-case description

Applications interacting with a YANG-modeled datastore might execute many small write transactions in rapid succession.
In this situation, each event might result in a new write transaction that:

1. Writes a single data node,
2. Commits immediately.

This test emulates that pattern by writing 1,000 `counter` values (1 to 1000) to the operational datastore using
separate transactions.


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
see the [Java docs](../../../dom/mdsal-dom-api/src/main/java/org/opendaylight/mdsal/dom/api/DOMTransactionFactory.java#L190).
The snapshot captures the datastore generation at that moment. All writes are staged on top of that frozen view.
At `commit()` the broker validates that the head generation is unchanged; if not, it throws
`OptimisticLockFailedException`.

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

A [`DOMTransactionChain`](../../../dom/mdsal-dom-api/src/main/java/org/opendaylight/mdsal/dom/api/DOMTransactionChain.java#L43)
serialises transactions: the broker starts transaction N+1 only after commit N finishes. A merging chain allows each
transaction to write to the same subtree without conflicts while keeping the API asynchronous.

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

## 5 Results

### 5.1 Independent transactions

Total tx:1000  Success tx:332  Failure tx:668 Observed by listener tx: 332 Elapsed:267ms  TPS:3745.3

Conflicts - 668 failed commits

### 5.2 Merging Transaction Chain

Total tx:1000  Success tx:1000  Failure tx:0 Observed by listener tx: 16 Elapsed:35 ms  TPS:28571.4

Conflict‑free – 0 failed commits.

(The absolute numbers vary by hardware and JVM options but the qualitative improvement is consistent.)

## 6 How Merging Transaction Chain works under the hood

* [`createMergingTransactionChain()`](../../../dom/mdsal-dom-api/src/main/java/org/opendaylight/mdsal/dom/api/DOMDataBroker.java#L70)
returns a chain bound to a single worker thread.
* [`newWriteOnlyTransaction()`](../../../dom/mdsal-dom-api/src/main/java/org/opendaylight/mdsal/dom/api/DOMTransactionChain.java#L82)
captures the current datastore generation, not a stale one, because the previous commit has already completed.
* The broker queues each commit in order; if commits touch the same subtree it auto‑merges compatible modifications.
* Because the chain always operates on the latest generation, the head‑check inside `canCommit()` succeeds,
so no `OptimisticLockFailedException` is thrown.

## 7. Why we chose createMergingTransactionChain() over a regular createTransactionChain()

Choosing between the two chain types boils down to the classic trade‑off between strict sequencing guarantees and raw
write throughput with minimal code overhead.
A plain TransactionChain demands that you open the next transaction only after the previous commit has finished.
If you forget to enforce that discipline(as the simple loop in
[previous demo](../dom-databroker/src/main/java/org/opendaylight/mdsal/ScalabilityDemo.java) did),
the chain almost immediately falls into the `FAILED` state because each child transaction still holds
the old snapshot and collides with the datastore head. A MergingTransactionChain, on the other hand,
transparently re‑bases every logical transaction onto the latest committed snapshot and even fuses compatible writes,
so you can fire a thousand commit() calls back‑to‑back without a single conflict.

## 8. Observed DataTreeChangeListener events
The demo registers a DOMDataTreeChangeListener on the same counter leaf for both the simple independent transactions
demo(section 5.1) and the MergingTransactionChain (section 5.2). The listener reports the number of
times it sees a data change event:

* Independent transactions (independent transactions loop): Observed events: 332

* MergingTransactionChain: Observed events: 16

* Why 332 events in independent transactions?<br>
Each of the 332 successful commits writes a new leaf value to the datastore, and the listener receives a distinct change
event for each. Although 1000 write attempts are made, only the 332 non-conflicting ones propagate a change.

* Why only 16 events in MergingTransactionChain?<br>
The merging chain batches and coalesces rapid updates into fewer physical write operations. Internally,
it groups many logical commits touching the same leaf into approximately 16 physical changes,
so the listener is notified only once per batched write rather than once per logical transaction.

Together with the success/failure and performance metrics, these observations highlight that the merging chain not only
eliminates conflicts but also reduces the number of notifications and overall work performed by the datastore.

