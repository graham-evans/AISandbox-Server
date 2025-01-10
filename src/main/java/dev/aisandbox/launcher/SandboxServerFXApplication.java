package dev.aisandbox.launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ResourceBundle;

@Slf4j
@SpringBootApplication(scanBasePackages = "dev.aisandbox.server")
public class SandboxServerFXApplication extends Application {
    private ConfigurableApplicationContext context;
    private Parent rootNode;

    @Override
    public void init() throws Exception {
        log.info("Initialising application - FX");
        SpringApplicationBuilder builder = new SpringApplicationBuilder(SandboxServerFXApplication.class);
        builder.headless(false);
        context = builder.run(getParameters().getRaw().toArray(new String[0]));
        // load the root FXML screen, using spring to create the controller
        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/fx/simulation.fxml"));
   //     loader.setResources(ResourceBundle.getBundle("dev.aisandbox.client.fx.UI"));
        loader.setControllerFactory(context::getBean);
        rootNode = loader.load();
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.info("Starting application - FX");
        stage.setScene(new Scene(rootNode, 800, 600));
        stage.centerOnScreen();
        stage.setTitle("AI Sandbox");
        stage
                .getIcons()
                .add(
                        new Image(
                                SandboxServerFXApplication.class.getResourceAsStream("/images/AILogo.png")));
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping application");
  /*      ApplicationModel model = context.getBean(ApplicationModel.class);
        try {
            model.resetRuntime();
        } catch (Exception e) {
            log.debug("Error when closing runtime", e);
        }*/
        context.close();
        System.exit(0);
    }
}
