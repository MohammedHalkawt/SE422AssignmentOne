public class ResultPrinter implements Runnable {
    private final VolatileCounter singleThreadCount;
    private final VolatileCounter fourThreadsCount;
    private final VolatileCounter threadPoolCount;
    private final Object lock;  // Shared lock for wait/notify

    public ResultPrinter(VolatileCounter single, VolatileCounter four, VolatileCounter pool, Object lock) {
        this.singleThreadCount = single;
        this.fourThreadsCount = four;
        this.threadPoolCount = pool;
        this.lock = lock;
    }

    @Override
    public void run() {
        synchronized (lock) {
            try {
                lock.wait();  // Wait until notified by the main thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("\n--- Final Results (Event-Driven) ---");
        System.out.println("Single thread count: " + singleThreadCount.getCount());
        System.out.println("Four threads count: " + fourThreadsCount.getCount());
        System.out.println("Thread pool count: " + threadPoolCount.getCount());
    }
}