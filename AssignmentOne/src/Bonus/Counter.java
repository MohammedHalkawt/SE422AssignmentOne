package Bonus;

public class Counter {
    private int count = 0;
    private final PDFFileCounter countHolder;

    public Counter(PDFFileCounter countHolder) {
        this.countHolder = countHolder;
    }

    public void increment(String threadName, String counterType) {
        synchronized (this) {
            count++;
        }
        String updateMessage = counterType + " count: " + count + " (" + threadName + ")";
        countHolder.addUpdate(updateMessage);
    }

    public synchronized int getCount() {
        return count;
    }

    public void sendFinalCount(String counterType) {
        countHolder.addUpdate("Final " + counterType + " count: " + getCount());
    }
}