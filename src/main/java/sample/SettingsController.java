package sample;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class SettingsController{

	@FXML
    private ListView musicDirectoriesView;
    @FXML
    private Text saved;
    @FXML
    private RadioButton repNone;
    @FXML
    private RadioButton repAll;
    @FXML
    private RadioButton repSel;
    private DirectoryChooser directoryChooser;

	private String directories;
	ToggleGroup repSetting;

    @FXML
	public void initialize() {
        directoryChooser = new DirectoryChooser();
        musicDirectoriesView.setItems(Main.context.musicDirectories);
		repSetting = new ToggleGroup();
		repNone.setToggleGroup(repSetting);
		repAll.setToggleGroup(repSetting);
		repSel.setToggleGroup(repSetting);
		repNone.setSelected(true);
		
		System.out.println("ready");
	}

	@FXML
    private void add(MouseEvent event) {
        File chosen = directoryChooser.showDialog(musicDirectoriesView.getScene().getWindow());
        if (chosen != null) {
            Main.context.musicDirectories.add(chosen.getAbsolutePath());
        }
    }

    @FXML
    private void remove(MouseEvent event) {
        int index = musicDirectoriesView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Main.context.musicDirectories.remove(musicDirectoriesView.getSelectionModel().getSelectedIndex());
        }
    }

}
