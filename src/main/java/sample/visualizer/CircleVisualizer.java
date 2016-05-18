package sample.visualizer;

import ddf.minim.AudioPlayer;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * :l
 */
public class CircleVisualizer extends Visualizer {
    private double left;
    private double left2;
    private double polarR;
    private double polarR2;
    private double polarTheta;
    private double polarTheta2;

    @Override
    public void draw(long now, int position, int length, GraphicsContext gc, double width, double height, AudioPlayer player, FFT fft, BeatDetect beat) {
        super.draw(now, position, length, gc, width, height, player, fft, beat);
        for (int i = 0; i < player.bufferSize(); i = i + (int) (10 - Math.abs(Math.random() * 8))) {
            left = (height / 2 + player.left.get(i) * height / 2);
            //gc.lineTo(width * ((double)i)/player.bufferSize(), left);
            //polarR=Math.abs(Math.sin(posit / 10000000.0)) + (double)left/5.0;

            //outside radius
            polarR = 20 * Math.abs(Math.cos(position / 1000.0)) + left / 2.0;
            polarTheta = (i + now / 100000000.0) / ((double) player.bufferSize()) * Math.PI * 2;
            //gc.lineTo(polarR*Math.cos(polarTheta)+width/2,polarR*Math.sin(polarTheta)+height/2);
            //System.out.print(left + " ");
            //gc.lineTo(width * ((double)i)/player.bufferSize(), left);

            if (i < player.bufferSize()) {
                for (int m = 80; m < 101; m = m + 20) {
                    //y values
                    left2 = (height / 2 + player.left.get((i + m) % player.bufferSize()) * height / 2);
                    left2 = Math.log(left2) + height / 2;

                    //converting to polar
                    polarR2 = 40 * (Math.abs(Math.cos(now / 100000000000.0))) + left2 / 2.0;
                    //polarR2 =  left2 / 5.0;

                    polarTheta2 = (i + m + position / 100000.0) / ((double) player.bufferSize()) * Math.PI * 2;

                    //color and drawing the line
                    gc.setStroke(new Color(Math.abs(Math.cos(position / 100000.0)), Math.abs(Math.sin(position / 5000000.0)), Math.abs(Math.cos(now / 60000000000.0)), 0.2));
                    gc.strokeLine(polarR * Math.cos(polarTheta) + width / 2, polarR * Math.sin(polarTheta) + height / 2, polarR2 * Math.cos(polarTheta2) + width / 2, polarR2 * Math.sin(polarTheta2) + height / 2);
                }
            }


        }
        double right;
        for (int i = 0; i < player.bufferSize(); i = i + 10) {
            right = height / 2 + player.right.get(i) * height / 2;
            right = right - 200;
            //right = Math.sqrt(right)+height/2;
            //gc.lineTo(width * ((double)i)/player.bufferSize(), right);
            gc.strokeLine(width * (double) i / player.bufferSize(), right / 2.0 + height / 2, width * (double) i / player.bufferSize(), height / 2 - right / 2.0);

        }
    }
}
