# EasyQueue

`EasyQueue` is a thread-safe, flexible, and easy-to-use queue with a fixed size, supporting batch confirmation and rollback operations. It also provides a mechanism for graceful shutdown via the `shutdown` function.

## Key Features
- **Fixed Size**: Automatically removes old elements when the queue exceeds its maximum size.
- **Batch Confirmation and Rollback**: Allows confirming processed elements and reverting to a previous state.
- **Thread-Safe**: Safely operates in a multithreaded environment using `ReentrantLock`.
- **Graceful Shutdown**: Prevents further operations on the queue once shut down.
- **State Retrieval**: Access the current state of the queue for diagnostics or persistence.

---

## Installation

Add the `EasyQueue` source code to your project or package it as a JAR file for reuse in other applications.

---

## Usage

### Create a Queue
```java
EasyQueue<String> queue = new EasyQueue<>(5); // Maximum queue size: 5
```

### Add Elements
```java
queue.add("A");
queue.add("B");
queue.add("C");
```

### Poll Elements in Batch
```java
List<String> batch = queue.pollBatch(2); // Retrieves 2 elements from the queue
System.out.println(batch); // [A, B]
```

### Confirm Processing
```java
queue.confirmBatch(); // Confirms the processing of polled elements
```

### Rollback Processing
```java
queue.rollbackBatch(); // Reverts the pointer to the last confirmed position
```

### Check Queue State
```java
boolean isEmpty = queue.isEmpty(); // true if the queue is empty
int size = queue.size(); // Returns the current size of the queue
```

### Shutdown the Queue
```java
EasyQueueState<String> state = queue.shutdown(); // Shuts down the queue and retrieves its state
System.out.println(state.getEntries()); // Prints remaining entries in the queue
```

---

## Example

```java
public class EasyQueueExample {
    public static void main(String[] args) {
        EasyQueue<String> queue = new EasyQueue<>(3);

        queue.add("A");
        queue.add("B");
        queue.add("C");

        System.out.println(queue.pollBatch(2)); // [A, B]
        queue.confirmBatch();

        queue.add("D");
        queue.add("E"); // Overflows, old elements are removed

        System.out.println(queue.pollBatch(3)); // [C, D, E]
        queue.shutdown();

        // Attempting to add elements after shutdown
        try {
            queue.add("F");
        } catch (EasyQueueShutdownException e) {
            System.out.println("Queue is shut down!");
        }
    }
}
```

---

## Thread Safety
The queue is implemented using `ReentrantLock` to synchronize operations, ensuring thread safety in multithreaded environments. However, for high concurrency scenarios, performance testing in your specific environment is recommended.

---

## Testing
The library is tested using JUnit 5. The repository includes examples of tests for functionality, multithreaded operations, and graceful shutdown handling.

---

## Limitations
- The queue supports in-memory storage only. Persistence must be implemented externally if required.
- The `shutdown` function makes the queue immutable, preventing any further operations once invoked.

---

## License
This library is provided "as is." You are free to use it in personal and commercial projects.

