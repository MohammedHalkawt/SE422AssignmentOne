package Assignment2_Step3;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

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

        Object lock = new Object();
        VolatileCounter threadPoolCount = new VolatileCounter();

        Thread resultPrinterThread = new Thread(new ResultPrinter(threadPoolCount, lock));
        resultPrinterThread.start();

        System.out.println("\nCounting with thread pool");
        int poolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        CountDownLatch startLatch = new CountDownLatch(1);

        for (File file : allFiles) {
            threadPool.execute(() -> {
                try {
                    startLatch.await();
                    if (file.getName().toLowerCase().endsWith(".pdf")) {
                        threadPoolCount.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startLatch.countDown();
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread pool processing interrupted");
        }

        synchronized (lock) {
            lock.notify();
        }

        try {
            resultPrinterThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        scanner.close();
    }
}
