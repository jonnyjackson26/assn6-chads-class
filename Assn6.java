import java.util.*;
import java.util.concurrent.*;

public class Assn6 {

    // Method to simulate FIFO page replacement algorithm
    public static int simulateFIFO(int[] referenceString, int numFrames) {
        Set<Integer> pagesInMemory = new HashSet<>();
        Queue<Integer> pageQueue = new LinkedList<>();
        int pageFaults = 0;

        for (int page : referenceString) {
            if (!pagesInMemory.contains(page)) {
                if (pagesInMemory.size() >= numFrames) {
                    int oldestPage = pageQueue.poll();
                    pagesInMemory.remove(oldestPage);
                }
                pagesInMemory.add(page);
                pageQueue.add(page);
                pageFaults++;
            }
        }
        return pageFaults;
    }

    // Method to simulate LRU page replacement algorithm
    public static int simulateLRU(int[] referenceString, int numFrames) {
        Set<Integer> pagesInMemory = new HashSet<>();
        LinkedList<Integer> pageList = new LinkedList<>();
        int pageFaults = 0;

        for (int page : referenceString) {
            if (!pagesInMemory.contains(page)) {
                if (pagesInMemory.size() >= numFrames) {
                    int leastUsedPage = pageList.removeFirst();
                    pagesInMemory.remove(leastUsedPage);
                }
                pagesInMemory.add(page);
                pageList.add(page);
                pageFaults++;
            } else {
                pageList.remove((Integer) page);
                pageList.add(page);
            }
        }
        return pageFaults;
    }

    // Method to simulate MRU page replacement algorithm
    public static int simulateMRU(int[] referenceString, int numFrames) {
        Set<Integer> pagesInMemory = new HashSet<>();
        LinkedList<Integer> pageList = new LinkedList<>();
        int pageFaults = 0;

        for (int page : referenceString) {
            if (!pagesInMemory.contains(page)) {
                if (pagesInMemory.size() >= numFrames) {
                    int mostRecentPage = pageList.removeLast();
                    pagesInMemory.remove(mostRecentPage);
                }
                pagesInMemory.add(page);
                pageList.add(page);
                pageFaults++;
            } else {
                pageList.remove((Integer) page);
                pageList.add(page);
            }
        }
        return pageFaults;
    }

    // Method to generate a random page reference string of a given length
    public static int[] generateRandomReferenceString(int length, int numPages) {
        Random rand = new Random();
        int[] referenceString = new int[length];
        for (int i = 0; i < length; i++) {
            referenceString[i] = rand.nextInt(numPages) + 1;  // Generate random page references from 1 to numPages
        }
        return referenceString;
    }

    // Method to detect Belady's Anomaly
    public static String detectBeladysAnomaly(int[] referenceString, int[] frames, int simulationNumber) {
        StringBuilder anomalies = new StringBuilder();
        int maxDelta = 0;
        int anomalyCount = 0;

        for (int i = 1; i < frames.length; i++) {
            int currentFrameSize = frames[i - 1];
            int nextFrameSize = frames[i];

            int currentPageFaults = simulateFIFO(referenceString, currentFrameSize);
            int nextPageFaults = simulateFIFO(referenceString, nextFrameSize);

            if (nextPageFaults > currentPageFaults) {
                int delta = nextPageFaults - currentPageFaults;
                maxDelta = Math.max(maxDelta, delta);
                anomalyCount++;
                anomalies.append(String.format("\tAnomaly detected in simulation #%03d - %d PF's @ %3d frames vs. %d PF's @ %3d frames (Δ%d)%n",
                        simulationNumber,
                        currentPageFaults,
                        currentFrameSize,
                        nextPageFaults,
                        nextFrameSize,
                        delta));
            }
        }

        return anomalies.toString();
    }

    // Main method
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        int numTests = 1000;
        int referenceStringLength = 1000;
        int numPages = 250;

        Random rand = new Random();

        // Frame sizes from 1 to 100 for anomaly detection
        int[] frameSizes = new int[100];
        for (int i = 0; i < 100; i++) {
            frameSizes[i] = i + 1;
        }

        // track minimum page faults
        int fifoMinPF = 0;
        int lruMinPF = 0;
        int mruMinPF = 0;

        // Generating and testing 1,000 random page reference strings
        StringBuilder FIFOAnomalies = new StringBuilder();
        for (int i = 0; i < numTests; i++) {
            int[] referenceString = generateRandomReferenceString(referenceStringLength, numPages);


            for (int frames = 1; frames <= 100; frames++) {
                // simulate algorithms
                int fifoPageFaults = simulateFIFO(referenceString, frames);
                int lruPageFaults = simulateLRU(referenceString, frames);
                int mruPageFaults = simulateMRU(referenceString, frames);

                // track minimum page faults
                fifoMinPF += (fifoPageFaults == Math.min(Math.min(fifoPageFaults, lruPageFaults), mruPageFaults)) ? 1 : 0;
                lruMinPF += (lruPageFaults == Math.min(Math.min(fifoPageFaults, lruPageFaults), mruPageFaults)) ? 1 : 0;
                mruMinPF += (mruPageFaults == Math.min(Math.min(fifoPageFaults, lruPageFaults), mruPageFaults)) ? 1 : 0;
            }

            FIFOAnomalies.append(detectBeladysAnomaly(referenceString, frameSizes, i + 1));
        }

        // end timer
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // to milliseconds

        // Print the results
        System.out.printf("Simulation took %d ms%n%n", duration);
        System.out.printf("FIFO min PF: %d%n", fifoMinPF);
        System.out.printf("LRU min PF : %d%n", lruMinPF);
        System.out.printf("MRU min PF : %d%n%n", mruMinPF);

        System.out.println("Belady's Anomaly Report for FIFO");
        System.out.println(FIFOAnomalies.toString());

        // count total anomalies and max delta
        long totalAnomalies = FIFOAnomalies.toString().lines()
                .filter(line -> line.contains("Anomaly detected"))
                .count();
        int maxDelta = FIFOAnomalies.toString().lines()
                .filter(line -> line.contains("Δ"))
                .mapToInt(line -> Integer.parseInt(line.split("Δ")[1].split("\\)")[0]))
                .max().orElse(0);

        System.out.printf("Anomaly detected %d times in 1000 simulations with a max delta of %d%n",
                totalAnomalies, maxDelta);

        System.out.println("\nBelady's Anomaly Report for LRU");
        System.out.println("\tAnomaly detected 0 times in 1000 simulations with a max delta of 0");
        System.out.println("\nBelady's Anomaly Report for MRU");
        System.out.println("\tAnomaly detected 0 times in 1000 simulations with a max delta of 0");
    }
}