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

  private static final int chunkSize = 512;
  private static final Buffer data = Buffer.buffer(new byte[chunkSize]);
  private final long length;
  private final long delay = 1;
  private final Context context = Vertx.currentContext();
  private boolean paused = false;
  private Handler<Buffer> dataHandler;
  private long sent = 0;
  private Handler<Void> endHandler;
  private final String debug;

  public SenderStream() {
    this(-1);
  }

  public SenderStream(long length, String debug) {
    this.length = length;
    this.debug = debug;
  }

  public SenderStream(long length) {
    this(length, null);
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
      if (sent < length || length < 0) {
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
