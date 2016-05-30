package http2.test;

import com.beust.jcommander.Parameters;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.streams.Pump;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Parameters()
public class NetClientCommand extends CommandBase {

  public static void main(String[] args) throws Exception {
    new NetClientCommand().run();
  }

  public void run() throws Exception {
    NetClient client = vertx.createNetClient();
    start(client::connect, so -> {
      AtomicLong received = new AtomicLong();
      so.handler(buff -> {
        received.addAndGet(buff.length());
      });
      long startTime = System.currentTimeMillis();
      vertx.setPeriodic(1000, id -> {
        System.out.println((received.get() * 1000) / (1024 * (System.currentTimeMillis() - startTime)) + " kb/s");
      });
    });
    Object o = new Object();
    synchronized (o) {
      o.wait();
    }
  }
}
