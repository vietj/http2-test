package http2.test;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SenderStream implements ReadStream<Buffer> {

  private static final int CHUNK_SIZE = 512;
  private static final Buffer data = Buffer.buffer(new byte[CHUNK_SIZE]);
  private final long num;
  private final Context context = Vertx.currentContext();
  private boolean paused = false;
  private Handler<Buffer> dataHandler;
  private long sent = 0;
  private Handler<Void> endHandler;

  public SenderStream(long length) {
    this.num = length / CHUNK_SIZE;
  }

  @Override
  public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
    return this;
  }

  @Override
  public ReadStream<Buffer> handler(Handler<Buffer> handler) {
    dataHandler = handler;
    return this;
  }

  public void send() {
    if (!paused) {
      if (sent < num || num < 0) {
        sent += data.length();
        dataHandler.handle(data);
        context.runOnContext(v -> {
          send();
        });
      } else {
        paused = true;
        if (endHandler != null) {
          endHandler.handle(null);
        }
      }
    }
  }

  @Override
  public ReadStream<Buffer> pause() {
    paused = true;
    return this;
  }

  @Override
  public ReadStream<Buffer> resume() {
    paused = false;
    send();
    return this;
  }

  @Override
  public ReadStream<Buffer> endHandler(Handler<Void> handler) {
    endHandler = handler;
    return this;
  }
}
