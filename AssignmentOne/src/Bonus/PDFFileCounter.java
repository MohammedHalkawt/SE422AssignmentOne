package Bonus;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    Names: Mohammed Halkawt Ali, Kamiran Sulaiman Ilyas
    Emails: mh21096@auis.edu.krd, ks20096@auis.edu.krd
 */

public class PDFFileCounter {
    private final List<String> updateMessages = new LinkedList<>();
    public static void main(String[] args) throws InterruptedException {
        PDFFileCounter countHolder = new PDFFileCounter();

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
        //Note: Here we scanned all files in the beginning and added them to this List to increase efficiency,
            //  since if the threads themselves were to scan the directory everytime they wanted to count, it would be very inefficient.

        Counter singleThreadCount = new Counter(countHolder);
        Counter fourThreadsCount = new Counter(countHolder);
        Counter threadPoolCount = new Counter(countHolder);

        System.out.println("\nLaunching result printer thread...");
        Thread resultPrinterThread = new Thread(new ResultPrinter(
                countHolder, singleThreadCount, fourThreadsCount, threadPoolCount));
        resultPrinterThread.start();

        System.out.println("\nCounting with single thread...");
        countHolder.addUpdate("--- Single-Threaded Counting ---");
        Thread singleThread = new Thread(() -> {
            for (File file : allFiles) {
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    singleThreadCount.increment(Thread.currentThread().getName(), "Single thread");
                }
            }
            singleThreadCount.sendFinalCount("single thread");
        });
        singleThread.start();
        try {
            singleThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nCounting with four threads...");
        countHolder.addUpdate("Starting four-threaded count...");
        ExecutorService fourThreadExecutor = Executors.newFixedThreadPool(4);
        int workload = allFiles.size() / 4;
        for (int i = 0; i < 4; i++) {
            final int start = i * workload;
            final int end = (i == 3) ? allFiles.size() : start + workload;
            fourThreadExecutor.execute(() -> {
                for (int j = start; j < end; j++) {
                    if (allFiles.get(j).getName().toLowerCase().endsWith(".pdf")) {
                        fourThreadsCount.increment(Thread.currentThread().getName(), "Four threads");
                    }
                }
            });
        }
        fourThreadExecutor.shutdown();
        try {
            fourThreadExecutor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        fourThreadsCount.sendFinalCount("four threads");

        System.out.println("\nCounting with thread pool...");
        countHolder.addUpdate("Starting thread pool count...");
        int poolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);

        for (File file : allFiles) {
            threadPool.execute(() -> {
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    threadPoolCount.increment(Thread.currentThread().getName(), "Thread pool");
                }
            });
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        threadPoolCount.sendFinalCount("thread pool");

        countHolder.addUpdate("Final Results");
        try {
            resultPrinterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    public synchronized void addUpdate(String message) {
        updateMessages.add(message);
        notifyAll();
    }

    public synchronized String getNextUpdate() throws InterruptedException {
        while (updateMessages.isEmpty()) {
            wait();
        }
        return updateMessages.remove(0);
    }
}