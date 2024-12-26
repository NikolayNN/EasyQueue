package by.aurorasoft.easyqueue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EasyQueueConcurrentTest {

    @Test
    void testConcurrentAddAndPoll() throws InterruptedException, ExecutionException {
        int maxSize = 100;
        EasyQueue<Integer> queue = new EasyQueue<>(maxSize);
        int threadCount = 10;
        int operationsPerThread = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Void>> futures = new ArrayList<>();

        // AtomicInteger для проверки количества добавленных и извлечённых элементов
        AtomicInteger addedElements = new AtomicInteger(0);
        AtomicInteger polledElements = new AtomicInteger(0);

        // Потоки для добавления элементов
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    queue.add(addedElements.incrementAndGet());
                }
                return null;
            }));
        }

        // Потоки для извлечения элементов
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    List<Integer> batch = queue.pollBatch(1);
                    if (!batch.isEmpty()) {
                        polledElements.incrementAndGet();
                    }
                }
                return null;
            }));
        }

        // Ожидаем завершения всех потоков
        for (Future<Void> future : futures) {
            future.get();
        }

        executor.shutdown();

        // Проверяем, что число извлечённых элементов не превышает число добавленных
        assertTrue(polledElements.get() <= addedElements.get(), "Polled elements should not exceed added elements");

        // Проверяем, что очередь не превышает максимального размера
        assertTrue(queue.size() <= maxSize, "Queue size should not exceed maxSize");
    }
}
