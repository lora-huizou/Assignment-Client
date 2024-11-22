package producer;

import java.util.concurrent.TimeUnit;

public class ExponentialBackoff {
  private final long baseDelayMs;
  private final long maxDelayMs;

  public ExponentialBackoff(long baseDelayMs, long maxDelayMs) {
    this.baseDelayMs = baseDelayMs;
    this.maxDelayMs = maxDelayMs;
  }

  public void waitBeforeRetry(int retryCount) throws InterruptedException {
    long delay = Math.min(baseDelayMs * (1L << retryCount), maxDelayMs);
    TimeUnit.MILLISECONDS.sleep(delay);
  }
}
