package sample;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.InputStream;

public class VisualizerMain extends Application {
    private Canvas canvas;
    private Minim minim;
    private AudioPlayer player;
    private AnimationTimer timer;

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * <p>
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set. The primary stage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Visualizer Test");
        Group root = new Group();
        canvas = new Canvas(300, 250);
        minim = new Minim(this);
        player = minim.loadFile("/music/Billy Boyd - The Last Goodbye.mp3");
        timer = new AnimationTimer() {
            GraphicsContext gc;

            @Override
            public void handle(long now) {
                // now to drawing things
                gc = canvas.getGraphicsContext2D();
                gc.setStroke(Color.BLACK);
                gc.setFill(Color.gray(1));
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.setFill(Color.gray(0.2));
                gc.fillRect(0, 0, canvas.getWidth()*(player.position()/(double)(player.length())), 2);
                gc.beginPath();
                //gc.fill();
                gc.moveTo(0, 50);
                for(int i = 0; i < player.bufferSize()-5; i++)
                {
                    double left = canvas.getHeight()/2 + player.left.get(i) * canvas.getHeight()/2;
                    //System.out.print(left + " ");
                    gc.lineTo(canvas.getWidth() * ((double)i)/player.bufferSize(), left);
                }
                //System.out.println();
                //gc.closePath();
                gc.stroke();
            }
        };
        timer.start();
        player.play(30000);
        //GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public InputStream createInput(String path) {
        return getClass().getResourceAsStream(path);
    }

    public void stop() {
        player.close();
        minim.stop();
        timer.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
