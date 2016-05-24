package http2.test;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.vertx.core.Vertx;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.streams.Pump;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Parameters()
public class HttpDownloadClientCommand extends CommandBase {

  @Parameter(names = "--requests")
  public int requests = 100;

  @Parameter(names = "--protocol")
  public HttpVersion protocol = HttpVersion.HTTP_1_1;

  @Parameter(names = "--concurrency")
  public int concurrency = -1;

  @Parameter(names = "--pool-size")
  public int poolSize = 5;

  @Parameter(names = "--keep-alive")
  public boolean keepAlive = false;

  @Parameter(names = "--pipelining")
  public boolean pipelining = false;

  @Parameter(names = "--window-size")
  public int windowSize = 65535;

  private final CountDownLatch doneLatch = new CountDownLatch(1);

  public static void main(String[] args) throws Exception {
    new HttpDownloadClientCommand().run();
  }

  public void run() throws Exception {
    Vertx vertx = Vertx.vertx();
    HttpClient client = vertx.createHttpClient(new HttpClientOptions()
//        .setLogActivity(true)
        .setInitialSettings(new Http2Settings().setInitialWindowSize(windowSize))
        .setH2cUpgrade(false)
        .setProtocolVersion(protocol)
        .setPipelining(pipelining)
        .setKeepAlive(keepAlive)
        .setMaxPoolSize(poolSize)
        .setMaxStreams(concurrency));
    int size = requests;
    LongAdder received = new LongAdder();
    AtomicInteger done = new AtomicInteger();
    long startTime = System.currentTimeMillis();
    long timerId = vertx.setPeriodic(1000, id -> {
      System.out.println("" + done.get() + "/" + size + " " + (received.longValue() * 1000) / (1024 * (System.currentTimeMillis() - startTime)) + " kb/s");
    });
    vertx.runOnContext(v -> {
      for (int i = 0;i < size;i++) {
        client.getNow(port, host, "/", resp -> {
          resp.handler(buff -> {
            received.add(buff.length());
          });
          resp.endHandler(v2 -> {
            if (done.incrementAndGet() == size) {
              System.out.println("finished in " + (System.currentTimeMillis() - startTime) / 1000);
              doneLatch.countDown();
              vertx.cancelTimer(timerId);
            }
          });
        });
      }
    });
    doneLatch.await();
  }

}
