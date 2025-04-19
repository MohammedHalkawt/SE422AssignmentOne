package Bonus;

public class ResultPrinter implements Runnable {
    private final PDFFileCounter coordinator;
    private final Counter singleThreadCount;
    private final Counter fourThreadsCount;
    private final Counter threadPoolCount;

    public ResultPrinter(PDFFileCounter coordinator, Counter single, Counter four, Counter pool) {
        this.coordinator = coordinator;
        this.singleThreadCount = single;
        this.fourThreadsCount = four;
        this.threadPoolCount = pool;
    }

    @Override
    public void run() {
        System.out.println("--- Live Counting Updates ---");
        try {

            System.out.println(coordinator.getNextUpdate());
            String message;
            while (!(message = coordinator.getNextUpdate()).startsWith("Final single thread count")) {
                System.out.println(message);
            }
            System.out.println(message);
            System.out.println();


            System.out.println(coordinator.getNextUpdate());
            System.out.println("\n--- Four-Threaded Counting ---");
            while (!(message = coordinator.getNextUpdate()).startsWith("Final four threads count")) {
                System.out.println(message);
            }
            System.out.println(message);
            System.out.println();

        
            System.out.println(coordinator.getNextUpdate());
            System.out.println("\n--- Thread Pool Counting ---");
            while (!(message = coordinator.getNextUpdate()).startsWith("Final thread pool count")) {
                System.out.println(message);
            }
            System.out.println(message);
            System.out.println();

            System.out.println(coordinator.getNextUpdate());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("\n--- Final Results from Printer Thread ---");
        System.out.println("Single thread count: " + singleThreadCount.getCount());
        System.out.println("Four threads count: " + fourThreadsCount.getCount());
        System.out.println("Thread pool count: " + threadPoolCount.getCount());
    }
}