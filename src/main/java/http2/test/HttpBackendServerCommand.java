package http2.test;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Parameters()
public class HttpBackendServerCommand extends BaseHttpServerCommand {

  @Parameter(names = "--size")
  public long size = 0;

  @Parameter(names = "--think-time")
  public long thinkTime = 40;

  public static void main(String[] args) throws Exception {
    new HttpBackendServerCommand().run();
  }

  @Override
  protected void handle(HttpServerRequest req) {
    HttpServerResponse resp = req.response();
    if (thinkTime > 0) {
      vertx.setTimer(thinkTime, v -> {
        handleResp(resp);
      });
    } else {
      handleResp(resp);
    }
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
