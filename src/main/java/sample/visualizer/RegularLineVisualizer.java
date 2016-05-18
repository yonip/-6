package sample.visualizer;

import ddf.minim.AudioPlayer;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Created by yonipedersen on 5/10/16.
 */
public class RegularLineVisualizer extends Visualizer {

    @Override
    public void draw(long now, int position, int length, GraphicsContext gc, double width, double height, AudioPlayer player, FFT fft, BeatDetect beat) {
        super.draw(now, position, length, gc, width, height, player, fft, beat);
        double right2;
        double xValue;
        double xValueNext;
        double zoom;
        double right;
        for (int i = 0; i < player.bufferSize() - 80; i++) {
            int forLoop = (int) (80 * Math.abs(Math.sin(position / 10000.0)));
            right = height / 2 + player.right.get(i) * height / 2;
            //right = right -200;
            for (int m = forLoop; m < forLoop + 2; m = m + 20) {
                right2 = height / 2 + player.right.get(i + m) * height / 2;
                xValue = width * ((double) i) / player.bufferSize();
                xValueNext = width * ((double) (i + m)) / player.bufferSize();
                right2 = Math.log(right2 + 30 * Math.random()) + height / 2;
                //gc.setStroke(new Color(0.25,0.4,0.72,1-m/100.0));
                gc.setStroke(new Color(Math.abs(Math.sin(position / 40000.0)), Math.abs(Math.sin(0.4 - (position / 20000.0))), 0.7, 0.5));

                zoom = 3 * Math.sin(position / 10000.0);
                gc.strokeLine((xValue - width / 2) * zoom + width / 2, (right - height / 2.0) * zoom + height / 2, (xValueNext - width / 2) * zoom + width / 2, (right2 - height / 2) * zoom + height / 2);
            }
        }
    }
}
