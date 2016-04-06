package sample;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.text.Text;

public class SettingsController{

	@FXML
	private TextArea musicDir;
    @FXML
    private Text saved;
    @FXML
    private RadioButton repNone;
    @FXML
    private RadioButton repAll;
    @FXML
    private RadioButton repSel;

	private String directories;
	ToggleGroup repSetting;

	public void initialize() {
		repSetting = new ToggleGroup();
		repNone.setToggleGroup(repSetting);
		repAll.setToggleGroup(repSetting);
		repSel.setToggleGroup(repSetting);
		repNone.setSelected(true);
		
		System.out.println("ready");
	}

	@FXML
    void update(MouseEvent event) {
		String dir=musicDir.getText();
		saved.setText("saved");
		
    }

}
