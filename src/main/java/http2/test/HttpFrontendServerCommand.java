package http2.test;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.WebSocket;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.metrics.impl.DummyVertxMetrics;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import org.HdrHistogram.Histogram;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Parameters()
public class HttpFrontendServerCommand extends BaseHttpServerCommand {

  @Parameter(names = "--backend-host")
  public String backendHost = "localhost";

  @Parameter(names = "--backend-port")
  public int backendPort = 8080;

  @Parameter(names = "--backend-protocol")
  public HttpVersion backendProtocol = HttpVersion.HTTP_1_1;

  @Parameter(names = "--pool-size")
  public int poolSize = 32;

  @Parameter(names = "--backend-limit")
  public int backendLimit = -1;

  private HttpClient client;
  private int frontRequests;
  private Histogram histogram = new Histogram(4);
  private Map<Object, SocketMetric> queuedStreams = new LinkedHashMap<>();
  private AtomicInteger pendingRequests = new AtomicInteger();

  public HttpFrontendServerCommand() throws Exception {
  }

  public static void main(String[] args) throws Exception {
    new HttpFrontendServerCommand().run();
  }

  static class SocketMetric {
    static volatile int serial = 0;
    final int id = serial++;
    final AtomicInteger pendingResponses = new AtomicInteger();
    final AtomicInteger responsesCount = new AtomicInteger();
  }

  @Override
  protected VertxOptions createOptions() {
    VertxOptions options = super.createOptions();
    options.setMetricsOptions(new MetricsOptions().setEnabled(true).setFactory(new VertxMetricsFactory() {
      @Override
      public VertxMetrics metrics(Vertx vertx, VertxOptions options) {
        return new DummyVertxMetrics() {
          @Override
          public HttpClientMetrics<SocketMetric, Void, SocketMetric, Void, Void> createMetrics(HttpClient client, HttpClientOptions options) {
            return new HttpClientMetrics<SocketMetric, Void, SocketMetric, Void, Void>() {

              @Override
              public SocketMetric connected(SocketAddress remoteAddress, String remoteName) {
                SocketMetric socketMetric = new SocketMetric();
                queuedStreams.put(socketMetric, socketMetric);
                return socketMetric;
              }

              @Override
              public SocketMetric requestBegin(Void endpointMetric, SocketMetric socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
                return socketMetric;
              }

              @Override
              public void requestEnd(SocketMetric socketMetric) {
                queuedStreams.get(socketMetric).pendingResponses.incrementAndGet();
              }

              @Override
              public void responseBegin(SocketMetric socketMetric, HttpClientResponse response) {
                socketMetric.pendingResponses.decrementAndGet();
              }

              @Override
              public void responseEnd(SocketMetric socketMetric, HttpClientResponse response) {
                socketMetric.responsesCount.incrementAndGet();
              }

              @Override
              public void disconnected(SocketMetric socketMetric, SocketAddress remoteAddress) {
                queuedStreams.remove(socketMetric);
              }

              public Void enqueueRequest(Void endpointMetric) {
                pendingRequests.incrementAndGet();
                return null;
              }

              public void dequeueRequest(Void endpointMetric, Void taskMetric) {
                pendingRequests.decrementAndGet();
              }

              public Void createEndpoint(String host, int port, int maxPoolSize) {
                return null;
              }
              public void closeEndpoint(String host, int port, Void endpointMetric) { }
              public void endpointConnected(Void endpointMetric, SocketMetric socketMetric) { }
              public void endpointDisconnected(Void endpointMetric, SocketMetric socketMetric) { }
              public SocketMetric responsePushed(Void endpointMetric, SocketMetric socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) { return null; }
              public void requestReset(SocketMetric requestMetric) { }
              public Void connected(Void endpointMetric, SocketMetric socketMetric, WebSocket webSocket) { return null; }
              public void disconnected(Void webSocketMetric) { }
              public void bytesRead(SocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) { }
              public void bytesWritten(SocketMetric socketMetric, SocketAddress remoteAddress, long numberOfBytes) { }
              public void exceptionOccurred(SocketMetric socketMetric, SocketAddress remoteAddress, Throwable t) { }
              public boolean isEnabled() {
                return true;
              }
              public void close() { }
            };
          }
        };
      }
    }));
    return options;
  }

  @Override
  public void run() throws Exception {
    HttpClientOptions options = new HttpClientOptions();
    options.setMaxPoolSize(poolSize);
    options.setPipelining(true);
    options.setKeepAlive(true);
    options.setProtocolVersion(backendProtocol);
    options.setHttp2ClearTextUpgrade(false);
    if (backendLimit > 0) {
      options.setHttp2MultiplexingLimit(backendLimit);
      options.setPipeliningLimit(backendLimit);
    }
    client = vertx.createHttpClient(options);
    vertx.setPeriodic(1000, timerID -> {
      int f = frontRequests;
//      long l90 = histogram.getValueAtPercentile(0.90);
//      long l99 = histogram.getValueAtPercentile(0.99);
//      long l999 = histogram.getValueAtPercentile(0.99);
//      long max = histogram.getMaxValue();
      StringBuilder buff = new StringBuilder();
      queuedStreams.forEach((conn, stream) -> {
        if (buff.length() > 0) {
          buff.append(" - ");
        }
        buff.append(stream.id).append("=").append(stream.pendingResponses.get()).append("/").append(stream.responsesCount.get());
      });
      String log = buff.toString();
      vertx.executeBlocking(fut -> {
        fut.complete();
        System.out.format("current front requests %d, back pending/completed responses %s%n", f, log);
      }, ar -> {

      });
    });
    super.run();
  }

  @Override
  protected void handle(HttpServerRequest req) {
    frontRequests++;
    long now = System.currentTimeMillis();
    HttpClientRequest clientReq = client.get(backendPort, backendHost, "/somepath");
    clientReq.handler(resp -> {
      resp.endHandler(v -> {
        frontRequests--;
        long latency = System.currentTimeMillis() - now;
        histogram.recordValue(latency);
        req.response().end();
      });
    });
    clientReq.end();
  }
}
