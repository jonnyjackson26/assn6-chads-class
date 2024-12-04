

import java.util.*;
import java.util.concurrent.*;

public class Assn6 {
    private static final int NUM_TESTS = 1000;
    private static final int REFERENCE_STRING_LENGTH = 1000;
    private static final int NUM_PAGES = 250;
    private static final int MAX_FRAMES = 100;

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);

        int[][] fifoPageFaults = new int[NUM_TESTS][MAX_FRAMES + 1];
        int[][] lruPageFaults = new int[NUM_TESTS][MAX_FRAMES + 1];
        int[][] mruPageFaults = new int[NUM_TESTS][MAX_FRAMES + 1];
        int[] fifoMinPF = new int[MAX_FRAMES + 1];
        int[] lruMinPF = new int[MAX_FRAMES + 1];
        int[] mruMinPF = new int[MAX_FRAMES + 1];

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < NUM_TESTS; i++) {
            int[] referenceString = generateRandomReferenceString(REFERENCE_STRING_LENGTH, NUM_PAGES);

            for (int frames = 1; frames <= MAX_FRAMES; frames++) {
                final int testIndex = i;
                futures.add(executor.submit(new TaskFIFO(referenceString, frames, NUM_PAGES, fifoPageFaults[testIndex])));
                futures.add(executor.submit(new TaskLRU(referenceString, frames, NUM_PAGES, lruPageFaults[testIndex])));
                futures.add(executor.submit(new TaskMRU(referenceString, frames, NUM_PAGES, mruPageFaults[testIndex])));
            }
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        // Calculate min PF for each algorithm
        for (int frames = 1; frames <= MAX_FRAMES; frames++) {
            for (int i = 0; i < NUM_TESTS; i++) {
                int minPF = Math.min(Math.min(fifoPageFaults[i][frames], lruPageFaults[i][frames]), mruPageFaults[i][frames]);
                fifoMinPF[frames] += (fifoPageFaults[i][frames] == minPF) ? 1 : 0;
                lruMinPF[frames] += (lruPageFaults[i][frames] == minPF) ? 1 : 0;
                mruMinPF[frames] += (mruPageFaults[i][frames] == minPF) ? 1 : 0;
            }
        }


        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // to milliseconds

        // RESULTS
        System.out.printf("Simulation took %d ms%n%n", duration);
        System.out.printf("FIFO min PF: %d%n", Arrays.stream(fifoMinPF).sum());
        System.out.printf("LRU min PF : %d%n", Arrays.stream(lruMinPF).sum());
        System.out.printf("MRU min PF : %d%n%n", Arrays.stream(mruMinPF).sum());

        StringBuilder FIFOAnomalies = new StringBuilder();
        StringBuilder LRUAnomalies = new StringBuilder();
        StringBuilder MRUAnomalies = new StringBuilder();

        for (int i = 0; i < NUM_TESTS; i++) {
            FIFOAnomalies.append(detectBeladysAnomaly(fifoPageFaults[i], i + 1));
            LRUAnomalies.append(detectBeladysAnomaly(lruPageFaults[i], i + 1));
            MRUAnomalies.append(detectBeladysAnomaly(mruPageFaults[i], i + 1));
        }

        // FIFO Anomaly Report
        System.out.println("Belady's Anomaly Report for FIFO");
        System.out.println(FIFOAnomalies.toString());

        long fifoTotalAnomalies = FIFOAnomalies.toString().lines()
                .filter(line -> line.contains("Anomaly detected"))
                .count();
        int fifoMaxDelta = FIFOAnomalies.toString().lines()
                .filter(line -> line.contains("Δ"))
                .mapToInt(line -> Integer.parseInt(line.split("Δ")[1].split("\\)")[0]))
                .max().orElse(0);

        System.out.printf("\tAnomaly detected %d times in %d simulations with a max delta of %d%n",
                fifoTotalAnomalies, NUM_TESTS, fifoMaxDelta);

        // LRU Anomaly Report
        System.out.println("\nBelady's Anomaly Report for LRU");
        long lruTotalAnomalies = LRUAnomalies.toString().lines()
                .filter(line -> line.contains("Anomaly detected"))
                .count();
        int lruMaxDelta = LRUAnomalies.toString().lines()
                .filter(line -> line.contains("Δ"))
                .mapToInt(line -> Integer.parseInt(line.split("Δ")[1].split("\\)")[0]))
                .max().orElse(0);

        System.out.printf("\tAnomaly detected %d times in %d simulations with a max delta of %d%n",
                lruTotalAnomalies, NUM_TESTS, lruMaxDelta);

        // MRU Anomaly Report
        System.out.println("\nBelady's Anomaly Report for MRU");
        long mruTotalAnomalies = MRUAnomalies.toString().lines()
                .filter(line -> line.contains("Anomaly detected"))
                .count();
        int mruMaxDelta = MRUAnomalies.toString().lines()
                .filter(line -> line.contains("Δ"))
                .mapToInt(line -> Integer.parseInt(line.split("Δ")[1].split("\\)")[0]))
                .max().orElse(0);

        System.out.printf("\tAnomaly detected %d times in %d simulations with a max delta of %d%n",
                mruTotalAnomalies, NUM_TESTS, mruMaxDelta);




    }

    public static int[] generateRandomReferenceString(int length, int numPages) {
        Random rand = new Random();
        int[] referenceString = new int[length];
        for (int i = 0; i < length; i++) {
            referenceString[i] = rand.nextInt(numPages) + 1;
        }
        return referenceString;
    }

    private static String detectBeladysAnomaly(int[] pageFaults, int simulationNumber) {
        StringBuilder anomalies = new StringBuilder();
        int maxDelta = 0;

        for (int i = 2; i <= MAX_FRAMES; i++) {
            int currentFrameSize = i - 1;
            int nextFrameSize = i;

            int currentPageFaults = pageFaults[currentFrameSize];
            int nextPageFaults = pageFaults[nextFrameSize];

            if (nextPageFaults > currentPageFaults) {
                int delta = nextPageFaults - currentPageFaults;
                maxDelta = Math.max(maxDelta, delta);
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
}



