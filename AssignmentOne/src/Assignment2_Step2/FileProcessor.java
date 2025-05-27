import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public class FileProcessor {
    private final ReentrantLock fileLock = new ReentrantLock();

    
    public boolean isPDF(File file) {
        fileLock.lock();
        try {
            return file.getName().toLowerCase().endsWith(".pdf");
        } finally {
            fileLock.unlock();
        }
    }
}