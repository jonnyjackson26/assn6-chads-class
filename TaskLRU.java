import java.util.*;

public class TaskLRU implements Runnable {
    private int[] sequence;
    private int maxMemoryFrames;
    private int maxPageReference;
    private int[] pageFaults;

    public TaskLRU(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults) {
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;
    }

    @Override
    public void run() {
        // LRU page replacement logic
        Set<Integer> frames = new LinkedHashSet<>();
        int faults = 0;
        for (int page : sequence) {
            if (!frames.contains(page)) {
                faults++;
                if (frames.size() >= maxMemoryFrames) {
                    frames.remove(frames.iterator().next()); // Remove least recently used
                }
            } else {
                frames.remove(page); // Remove and re-add to mark as recently used
            }
            frames.add(page);
        }
        pageFaults[maxMemoryFrames] = faults;
    }
}
