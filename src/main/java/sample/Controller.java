package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.net.URISyntaxException;

public class Controller {
    @FXML
    private VBox information;

    @FXML
    private Canvas canvas;
    @FXML
    private ListView list;

    @FXML
    private Text songName;
    @FXML
    private Slider songtime;
    @FXML
    private ProgressBar progress;
    @FXML
    private Text time;

    @FXML
    private Button playPause;
    @FXML
    private Button rewind;
    @FXML
    private Button skip;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     */
    @FXML
    public void initialize() {
        Media ac = new Media(getClass().getResource("/music/BillyBoyd-TheLastGoodbye.mp3").toString());
        MediaPlayer m = new MediaPlayer(ac);
        m.totalDurationProperty().addListener((ov, oldValue, newValue) -> songtime.setMax(newValue.toSeconds()));
        m.setOnReady(() -> {
            songtime.setMax(ac.getDuration().toSeconds());
            songtime.setValue(0);
            m.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                int hrs = (int)(newValue.toHours());
                int min = (int)(newValue.toMinutes())%60;
                int sec = (int)(newValue.toSeconds())%60;
                String t = ((hrs == 0) ? "" : hrs + ":");
                t += ((t.isEmpty() && min < 10) ? "0" : "") + min + ":" + ((sec < 10) ? "0" : "") + sec;
                time.setText(t);
                if (!songtime.isValueChanging()) {
                    songtime.setValue(newValue.toSeconds());
                }
            });
            songtime.valueChangingProperty().addListener((ov, wasChanging, isChanging) -> {
                if (!isChanging) {
                    m.seek(Duration.seconds(songtime.getValue()));
                }
            });
            songtime.valueProperty().addListener((ov, oldVal, newVal) -> {
                progress.setProgress(newVal.doubleValue() / songtime.getMax());
                if (!songtime.isValueChanging()) {
                    double currentTime = m.getCurrentTime().toSeconds();
                    if (Math.abs(currentTime - newVal.doubleValue()) > 0.5) {
                        m.seek(Duration.seconds(newVal.doubleValue()));
                    }
                }
            });

            m.play();
            System.out.println("ready");
        });
    }
}
