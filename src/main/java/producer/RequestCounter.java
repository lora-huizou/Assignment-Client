package producer;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class RequestCounter {

  private final AtomicInteger successfulRequests;
  private final AtomicInteger failedRequests;

  public RequestCounter() {
    this.successfulRequests = new AtomicInteger(0);
    this.failedRequests = new AtomicInteger(0);
  }

  public void incrementSuccessfulRequests() {
    successfulRequests.incrementAndGet();
  }

  public void incrementFailedRequests() {
    failedRequests.incrementAndGet();
  }

}
