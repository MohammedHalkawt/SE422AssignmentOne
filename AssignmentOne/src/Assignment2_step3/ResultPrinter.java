package Assignment2_Step3;

public class ResultPrinter implements Runnable {
    private final VolatileCounter threadPoolCount;
    private final Object lock;

    public ResultPrinter(VolatileCounter poolCount, Object lock) {
        this.threadPoolCount = poolCount;
        this.lock = lock;
    }

    @Override
    public void run() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("\n--- Final Result ---");
        System.out.println("PDF files counted: " + threadPoolCount.getCount());
    }
}
