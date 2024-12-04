import java.util.*;

public class TaskLRU implements Runnable {
    private final int[] sequence;
    private final int maxMemoryFrames;
    private final int maxPageReference;
    private final int[] pageFaults;

    public TaskLRU(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults) {
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;
    }

    @Override
    public void run() {
        Set<Integer> pagesInMemory = new HashSet<>();
        LinkedList<Integer> pageList = new LinkedList<>();
        int faults = 0;

        for (int page : sequence) {
            if (!pagesInMemory.contains(page)) {
                if (pagesInMemory.size() >= maxMemoryFrames) {
                    int leastUsedPage = pageList.removeFirst();
                    pagesInMemory.remove(leastUsedPage);
                }
                pagesInMemory.add(page);
                pageList.add(page);
                faults++;
            } else {
                pageList.remove((Integer) page);
                pageList.add(page);
            }
        }

        pageFaults[maxMemoryFrames] = faults;
    }
}