package sample.visualizer;

import ddf.minim.AudioPlayer;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Created by yonipedersen on 5/10/16.
 */
public class DriftingVisualizer extends Visualizer {
    private final VisualizerPoint[] circles;
    private final int dotNumber = 100;

    public DriftingVisualizer() {
        circles = new VisualizerPoint[dotNumber];
        for (int i = 0; i < circles.length; i++) {
            circles[i] = new VisualizerPoint();
            circles[i].x = Math.random();
            circles[i].y = Math.random();
            circles[i].v = Math.random() * 0.0005;
        }
    }
    
    @Override
    public void draw(long now, int position, int length, GraphicsContext gc, double width, double height, AudioPlayer player, FFT fft, BeatDetect beat) {
        super.draw(now, position, length, gc, width, height, player, fft, beat);
        Color circleColor;
        double left;
        double dist;
        double tempX;
        double tempY;
        for (int i = 0; i < circles.length; i++) {
            // update x coord of point
            // use the sound for things later (?)
            left = (height / 2 + player.left.get(i) * height / 2);
            left = left / (Math.abs(29 - 2 * Math.sin(position / 10000000.0)));
            circles[i].r = left/2;
            // figure out color
            circleColor = new Color(Math.abs(circles[i].x*width / (width + 11)), Math.abs(Math.sin(now / 100000000000.0)), Math.abs(circles[i].y*height / (height + 110.0)), Math.abs(Math.cos(position / 1000000.0)) / 2.0);
            gc.setFill(circleColor);
            gc.setStroke(circleColor);

            // account for velocity
            circles[i].x += circles[i].v;
            circles[i].x %= 1;
            circles[i].y += now/1000000;
            circles[i].y %= 1;
            // get center of point in real units
            tempX = (circles[i].x*width); // velocity already taken car of
            tempY = (circles[i].y*height);
            // distance from center to this point
            //dist = Math.sqrt(Math.pow(tempX - width/2, 2) + Math.pow(tempY - height/2, 2));

            // get a shake for the dots from the snare
            if (beat.isKick()) {
                // beat was hit. move the point away from the center:
                // horizontal
                circles[i].dx = -(tempX - width / 2) / 500;
                // vertical
                circles[i].dy = -(tempY - height / 2) / 500;
            } else {
                // beat was not hit. keep the point where they should be
                circles[i].dx = 0;
                circles[i].dy = 0;
            }
            // draw the points
            circles[i].fx = (tempX+circles[i].dx)%width;
            circles[i].fy = (tempY+circles[i].dy)%height;
            gc.fillOval(circles[i].fx-circles[i].r, circles[i].fy-circles[i].r, 2*circles[i].r, 2*circles[i].r);
        }
        // check *all* the points and if they are close enough, draw a line to them.
        for (int i = 0; i < circles.length-1; i++) {
            for (int j = i+1; j < circles.length; j++) {
                if (Math.sqrt(Math.pow(circles[i].fx - circles[j].fx, 2) + Math.pow(circles[i].fy - circles[j].fy, 2)) < 40) {
                    gc.strokeLine(circles[i].fx, circles[i].fy, circles[j].fx, circles[j].fy);
                }
            }
        }
    }

    public class VisualizerPoint {
        public double x;
        public double y;
        public double dx;
        public double dy;
        public double r;
        public double v;
        public double fx;
        public double fy;
    }
}
