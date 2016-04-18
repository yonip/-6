package sample.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sample.Main;

/**
 * A class to hold the image pair used for buttons
 */
public class ButtonImage {
    private static final String PRESSED_EXT = "_pressed.png";
    private static final String DEFAULT_EXT = "_default.png";
    private static final double FIT_HEIGHT = 26;
    private static final double FIT_WIDTH = 26;
    private ImageView pressedImage;
    private ImageView defaultImage;

    public ButtonImage(String name) {
        pressedImage = new ImageView(new Image(getClass().getResourceAsStream(Main.context.BUTTON_IMAGES_ROOT + "/" + name + PRESSED_EXT)));
        pressedImage.setFitHeight(FIT_HEIGHT);
        pressedImage.setFitWidth(FIT_WIDTH);
        defaultImage = new ImageView(new Image(getClass().getResourceAsStream(Main.context.BUTTON_IMAGES_ROOT + "/" + name + DEFAULT_EXT)));
        defaultImage.setFitHeight(FIT_HEIGHT);
        defaultImage.setFitWidth(FIT_WIDTH);
    }

    public ImageView getDefaultImage() {
        return defaultImage;
    }

    public ImageView getPressedImage() {
        return pressedImage;
    }
}
