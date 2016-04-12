package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sun.reflect.Reflection;

import java.nio.charset.Charset;

public class Main extends Application {
    private FXMLLoader fxmlLoader;


    @Override
    public void start(Stage primaryStage) throws Exception {
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"), null, new JavaFXBuilderFactory(), null);
        primaryStage.setScene(new Scene(fxmlLoader.load()));
        primaryStage.show();
    }

    @Override
    public void stop() {
        ((Controller)fxmlLoader.getController()).stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
