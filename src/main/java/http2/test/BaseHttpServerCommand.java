package http2.test;

import com.beust.jcommander.Parameter;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class BaseHttpServerCommand extends CommandBase {

  @Parameter(names = "--concurrency")
  public long concurrency = -1;

  public void run() throws Exception {
    HttpServerOptions options = new HttpServerOptions().setInitialSettings(new Http2Settings().
        setMaxConcurrentStreams(concurrency == -1 ? Http2Settings.DEFAULT_MAX_CONCURRENT_STREAMS : concurrency));
    options.setSendBufferSize(sendBufferSize);
    options.setReceiveBufferSize(receiveBufferSize);
    HttpServer server = vertx.createHttpServer(options);
    server.requestHandler(this::handle);
    this.<HttpServer>start(server::listen);
    System.out.println("server started on " + port);
  }

  protected abstract void handle(HttpServerRequest req);

}
