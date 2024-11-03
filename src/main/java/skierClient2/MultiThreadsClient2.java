package skierClient2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import model.LiftRideEvent;
import producer.LiftRideGenerator;
import producer.LiftRidePostWorker;
import producer.ReportPrinter;
import producer.RequestCounter;
import model.RequestPerformanceMetric;

public class MultiThreadsClient2 {

  private static final int TOTAL_REQUESTS = 200_000;
  private static final int INITIAL_THREADS = 32;
  private static final int MAXIMUM_THREADS = 200;
  private static final int REQUESTS_PER_THREAD = 1000;
  private static final int REMAINING_THREADS = 200;
  private static final int REQUESTS_PER_REMAINING_THREAD = 840;
  private static final int LIFT_RIDE_QUEUE_SIZE = 50_000; // adjust it base on performance

  /**
   * Use ThreadPoolExecutor with dynamic thread management to improve performance.
   */
  public static void main(String[] args) throws InterruptedException {
    BlockingQueue<LiftRideEvent> eventQueue = new ArrayBlockingQueue<>(LIFT_RIDE_QUEUE_SIZE);
    RequestCounter requestCounter = new RequestCounter();
    List<RequestPerformanceMetric> metricsList = new ArrayList<>();
    //int additionalThreads = 0;

    // Start the data generator thread
    Thread dataGenerator = new Thread(new LiftRideGenerator(eventQueue, TOTAL_REQUESTS));
    dataGenerator.start();

    // Use ExecutorService with dynamic thread management (or use ThreadPoolExecutor?)
    ExecutorService executor = Executors.newFixedThreadPool(INITIAL_THREADS + REMAINING_THREADS);

    // Track start time
    long startTime = System.currentTimeMillis();

    // Create initial 32 threads, each sending 1000 POST requests
    for (int i = 0; i < INITIAL_THREADS; i++) {
      executor.submit(new LiftRidePostWorker(eventQueue, REQUESTS_PER_THREAD, requestCounter, metricsList));
    }

    // Next 64 threads, each sending 2000 POST requests for the remaining 168,000 requests
    for (int i = 0; i < REMAINING_THREADS; i++) {
      executor.submit(new LiftRidePostWorker(eventQueue, REQUESTS_PER_REMAINING_THREAD, requestCounter, metricsList));
    }
    // Calculate remaining requests and start additional threads
//    int remainingRequests = TOTAL_REQUESTS - (INITIAL_THREADS * REQUESTS_PER_THREAD);
//    while (remainingRequests > 0) {
//      int requestsForThread = Math.min(REQUESTS_PER_THREAD, remainingRequests);
//      executor.submit(new LiftRidePostWorker(eventQueue, requestsForThread, requestCounter, metricsList));
//      additionalThreads++;
//      remainingRequests -= requestsForThread;
//    }

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
        TOTAL_REQUESTS, INITIAL_THREADS, REQUESTS_PER_THREAD, LIFT_RIDE_QUEUE_SIZE, REMAINING_THREADS
    );
    ReportPrinter.printDetailResult(metricsList);
    ReportPrinter.writeMetricsToCSV("request_metrics.csv", metricsList);
    ReportPrinter.calculateThroughputFromMetrics(metricsList, "throughput_data.csv");
  }

}
