package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestPerformanceMetric {
  private final long startTime;
  private final long endTime;
  private final String requestType;
  private final long latency;
  private final int responseCode;
}
