package latencyTest;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

public class SingleThreadLatencyTest {
  //private static final String BASE_URL = "http://35.91.96.75:8080/JavaServlets_war";
  private static final String BASE_URL = "http://localhost:8080/JavaServlets_war_exploded";
  //private static final String BASE_URL = "http://localhost:8080"; // for local springboot server
  private static final int TOTAL_REQUESTS = 10_000;

  public static void main(String[] args) {
    ApiClient client = new ApiClient();
    client.setBasePath(BASE_URL);
    SkiersApi skiersApi = new SkiersApi(client);

    long totalLatency = 0;
    int successfulRequests = 0;
    int failedRequests = 0;

    for (int i = 0; i < TOTAL_REQUESTS; i++) {
      try {
        LiftRide liftRide = new LiftRide();
        liftRide.setLiftID((int) (Math.random() * 40) + 1);
        liftRide.setTime((int) (Math.random() * 360) + 1);

        long startTime = System.currentTimeMillis();
        skiersApi.writeNewLiftRide(liftRide, 1, "2024", "1", 123);
        long endTime = System.currentTimeMillis();
        long latency = endTime - startTime;
        totalLatency += latency;
        successfulRequests++;
      } catch (ApiException e) {
        e.printStackTrace();
        failedRequests++;
      }
    }

    double averageLatency = totalLatency / (double) successfulRequests;

    System.out.println("Total Requests: " + TOTAL_REQUESTS);
    System.out.println("Successful Requests: " + successfulRequests);
    System.out.println("Failed Requests: " + failedRequests);
    System.out.println("Average Latency (ms): " + averageLatency);
  }
}
