package by.aurorasoft.easyqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class EasyQueue<T> {
    private final List<T> entries;
    private final int maxSize;
    private int lastSentIndex = -1;
    private int lastConfirmedIndex = -1;
    private final ReentrantLock lock = new ReentrantLock();

    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public EasyQueue(int maxSize) {
        this.entries = new ArrayList<>(maxSize);
        this.maxSize = maxSize;
    }

    public void add(T element) {
        lock.lock();
        if (isShutdown.get()) {
            throw new EasyQueueShutdownException();
        }
        try {
            if (entries.size() >= maxSize) {
                removeBeforeLastConfirmed();
            }
            entries.add(element);
        } finally {
            lock.unlock();
        }
    }

    private void removeBeforeLastConfirmed() {
        if (lastConfirmedIndex >= 0) {
            entries.subList(0, lastConfirmedIndex + 1).clear();
            lastSentIndex -= (lastConfirmedIndex + 1);
            lastConfirmedIndex = -1;
        } else if (entries.size() >= maxSize) {
            int elementsToRemove = Math.max(1, maxSize / 5);
            entries.subList(0, elementsToRemove).clear();
            lastSentIndex -= elementsToRemove;
        }
    }

    public List<T> pollBatch(int n) {
        if (isShutdown.get()) {
            throw new EasyQueueShutdownException();
        }
        lock.lock();
        try {
            int start = lastConfirmedIndex + 1;
            int end = Math.min(start + n, entries.size());

            if (start >= end) {
                return new ArrayList<>();
            }

            List<T> result = new ArrayList<>(entries.subList(start, end));
            lastSentIndex = end - 1;
            return result;
        } finally {
            lock.unlock();
        }
    }

    public void confirmBatch() {
        if (isShutdown.get()) {
            throw new EasyQueueShutdownException();
        }
        lock.lock();
        try {
            lastConfirmedIndex = lastSentIndex;
        } finally {
            lock.unlock();
        }
    }

    public void rollbackBatch() {
        if (isShutdown.get()) {
            throw new EasyQueueShutdownException();
        }
        lock.lock();
        try {
            lastSentIndex = lastConfirmedIndex;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return entries.size();
        } finally {
            lock.unlock();
        }
    }

    public EasyQueueState<T> getState() {
        return new EasyQueueState<>(new ArrayList<>(entries), maxSize, lastSentIndex, lastConfirmedIndex);
    }

    public void restoreState(EasyQueueState<T> state) {
        lock.lock();
        try {
            this.entries.clear();
            this.entries.addAll(state.getEntries());
            this.lastConfirmedIndex = state.getLastConfirmedIndex();
            this.lastSentIndex = state.getLastSentIndex();
        } finally {
            lock.unlock();
        }
    }

    public EasyQueueState<T> shutdown() {
        lock.lock();
        try {
            if (lastConfirmedIndex > -1) {
                removeBeforeLastConfirmed();
            }
            isShutdown.set(true);
            return getState();
        } finally {
            lock.unlock();
        }
    }

}
