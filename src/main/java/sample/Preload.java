package sample;

import javafx.application.Preloader;
import javafx.application.Preloader.StateChangeNotification.Type;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Preload extends Preloader{
	private Stage preloaderStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.preloaderStage = primaryStage;
		ImageView logo = new ImageView(new Image("logo.png"));
		BorderPane pane = new BorderPane();
		pane.setCenter(logo);
		
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
