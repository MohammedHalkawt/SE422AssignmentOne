package AssignmentBonus;

import java.util.concurrent.SynchronousQueue;

public class ResultPrinter implements Runnable {
    private final SynchronousQueue<Long> resultQueue;
    
    public ResultPrinter(SynchronousQueue<Long> queue) {
        this.resultQueue = queue;
    }
    
    @Override
    public void run() {
        try {
            long count = resultQueue.take();
            System.out.println("\n--- Final Result ---");
            System.out.println("PDF files counted: " + count);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Printer thread interrupted");
        }
    }
}