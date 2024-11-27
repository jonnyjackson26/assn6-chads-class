import java.util.*;

public class TaskMRU implements Runnable {
    private int[] sequence;
    private int maxMemoryFrames;
    private int maxPageReference;
    private int[] pageFaults;

    public TaskMRU(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults) {
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;
    }

    @Override
    public void run() {
        // MRU page replacement logic
        Set<Integer> frames = new LinkedHashSet<>();
        int faults = 0;
        for (int page : sequence) {
            if (!frames.contains(page)) {
                faults++;
                if (frames.size() >= maxMemoryFrames) {
                    // Remove most recently used page
                    Iterator<Integer> iterator = frames.iterator();
                    int last = 0;
                    while (iterator.hasNext()) last = iterator.next();
                    frames.remove(last);
                }
            }
            frames.add(page);
        }
        pageFaults[maxMemoryFrames] = faults;
    }
}
