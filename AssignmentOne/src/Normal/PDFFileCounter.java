package Normal;
import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    Names: Mohammed Halkawt Ali, Kamiran Sulaiman Ilyas
    Emails: mh21096@auis.edu.krd, ks20096@auis.edu.krd
 */

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
        //Note: Here we scanned all files in the beginning and added them to this List to increase efficiency,
        //  since if the threads themselves were to scan the directory everytime they wanted to count, it would be very inefficient.
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