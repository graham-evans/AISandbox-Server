package dev.aisandbox.launcher;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SandboxServerLauncher {

  public static void main(String[] args) {
    log.info("Launching AISandbox");
    if (args.length > 0) {
      // launch UI
      log.info("Launching AISandbox Server CLI");
      SandboxServerCLIApplication application = new SandboxServerCLIApplication();
      application.run(args);
    } else {
      // launch CLI
      log.info("Launching AISandbox Server FX");
      Application.launch(SandboxServerFXApplication.class, args);
    }
  }

}
