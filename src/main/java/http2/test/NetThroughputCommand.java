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
public class NetThroughputCommand extends CommandBase {

  public static void main(String[] args) throws Exception {
    new NetThroughputCommand().run();
  }

  public void run() throws Exception {
    NetServer server = startServer();
    NetClient client = startClient();
    Object o = new Object();
    synchronized (o) {
      o.wait();
    }
  }

  private NetServer startServer() throws Exception {
    Vertx vertxServer = Vertx.vertx();
    NetServer server = vertxServer.createNetServer();
    server.connectHandler(so -> {
      so.exceptionHandler(err -> {
        err.printStackTrace();
      });
      so.handler(buff -> {
        // Discard
      });
      so.closeHandler(v -> {
        System.out.println("closed");
      });
      SenderStream stream = new SenderStream(1000 * 1000);
      Pump pump = Pump.pump(stream, so);
      pump.start();
      stream.send();
    });
    this.<NetServer>start(server::listen);
    return server;
  }

  private NetClient startClient() throws Exception {
    Vertx vertxClient = Vertx.vertx();
    NetClient client = vertxClient.createNetClient();
    start(client::connect, so -> {
      System.out.println("Connected");
      AtomicLong received = new AtomicLong();
      so.handler(buff -> {
        received.addAndGet(buff.length());
      });
      long startTime = System.currentTimeMillis();
      vertxClient.setPeriodic(1000, id -> {
        System.out.println((received.get() * 1000) / (1024 * (System.currentTimeMillis() - startTime)) + " kb/s");
      });
    });
    return client;
  }

}
