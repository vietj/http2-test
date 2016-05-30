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
    NetClientCommand netClient = new NetClientCommand();
    NetServerCommand netServer = new NetServerCommand();
    HttpBackendServerCommand httpBackendServer = new HttpBackendServerCommand();
    HttpFrontendServerCommand httpFrontendServer = new HttpFrontendServerCommand();
    HttpClientCommand httpClient = new HttpClientCommand();
    jc.addCommand("net-client", netClient);
    jc.addCommand("net-server", netServer);
    jc.addCommand("http-backend", httpBackendServer);
    jc.addCommand("http-frontend", httpFrontendServer);
    jc.addCommand("http-client", httpClient);
    jc.parse(args);
    String cmd = jc.getParsedCommand();
    CommandBase command = null;
    if (cmd != null) {
      switch (cmd) {
        case "net-client":
          command = netClient;
          break;
        case "net-server":
          command = netServer;
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
