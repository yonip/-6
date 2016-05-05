package sample.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A thing for constants. will probably be read from a config file later
 */
public class Context {
    public final String IMAGES_ROOT;
    public final String BUTTON_IMAGES_ROOT;
    public final ObservableList<String> musicDirectories;
    public final String[] extensions;
    public enum Comparisons {
        SONG_NAME, AUTHOR_NAME, MOD_TIME, SONG_LENGTH
    }

    public Context() {

        IMAGES_ROOT = "/images";
        BUTTON_IMAGES_ROOT = IMAGES_ROOT + "/button";
        musicDirectories = FXCollections.observableArrayList();
        musicDirectories.add(System.getProperty("user.home")+"/Documents/music");
        extensions = new String[]{"mp3"};
    }
}
