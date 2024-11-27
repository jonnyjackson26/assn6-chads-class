public class TaskFIFO implements Runnable {
    private int[] sequence;
    private int maxMemoryFrames;
    private int maxPageReference;
    private int[] pageFaults;

    public TaskFIFO(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults) {
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;
    }

    @Override
    public void run() {
        // FIFO page replacement logic
        // Use an array to simulate the frames and count page faults
        // For simplicity, this implementation doesn't handle memory directly
        int faults = 0;
        boolean[] frames = new boolean[maxPageReference + 1];
        int nextPage = 0;
        for (int page : sequence) {
            if (!frames[page]) {
                faults++;
                if (nextPage < maxMemoryFrames) {
                    frames[page] = true;
                    nextPage++;
                } else {
                    // Replace the first page
                    frames[page] = true;
                }
            }
        }
        pageFaults[maxMemoryFrames] = faults;
    }
}
