package Normal;

public class ResultPrinter implements Runnable {
    private final Counter singleThreadCount;
    private final Counter fourThreadsCount;
    private final Counter threadPoolCount;

    public ResultPrinter(Counter single, Counter four, Counter pool) {
        this.singleThreadCount = single;
        this.fourThreadsCount = four;
        this.threadPoolCount = pool;
    }

    @Override
    public void run() {
        System.out.println("\n--- Final Results from Printer Thread ---");
        System.out.println("Single thread count: " + singleThreadCount.getCount());
        System.out.println("Four threads count: " + fourThreadsCount.getCount());
        System.out.println("Thread pool count: " + threadPoolCount.getCount());
    }
}