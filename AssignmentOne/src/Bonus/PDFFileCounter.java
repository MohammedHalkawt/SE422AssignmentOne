package Bonus;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Names: Your Name 1, Your Name 2
 * Emails: your_email1@example.com, your_email2@example.com
 */

class Counter {
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

class DirectoryScanner {
    public static List<File> getAllFiles(File directory) {
        List<File> fileList = new ArrayList<>();
        scanDirectory(directory, fileList);
        return fileList;
    }

    private static void scanDirectory(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, fileList);
                } else {
                    fileList.add(file);
                }
            }
        }
    }
}

class ResultPrinter implements Runnable {
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

public class PDFFileCounter {
    private final List<String> updateMessages = new LinkedList<>();

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

    public static void main(String[] args) throws InterruptedException {
        PDFFileCounter coordinator = new PDFFileCounter();

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

        Counter singleThreadCount = new Counter(coordinator);
        Counter fourThreadsCount = new Counter(coordinator);
        Counter threadPoolCount = new Counter(coordinator);

        System.out.println("\nLaunching result printer thread...");
        Thread resultPrinterThread = new Thread(new ResultPrinter(
                coordinator, singleThreadCount, fourThreadsCount, threadPoolCount));
        resultPrinterThread.start();

        System.out.println("\nCounting with single thread...");
        coordinator.addUpdate("--- Single-Threaded Counting ---");
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
        coordinator.addUpdate("Starting four-threaded count...");
        ExecutorService fourThreadExecutor = Executors.newFixedThreadPool(4);
        int chunkSize = allFiles.size() / 4;
        for (int i = 0; i < 4; i++) {
            final int start = i * chunkSize;
            final int end = (i == 3) ? allFiles.size() : start + chunkSize;
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
        coordinator.addUpdate("Starting thread pool count...");
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

        coordinator.addUpdate("Final Results");
        try {
            resultPrinterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}