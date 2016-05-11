package sample.visualizer;

import ddf.minim.AudioPlayer;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * karnina y
 */
public abstract class Visualizer {
    public void draw(long now, int position, int length, GraphicsContext gc, double width, double height, AudioPlayer player, FFT fft, BeatDetect beat) {
        gc.setStroke(Color.BLACK);
        //gc.setFill(Color.gray(1)); ORIGINAL
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        gc.setFill(Color.gray(0.2));
        gc.fillRect(0, height, width * (position / length), height - 2);
        gc.beginPath();
    }
}
