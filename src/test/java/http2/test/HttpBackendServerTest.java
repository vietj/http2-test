package http2.test;

import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public class HttpBackendServerTest {

  @Test
  public void testSendFile(TestContext ctx) throws Exception {
    HttpBackendServerCommand cmd = new HttpBackendServerCommand();
    cmd.delay = 0;
    cmd.length = "" + cmd.chunkSize * 30 + 27;
    cmd.run();
    HttpClient client = cmd.vertx.createHttpClient();
    Async async = ctx.async();
    client.getNow(8080, "localhost", "/somepath", resp -> {
      resp.bodyHandler(buff -> {
        ctx.assertEquals(cmd.length, "" + buff.length());
        async.complete();
      });
    });
  }

}
