import java.util.*;

public class TaskFIFO implements Runnable {
    private final int[] sequence;
    private final int maxMemoryFrames;
    private final int maxPageReference;
    private final int[] pageFaults;

    public TaskFIFO(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults) {
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;
    }

    @Override
    public void run() {
        Set<Integer> pagesInMemory = new HashSet<>();
        Queue<Integer> pageQueue = new LinkedList<>();
        int faults = 0;

        for (int page : sequence) {
            if (!pagesInMemory.contains(page)) {
                if (pagesInMemory.size() >= maxMemoryFrames) {
                    int oldestPage = pageQueue.poll();
                    pagesInMemory.remove(oldestPage);
                }
                pagesInMemory.add(page);
                pageQueue.add(page);
                faults++;
            }
        }

        pageFaults[maxMemoryFrames] = faults;
    }
}