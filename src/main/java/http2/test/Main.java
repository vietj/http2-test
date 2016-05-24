package http2.test;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Main {

  @Parameters()
  public static class MainCmd {
  }

  public static void main(String[] args) throws Exception {
    JCommander jc = new JCommander(new MainCmd());
    NetThroughputCommand tcp = new NetThroughputCommand();
    HttpDownloadServerCommand httpDownloadServer = new HttpDownloadServerCommand();
    HttpDownloadClientCommand httpDownloadClient = new HttpDownloadClientCommand();
    jc.addCommand("tcp", tcp);
    jc.addCommand("http-download-server", httpDownloadServer);
    jc.addCommand("http-download-client", httpDownloadClient);
    jc.parse(args);
    String cmd = jc.getParsedCommand();
    CommandBase command = null;
    if (cmd != null) {
      switch (cmd) {
        case "tcp":
          command = tcp;
          break;
        case "http-download-server":
          command = httpDownloadServer;
          break;
        case "http-download-client":
          command = httpDownloadClient;
          break;
        default:
          break;
      }
    }
    if (command == null) {
      jc.usage();
    } else {
      if (command.help) {
        new JCommander(command).usage();
      } else {
        command.run();
      }
    }
  }
}
