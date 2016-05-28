package http2.test;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.vertx.core.Vertx;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Parameters()
public class HttpDownloadServerCommand extends CommandBase {

  @Parameter(names = "--size")
  public long size = 10000 * 1000;

  @Parameter(names = "--think-time")
  public long thinkTime = 40;

  @Parameter(names = "--concurrency")
  public long concurrency = -1;

  public static void main(String[] args) throws Exception {
    new HttpDownloadServerCommand().run();
  }

  public void run() throws Exception {
    Vertx vertx = Vertx.vertx();
    HttpServerOptions options = new HttpServerOptions().setInitialSettings(new Http2Settings().
        setMaxConcurrentStreams(concurrency == -1 ? Http2Settings.DEFAULT_MAX_CONCURRENT_STREAMS : concurrency));
    options.setSendBufferSize(sendBufferSize);
    options.setReceiveBufferSize(receiveBufferSize);
    HttpServer server = vertx.createHttpServer(options);
    server.connectionHandler(conn -> {
      conn.closeHandler(v -> {
//        System.out.println("closed");
      });
    });
    server.requestHandler(req -> {
      HttpServerResponse resp = req.response();
      if (thinkTime > 0) {
        vertx.setTimer(thinkTime, v -> {
          handleResp(resp);
        });
      } else {
        handleResp(resp);
      }
    });
    this.<HttpServer>start(server::listen);
    System.out.println("server started on " + port);
  }

  private void handleResp(HttpServerResponse resp) {
    if (size > 0) {
      resp.setChunked(true);
      SenderStream stream = new SenderStream(size);
      stream.endHandler(v -> {
        resp.end();
      });
      Pump pump = Pump.pump(stream, resp);
      pump.start();
      stream.send();
    } else {
      resp.end();
    }
  }

}
