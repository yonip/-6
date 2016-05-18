package sample;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.application.Preloader.StateChangeNotification.Type;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Preload extends Preloader{
	private Stage preloaderStage;
	private String text = "Loading";

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.preloaderStage = primaryStage;
		ImageView logo = new ImageView(new Image("logo.png"));
		VBox pane = new VBox();
		pane.setAlignment(Pos.CENTER);
		pane.getChildren().add(logo);

		Label lblText = new Label(text);
		lblText.setFont(new Font(20.0));
		pane.getChildren().add(lblText);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						if (lblText.getText().equals("Loading"))
							text = "Loading.";
						else if(lblText.getText().equals("Loading."))
							text = "Loading..";
						else if(lblText.getText().equals("Loading.."))
							text = "Loading...";
						else text = "Loading";

						Platform.runLater(new Runnable() {
							@Override 
							public void run() {
								lblText.setText(text);
							}
						});

						Thread.sleep(400);
					}
				}
				catch (InterruptedException ex) {
				}
			}
		}).start();

		Scene sc = new Scene(pane);

		primaryStage.setWidth(800);
		primaryStage.setHeight(600);
		primaryStage.setScene(sc);
		primaryStage.show();
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
		if (stateChangeNotification.getType() == Type.BEFORE_START) {
			preloaderStage.hide();
		}
	}

}
