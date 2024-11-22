package producer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CircuitBreaker {
  private final int failureThreshold; // Number of failures before opening the circuit
  private final long resetTimeout;    // Time to wait before resetting (in ms)

  private final AtomicInteger failureCount = new AtomicInteger(0);
  private final AtomicLong lastFailureTime = new AtomicLong(0);
  private volatile boolean isOpen = false;

  public CircuitBreaker(int failureThreshold, long resetTimeout) {
    this.failureThreshold = failureThreshold;
    this.resetTimeout = resetTimeout;
  }

  public boolean allowRequest() {
    if (!isOpen) {
      return true;
    }

    // Check if the circuit should reset
    long elapsedTimeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
    if (elapsedTimeSinceLastFailure > resetTimeout) {
      isOpen = false;
      failureCount.set(0); // Reset failure count
      return true;
    }
    return false;
  }

  public void recordSuccess() {
    if (isOpen) {
      isOpen = false; // Close circuit on success
    }
    failureCount.set(0); // Reset failure count
  }

  public void recordFailure() {
    lastFailureTime.set(System.currentTimeMillis());
    if (failureCount.incrementAndGet() >= failureThreshold) {
      isOpen = true; // Open the circuit
    }
  }
}

