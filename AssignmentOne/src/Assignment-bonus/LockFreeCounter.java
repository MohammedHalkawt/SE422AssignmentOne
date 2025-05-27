import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeCounter {
    private final AtomicLong count = new AtomicLong(0);
    private final AtomicReference<ImmutableFileRecord> lastProcessed = 
        new AtomicReference<>(null);
    
    public void process(ImmutableFileRecord record) {
        // Memory barrier via volatile read in AtomicReference
        ImmutableFileRecord prev = lastProcessed.get();
        
        // CAS operation for publication
        while (!lastProcessed.compareAndSet(prev, record)) {
            prev = lastProcessed.get();
        }
        
        if (record.isPDF()) {
            count.incrementAndGet(); // CAS-based increment
        }
    }
    
    public long getCount() {
        return count.get(); // Volatile read
    }
}