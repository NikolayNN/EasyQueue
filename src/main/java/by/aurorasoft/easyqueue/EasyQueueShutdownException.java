package by.aurorasoft.easyqueue;

public class EasyQueueShutdownException extends RuntimeException {
    public EasyQueueShutdownException() {
        this("Queue stopped");
    }

    public EasyQueueShutdownException(String message) {
        super(message);
    }
}
