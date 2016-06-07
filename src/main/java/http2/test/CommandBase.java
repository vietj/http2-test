package http2.test;

import com.beust.jcommander.Parameter;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class CommandBase {

  @Parameter(names = "--help", help = true)
  public boolean help;

  @Parameter(names = "--so-backlog")
  public int soBacklog = 1024;

  @Parameter(names = "--open-ssl")
  public boolean openSSL;

  @Parameter(names = "--send-buffer-size")
  public int sendBufferSize = -1;

  @Parameter(names = "--receive-buffer-size")
  public int receiveBufferSize = -1;

  protected final Vertx vertx = Vertx.vertx(createOptions());

  protected VertxOptions createOptions() {
    return new VertxOptions();
  }

  public abstract void run() throws Exception;

}
