import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PDFFileCounter {
    public static void main(String[] args) {
        System.out.println("Enter the path to an existing directory:");
        Scanner scanner = new Scanner(System.in);
        String directoryPath = scanner.nextLine();
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory path.");
            scanner.close();
            return;
        }

        List<File> allFiles = DirectoryScanner.getAllFiles(directory);
        System.out.println("Total files found: " + allFiles.size());

        // Shared lock for wait/notify
        Object lock = new Object();

        // Initialize counters
        VolatileCounter singleThreadCount = new VolatileCounter();
        VolatileCounter fourThreadsCount = new VolatileCounter();
        VolatileCounter threadPoolCount = new VolatileCounter();

        // Start printer thread early (it will wait)
        Thread resultPrinterThread = new Thread(new ResultPrinter(
            singleThreadCount, fourThreadsCount, threadPoolCount, lock));
        resultPrinterThread.start();

        // 1. Single-threaded count
        System.out.println("\nCounting with single thread...");
        Thread singleThread = new Thread(() -> {
            for (File file : allFiles) {
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    singleThreadCount.increment();
                }
            }
        });
        singleThread.start();
        try {
            singleThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 2. Four-threaded count (unchanged)
        System.out.println("\nCounting with four threads...");
        ExecutorService fourThreadExecutor = Executors.newFixedThreadPool(4);
        int filesPerThread = allFiles.size() / 4;
        int remainingFiles = allFiles.size() % 4;
        int currentIndex = 0;

        for (int i = 0; i < 4; i++) {
            int numFilesToProcess = filesPerThread + (i < remainingFiles ? 1 : 0);
            final int start = currentIndex;
            final int end = currentIndex + numFilesToProcess;
            fourThreadExecutor.execute(() -> {
                for (int j = start; j < end; j++) {
                    if (allFiles.get(j).getName().toLowerCase().endsWith(".pdf")) {
                        fourThreadsCount.increment();
                    }
                }
            });
            currentIndex = end;
        }
        fourThreadExecutor.shutdown();
        while (!fourThreadExecutor.isTerminated()) {}

        // 3. Thread pool with CountDownLatch
        System.out.println("\nCounting with thread pool (volatile + latch)...");
        int poolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        CountDownLatch startLatch = new CountDownLatch(1);  // Ensure all threads start together

        for (File file : allFiles) {
            threadPool.execute(() -> {
                try {
                    startLatch.await();  // Wait for the signal
                    if (file.getName().toLowerCase().endsWith(".pdf")) {
                        threadPoolCount.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Release all threads
        startLatch.countDown();
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {}

        // Notify printer thread to print results
        synchronized (lock) {
            lock.notify();
        }

        try {
            resultPrinterThread.join();  // Wait for printer thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        scanner.close();
    }
}