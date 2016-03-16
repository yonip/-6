package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.swing.*;

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

    private ImageView playDefault;
    private ImageView playPressed;
    private ImageView pauseDefault;
    private ImageView pausePressed;
    private ImageView repeatDefault;
    private ImageView repeatPressed;
    private ImageView skipbackDefault;
    private ImageView skipbackPressed;
    private ImageView skipforwardDefault;
    private ImageView skipforwardPressed;

    private boolean playing;
    Media media;
    MediaPlayer player;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     */
    @FXML
    public void initialize() {
        playPause.setText("");
        skip.setText("");
        rewind.setText("");
        playDefault = new ImageView(new Image(getClass().getResourceAsStream("/images/button/play_default.png")));
        playPressed = new ImageView(new Image(getClass().getResourceAsStream("/images/button/play_pressed.png")));
        pauseDefault = new ImageView(new Image(getClass().getResourceAsStream("/images/button/pause_default.png")));
        pausePressed = new ImageView(new Image(getClass().getResourceAsStream("/images/button/pause_pressed.png")));
        repeatDefault = new ImageView(new Image(getClass().getResourceAsStream("/images/button/repeat_default.png")));
        repeatPressed = new ImageView(new Image(getClass().getResourceAsStream("/images/button/repeat_pressed.png")));
        skipbackDefault = new ImageView(new Image(getClass().getResourceAsStream("/images/button/skipback_default.png")));
        skipbackPressed = new ImageView(new Image(getClass().getResourceAsStream("/images/button/skipback_pressed.png")));
        skipforwardDefault = new ImageView(new Image(getClass().getResourceAsStream("/images/button/skipforward_default.png")));
        skipforwardPressed = new ImageView(new Image(getClass().getResourceAsStream("/images/button/skipforward_pressed.png")));
        playPause.setGraphic(playDefault);
        skip.setGraphic(skipforwardDefault);
        rewind.setGraphic(skipbackDefault);
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

            pause();

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
    private void playPausePressed(MouseEvent event) {
        if (playing) {
            playPause.setGraphic(pausePressed);
            pause();
        } else {
            playPause.setGraphic(playPressed);
            play();
        }
    }

    @FXML
    private void playPauseReleased(MouseEvent event) {
        if (playing) {
            playPause.setGraphic(pauseDefault);
        } else {
            playPause.setGraphic(playDefault);
        }
    }

    @FXML
    private void rewindPressed(MouseEvent event) {
        rewind.setGraphic(skipbackPressed);
        player.seek(player.getStartTime());
    }

    @FXML
    private void rewindReleased(MouseEvent event) {
        rewind.setGraphic(skipbackDefault);
    }

    @FXML
    private void skipPressed(MouseEvent event) {
        skip.setGraphic(skipforwardPressed);
        player.seek(player.getStopTime());
    }

    @FXML
    private void skipReleased(MouseEvent event) {
        skip.setGraphic(skipforwardDefault);
    }

    private void play() {
        this.playing = true;
        this.player.play();
    }

    private void pause() {
        this.playing = false;
        this.player.pause();
    }
}
