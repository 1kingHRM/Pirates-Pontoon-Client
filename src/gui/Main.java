package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class Main extends Application
{
    public static Stage mainStage;

    @Override
    public void start(Stage stage) throws Exception
    {
        Main.mainStage = stage;

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("game.fxml")));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());

        stage.setResizable(false);
        stage.getIcons().add(new Image("gui//background.jpg"));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Pirates Pontoon");
        stage.setScene(scene);
        stage.show();
    }

    public static void start(String[] args)
    {
        Application.launch(Main.class, args);
    }
}
