package http2.test;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.vertx.core.Vertx;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpVersion;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Parameters()
public class HttpClientCommand extends CommandBase {

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

  private final CountDownLatch doneLatch = new CountDownLatch(1);

  public static void main(String[] args) throws Exception {
    new HttpClientCommand().run();
  }

  public void run() throws Exception {
    Vertx vertx = Vertx.vertx();
    HttpClientOptions options = new HttpClientOptions()
        .setInitialSettings(new Http2Settings().setInitialWindowSize(windowSize).setMaxFrameSize(frameSize))
        .setHttp2ClearTextUpgrade(false)
        .setHttp2ConnectionWindowSize(65536 * 100)
        .setProtocolVersion(protocol)
        .setPipelining(pipelining)
        .setKeepAlive(keepAlive)
        .setSendBufferSize(sendBufferSize)
        .setReceiveBufferSize(receiveBufferSize);
    if (poolSize > 0) {
      options.setHttp2MaxPoolSize(poolSize);
      options.setMaxPoolSize(poolSize);
    }
    if (limit > 0) {
      options.setHttp2MultiplexingLimit(limit);
      options.setPipeliningLimit(limit);
    }
    options.setHttp2MaxPoolSize(poolSize);
    options.setMaxPoolSize(poolSize);
    HttpClient client = vertx.createHttpClient(options);
    int size = requests;
    LongAdder received = new LongAdder();
    AtomicInteger done = new AtomicInteger();
    long startTime = System.currentTimeMillis();
    long timerId = vertx.setPeriodic(1000, id -> {
      System.out.format("%d/%d %d kb/s%n", done.get(), size, (received.longValue() * 1000) / (1024 * (System.currentTimeMillis() - startTime)));
    });
    vertx.runOnContext(v1 -> {
      for (int i = 0;i < size;i++) {
        client.getNow(port, host, "/", resp -> {
          resp.handler(buff -> {
            received.add(buff.length());
          });
          resp.endHandler(v2 -> {
            if (done.incrementAndGet() == size) {
              System.out.format("finished in %.2f s%n", (System.currentTimeMillis() - startTime) / 1000D);
              doneLatch.countDown();
              vertx.cancelTimer(timerId);
              vertx.close(v3 -> {
                System.exit(0);
              });
            }
          });
        });
      }
    });
    doneLatch.await();
  }

}
