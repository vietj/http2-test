package http2.test;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.WebSocket;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.metrics.impl.DummyVertxMetrics;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;

import java.io.PrintStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Parameters()
public class HttpClientCommand extends CommandBase {

  @Parameter(names = "--histogram")
  public String histogramParam = null;

  @Parameter(names = "--requests", description = "the number of requests")
  public int requests = 100;

  @Parameter(names = "--protocol", description = "the protocol")
  public HttpVersion protocol = HttpVersion.HTTP_1_1;

  @Parameter(names = "--limit", description = "the pipelining/multiplexing limit")
  public int limit = -1;

  @Parameter(names = "--pool-size", description = "the pool size")
  public int poolSize = -1;

  @Parameter(names = "--keep-alive")
  public boolean keepAlive = false;

  @Parameter(names = "--pipelining")
  public boolean pipelining = false;

  @Parameter(names = "--window-size")
  public int windowSize = 65535;

  @Parameter(names = "--frame-size")
  public int frameSize = 16384;

  @Parameter
  public List<String> uriParam;

  private final CountDownLatch doneLatch = new CountDownLatch(1);
  private final Histogram histogram = new ConcurrentHistogram(TimeUnit.MINUTES.toNanos(1), 2);
  private long startTime;

  public static void main(String[] args) throws Exception {
    new HttpClientCommand().run();
  }

  public void run() throws Exception {

    if (uriParam == null || uriParam.size() < 1) {
      throw new Exception("no URI or input file given");
    }
    URI absoluteURI = new URI(uriParam.get(0));
    String host = absoluteURI.getHost();
    int port = absoluteURI.getPort();
    String path;
    if (absoluteURI.getPath() == null || absoluteURI.getPath().isEmpty()) {
      path = "/";
    } else {
      path = absoluteURI.getPath();
    }

    MetricsOptions metricsOptions = new MetricsOptions().setEnabled(true).setFactory((v, options) -> new DummyVertxMetrics() {
      @Override
      public HttpClientMetrics createMetrics(HttpClient client, HttpClientOptions options) {
        return new HttpClientMetrics<Long, Void, Void, Void, Void>() {
          public Void createEndpoint(String host, int port, int maxPoolSize) { return null; }
          public void closeEndpoint(String host, int port, Void endpointMetric) {}
          public Void enqueueRequest(Void endpointMetric) { return null; }
          public void dequeueRequest(Void endpointMetric, Void taskMetric) { }
          public void endpointConnected(Void endpointMetric, Void socketMetric) { }
          public void endpointDisconnected(Void endpointMetric, Void socketMetric) { }
          public Long requestBegin(Void endpointMetric, Void socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
            return System.nanoTime();
          }
          public void requestEnd(Long requestMetric) { }
          public void responseBegin(Long requestMetric, HttpClientResponse response) { }
          public Long responsePushed(Void endpointMetric, Void socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) { return null; }
          public void requestReset(Long requestMetric) {
            histogram.recordValue(System.nanoTime() - requestMetric);
          }
          public void responseEnd(Long requestMetric, HttpClientResponse response) {
            histogram.recordValue(System.nanoTime() - requestMetric);
          }
          public Void connected(Void endpointMetric, Void socketMetric, WebSocket webSocket) { return null; }
          public void disconnected(Void webSocketMetric) { }
          public Void connected(SocketAddress remoteAddress, String remoteName) { return null; }
          public void disconnected(Void socketMetric, SocketAddress remoteAddress) { }
          public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) { }
          public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) { }
          public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) { }
          public void close() { }
          public boolean isEnabled() { return true; }
        };
      }
    });

    Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(metricsOptions));
    HttpClientOptions clientOptions = new HttpClientOptions()
        .setInitialSettings(new Http2Settings().setInitialWindowSize(windowSize).setMaxFrameSize(frameSize))
        .setHttp2ClearTextUpgrade(false)
        .setHttp2ConnectionWindowSize(65536 * 100)
        .setProtocolVersion(protocol)
        .setPipelining(pipelining)
        .setKeepAlive(keepAlive)
        .setSendBufferSize(sendBufferSize)
        .setReceiveBufferSize(receiveBufferSize);
    if (poolSize > 0) {
      clientOptions.setHttp2MaxPoolSize(poolSize);
      clientOptions.setMaxPoolSize(poolSize);
    }
    if (limit > 0) {
      clientOptions.setHttp2MultiplexingLimit(limit);
      clientOptions.setPipeliningLimit(limit);
    }
//    clientOptions.setHttp2MaxPoolSize(poolSize);
//    clientOptions.setMaxPoolSize(poolSize);
    HttpClient client = vertx.createHttpClient(clientOptions);
    int size = requests;
    LongAdder received = new LongAdder();
    AtomicInteger done = new AtomicInteger();
    startTime = System.currentTimeMillis();
    long timerId = vertx.setPeriodic(1000, id -> {
      System.out.format("%d/%d %d kb/s%n", done.get(), size, (received.longValue() * 1000) / (1024 * (System.currentTimeMillis() - startTime)));
    });
    vertx.runOnContext(v1 -> {
      for (int i = 0;i < size;i++) {
        client.getNow(port, host, path, resp -> {
          resp.handler(buff -> {
            received.add(buff.length());
          });
          resp.endHandler(v2 -> {
            if (done.incrementAndGet() == size) {
              vertx.cancelTimer(timerId);
              end();
            }
          });
        });
      }
    });
    doneLatch.await();
  }

  private void end() {
    System.out.format("finished in %.2f s%n", (System.currentTimeMillis() - startTime) / 1000D);
    Histogram cp = histogram.copy();
    System.out.println("min    = " + TimeUnit.NANOSECONDS.toMillis(cp.getMinValue()));
    System.out.println("max    = " + TimeUnit.NANOSECONDS.toMillis(cp.getMaxValue()));
    System.out.println("50%    = " + TimeUnit.NANOSECONDS.toMillis(cp.getValueAtPercentile(50)));
    System.out.println("90%    = " + TimeUnit.NANOSECONDS.toMillis(cp.getValueAtPercentile(90)));
    System.out.println("99%    = " + TimeUnit.NANOSECONDS.toMillis(cp.getValueAtPercentile(99)));
    System.out.println("99.9%  = " + TimeUnit.NANOSECONDS.toMillis(cp.getValueAtPercentile(99.9)));
    System.out.println("99.99% = " + TimeUnit.NANOSECONDS.toMillis(cp.getValueAtPercentile(99.99)));
    if (histogramParam != null) {
      try (PrintStream ps = new PrintStream(histogramParam)) {
        cp.outputPercentileDistribution(ps, 1000000.0);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    doneLatch.countDown();
    vertx.close(v3 -> {
      System.exit(0);
    });
  }

}
