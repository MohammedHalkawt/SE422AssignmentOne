import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class PDFFileCounter {
    public static void main(String[] args) {
        // 1. Get directory input
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter directory path:");
        File directory = new File(scanner.nextLine().trim());
        
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Invalid directory path");
            scanner.close();
            return;
        }

        // 2. Scan files
        List<File> allFiles = DirectoryScanner.getAllFiles(directory);
        System.out.println("Files found: " + allFiles.size());
        scanner.close();

        // 3. Initialize components
        FileCounter counter = new FileCounter();
        FileProcessor processor = new FileProcessor();
        SynchronousQueue<Long> resultQueue = new SynchronousQueue<>();
        
        // 4. Start printer thread
        new Thread(new ResultPrinter(resultQueue)).start();

        // 5. Process files with 4 threads
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (File file : allFiles) {
            executor.execute(() -> {
                if (processor.isPDF(file)) {  // Now correctly matches the method name
                    counter.increment();
                }
            });
        }

        // 6. Shutdown and send result
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
            resultQueue.put(counter.getCount());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Processing interrupted");
        }
    }
}