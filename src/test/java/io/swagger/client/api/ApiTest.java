package io.swagger.client.api;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.model.LiftRide;

public class ApiTest {

  public static void main(String[] args) {
    // Create an instance of ApiClient and configure the base path
    ApiClient client = new ApiClient();
    String serverBasePath = "http://localhost:8080/JavaServlets_war_exploded";
    client.setBasePath(serverBasePath);
    System.out.println("Base URL: " + client.getBasePath());

    // Create an instance of the SkiersApi using the ApiClient
    SkiersApi skiersApi = new SkiersApi(client);

    // sample LiftRide object to send in the request
    LiftRide liftRide = new LiftRide();
    liftRide.setLiftID(5);
    liftRide.setTime(120);

    try {
      Integer resortID = 1;
      String seasonID = "2024";
      String dayID = "1";
      Integer skierID = 123;

      String fullUrl = client.getBasePath() + "/skiers/" + resortID + "/seasons/" + seasonID + "/days/" + dayID + "/skiers/" + skierID;
      System.out.println("Requesting URL: " + fullUrl);
      // Call the writeNewLiftRide method
      skiersApi.writeNewLiftRide(liftRide, resortID, seasonID, dayID, skierID);
      System.out.println("Successfully sent a lift ride to the server!");
    } catch (ApiException e) {
      System.err.println("Exception when calling SkiersApi#writeNewLiftRide");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }

}
