# Scalability Demo – DOM DataBroker Write Transactions

This demo stresses the **MD-SAL DOM DataBroker** by opening **1 000 independent write transactions**, each of which writes a single value and commits immediately. 
It measures how many commits succeed or fail and how long the whole burst takes.

The code lives in **`mdsal/docs/examples/dom-databroker/`** and is self-contained – it spins up an in-memory broker, loads the example YANG model, and performs the test.

---

## 1. Use-case description

Applications interacting with a YANG-modeled datastore might execute many small write transactions in rapid succession.
In this situation, each event might result in a new write transaction that:

1. writes a single data node,
2. commits immediately.

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
cd docs/examples/dom-databroker
mvn clean install -Pq
```

### Run
```sh
# From the repository root
cd docs/examples/dom-databroker/target
java -jar dom-databroker-x.x.x-SNAPSHOT.jar
```

## 3. Observed results

|       Metric       |    Value    |           Notes           |
|:------------------:|:-----------:|:-------------------------:|
|  Java heap (used)  | 508.00 MB   |                           |
|    Transactions    |    1 000    | loop writes counter = i   |
| Successful commits |     332     | 33 % success              |
|   Failed commits   |     668     | optimistic-lock conflicts |
|    Elapsed time    |   267 ms    | ≈ 3.7 k tx/s              |

Important: results vary with CPU, JVM flags, logging, etc.

## 4. Results analysis

* Conflict rate: All transactions target the same leaf /counter. Because commits execute concurrently, 668 / 1 000 (~67 %) failed with OptimisticLockFailedException.

* Throughput: The run achieved ≈ 3 745 tx/s on an in-memory store; most time is object allocation & callback overhead.
