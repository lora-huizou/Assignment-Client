package producer;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import model.LiftRideEvent;
import model.RequestPerformanceMetric;

@Data
@AllArgsConstructor
public class LiftRidePostWorker implements Runnable{
  private final BlockingQueue<LiftRideEvent> queue;
  private final int numRequests;
  private final RequestCounter requestCounter;
  private List<RequestPerformanceMetric> metricsList;

  private static final int MAX_RETRIES = 5; // Max retry attempts per request
  private static final int FAILURE_THRESHOLD = 10; // Open circuit after 10 failures
  private static final long RESET_TIMEOUT_MS = 5000; // Reset circuit after 5 seconds
  private static final long BASE_BACKOFF_MS = 100; // Initial backoff time (100ms)
  private static final long MAX_BACKOFF_MS = 5000; // Maximum backoff time (5s)
  private final CircuitBreaker circuitBreaker = new CircuitBreaker(FAILURE_THRESHOLD, RESET_TIMEOUT_MS);
  private final ExponentialBackoff backoff = new ExponentialBackoff(BASE_BACKOFF_MS, MAX_BACKOFF_MS);



  //private static final String BASE_URL = "http://localhost:8080/JavaServlets_war_exploded";
  //private static final String BASE_URL = "http://52.36.116.111:8080/JavaServlets_war"; // ec2
  //private static final String BASE_URL = "http://alb6650-1246201829.us-west-2.elb.amazonaws.com/JavaServlets_war";
  private static final String BASE_URL = "http://52.25.21.118:8080/JavaServlets_war";

  //private static final String BASE_URL = "http://localhost:8080"; // local springboot server
  public void run() {
    // Create a new ApiClient and SkiersApi instance for each thread
    ApiClient client = new ApiClient();
    client.setBasePath(BASE_URL);
    SkiersApi api = new SkiersApi(client);
    for (int i = 0; i < numRequests; i++) {
      try {
        LiftRideEvent event = queue.take();
        int retryCount = 0;
        boolean success = false;
        long startTime = 0, endTime = 0;
        int responseCode = 0;

        while (retryCount < MAX_RETRIES && !success) {
          // Check if the circuit breaker allows the request
          if (!circuitBreaker.allowRequest()) {
            System.out.println("Circuit breaker is open. Skipping request.");
            break; // Skip this request if the circuit is open
          }
          try {
            // Send the request
            startTime = System.currentTimeMillis();
            api.writeNewLiftRide(event.getLiftRide(), event.getResortID(), event.getSeasonID(),
                event.getDayID(), event.getSkierID());
            endTime = System.currentTimeMillis();
            // Mark request as successful
            requestCounter.incrementSuccessfulRequests();
            circuitBreaker.recordSuccess();
            success = true;
            responseCode = 201;
          } catch (ApiException e) {
            endTime = System.currentTimeMillis();
            responseCode = e.getCode();
            retryCount++;
            // Record failure in the circuit breaker
            circuitBreaker.recordFailure();
            // Apply exponential backoff before retrying
            if (retryCount < MAX_RETRIES) {
              try {
                backoff.waitBeforeRetry(retryCount);
              } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break; // Exit the retry loop if interrupted
              }
            } else {
              // Mark as a failed request if retries are exhausted
              requestCounter.incrementFailedRequests();
            }
          }
        }
        long latency = endTime - startTime;
        RequestPerformanceMetric metric = new RequestPerformanceMetric(startTime, endTime, "POST", latency, responseCode);
        synchronized (metricsList) {
          metricsList.add(metric);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

}
