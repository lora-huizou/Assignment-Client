package producer;

import io.swagger.client.model.LiftRide;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import lombok.Data;
import model.LiftRideEvent;

@Data
@AllArgsConstructor
public class LiftRideGenerator implements Runnable {
  private final BlockingQueue<LiftRideEvent> queue;
  private final int totalRequests;


  @Override
  public void run() {
    int generatedCount = 0;
    for (int i = 0; i < totalRequests; i++) {
      try {
        int liftID = ThreadLocalRandom.current().nextInt(1, 41);
        int time = ThreadLocalRandom.current().nextInt(1, 361);
        int resortID = ThreadLocalRandom.current().nextInt(1, 11);
        int skierID = ThreadLocalRandom.current().nextInt(1, 100001);
        LiftRide liftRide = new LiftRide();
        liftRide.setLiftID(liftID);
        liftRide.setTime(time);
        LiftRideEvent event = new LiftRideEvent(liftRide, resortID, "2024", "1", skierID);
        queue.put(event);
        generatedCount++;

        // Log the queue size every 1000 events
//        if (generatedCount % 1000 == 0) {
//          System.out.println("Generated " + generatedCount + " events. Current queue size: " + queue.size());
//        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      //System.out.println("Total events generated: " + generatedCount);
    }
  }
}
