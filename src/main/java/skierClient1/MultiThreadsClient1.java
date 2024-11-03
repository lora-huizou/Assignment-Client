package skierClient1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import model.LiftRideEvent;
import model.RequestPerformanceMetric;
import producer.LiftRideGenerator;
import producer.LiftRidePostWorker;
import producer.ReportPrinter;
import producer.RequestCounter;

public class MultiThreadsClient1 {

  private static final int TOTAL_REQUESTS = 32_000;
  private static final int INITIAL_THREADS = 32;
  private static final int MAXIMUM_THREADS = 64;
  private static final int REQUESTS_PER_THREAD = 1000;
  private static final int LIFT_RIDE_QUEUE_SIZE = 50_000; // adjust it base on performance
  //private static final long STAGGER_DELAY_MS = 200;

  /**
   * Use ThreadPoolExecutor with dynamic thread management to improve performance.
   */
  public static void main(String[] args) throws InterruptedException {
    BlockingQueue<LiftRideEvent> eventQueue = new ArrayBlockingQueue<>(LIFT_RIDE_QUEUE_SIZE);
    RequestCounter requestCounter = new RequestCounter();
    List<RequestPerformanceMetric> metricsList = new ArrayList<>();
    int additionalThreads = 0;

    // Start the data generator thread
    Thread dataGenerator = new Thread(new LiftRideGenerator(eventQueue, TOTAL_REQUESTS));
    dataGenerator.start();

    // Use ExecutorService with dynamic thread management (or use ThreadPoolExecutor?)
    ExecutorService executor = Executors.newFixedThreadPool(MAXIMUM_THREADS);

    // Track start time
    long startTime = System.currentTimeMillis();

    // Create initial 32 threads, each sending 1000 POST requests
    for (int i = 0; i < INITIAL_THREADS; i++) {
      executor.submit(new LiftRidePostWorker(eventQueue, REQUESTS_PER_THREAD, requestCounter, metricsList));
//      try {
//        Thread.sleep(STAGGER_DELAY_MS);
//      } catch (InterruptedException e) {
//        Thread.currentThread().interrupt();
//        System.err.println("Thread was interrupted during staggered start.");
//      }
    }

    // Calculate remaining requests and start additional threads
    int remainingRequests = TOTAL_REQUESTS - (INITIAL_THREADS * REQUESTS_PER_THREAD);
    while (remainingRequests > 0) {
      int requestsForThread = Math.min(REQUESTS_PER_THREAD, remainingRequests);
      executor.submit(new LiftRidePostWorker(eventQueue, requestsForThread, requestCounter, metricsList));
      additionalThreads++;
      remainingRequests -= requestsForThread;
    }

    // Shutdown the executor and wait for completion
    executor.shutdown();
    if (!executor.awaitTermination(20, TimeUnit.MINUTES)) {
      System.out.println("Some threads did not finish in the expected time.");
    }

    // Calculate end time and print the results
    long endTime = System.currentTimeMillis();
    double totalTimeSeconds = (endTime - startTime) / 1000.0;

    // Use the ReportPrinter class to print the summary
    ReportPrinter.printBasicResult(
        requestCounter.getSuccessfulRequests(),
        requestCounter.getFailedRequests(),
        totalTimeSeconds,
        TOTAL_REQUESTS, INITIAL_THREADS, REQUESTS_PER_THREAD, LIFT_RIDE_QUEUE_SIZE, additionalThreads
    );
    ReportPrinter.calculateThroughputFromMetrics(metricsList, "throughput_data.csv");

  }

}
