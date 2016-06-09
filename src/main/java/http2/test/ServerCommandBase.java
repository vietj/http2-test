package http2.test;

import com.beust.jcommander.Parameter;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class ServerCommandBase extends CommandBase {

  @Parameter(names = "--host")
  public String host = "0.0.0.0";

  @Parameter(names = "--port")
  public int port = 8080;

  protected final <T> T start(Startable<T> server) throws Exception {
    return start(server, t -> {});
  }

  protected final <T> T start(Startable<T> server, Handler<T> handler) throws Exception {
    CompletableFuture<T> fut = new CompletableFuture<>();
    server.start(port, host, ar -> {
      if (ar.succeeded()) {
        T result = ar.result();
        fut.complete(result);
        handler.handle(result);
      } else {
        fut.completeExceptionally(ar.cause());
      }
    });
    try {
      return fut.get(10, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      throw (Exception) e.getCause();
    }
  }

  public interface Startable<T> {
    void start(int port, String host, Handler<AsyncResult<T>> handler);
  }
}
