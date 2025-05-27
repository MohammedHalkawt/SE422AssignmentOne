package AssignmentBonus;

import java.util.concurrent.atomic.AtomicLong;

public class LockFreeCounter {
    private final AtomicLong count = new AtomicLong(0);

    public void process(ImmutableFileRecord record) {
        if (record.isPDF()) {
            count.incrementAndGet();
        }
    }

    public long getCount() {
        return count.get();
    }
}
