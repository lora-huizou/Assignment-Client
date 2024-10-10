package skierClient2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import model.LiftRideEvent;
import producer.LiftRideGenerator;
import producer.ReportPrinter;
import producer.RequestCounter;
import model.RequestPerformanceMetric;

public class MultithreadedClient2 {

  private static final int TOTAL_REQUESTS = 200_000;
  private static final int INITIAL_THREADS = 32;
  private static final int REQUESTS_PER_THREAD = 1000;
  private static final int LIFT_RIDE_QUEUE_SIZE = 50_000; // to be adjusted base on performance


  public static void main(String[] args) throws InterruptedException {
    BlockingQueue<LiftRideEvent> eventQueue = new ArrayBlockingQueue<>(LIFT_RIDE_QUEUE_SIZE);
    RequestCounter requestCounter = new RequestCounter();
    List<RequestPerformanceMetric> metricsList = new ArrayList<>();

    // Start the data generator thread
    Thread dataGenerator = new Thread(new LiftRideGenerator(eventQueue, TOTAL_REQUESTS));
    dataGenerator.start();

    // Track start time
    long startTime = System.currentTimeMillis();

    // Create initial 32 threads, each sending 1000 POST requests
    Thread[] threads = new Thread[INITIAL_THREADS];
    for (int i = 0; i < INITIAL_THREADS; i++) {
      threads[i] = new Thread(new LiftRidePostWorker2(eventQueue, REQUESTS_PER_THREAD,requestCounter,metricsList));
      threads[i].start();
    }

    // Wait for the initial 32 threads to complete
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // Calculate remaining requests and start additional threads
    int remainingRequests = TOTAL_REQUESTS - (INITIAL_THREADS * REQUESTS_PER_THREAD);
    int additionalThreads = remainingRequests / REQUESTS_PER_THREAD;
    if (remainingRequests % REQUESTS_PER_THREAD > 0) {
      additionalThreads++;
    }

    // Create and start the additional threads
    threads = new Thread[additionalThreads];
    for (int i = 0; i < additionalThreads; i++) {
      int requestsForThread = Math.min(REQUESTS_PER_THREAD, remainingRequests);
      threads[i] = new Thread(new LiftRidePostWorker2(eventQueue, requestsForThread, requestCounter, metricsList));
      threads[i].start();
      remainingRequests -= requestsForThread;
    }

    // Wait for all additional threads to complete
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // Calculate end time and print the results
    long endTime = System.currentTimeMillis();
    double totalTimeSeconds = (endTime - startTime) / 1000.0;

    // Use the ReportPrinter class to print the summary
    ReportPrinter.printBasicResult(
        requestCounter.getSuccessfulRequests(),
        requestCounter.getFailedRequests(),
        totalTimeSeconds,
        TOTAL_REQUESTS
    );
    ReportPrinter.printDetailResult(metricsList);
    ReportPrinter.writeMetricsToCSV("request_metrics.csv", metricsList);
  }

}
