package producer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import model.RequestPerformanceMetric;

public class ReportPrinter {
  public static void printBasicResult(AtomicInteger successfulRequests, AtomicInteger failedRequests,
      double totalTimeSeconds, int totalRequests, int initialThread, int requestPerThread,
      int liftRideQueueSize) {
    System.out.println("--- Execution Summary for Part 1 ---");
    System.out.println("Number of successful requests: " + successfulRequests);
    System.out.println("Number of failed requests: " + failedRequests);
    System.out.println("Total run/wall time in seconds: " + totalTimeSeconds);
    System.out.println("Throughput (requests/second): " + calculateThroughput(totalRequests, totalTimeSeconds));
    System.out.println("-------------------------------------");
    System.out.println("Client Configuration:");
    System.out.println("Initial Threads: " + initialThread);
    System.out.println("Requests per Thread: " + requestPerThread);
    System.out.println("Total Requests: " + totalRequests);
    System.out.println("Lift Ride Queue Size: " + liftRideQueueSize);
  }

  public static void printDetailResult(List<RequestPerformanceMetric> metricsList) {
    // Sort metrics by latency
    Collections.sort(metricsList, (m1, m2) -> Long.compare(m1.getLatency(), m2.getLatency()));

    // Calculate total latency
    long totalLatency = metricsList.stream().mapToLong(RequestPerformanceMetric::getLatency).sum();
    double meanLatency = totalLatency / (double) metricsList.size();

    // Calculate min and max latency
    long minLatency = metricsList.get(0).getLatency();
    long maxLatency = metricsList.get(metricsList.size() - 1).getLatency();

    // Calculate median latency
    long medianLatency = metricsList.get(metricsList.size() / 2).getLatency();

    // Calculate 99th percentile latency
    long p99Latency = metricsList.get((int) (metricsList.size() * 0.99)).getLatency();

    System.out.println("--- Execution Summary for Part 2 ---");
    System.out.printf("Mean Latency (ms): %.2f\n", meanLatency);
    System.out.println("Median Latency (ms): " + medianLatency);
    System.out.println("99th Percentile Latency (ms): " + p99Latency);
    System.out.println("Min Latency (ms): " + minLatency);
    System.out.println("Max Latency (ms): " + maxLatency);
    System.out.println("-------------------------------------");
  }

  public static void writeMetricsToCSV(String fileName, List<RequestPerformanceMetric> metricsList) {
    try (FileWriter writer = new FileWriter(fileName)) {
      // CSV header
      writer.append("Start Time, End Time, Request Type, Latency, Response Code\n");
      // Write each metric
      for (RequestPerformanceMetric metric : metricsList) {
        writer.append(metric.toString()).append("\n");
      }
      System.out.println("Request metrics successfully written to " + fileName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static double calculateThroughput(int totalRequests, double totalTimeSeconds) {
    return totalRequests / totalTimeSeconds;
  }


}
