package dev.aisandbox.launcher;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;

@Slf4j
public class SandboxServerLauncher {

    public static void main(String[] args) {
        log.info("Launching AISandbox");
        if (args.length > 0) {
            // launch UI
            log.info("Launching AISandbox Server CLI");
            SpringApplication.run(SandboxServerCLIApplication.class, args);
        } else {
            // launch CLI
            log.info("Launching AISandbox Server FX");
            Application.launch(SandboxServerFXApplication.class, args);
        }
    }


}
