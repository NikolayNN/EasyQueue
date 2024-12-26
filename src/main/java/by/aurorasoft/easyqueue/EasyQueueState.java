package by.aurorasoft.easyqueue;

import java.io.Serializable;
import java.util.List;

public class EasyQueueState<T> implements Serializable {
    private final List<T> entries;
    private final int maxSize;
    private final int lastSentIndex;
    private final int lastConfirmedIndex;

    public EasyQueueState(List<T> entries, int maxSize, int lastSentIndex, int lastConfirmedIndex) {
        this.entries = entries;
        this.maxSize = maxSize;
        this.lastSentIndex = lastSentIndex;
        this.lastConfirmedIndex = lastConfirmedIndex;
    }

    public List<T> getEntries() {
        return entries;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getLastSentIndex() {
        return lastSentIndex;
    }

    public int getLastConfirmedIndex() {
        return lastConfirmedIndex;
    }
}
