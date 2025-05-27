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

        // Lock-free components
        LockFreeCounter counter = new LockFreeCounter();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        
        // Process files
        for (File file : files) {
            completionService.submit(() -> {
                counter.process(new ImmutableFileRecord(file));
                return null;
            });
        }

        // Shutdown
        executor.shutdown();
        try {
            for (int i = 0; i < files.size(); i++) {
                completionService.take();
            }
            System.out.println("\n--- Lock-Free Results ---");
            System.out.println("PDF files counted: " + counter.getCount());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Processing interrupted");
        }
    }
}