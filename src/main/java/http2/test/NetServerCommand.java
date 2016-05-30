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
public class NetServerCommand extends CommandBase {

  public static void main(String[] args) throws Exception {
    new NetServerCommand().run();
  }
  public void run() throws Exception {
    NetServer server = vertx.createNetServer();
    server.connectHandler(so -> {
      so.exceptionHandler(err -> {
        err.printStackTrace();
      });
      so.handler(buff -> {
        // Discard
      });
      so.closeHandler(v -> {
      });
      SenderStream stream = new SenderStream(1000 * 1000);
      Pump pump = Pump.pump(stream, so);
      pump.start();
      stream.send();
    });
    this.<NetServer>start(server::listen);
    Object o = new Object();
    synchronized (o) {
      o.wait();
    }
  }
}
