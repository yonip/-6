package sample;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import sample.util.ButtonImage;
import sample.util.Context;

import java.io.*;
import java.time.Clock;
import java.util.*;

@SuppressWarnings("unused")
public class Controller {
    @FXML
    private VBox information;

    @FXML
    private Pane canvasHolder;
    private Canvas canvas;
    private ObservableList<FileMeta> musicList;
    private ObservableList<Integer> presList;
    private Map<String, FileMeta> files;

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

    @FXML
    private TableView<Integer> songTable;
    @FXML
    private TableColumn<Integer, String> songTitleColumn;
    @FXML
    private TableColumn<Integer, String> songAuthorColumn;
    @FXML
    private TableColumn<Integer, String> songDurationColumn;
    @FXML
    private TableColumn<Integer, String> songAgeColumn;

    private ButtonImage playImgs;
    private ButtonImage pauseImgs;
    private ButtonImage repeatImgs;
    private ButtonImage skipbackImgs;
    private ButtonImage skipforwardImgs;

    private boolean playing;
    private Minim minim;
    private FFT fft;
    private AudioPlayer currentPlayer;
    private BeatDetect beat;
    private int pos;
    private AnimationTimer timer;
    private long lastRewind;
    private long skipbackTimeout = 250;
    private ArrayList<Double> circles=null;
    private ArrayList<Double> circlesNew = null;
    private ArrayList<Double> speed=null;
    private ArrayList<Double> disks = null;

    private int amountSkip=10;
    private double tall = 1.4;
    double addedFactor =0;
    double posit = 0;
    double polarR = 100;
    double polarTheta=0;
    double polarR2 =100;
    double polarTheta2=0;
    double dist;
    double left =0;
    double right;
    double left2 = 0;
    double tempX;
    double tempY;
    double intervalBar=0;
    public static final int SECOND = 1000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     */
    @FXML
    public void initialize() {
        files = new HashMap<>();
        canvas = (Canvas) canvasHolder.getChildren().get(0);
        playPause.setText("");
        skip.setText("");
        rewind.setText("");
        playImgs = new ButtonImage("play");
        pauseImgs = new ButtonImage("pause");
        repeatImgs = new ButtonImage("repeat");
        skipbackImgs = new ButtonImage("skipback");
        skipforwardImgs = new ButtonImage("skipforward");
        playPause.setGraphic(playImgs.getImage());
        skip.setGraphic(skipforwardImgs.getImage());
        rewind.setGraphic(skipbackImgs.getImage());
        circles=new ArrayList<Double>();
        circlesNew=new ArrayList<Double>();
        speed = new ArrayList<Double>();

        minim = new Minim(this);
        musicList = FXCollections.observableArrayList();
        presList = FXCollections.observableArrayList();
        songTable.setItems(presList);
        songTitleColumn.setCellValueFactory(param -> musicList.get(param.getValue()).title);
        songAuthorColumn.setCellValueFactory(param -> musicList.get(param.getValue()).artist);
        songAgeColumn.setCellValueFactory(param -> {
            long secs = Clock.systemUTC().instant().getEpochSecond() - (musicList.get(param.getValue()).modTime.getTime() / 1000);
            long mins = secs / 60;
            long hrs = mins / 60;
            long days = hrs / 24;
            long weeks = days / 7;
            long months = (long) (days / 30.42);
            long years = months / 12;
            String word;
            long res;
            if (years > 0) {
                return new ReadOnlyStringWrapper(years + " year" + (years > 1 ? "s" : ""));
            }
            if (months > 0) {
                return new ReadOnlyStringWrapper(months + " month" + (months > 1 ? "s" : ""));
            }
            if (weeks > 0) {
                return new ReadOnlyStringWrapper(weeks + " week" + (weeks > 1 ? "s" : ""));
            }
            if (days > 0) {
                return new ReadOnlyStringWrapper(days + " day" + (days > 1 ? "s" : ""));
            }
            if (hrs > 0) {
                return new ReadOnlyStringWrapper(hrs + " hour" + (hrs > 1 ? "s" : ""));
            }
            if (mins > 0) {
                return new ReadOnlyStringWrapper(mins + " minute" + (mins > 1 ? "s" : ""));
            }
            return new ReadOnlyStringWrapper("Just now");
        });
        songDurationColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(timeToText(musicList.get(param.getValue()).length, true)));
        read();
        pos = 0;
        configurePlayer(pos);
        fft = new FFT(player().bufferSize(), player().sampleRate());

