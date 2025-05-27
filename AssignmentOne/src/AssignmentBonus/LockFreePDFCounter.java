package AssignmentBonus;
import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class LockFreePDFCounter {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter directory path:");
        File directory = new File(scanner.nextLine().trim());

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Invalid directory path");
            scanner.close();
            return;
        }

        List<File> files = DirectoryScanner.getAllFiles(directory);
        System.out.println("Files found: " + files.size());
        scanner.close();

        LockFreeCounter counter = new LockFreeCounter();

        SynchronousQueue<Long> resultQueue = new SynchronousQueue<>();
        Thread resultPrinterThread = new Thread(new ResultPrinter(resultQueue));
        resultPrinterThread.start();

        System.out.println("\nCounting with 4 threads");
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (File file : files) {
            executor.execute(() -> {
                ImmutableFileRecord record = new ImmutableFileRecord(file);
                counter.process(record);
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
            resultQueue.put(counter.getCount());
            resultPrinterThread.join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Processing interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred during processing: " + e.getMessage());
        }
    }
}
