package sample;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.InputStream;

public class TestController {
    @FXML
    private VBox information;

    @FXML
    private Pane canvasHolder;
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
    private Minim minim;
    private AudioPlayer player;
    private AnimationTimer timer;

    public static final int SECOND = 1000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     */
    @FXML
    public void initialize() {
        //canvas = new ResizableCanvas();
        //canvas.setWidth(canvasHolder.getWidth());
        //canvas.setHeight(canvasHolder.getHeight());
        System.out.println(canvasHolder.getHeight() + " " + canvasHolder.getWidth());
        //canvasHolder.getChildren().add(canvas);
        canvas = (Canvas)canvasHolder.getChildren().get(0);
        System.out.println(canvas.getHeight() + " " + canvas.getWidth());
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
        setButtonGraphic(playPause, playDefault);
        setButtonGraphic(skip, skipforwardDefault);
        setButtonGraphic(rewind, skipbackDefault);

        //media = new Media(getClass().getResource("/music/Billy Boyd - The Last Goodbye.mp3").toString());
        String file = "/music/Billy Boyd - The Last Goodbye.mp3";
        String name = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".mp3"));
        String artist = name.substring(0,name.indexOf("-")).trim();
        String song = name.substring(name.indexOf("-")+1).trim();
        songName.setText("Artist: " + artist + "\nSong: " + song);

        minim = new Minim(this);
        player = minim.loadFile(file);
        timer = new AnimationTimer() {
            GraphicsContext gc;
            double width;
            double height;

            @Override
            public void handle(long now) {
                // make sure sliders and text are updated
                songtime.setValue(player.position());
                time.setText(timeToText(player.position()));
                // now to drawing things
                gc = canvas.getGraphicsContext2D();
                width = canvas.getWidth();
                height = canvas.getHeight();
                gc.setStroke(Color.BLACK);
                gc.setFill(Color.gray(1));
                gc.fillRect(0, 0, width, height);
                gc.setFill(Color.gray(0.2));
                gc.fillRect(0, 0, width * (player.position() / (double) (player.length())), 2);
                gc.beginPath();
                //gc.fill();
                gc.moveTo(0, height/2);
                for(int i = 0; i < player.bufferSize(); i++)
                {
                    double left = height/2 + player.left.get(i) * height/2;
                    //System.out.print(left + " ");
                    gc.lineTo(width * ((double)i)/player.bufferSize(), left);
                }
                //System.out.println();
                //gc.closePath();
                gc.stroke();
            }
        };
        songtime.valueChangingProperty().addListener((ov, wasChanging, isChanging) -> {
            if (!isChanging) {
                player.cue((int) (songtime.getValue()));
            }
        });
        songtime.valueProperty().addListener((ov, oldVal, newVal) -> {
            progress.setProgress(newVal.doubleValue() / songtime.getMax());
            if (!songtime.isValueChanging()) {
                double currentTime = player.position();
                if (Math.abs(currentTime - newVal.doubleValue()) > 0.5) {
                    player.cue((int) newVal.doubleValue());
                }
            }
        });
        soundvol.valueProperty().addListener((observable, oldValue, newValue) -> {
            soundprog.setProgress((newValue.doubleValue()-soundvol.getMin()) / (soundvol.getMax()-soundvol.getMin()));
            player.setGain((float) newValue.doubleValue());
        });
        songtime.setMax(player.length());
        songtime.setValue(0);
        soundvol.setMax(14);
        soundvol.setMin(-80);
        soundvol.setValue(0);
        timer.start();
        player.pause();
    }

    private static void setButtonGraphic(Button b, ImageView iv) {
        iv.setFitHeight(26);
        iv.setFitWidth(26);
        b.setGraphic(iv);
    }

    private static String timeToText(int time) {
        int hrs = time/HOUR;
        int min = (time/MINUTE) % 60;
        int sec = (time/SECOND) % 60;
        String t = ((hrs == 0) ? "" : hrs + ":");
        t += ((t.isEmpty() && min < 10) ? "0" : "") + min + ":" + ((sec < 10) ? "0" : "") + sec;
        return t;
    }

    @FXML
    private void playPausePressed(MouseEvent event) {
        if (playing) {
            setButtonGraphic(playPause, pausePressed);
            pause();
        } else {
            setButtonGraphic(playPause, playPressed);
            play();
        }
    }

    @FXML
    private void playPauseReleased(MouseEvent event) {
        if (playing) {
            setButtonGraphic(playPause, pauseDefault);
        } else {
            setButtonGraphic(playPause, playDefault);
        }
    }

    @FXML
    private void rewindPressed(MouseEvent event) {
        setButtonGraphic(rewind, skipbackPressed);
        player.cue(0);
    }

    @FXML
    private void rewindReleased(MouseEvent event) {
        setButtonGraphic(rewind, skipbackDefault);
    }

    @FXML
    private void skipPressed(MouseEvent event) {
        setButtonGraphic(skip, skipforwardPressed);
        player.cue(player.length());
    }

    @FXML
    private void skipReleased(MouseEvent event) {
        setButtonGraphic(skip, skipforwardDefault);
    }

    private void play() {
        this.playing = true;
        this.player.play();
    }

    private void pause() {
        this.playing = false;
        this.player.pause();
    }

    public InputStream createInput(String path) {
        return getClass().getResourceAsStream(path);
    }

    public void stop() {
        player.close();
        minim.stop();
    }
}
