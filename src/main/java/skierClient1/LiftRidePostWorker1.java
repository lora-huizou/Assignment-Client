package skierClient1;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import model.LiftRideEvent;
import model.RequestPerformanceMetric;
import producer.RequestCounter;

@Data
@AllArgsConstructor
public class LiftRidePostWorker1 implements Runnable{
  private final BlockingQueue<LiftRideEvent> queue;
  private final int numRequests;
  private final RequestCounter requestCounter;

  public void run() {
    // Create a new ApiClient and SkiersApi instance for each thread
    ApiClient client = new ApiClient();
    //client.setBasePath("http://35.91.96.75:8080/JavaServlets_war");
    client.setBasePath("http://localhost:8080/JavaServlets_war_exploded");
    //client.setBasePath("http://localhost:8080"); //run local springboot
    SkiersApi api = new SkiersApi(client);

    for (int i = 0; i < numRequests; i++) {
      try {
        LiftRideEvent event = queue.take();
        int retryCount = 0;
        boolean success = false;
        while (retryCount < 5 && !success) {
          try {
            api.writeNewLiftRide(event.getLiftRide(), event.getResortID(), event.getSeasonID(),
                event.getDayID(), event.getSkierID());
            requestCounter.incrementSuccessfulRequests();
            success = true;

          } catch (ApiException e) {
            retryCount++;
            if (retryCount == 5) {
              requestCounter.incrementFailedRequests();
            }
          }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

}
