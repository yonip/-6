package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;

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

    @FXML
    private Slider soundvol;
    @FXML
    private ProgressBar soundprog;

    private boolean playing;
    Media media;
    MediaPlayer player;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     */
    @FXML
    public void initialize() {
        media = new Media(getClass().getResource("/music/Billy Boyd - The Last Goodbye.mp3").toString());
        String file = media.getSource().replace("%20", " ");
        String name = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".mp3"));
        String artist = name.substring(0,name.indexOf("-")).trim();
        String song = name.substring(name.indexOf("-")+1).trim();
        songName.setText("Artist: " + artist + "\nSong: " + song);
        player = new MediaPlayer(media);
        player.totalDurationProperty().addListener((ov, oldValue, newValue) -> songtime.setMax(newValue.toSeconds()));
        player.setOnReady(() -> {
            player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                time.setText(timeToText(newValue) + "/" + timeToText(player.getStopTime()));
                if (!songtime.isValueChanging()) {
                    songtime.setValue(newValue.toSeconds());
                }
            });
            songtime.valueChangingProperty().addListener((ov, wasChanging, isChanging) -> {
                if (!isChanging) {
                    player.seek(Duration.seconds(songtime.getValue()));
                }
            });
            songtime.valueProperty().addListener((ov, oldVal, newVal) -> {
                progress.setProgress(newVal.doubleValue() / songtime.getMax());
                if (!songtime.isValueChanging()) {
                    double currentTime = player.getCurrentTime().toSeconds();
                    if (Math.abs(currentTime - newVal.doubleValue()) > 0.5) {
                        player.seek(Duration.seconds(newVal.doubleValue()));
                    }
                }
            });
            songtime.setMax(media.getDuration().toSeconds());
            songtime.setValue(0);

            soundvol.valueProperty().addListener((observable, oldValue, newValue) -> {
                soundprog.setProgress(newValue.doubleValue() / soundvol.getMax());
                player.setVolume(newValue.doubleValue());
            });
            soundvol.setMax(1);
            soundvol.setValue(0);

            play();

            System.out.println("ready");
        });
    }

    private static String timeToText(Duration time) {
        int hrs = (int) (time.toHours());
        int min = (int) (time.toMinutes()) % 60;
        int sec = (int) (time.toSeconds()) % 60;
        String t = ((hrs == 0) ? "" : hrs + ":");
        t += ((t.isEmpty() && min < 10) ? "0" : "") + min + ":" + ((sec < 10) ? "0" : "") + sec;
        return t;
    }

    @FXML
    private void playPause(ActionEvent event) {
        if (playing) {
            pause();
        } else {
            play();
        }
    }

    @FXML
    private void rewind(ActionEvent event) {
        player.seek(player.getStartTime());
    }

    @FXML
    private void skip(ActionEvent event) {
        player.seek(player.getStopTime());
    }

    private void play() {
        this.playing = true;
        this.playPause.setText("||");
        this.player.play();
    }

    private void pause() {
        this.playing = false;
        this.playPause.setText(">");
        this.player.pause();
    }
}
