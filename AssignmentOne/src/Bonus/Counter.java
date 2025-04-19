package Bonus;

public class Counter {
    private int count = 0;
    private final PDFFileCounter coordinator;

    public Counter(PDFFileCounter coordinator) {
        this.coordinator = coordinator;
    }

    public void increment(String threadName, String counterType) {
        synchronized (this) {
            count++;
        }
        String updateMessage = counterType + " count: " + count + (threadName != null ? " (" + threadName + ")" : "");
        coordinator.addUpdate(updateMessage);
    }

    public synchronized int getCount() {
        return count;
    }

    public void sendFinalCount(String counterType) {
        coordinator.addUpdate("Final " + counterType + " count: " + getCount());
    }
}