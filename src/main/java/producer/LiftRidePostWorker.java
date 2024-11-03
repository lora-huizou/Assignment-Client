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

  //private static final String BASE_URL = "http://localhost:8080/JavaServlets_war_exploded";
  //private static final String BASE_URL = "http://52.36.116.111:8080/JavaServlets_war"; // ec2
  private static final String BASE_URL = "http://alb6650-1246201829.us-west-2.elb.amazonaws.com/JavaServlets_war";

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

        while (retryCount < 5 && !success) {
          try {
            startTime = System.currentTimeMillis();
            api.writeNewLiftRide(event.getLiftRide(), event.getResortID(), event.getSeasonID(),
                event.getDayID(), event.getSkierID());
            endTime = System.currentTimeMillis();
            requestCounter.incrementSuccessfulRequests();
            success = true;
            responseCode = 201;
          } catch (ApiException e) {
            endTime = System.currentTimeMillis();
            responseCode = e.getCode();
            retryCount++;
            if (retryCount == 5) {
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
