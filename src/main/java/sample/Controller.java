package sample;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import sample.util.ButtonImage;
import sample.util.Context;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Stack;

public class Controller {
    @FXML
    private VBox information;

    @FXML
    private Pane canvasHolder;
    private Canvas canvas;
    @FXML
    private ListView list;
    private ObservableList<FileMeta> musicList;
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

    private ButtonImage playImgs;
    private ButtonImage pauseImgs;
    private ButtonImage repeatImgs;
    private ButtonImage skipbackImgs;
    private ButtonImage skipforwardImgs;

    private boolean playing;
    private Minim minim;
    private FFT fft;
    private AudioPlayer currentPlayer;
    private int pos;
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
        System.out.println(canvasHolder.getHeight() + " " + canvasHolder.getWidth());
        canvas = (Canvas)canvasHolder.getChildren().get(0);
        System.out.println(canvas.getHeight() + " " + canvas.getWidth());
        playPause.setText("");
        skip.setText("");
        rewind.setText("");
        playImgs = new ButtonImage("play");
        pauseImgs = new ButtonImage("pause");
        repeatImgs = new ButtonImage("repeat");
        skipbackImgs = new ButtonImage("skipback");
        skipforwardImgs = new ButtonImage("skipforward");
        playPause.setGraphic(playImgs.getDefaultImage());
        skip.setGraphic(skipforwardImgs.getDefaultImage());
        rewind.setGraphic(skipbackImgs.getDefaultImage());

        //media = new Media(getClass().getResource("/music/Billy Boyd - The Last Goodbye.mp3").toString());

