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
    HttpBackendServerCommand httpBackendServer = new HttpBackendServerCommand();
    HttpFrontendServerCommand httpFrontendServer = new HttpFrontendServerCommand();
    HttpClientCommand httpClient = new HttpClientCommand();
    jc.addCommand("tcp", tcp);
    jc.addCommand("http-backend", httpBackendServer);
    jc.addCommand("http-frontend", httpFrontendServer);
    jc.addCommand("http-client", httpClient);
    jc.parse(args);
    String cmd = jc.getParsedCommand();
    CommandBase command = null;
    if (cmd != null) {
      switch (cmd) {
        case "tcp":
          command = tcp;
          break;
        case "http-backend":
          command = httpBackendServer;
          break;
        case "http-frontend":
          command = httpFrontendServer;
          break;
        case "http-client":
          command = httpClient;
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
