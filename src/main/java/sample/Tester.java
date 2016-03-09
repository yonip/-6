package sample;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

import java.util.Random;

public class Tester extends Application {

    private static Random rand;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Drawing Operations Test");
        Group root = new Group();
        Canvas canvas = new Canvas(720, 480);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        rand = new Random(200);
        //drawPoints(gc);
        drawLines(gc);
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void drawLines(GraphicsContext gc) {
        double dx = 50;
        double dy = 50;
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();
        Bez by = new Bez(0, -250, 300, 0);
        Bez bx = new Bez(0, 0, 0);
        for (int y = 0; y < h; y += dy) {
            gc.beginPath();
            gc.moveTo(0 + bx.getY(y/w), y + by.getY(0));
            for (int x = 0; x < w; x += dx) {
                gc.lineTo(x + bx.getY(y/w), y + by.getY(x / w));
            }
            gc.stroke();

        }
    }

    private class Bez {
        private double[] yVals;
        private int[] quad = new int[]{1, 2, 1};
        private int[] cube = new int[]{1, 3, 3, 1};
        private int[] quart = new int[]{1, 4, 6, 4, 1};

        public Bez(double y1, double y2, double y3) {
            yVals = new double[]{y1, y2, y3};
        }

        public Bez(double y1, double y2, double y3, double y4) {
            yVals = new double[]{y1, y2, y3, y4};
        }

        public Bez(double y1, double y2, double y3, double y4, double y5) {
            yVals = new double[]{y1, y2, y3, y4, y5};
        }

        public double getY(double t) {
            switch (yVals.length) {
                case 3:
                    return getX(t, yVals, quad);
                case 4:
                    return getX(t, yVals, cube);
                case 5:
                    return getX(t, yVals, quart);
                default:
                    throw new IllegalStateException("y values length somehow not a supported length: " + yVals.length);
            }
        }

        private double getX(double t, double[] points, int[] coef) {
            double v = 0;
            for (int i = 0; i < points.length; i++) {
                v += coef[i] * Math.pow(1 - t, points.length - i) * Math.pow(t, i) * yVals[i];
            }
            System.out.println(v);
            return v;
        }
    }

    private void drawPoints(GraphicsContext gc) {

        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();
        double y1 = rand.nextDouble() * h;
        double dx1 = (rand.nextDouble() / 20 + 0.05) * w;
        double dy1 = rand.nextDouble() * h / 20;
        double m1 = dy1 / dx1;
        double dist1 = ((rand.nextDouble() - .5) * 0.004 + .03) * h;
        double y2 = rand.nextDouble() * h;
        double dx2 = (rand.nextDouble() / 20 + 0.05) * w;
        double dy2 = rand.nextDouble() * h / 20;
        double m2 = dy2 / dx2;
        double dist2 = ((rand.nextDouble() - .5) * 0.004 + .03) * h;
        while (y1 - dist1 > 0) {
            y1 -= dist1;
        }
        while (y2 - dist2 > 0) {
            y2 -= dist2;
        }
        final double x = getX(m1, y1, m2, y2);
        final double y = m1 * x + y1;
        gc.strokeLine(x, y, x, y);
        System.out.println("(" + x + "," + y + ")");
        for (double ysum1 = 0, xsum1 = 0; x + xsum1 < w && y + ysum1 < h; ysum1 += dist1, xsum1 += dist1 / m1) {
            double ysum2 = 0;
            double xsum2 = 0;
            while (x + xsum1 + xsum2 + dist2 / m2 > 0 && x + xsum1 + xsum2 + dist2 / m2 < w && y + ysum1 + ysum2 - dist2 > 0 && y + ysum1 + ysum2 - dist2 < h) {
                xsum2 += dist2 / m2;
                ysum2 -= dist2;
            }
            for (; x + xsum1 + xsum2 > 0 && y + ysum1 + ysum2 < h; ysum2 += dist2, xsum2 -= dist2 / m2) {
                gc.strokeLine(x + xsum1 + xsum2, y + ysum1 + ysum2, x + xsum1 + xsum2, y + ysum1 + ysum2);
            }
        }
    }

    private double getX(double m1, double y1, double m2, double y2) {
        return (y2 - y1) / (m1 - m2);
    }

    private void drawShapes(GraphicsContext gc, boolean positive) {
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();
        double yIntercept = rand.nextDouble() * h;
        double dx = (rand.nextDouble() / 20 + 0.05) * w;
        double dy = rand.nextDouble() * h / 20;
        dy *= positive ? 1 : -1;
        double dist = ((rand.nextDouble() - .5) * 0.004 + .03) * h;
        double itr;
        double deltay;
        for (double y = yIntercept; ; y -= dist) {
            deltay = dy < 0 ? h - y : y;
            if (w / Math.abs(dx) < deltay / Math.abs(dy)) {
                itr = deltay / Math.abs(dy);
            } else {
                itr = w / Math.abs(dx);
            }
            System.out.println(y + " " + dx + " " + dy + " " + (y + itr * dy));
            if (y < 0 && y + itr * dy < 0) {
                break;
            }

            gc.strokeLine(0, y, itr * dx, y + itr * dy);
        }
        for (double y = yIntercept + dist; ; y += dist) {
            deltay = dy < 0 ? h - y : y;
            if (w / Math.abs(dx) < deltay / Math.abs(dy)) {
                itr = deltay / Math.abs(dy);
            } else {
                itr = w / Math.abs(dx);
            }
            System.out.println(y + " " + dx + " " + dy + " " + (y + itr * dy));
            if (y > h && y + itr * dy > h) {
                break;
            }
            gc.strokeLine(0, y, itr * dx, y + itr * dy);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