        for (int i=0;i<player().bufferSize();i=i+10){
            circles.add(Math.random()*canvas.getWidth());
            circles.add(Math.random()*canvas.getHeight());
            speed.add(Math.random()*0.5);

        }
        for (int i=0;i<circles.size();i++){

        }
        circlesNew.addAll(circles);

        beat = new BeatDetect(player().bufferSize(),player().sampleRate());
        timer = new AnimationTimer() {
            GraphicsContext gc;
            double width;
            double height;
            double left;
            double right;
            double band;
            int ln = 100;

            @SuppressWarnings("UnusedAssignment")
            @Override
            public void handle(long now) {
                // make sure sliders and text are updated
                songtime.setValue(player().position());
                time.setText(timeToText(player().position(), true) + "/" + timeToText(player().length(), true));
                if (!player().isPlaying() && playing) {
                    play();
                }
                if (player().position() >= player().length()-1000) { // cri. i need to figure out why it doesn't get to the end
                    Platform.runLater(() -> configurePlayer(pos + 1));
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

                beat.detect(player().mix);

                gc.stroke();
                fft.forward(player().mix);

                bigCircle(now,gc,height,width);



                    //gc.setStroke(Color.WHITE);
                    //gc.strokeLine(0,height/2+height*0.4,width,height/2-height*0.4);
                    for(int i = 0; i < ln+1; i++) {

                        intervalBar = i*(width/ln);
                        band = fft.getBand(i);
                        posit = player().position();
                        for (int dots=0;dots<band*tall;dots=dots + 5){
                            gc.setFill(new Color(Math.abs(Math.cos(now/10000000000.0)),Math.abs(Math.sin(band*tall)),Math.abs(Math.cos(i/1000000.0)),1));
                            gc.fillOval((intervalBar+posit/100.0)%width,height/2-dots,2,2);
                            gc.fillOval(width-(intervalBar+posit/100.0)%width,height/2+dots,2,2);
                        }
                        addedFactor=15;
                        if(Math.abs((intervalBar+posit/100.0)%width)-Math.abs(((i+1)*(width/ln)+posit/100.0)%width)<width-10){
                            gc.setStroke(new Color((double)i/ln,Math.abs(Math.cos((double)i/(Math.abs(10-Math.cos(now))))),0.7*Math.abs(Math.cos(i/1000000.0)),1));
                            gc.strokeLine((intervalBar+posit/100.0)%width,height/2 - band*tall,((i+1)*(width/ln)+posit/100.0)%width,height/2-fft.getBand(i+1)*tall);
                            gc.strokeLine(width-(intervalBar+posit/100.0)%width,height/2+band*tall,width-((i+1)*(width/ln)+posit/100.0)%width,height/2+fft.getBand(i+1)*tall);
                            if (i==0){
                                gc.strokeLine(posit/100.0%width,height/2-band*tall,(1+posit/100.0)%width,height/2-fft.getBand(ln)*tall);
                                gc.strokeLine(width-(posit/100.0)%width,height/2+band*tall,(width-(1+posit/100.0)%width),height/2+fft.getBand(ln)*tall);
                            }
                            if(beat.isSnare()){
                                gc.setStroke(new Color((double)i/ln,Math.abs(Math.cos((double)i/(Math.abs(10-Math.cos(now))))),0.7*Math.abs(Math.cos(i/1000000.0)),0.5));
                                gc.strokeLine((intervalBar+posit/100.0)%width+addedFactor,height/2 - band*tall-addedFactor,((i+1)*(width/ln)+posit/100.0)%width+addedFactor,height/2-fft.getBand(i+1)*tall-addedFactor);
                                gc.strokeLine(width-(intervalBar+posit/100.0)%width+addedFactor,height/2+band*tall+addedFactor,width-((i+1)*(width/ln)+posit/100.0)%width+addedFactor,height/2+fft.getBand(i+1)*tall+addedFactor);
                                if (i==0){
                                    gc.strokeLine(posit/100.0%width+addedFactor,height/2-band*tall-addedFactor,(1+posit/100.0)%width,height/2-fft.getBand(ln)*tall-addedFactor);
                                    gc.strokeLine(width-(posit/100.0)%width+addedFactor,height/2+band*tall+addedFactor,(width-(1+posit/100.0)%width),height/2+fft.getBand(ln)*tall+addedFactor);
                                }
                            }
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
            soundprog.setProgress((newValue.doubleValue() - soundvol.getMin()) / (soundvol.getMax() - soundvol.getMin()));
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
        return timeToText(time, false);
    }

    private static String timeToText(int time, boolean withMillis) {
        int hrs = time / HOUR;
        int min = (time / MINUTE) % 60;
        int sec = (time / SECOND) % 60;
        int milis = (time % SECOND);
        String t = ((hrs == 0) ? "" : hrs + ":");
        t += ((t.isEmpty() && min < 10) ? "0" : "") + min + ":" + ((sec < 10) ? "0" : "") + sec;
        if (withMillis) {
            String milisString = ".";
            if (milis < 10) {
                milisString += "0";
            }
            if (milis < 100) {
                milisString += "0";
            }
            t += milisString + milis;
        }
        return t;
    }

    @FXML
    private void playPausePressed(MouseEvent event) {
        pauseImgs.setPressed(true);
        playImgs.setPressed(true);
        playPause(event);
    }

    @FXML
    private void playPause(Event event) {
        if (playing) {
            playPause.setGraphic(playImgs.getImage());
            pause();
        } else {
            playPause.setGraphic(pauseImgs.getImage());
            play();
        }
    }

    @FXML
    private void playPauseReleased(MouseEvent event) {
        pauseImgs.setPressed(false);
        playImgs.setPressed(false);
        if (playing) {
            playPause.setGraphic(pauseImgs.getImage());
        } else {
            playPause.setGraphic(playImgs.getImage());
        }
    }

    @FXML
    private void rewindPressed(MouseEvent event) {
        skipbackImgs.setPressed(true);
        rewind.setGraphic(skipbackImgs.getImage());
        rewind(event);
    }

    @FXML
    private void rewind(Event event) {
        if (pos > 0 && (player().position() == 0 || System.currentTimeMillis() - lastRewind < skipbackTimeout)) {
            player().pause();
            configurePlayer(pos - 1);
        }
        lastRewind = System.currentTimeMillis();
        player().cue(0);
    }

    @FXML
    private void rewindReleased(MouseEvent event) {
        skipbackImgs.setPressed(false);
        rewind.setGraphic(skipbackImgs.getImage());
    }

    @FXML
    private void skipPressed(MouseEvent event) {
        skipforwardImgs.setPressed(true);
        skip.setGraphic(skipforwardImgs.getImage());
        skip(event);
    }

    @FXML
    private void skip(Event event) {
        player().pause();
        configurePlayer(pos + 1);
    }

    @FXML
    private void skipReleased(MouseEvent event) {
        skipforwardImgs.setPressed(false);
        skip.setGraphic(skipforwardImgs.getImage());
    }

    @FXML
    private void openSettings(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"), null, new JavaFXBuilderFactory(), null);
        Stage stage = new Stage();
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setAlwaysOnTop(true);
        stage.setOnCloseRequest(event1 -> Platform.runLater(this::read));
        stage.show();
    }

    private void read() {
        System.out.println("read start");
        Stack<File> dirs = new Stack<>();
        for (String path : Main.context.musicDirectories) {
            dirs.push(new File(path));
        }
        File f;
        Date d;
        String absPath;
        Map<String, FileMeta> holder = new HashMap<>();
        while (!dirs.isEmpty()) {
            f = dirs.pop();
            if (!f.exists()) {
                continue;
            }
            if (f.isDirectory()) {
                //noinspection ConstantConditions
                dirs.addAll(Arrays.asList(f.listFiles()));
                continue;
            }
            if (f.isFile()) {
                String name = f.getName();
                String ext = name.substring(name.lastIndexOf(".") + 1);
                for (String exten : Main.context.extensions) {
                    if (ext.equalsIgnoreCase(exten)) {
                        d = new Date(f.lastModified());
                        absPath = f.getAbsolutePath();
                        if (files.get(absPath) == null) {
                            holder.put(absPath, new FileMeta(f, minim)); // oh look, you're new! welcome home! or something
                        } else if (files.get(absPath).modTime.compareTo(d) < 0) { // ie the version we have is newer
                            holder.put(absPath, files.get(absPath)); // keep the current version. dont even know how this would happen
                        } else {
                            holder.put(absPath, new FileMeta(f, minim)); // take the other version
                        }
                        break;
                    }
                }
            }
        }
        files.values().forEach(v -> {
            if (!holder.containsValue(v)) {
                v.close();
            }
        });
        files = holder;
        musicList.clear();
        musicList.addAll(files.values().toArray(new FileMeta[0]));
        musicList.sort(null);
        presList.clear();
        for (int i = 0; i < musicList.size(); i++) {
            presList.add(i);
        }
        System.out.println("read end");
    }

    private void play() {
        this.playing = true;
        this.player().play();
    }

    private void pause() {
        this.playing = false;
        this.player().pause();
    }

    public void regularLine(long now, GraphicsContext gc){
        double height=canvas.getHeight()/2;
        double width=canvas.getWidth()/2;
        double right2=0;
        double extraX=0;
        double extraY=0;
        double extraY2=0;
        double xValue=0;
        double xValueNext=0;
        double zoom=0;
        double right = 0;
        for(int i = 0; i < player().bufferSize()-80; i++)
        {
            posit = player().position();
            int forLoop = (int)(80*Math.abs(Math.sin(posit/10000.0)));
            right = height/2 + player().right.get(i) * height/2;
            //right = right -200;
            for (int m=forLoop;m<forLoop+2;m=m+20){
                right2 = height/2 + player().right.get(i+m) * height/2;
                xValue = canvas.getWidth()*((double)i)/player().bufferSize();
                xValueNext = canvas.getWidth()* ((double)(i+m))/player().bufferSize();
                right2 = Math.log(right2+30*Math.random())+height/2;
                //gc.setStroke(new Color(0.25,0.4,0.72,1-m/100.0));
                gc.setStroke(new Color(Math.abs((double)Math.sin(posit/40000.0)),Math.abs(Math.sin(0.4-(posit/20000.0))),0.7,0.5));

                zoom = 3*Math.sin(posit/10000.0);
                gc.strokeLine((xValue-width/2)*zoom+width/2,(right-height/2.0)*zoom+height/2, (xValueNext-width/2)*zoom+width/2,(right2-height/2)*zoom+height/2);
            }
        }
    }

    public void drifting(long now, GraphicsContext gc, double height, double width){
        Color circleColor=null;
        amountSkip=10;
        beat.detect(player().mix);
        double left;
        double dist;
        double tempX;
        double tempY;
        for(int i = 0; i < player().bufferSize(); i=i+amountSkip){
            posit=player().position();
            left = (height/2 + player().left.get(i) * height/2);
            left = left/(Math.abs(29-2*Math.sin(posit/10000000.0)));
            circleColor = new Color(Math.abs(circles.get(i/amountSkip)/(width+amountSkip+1.0)),Math.abs(Math.sin(now/100000000000.0)),Math.abs(circles.get(i/amountSkip+1)/(height+amountSkip+100.0)),Math.abs(Math.cos(posit/1000000.0))/2.0);
            gc.setFill(circleColor);
            gc.setStroke(circleColor);
            dist=Math.sqrt(Math.pow(circlesNew.get(i/amountSkip)-width/2,2)+Math.pow(circlesNew.get(i/amountSkip+1)-height/2,2));
            tempX=(circles.get(i/amountSkip)-left/2.0+speed.get(i/amountSkip)*posit/10.0);
            tempY=(circles.get(i/amountSkip+1)-left/2.0+posit/1000.0);
            //tempX=circles.get(i/amountSkip);
            //tempY=circles.get(i/amountSkip+1);

            if(beat.isSnare()==true){

                if(tempX<width/2){circlesNew.set(i/amountSkip,(tempX-dist*(tempX-width/2)/5000000000.0)%width);}
                else{circlesNew.set(i/amountSkip,(tempX+dist*(tempX-width/2)/5000000000.0)%width);}
                circlesNew.set(i/amountSkip+1,(tempY-dist*(tempY-height/2)/5000000000.0)%(height));
            }
            else{
                circlesNew.set(i/amountSkip,tempX%width);
                circlesNew.set(i/amountSkip+1,tempY%(height));
            }
            gc.fillOval(circlesNew.get(i/amountSkip),circlesNew.get(i/amountSkip+1),6,6);

            for (int j=0;j<circlesNew.size();j=j+2){
                if (Math.sqrt(Math.pow(circlesNew.get(i/amountSkip)-circlesNew.get(j),2)+Math.pow(circlesNew.get(i/amountSkip+1)-circlesNew.get(j+1),2))<40){
                    gc.strokeLine(circlesNew.get(i/amountSkip)+left/2.0,circlesNew.get(i/amountSkip+1)+left/2.0,circlesNew.get(j)+left/2.0,circlesNew.get(j+1)+left/2.0);
                }
            }


            //gc.fillRect((circleLocations.get(i/amountSkip)-left/2.0+speed.get(i/amountSkip)*now/10000000.0)%width,(circleLocations.get(i/amountSkip+1)-left/2.0+now/1000000000000000.0)%height,left,left);
            //gc.setStroke(circles);
            //polarR = 20*Math.abs(Math.cos(player().position()/1000.0)) + left/2.0;
            //polarTheta = (double)(i+now/100000000.0)/((double)player().bufferSize())*Math.PI*2;
            //gc.lineTo(polarR*Math.cos(polarTheta)+width/2,polarR*Math.sin(polarTheta)+height);
            //System.out.print(left + " ");
            //gc.lineTo(width * ((double)i)/player().bufferSize(), left);
        }
    }
    public void bigCircle(long now,GraphicsContext gc, double height,double width){

        //System.out.println(height+" "+width);
        int sizeM=1000;

        for(int i = 0; i < sizeM; i=i+50)
        {
            posit=player().position();
            left = (height/2 + player().left.get(i) * height/2);
            right = (height/2 + player().right.get(i)*height/2);
            //gc.lineTo(width * ((double)i)/player().bufferSize(), left);
            //polarR=Math.abs(Math.sin(posit / 10000000.0)) + (double)left/5.0;

            //outside radius
            //polarR = 20*Math.abs(Math.cos(posit/1000.0)) + left/2.0;
            polarTheta = (double)(i+now/100000000.0)/((double)sizeM)*Math.PI*2;
            //gc.lineTo(polarR*Math.cos(polarTheta)+width/2,polarR*Math.sin(polarTheta)+height/2);
            //System.out.print(left + " ");
            //gc.lineTo(width * ((double)i)/player().bufferSize(), left);

            polarR=100+40*Math.sin(now/10000000000.0);
            polarR2=100+40*Math.sin(now/10000000000.0);
            polarTheta2 = (left+now/100000000.0)/(double)sizeM*Math.PI*2;

            gc.setStroke(new Color(Math.abs(Math.cos(polarR2/1000000.0)),0.2,0.7,Math.abs(Math.sin(polarTheta2))));
            gc.strokeLine(polarR*Math.cos(polarTheta)+width/2,polarR*Math.sin(polarTheta)+height/2,polarR2*Math.cos(polarTheta2)+width/2,polarR2*Math.sin(polarTheta2)+height/2);

            //polarR=20*Math.abs(Math.cos(posit/1000.0))+right/2.0;
            polarTheta = (i+now/100000000.0)/((double)sizeM)*Math.PI*2+Math.PI;

            polarR=100+40*Math.sin(now/10000000000.0);
            polarR2=100+40*Math.sin(now/10000000000.0);
            polarTheta2 = (right+now/100000000.0)/(double)sizeM*Math.PI*2+Math.PI;
            //polarTheta = i/(double)sizeM*Math.PI*2+Math.PI;
            //polarTheta2=right/(double)sizeM*Math.PI*2+Math.PI;
            gc.setStroke(new Color(0.2,0.2,Math.abs(Math.cos(polarR/100000.0)),Math.abs(Math.cos(polarTheta2))));
            gc.strokeLine(polarR*Math.cos(polarTheta)+width/2,polarR*Math.sin(polarTheta)+height/2,polarR2*Math.cos(polarTheta2)+width/2,polarR2*Math.sin(polarTheta2)+height/2);

            gc.setStroke(Color.GRAY);
            if (i<player().bufferSize()){
                for (int m=80;m<101;m=m+20) {
                    //y values
                    //left2 = (canvas.getHeight() / 2 + player().left.get((i + m)%player().bufferSize()) * canvas.getHeight() / 2);
                    //left2 = Math.log(left2)+height/2;

                    //converting to polar
                    //polarR2 = 40*(Math.abs(Math.cos(now/100000000000.0)))+left2/2.0;
                    //polarR2 =  left2 / 5.0;

                    //polarTheta2 = (double) (i + m+posit/100000.0) / ((double) player().bufferSize()) * Math.PI * 2;

                    //color and drawing the line
                    //gc.setStroke(new Color(Math.abs(Math.cos(posit/100000.0)), Math.abs(Math.sin(posit/5000000.0)), Math.abs(Math.cos(now/60000000000.0)), 0.2));
                    //gc.strokeLine(polarR * Math.cos(polarTheta) + width / 2, polarR * Math.sin(polarTheta) + height / 2, polarR2 * Math.cos(polarTheta2) + width / 2, polarR2 * Math.sin(polarTheta2) + height / 2);


                }

            }




        }
        double right=0;
        for(int i = 0; i < player().bufferSize(); i=i+10)
        {
            right = height/2 + player().right.get(i) * height/2;
            right = right -200;
            //right = Math.sqrt(right)+height/2;
            //gc.lineTo(width * ((double)i)/player().bufferSize(), right);
            //gc.strokeLine(width*(double)i/player().bufferSize(),right/2.0+height/2,width*(double)i/player().bufferSize(),height/2-right/2.0);

        }

    }

    public InputStream createInput(String path) throws FileNotFoundException {
        return new FileInputStream(path);
    }

    public void stop() {
        musicList.forEach(FileMeta::close);
        minim.stop();
        timer.stop();
    }

    public class FileMeta implements Comparable<FileMeta> {
        private StringProperty title;
        private StringProperty artist;
        private StringProperty album;
        private int length;
        private Date modTime;
        private String fileName;
        private final String filePath;
        private AudioPlayer player;
        private Context.Comparisons comparison;
        private Minim min;

        public FileMeta(File file, Minim min) {
            this.min = min;
            String name = file.getName();
            filePath = file.getAbsolutePath();
            player = min.loadFile(filePath);
            fileName = name.substring(name.lastIndexOf("/") + 1, name.lastIndexOf(".mp3"));
            //System.out.println(fileName);
            int ind = fileName.indexOf("-");
            int ind2 = ind + 1;
            if (ind < 0) {
                ind = fileName.length();
                ind2 = ind;
            }
            artistProperty().set(!player.getMetaData().author().isEmpty() ? player.getMetaData().author() : fileName.substring(0, ind).trim());
            titleProperty().set(!player.getMetaData().title().isEmpty() ? player.getMetaData().title() : fileName.substring(ind2).trim());
            modTime = new Date(file.lastModified());
            comparison = Context.Comparisons.SONG_NAME;
            length = player.length();
            close();
        }

        @Override
        public String toString() {
            return (titleProperty().getValueSafe().isEmpty() ? "" : title + " - ") + artist + " | " + modTime.toString();
        }

        public String fileName() {
            return artist() + (!title().isEmpty() ? " - " + title() : "");
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
                    return artist().compareTo(o.artist());
                case SONG_NAME:
                    return title().compareTo(o.title());
                case SONG_LENGTH:
                    return length - o.length;
                case MOD_TIME:
                default:
                    return modTime.compareTo(o.modTime);
            }
        }

        public StringProperty titleProperty() {
            if (title == null) {
                title = new SimpleStringProperty(this, "title");
            }
            return title;
        }

        public StringProperty artistProperty() {
            if (artist == null) {
                artist = new SimpleStringProperty(this, "artist");
            }
            return artist;
        }

        public StringProperty albumProperty() {
            if (album == null) {
                album = new SimpleStringProperty(this, "album");
            }
            return album;
        }

        public String title() {
            return titleProperty().getValueSafe();
        }

        public String artist() {
            return artistProperty().getValueSafe();
        }

        public String album() {
            return albumProperty().getValueSafe();
        }

        public AudioPlayer player() {
            if (player == null) {
                player = min.loadFile(filePath);
            }
            return player;
        }

        public void close() {
            if (player != null) {
                player.close();
                player = null;
            }
        }
    }

    private AudioPlayer player() {
        if (musicList.size() == 0) {
            throw new IllegalStateException("y u no hev songs in folder");
        }
        return musicList.get(pos).player();
    }

    /**
     * use this method when after changing to a different player (ie pos++, pos--, or pos = something). Will make sure sliders and volume and such is correct.
     * also cues the current player to 0.
     */
    private void configurePlayer(int newPos) {
        if (newPos != pos) {
            musicList.get(pos).close();
            pos = newPos;
        }
        songtime.setMax(player().length());
        songName.setText(musicList.get(pos).fileName());
        songTable.getSelectionModel().select(new Integer(pos));
        player().setGain((float) soundvol.getValue());
        player().cue(0);
    }
}