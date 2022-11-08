package gui;

import animatefx.animation.AnimationFX;
import animatefx.animation.FadeIn;
import client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

import static client.Constants.QUIT;

/**
 * This class is the GUI Controller for the application. This class contains all the needed GUI components required for this application to run.
 */
public class Controller implements Initializable
{
    private volatile Client client;

    private double xOffset;
    private double yOffset;

    @FXML
    private AnchorPane root, waitPane, gamePane, highScore, menu;

    @FXML
    private Region loading;

    @FXML
    private Label serverStatus, dealerScore, highScores,
            currentPlayerName, playerOneName, playerTwoName, playerThreeName,
            currentPlayerScore, playerOneScore, playerTwoScore, playerThreeScore;

    @FXML
    private TextField playerName, gameAddress, gamePort;

    @FXML
    private TextArea log;

    @FXML
    private Button startBtn, homeBtn;

    @FXML
    private HBox dealOrHold;

    @FXML
    private ScrollPane currentPlayerCards, playerOneCards, playerTwoCards, playerThreeCards;

    public Controller()
    {
        this.xOffset = 0.0;
        this.yOffset = 0.0;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        // Add event handlers for when dragging the app
        this.root.setOnMousePressed(mouseEvent ->
        {
            this.root.requestFocus();
            this.xOffset = Main.mainStage.getX() - mouseEvent.getScreenX();
            this.yOffset = Main.mainStage.getY() - mouseEvent.getScreenY();
        });

        this.root.setOnMouseDragged(mouseEvent ->
        {
            Main.mainStage.setX(mouseEvent.getScreenX() + this.xOffset);
            Main.mainStage.setY(mouseEvent.getScreenY() + this.yOffset);
        });
    }

    public void onJoinGame(MouseEvent event)
    {
        boolean connected = validateUser();

        if(connected)
        {
            this.waitPane.toFront();
            this.loading.toFront();

            AnimationFX animation = new FadeIn(this.waitPane).setSpeed(0.8);
            animation.setOnFinished(e ->
            {
                this.serverStatus.setTextFill(Color.rgb(140, 200, 200));
                this.serverStatus.setText("Connected To Server");

                NetworkManager.init(this.client);
                this.loading.visibleProperty().bind(NetworkManager.running());
                this.startBtn.disableProperty().bind(NetworkManager.getStatus());
            });
            animation.play();
        }
        else
        {
            this.client = null;

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Pirates Pontoon");
            alert.setHeaderText("Invalid Connection Details");
            alert.setContentText("""
                        Unable to establish a link with the game server.\s
                        Possible Causes:
                        Validate the address and port being supplied.
                        Check your network settings to confirm if you are actually connected to the server.\s
                        Solution:
                        Restart the game and try again."""
            );
            alert.show();
        }

        event.consume();
    }

    private boolean validateUser()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Pirates Pontoon");

        if(this.playerName.getText().isBlank() || this.gameAddress.getText().isBlank()
                || this.gamePort.getText().isBlank())
        {
            alert.setHeaderText("Invalid Connection Details");
            alert.setContentText("Please make sure the player name, the server address and the server port " +
                    "fields are filled before attempting to start the game.");
            alert.show();
            return false;
        }


        this.client = new Client(this.playerName.getText().trim());
        this.client.set(this.highScore, this.log, this.dealerScore, this.highScores, this.homeBtn);

        String address = this.gameAddress.getText().trim();
        int port;

        try
        {
            port = Integer.parseInt(this.gamePort.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            alert.setHeaderText("Connection Error");
            alert.setContentText("Illegal port number provided");
            alert.show();
            return false;
        }

        return this.client.connect(address, port);
    }

    public void onStartGame(MouseEvent event)
    {
        setPlayerDetails();

        this.client.request();
        this.client.ready();

        this.gamePane.toFront();

        new Thread(client).start();

        new FadeIn(this.gamePane).setSpeed(0.8).play();

        event.consume();
    }

    private void setPlayerDetails()
    {
        HBox[] containers =
        {
            (HBox) this.currentPlayerCards.getContent(),
            (HBox) this.playerOneCards.getContent(),
            (HBox) this.playerTwoCards.getContent(),
            (HBox) this.playerThreeCards.getContent(),
        };

        Label[] scores =
        {
              this.currentPlayerScore,
              this.playerOneScore,
              this.playerTwoScore,
              this.playerThreeScore
        };

        Label[] names =
        {
            this.currentPlayerName,
            this.playerOneName,
            this.playerTwoName,
            this.playerThreeName
        };

        this.client.setDetails(containers, scores, names, this.dealOrHold);
    }

    public void cardScroll(ScrollEvent event)
    {
        ScrollPane pane = (ScrollPane) event.getSource();
        double pos = pane.getHvalue();
        pos += (event.getDeltaY() / event.getMultiplierY()) * 0.1;
        if(pos > 1.0)
            pos = 1.0;
        else if(pos < 0.0)
            pos = 0.0;
        pane.setHvalue(pos);
        event.consume();
    }
    
    public void onDeal(MouseEvent event)
    {
        this.client.deal();
        this.dealOrHold.setVisible(false);
        event.consume();
    }

    public void onHold(MouseEvent event)
    {
        this.client.hold();
        this.dealOrHold.setVisible(false);
        event.consume();
    }

    public void minimize(MouseEvent event)
    {
        Main.mainStage.setIconified(true);
        event.consume();
    }

    public void home(MouseEvent event)
    {
        this.highScore.toBack();
        this.menu.toFront();
        new FadeIn(this.menu).setSpeed(0.8).play();
        event.consume();
    }

    public void quit(MouseEvent event)
    {
        event.consume();
        if(this.client != null)
        {
            this.client.send(QUIT);
            this.client.disconnect();
        }

        Platform.exit();
        System.exit(0);
    }
}
