package Assignment2_Step2;

import java.util.concurrent.atomic.LongAdder;

public class FileCounter {
    private final LongAdder count = new LongAdder();
    
    public void increment() {
        count.increment();
    }
    
    public long getCount() {
        return count.sum();
    }
}