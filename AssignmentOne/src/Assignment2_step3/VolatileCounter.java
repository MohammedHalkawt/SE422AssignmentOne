public class VolatileCounter {
    private volatile int count = 0;  // No need for synchronized methods

    public void increment() {
        count++; 
    }

    public int getCount() {
        return count;
    }
}