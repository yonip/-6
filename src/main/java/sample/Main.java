package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.util.Context;

public class Main extends Application {
    private FXMLLoader fxmlLoader;
    public static final Context context = new Context();


    @Override
    public void start(Stage primaryStage) throws Exception {
        //primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/package/macosx/μ6.icns")));
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"), null, new JavaFXBuilderFactory(), null);
        primaryStage.setScene(new Scene(fxmlLoader.load()));
        primaryStage.setTitle("μ6");
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
