package http2.test;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;

import java.math.BigInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Parameters()
public class HttpBackendServerCommand extends BaseHttpServerCommand {

  @Parameter(names = "--length", description = "the length in bytes")
  public String length = "0";

  @Parameter(names = "--chunk-size", description = "the chunk size in bytes")
  public int chunkSize = 1024;

  @Parameter(names = "--delay", description = "the delay in ms for sending the response")
  public long delay = 40;

  public static void main(String[] args) throws Exception {
    new HttpBackendServerCommand().run();
  }

  @Override
  protected void handle(HttpServerRequest req) {
    HttpServerResponse resp = req.response();
    if (delay > 0) {
      vertx.setTimer(delay, v -> {
        handleResp(resp);
      });
    } else {
      handleResp(resp);
    }
  }

  private void handleResp(HttpServerResponse resp) {
    long l = Utils.parseSize(length).longValue();
    if (l > 0) {
      resp.setChunked(true);
      SenderStream stream = new SenderStream(l, chunkSize);
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
