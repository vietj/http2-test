package http2.test;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;

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

  @Parameter(names = "--backend-concurrency")
  public int backendConcurrency = -1;

  private HttpClient client;

  public static void main(String[] args) throws Exception {
    new HttpFrontendServerCommand().run();
  }

  @Override
  public void run() throws Exception {
    HttpClientOptions options = new HttpClientOptions();
    options.setMaxPoolSize(poolSize);
    options.setPipelining(true);
    options.setKeepAlive(true);
    options.setProtocolVersion(backendProtocol);
    options.setH2cUpgrade(false);
    if (backendConcurrency > 0) {
      options.setHttp2MaxStreams(backendConcurrency);
    }
    client = vertx.createHttpClient(options);
    super.run();
  }

  @Override
  protected void handle(HttpServerRequest req) {
    client.getNow(backendPort, backendHost, "/somepath", resp -> {
      resp.endHandler(v -> {
        req.response().end();
      });
    });
  }
}
