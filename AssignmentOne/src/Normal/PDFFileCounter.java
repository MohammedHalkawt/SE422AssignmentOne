package Normal;
import java.io.File;
import java.util.ArrayList;
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

    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
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

        Counter singleThreadCount = new Counter();
        Counter fourThreadsCount = new Counter();
        Counter threadPoolCount = new Counter();

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
            e.printStackTrace();
        }

        System.out.println("\nCounting with four threads...");
        ExecutorService fourThreadExecutor = Executors.newFixedThreadPool(4);
        int filesPerThread = allFiles.size() / 4;
        int remainingFiles = allFiles.size() % 4;
        int currentIndex = 0;

        for (int i = 0; i < 4; i++) {
            int numFilesToProcess = filesPerThread;
            if (i < remainingFiles) {
                numFilesToProcess++;
            }
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
        while (!fourThreadExecutor.isTerminated()) {
        }

        System.out.println("\nCounting with thread pool...");
        int poolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        for (File file : allFiles) {
            threadPool.execute(() -> {
                if (file.getName().toLowerCase().endsWith(".pdf")) {
                    threadPoolCount.increment();
                }
            });
        }
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
        }

        Thread resultPrinterThread = new Thread(new ResultPrinter(
                singleThreadCount, fourThreadsCount, threadPoolCount));
        resultPrinterThread.start();
        try {
            resultPrinterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}