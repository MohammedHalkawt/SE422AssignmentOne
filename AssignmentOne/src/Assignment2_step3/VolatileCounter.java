package Assignment2_Step3;

import java.util.concurrent.atomic.AtomicInteger;

public class VolatileCounter {

    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }
}
