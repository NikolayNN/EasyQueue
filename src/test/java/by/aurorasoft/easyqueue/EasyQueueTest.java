package by.aurorasoft.easyqueue;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EasyQueueTest {
    @Test
    void testAddAndSize() {
        EasyQueue queue = new EasyQueue<>(3);
        queue.add("A");
        queue.add("B");  EasyQueueState<String> state = queue.getState();
        assertEquals(2, state.getEntries().size(), "Size should be 2 after adding 2 elements");
    }

    @Test
    void testAddA() {
        EasyQueue<String> queue = new EasyQueue<>(3);

        List<String> actual = queue.pollBatch(2);
        assertTrue(actual.isEmpty());
    }

    @Test
    void testConfirm() {
        EasyQueue<String> queue = new EasyQueue<>(3);

        queue.confirmBatch();
    }

    @Test
    void testPollBatch() {
        EasyQueue<String> queue = new EasyQueue<>(3);
        queue.add("A");
        queue.add("B");
        queue.add("C");

        List<String> batch = queue.pollBatch(2);
        assertEquals(List.of("A", "B"), batch, "PollBatch should return the first 2 elements");

        EasyQueueState<String> state = queue.getState();
        assertEquals(3, state.getEntries().size(), "Size should not change after polling");
    }

    @Test
    void testConfirmBatch() {
        EasyQueue<String> queue = new EasyQueue<>(3);
        queue.add("A");
        queue.add("B");
        queue.add("C");

        queue.pollBatch(2);
        queue.confirmBatch();

        EasyQueueState<String> state = queue.getState();
        assertEquals(3, state.getEntries().size(), "Size should remain unchanged after confirming a batch");
    }

    @Test
    void testRollbackBatch() {
        EasyQueue<String> queue = new EasyQueue<>(3);
        queue.add("A");
        queue.add("B");
        queue.add("C");

        List<String> batch = queue.pollBatch(2);
        queue.rollbackBatch();

        assertEquals(List.of("A", "B"), batch, "Rollback should not remove elements from the queue");

        EasyQueueState<String> state = queue.getState();
        assertEquals(3, state.getEntries().size(), "Size should remain unchanged after rollback");
    }

    @Test
    void testAddBeyondMaxSizeWithoutConfirmed() {
        EasyQueue<String> queue = new EasyQueue<>(5);

        queue.add("A");
        queue.add("B");
        queue.add("C");
        queue.add("D");
        queue.add("E");

        queue.add("F"); // Limit reached. Remove 20% oldest elements.

        EasyQueueState<String> state = queue.getState();
        assertEquals(5, state.getEntries().size(), "Size should remain within maxSize");
        assertEquals(List.of("B", "C", "D", "E", "F"), state.getEntries(), "Oldest 20% elements should be removed");
    }

    @Test
    void testAddBeyondMaxSizeWithConfirmed() {
        EasyQueue<String> queue = new EasyQueue<>(5);

        queue.add("A");
        queue.add("B");
        queue.add("C");
        queue.add("D");
        queue.add("E");

        queue.pollBatch(3); // A, B, C
        queue.confirmBatch(); // Confirm A, B, C

        queue.add("F"); // Limit reached. Confirmed elements removed.
        queue.add("G");

        EasyQueueState<String> state = queue.getState();
        assertEquals(4, state.getEntries().size(), "Size should be 4 after removing confirmed elements");
        assertEquals(List.of("D", "E", "F", "G"), state.getEntries(), "Confirmed elements should be removed");
    }

    @Test
    void testShutdownPreventsAdd() {
        EasyQueue<String> queue = new EasyQueue<>(3);
        queue.add("A");
        queue.add("B");

        // Shutdown queue
        queue.shutdown();

        // Verify add throws exception
        assertThrows(EasyQueueShutdownException.class, () -> queue.add("C"), "Add should throw exception after shutdown");
    }

    @Test
    void testShutdownPreventsPollBatch() {
        EasyQueue<String> queue = new EasyQueue<>(3);
        queue.add("A");
        queue.add("B");

        // Shutdown queue
        queue.shutdown();

        // Verify pollBatch throws exception
        assertThrows(EasyQueueShutdownException.class, () -> queue.pollBatch(1), "PollBatch should throw exception after shutdown");
    }

    @Test
    void testShutdownPreventsConfirmBatch() {
        EasyQueue<String> queue = new EasyQueue<>(3);
        queue.add("A");
        queue.add("B");
        queue.pollBatch(2);

        // Shutdown queue
        queue.shutdown();

        // Verify confirmBatch throws exception
        assertThrows(EasyQueueShutdownException.class, queue::confirmBatch, "ConfirmBatch should throw exception after shutdown");
    }

    @Test
    void testShutdownPreventsRollbackBatch() {
        EasyQueue<String> queue = new EasyQueue<>(3);
        queue.add("A");
        queue.add("B");
        queue.pollBatch(2);

        // Shutdown queue
        queue.shutdown();

        // Verify rollbackBatch throws exception
        assertThrows(EasyQueueShutdownException.class, queue::rollbackBatch, "RollbackBatch should throw exception after shutdown");
    }

}