        minim = new Minim(this);
        musicList = FXCollections.observableArrayList();
        list.setItems(musicList);
        read();
        pos = 0;
        songName.setText(musicList.get(pos).fileName());
        //player = minim.loadFile(musicList.get(0).filePath());
        fft = new FFT(player().bufferSize(), player().sampleRate());
        timer = new AnimationTimer() {
            GraphicsContext gc;
            double width;
            double height;
            double left;
            double right;
            double band;
            int ln = 40;

            @Override
            public void handle(long now) {
                // make sure sliders and text are updated
                songtime.setValue(player().position());
                time.setText(timeToText(player().position()));
                if (!player().isPlaying() && playing) {
                    playPause.setGraphic(playImgs.getDefaultImage());
                    pause();
                }
                if (player().position() == player().length()) {
                    Platform.runLater(() -> {
                        player().close();
                        pos++;
                        songtime.setMax(player().length());
                        songName.setText(musicList.get(pos).fileName());
                        playPause.setGraphic(pauseImgs.getDefaultImage());
                        play();
                    });
                }
                // now to drawing things
                gc = canvas.getGraphicsContext2D();
                width = canvas.getWidth();
                height = canvas.getHeight();
                gc.setStroke(Color.BLACK);
                //gc.setFill(Color.gray(1)); ORIGINAL
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, width, height);
                gc.setFill(Color.gray(0.2));
                gc.fillRect(0, height, width * (player().position() / (double) (player().length())), height - 2);
                gc.beginPath();
                //gc.fill();
                //gc.moveTo(0, height/2);
                double polarR = 100;
                double polarTheta=0;
                double polarR2 =100;
                double polarTheta2 = 0;
                double left = 0;
                double left2 = 0;
                for(int i = 0; i < player().bufferSize(); i=i+(int)(10-Math.abs(Math.random()*8)))
                {
                    left = (height/2 + player().left.get(i) * height/2);
                    //gc.lineTo(width * ((double)i)/player().bufferSize(), left);
                    //polarR=Math.abs(Math.sin(player().position() / 10000000.0)) + (double)left/5.0;

                    //outside radius
                    polarR = 20*Math.abs(Math.cos(player().position()/1000.0)) + left/2.0;
                    polarTheta = (double)(i+now/100000000.0)/((double)player().bufferSize())*Math.PI*2;
                    //gc.lineTo(polarR*Math.cos(polarTheta)+width/2,polarR*Math.sin(polarTheta)+height/2);
                    //System.out.print(left + " ");
                    //gc.lineTo(width * ((double)i)/player().bufferSize(), left);

                    if (i<player().bufferSize()){
                        for (int m=80;m<101;m=m+20) {
                            //y values
                            left2 = (canvas.getHeight() / 2 + player().left.get((i + m)%player().bufferSize()) * canvas.getHeight() / 2);
                            left2 = Math.log(left2)+height/2;

                            //converting to polar
                            polarR2 = 40*(Math.abs(Math.cos(now/100000000000.0)))+left2/2.0;
                            //polarR2 =  left2 / 5.0;

                            polarTheta2 = (double) (i + m+player().position()/100000.0) / ((double) player().bufferSize()) * Math.PI * 2;

                            //color and drawing the line
                            gc.setStroke(new Color(Math.abs(Math.cos(player().position()/100000.0)), Math.abs(Math.sin(player().position()/5000000.0)), Math.abs(Math.cos(now/60000000000.0)), 0.2));
                            gc.strokeLine(polarR * Math.cos(polarTheta) + width / 2, polarR * Math.sin(polarTheta) + height / 2, polarR2 * Math.cos(polarTheta2) + width / 2, polarR2 * Math.sin(polarTheta2) + height / 2);
                        }
                    }

                }
                gc.moveTo(0, height/2);


                //Some cool setups:
                //right = height/2 + player().right.get(i)*height/2
                //inside for loop, right2 = Math.log(right2)+height/2
                double right2=0;
                double extraX=0;
                double extraY=0;
                double extraY2=0;
                double xValue=0;
                double xValueNext=0;
                double zoom=0;
                for(int i = 0; i < player().bufferSize(); i=i+10)
                {
                    right = height/2 + player().right.get(i) * height/2;
                    right = right -200;
                    //right = Math.sqrt(right)+height/2;
                    //gc.lineTo(width * ((double)i)/player().bufferSize(), right);
                    gc.strokeLine(width*(double)i/player().bufferSize(),right/2.0+height/2,width*(double)i/player().bufferSize(),height/2-right/2.0);
                    int forLoop = (int)(80*Math.abs(Math.sin(player().position()/10000.0)));
                    /**
                    for (int m=forLoop;m<forLoop+2;m=m+20){
                        right2 = height/2 + player().right.get(i+m) * height/2;
                        xValue = canvas.getWidth()*((double)i)/player().bufferSize();
                        xValueNext = canvas.getWidth()* ((double)(i+m))/player().bufferSize();
                        right2 = Math.log(right2+30*Math.random())+height/2;
                        //gc.setStroke(new Color(0.25,0.4,0.72,1-m/100.0));
                        gc.setStroke(new Color(Math.abs((double)Math.sin(player().position()/40000.0)),Math.abs(Math.sin(0.4-(player().position()/20000.0))),0.7,0.5));
                        zoom = 3*Math.sin(player().position()/10000.0);
                        //gc.strokeLine((xValue-width/2)*zoom+width/2,(right-height/2.0)*zoom+height/2, (xValueNext-width/2)*zoom+width/2,(right2-height/2)*zoom+height/2);
                    } **/
                }
                //System.out.println();
                //gc.closePath();
                gc.stroke();
                fft.forward(player().mix);
                gc.setFill(Color.color(1, 0, 0, 0.5));
                gc.setFill(Color.DARKBLUE);
                for(int i = 0; i < ln; i++) {
                    band = fft.getBand(i);
                    for (int dots=0;dots<band*1.5;dots=dots + 5){
                        gc.setFill(new Color((double)(i/ln),0.74,0.9,0.5));
                        gc.fillOval(i*(width/ln),height-dots,2.5,2.5);
                    }
                    //gc.fillRect(i * (width/ln), 0, (width/ln), band);
                }
            }
        };
        songtime.valueChangingProperty().addListener((ov, wasChanging, isChanging) -> {
            if (!isChanging) {
                player().cue((int) (songtime.getValue()));
            }
        });
        songtime.valueProperty().addListener((ov, oldVal, newVal) -> {
            progress.setProgress(newVal.doubleValue() / songtime.getMax());
            if (!songtime.isValueChanging()) {
                double currentTime = player().position();
                if (Math.abs(currentTime - newVal.doubleValue()) > 0.5) {
                    player().cue((int) newVal.doubleValue());
                }
            }
        });
        soundvol.valueProperty().addListener((observable, oldValue, newValue) -> {
            soundprog.setProgress((newValue.doubleValue()-soundvol.getMin()) / (soundvol.getMax()-soundvol.getMin()));
            player().setGain((float) newValue.doubleValue());
        });
        songtime.setMax(player().length());
        songtime.setValue(0);
        soundvol.setMax(14);
        soundvol.setMin(-80);
        soundvol.setValue(0);
        timer.start();
        player().pause();
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
            playPause.setGraphic(pauseImgs.getPressedImage());
            pause();
        } else {
            playPause.setGraphic(playImgs.getPressedImage());
            play();
        }
    }

    @FXML
    private void playPauseReleased(MouseEvent event) {
        if (playing) {
            playPause.setGraphic(pauseImgs.getDefaultImage());
        } else {
            playPause.setGraphic(playImgs.getDefaultImage());
        }
    }

    @FXML
    private void rewindPressed(MouseEvent event) {
        rewind.setGraphic(skipbackImgs.getPressedImage());
        player().cue(0);
    }

    @FXML
    private void rewindReleased(MouseEvent event) {
        rewind.setGraphic(skipbackImgs.getDefaultImage());
    }

    @FXML
    private void skipPressed(MouseEvent event) {
        skip.setGraphic(skipforwardImgs.getPressedImage());
        player().cue(player().length());
    }

    @FXML
    private void skipReleased(MouseEvent event) {
        skip.setGraphic(skipforwardImgs.getDefaultImage());
    }

    @FXML
    private void openSettings(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"), null, new JavaFXBuilderFactory(), null);
        Stage stage = new Stage();
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setAlwaysOnTop(true);
        stage.setOnCloseRequest(event1 -> read());
        stage.show();
    }

    private void read() {
        Stack<File> dirs = new Stack<>();
        for (String path : Main.context.musicDirectories) {
            dirs.push(new File(path));
        }
        File f;
        while (!dirs.isEmpty()) {
            f = dirs.pop();
            if (!f.exists()) {
                continue;
            }
            if (f.isDirectory()) {
                dirs.addAll(Arrays.asList(f.listFiles()));
                continue;
            }
            if(f.isFile()) {
                String name = f.getName();
                String ext = name.substring(name.lastIndexOf(".")+1);
                for (String exten : Main.context.extensions) {
                    if (ext.equals(exten)) {
                        musicList.add(new FileMeta(f, minim));
                        break;
                    }
                }
            }
        }
        musicList.sort(null);
    }

    private void play() {
        this.playing = true;
        this.player().play();
    }

    private void pause() {
        this.playing = false;
        this.player().pause();
    }

    public InputStream createInput(String path) throws FileNotFoundException {
        return new FileInputStream(path);
    }

    public void stop() {
        for (FileMeta f : musicList) {
            f.player.close();
        }
        minim.stop();
        timer.stop();
    }

    private class FileMeta implements Comparable<FileMeta> {
        private String song;
        private String artist;
        private String album;
        private Date modTime;
        private String fileName;
        private String filePath;
        private AudioPlayer player;
        private Context.Comparisons comparison;

        public FileMeta(File file, Minim min) {
            String name = file.getName();
            filePath = file.getAbsolutePath();
            player = min.loadFile(filePath);
            fileName = name.substring(name.lastIndexOf("/") + 1, name.lastIndexOf(".mp3"));
            int ind = fileName.indexOf("-");
            int ind2 = ind + 1;
            if (ind < 0) {
                ind = fileName.length();
                ind2 = ind;
            }
            artist = !player.getMetaData().author().isEmpty() ? player.getMetaData().author() : fileName.substring(0, ind).trim();
            song = !player.getMetaData().title().isEmpty() ? player.getMetaData().title() : fileName.substring(ind2).trim();
            modTime = new Date(file.lastModified());
            comparison = Context.Comparisons.SONG_NAME;
        }

        @Override
        public String toString() {
            return (song.isEmpty() ? "" : song + " - ") + artist + " | " + modTime.toString();
        }

        public String fileName() {
            return fileName;
        }

        public String filePath() {
            return filePath;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         * <p>
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
         * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
         * <tt>y.compareTo(x)</tt> throws an exception.)
         * <p>
         * <p>The implementor must also ensure that the relation is transitive:
         * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         * <p>
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
         * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         * <p>
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         * <p>
         * <p>In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(FileMeta o) {
            switch (comparison) {
                case AUTHOR_NAME:
                    return artist.compareTo(o.artist);
                case SONG_NAME:
                    return song.compareTo(o.song);
                case SONG_LENGTH:
                    return player.length() - o.player.length();
                case MOD_TIME:
                default:
                    return modTime.compareTo(o.modTime);
            }
        }
    }

    private AudioPlayer player() {
        return musicList.get(pos).player;
    }
}
