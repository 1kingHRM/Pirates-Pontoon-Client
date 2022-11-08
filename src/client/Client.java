package client;

import animatefx.animation.AnimationFX;
import animatefx.animation.FadeIn;
import gui.Card;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static client.Constants.*;

/**
 * This class represents a connection to the server and is used to interact with the server.
 */
public class Client implements Runnable
{
    private final Player player; // The player this client encapsulates

    private Socket socket; // The socket connecting the client to the server
    private PrintWriter writer; //For sending server responses to the client
    private BufferedReader reader; // For receiving client responses

    /**
     * The following are the necessary GUI components needed to display the game
     */
    private String[] playerNames; // The names of the players
    private int[] playerScores; // The high scores of the players
    private HBox[] cardContainers; // The containers for the cards of each player
    private HBox dealOrHold; // The deal or hold button container
    private Label[] scores; // The scores of each player
    private Label[] playerLabels; // The players labels
    private Label dealerScore, highScores; // The dealer score and the high scores of the game
    private TextArea log; // The log which displays the messages from the server
    private AnchorPane highScorePane; // The pane which contains the high scores
    private Button homeBtn; // The button which takes the user back to the home page

    private boolean deal; // A flag to indicate if the player chose to deal another card
    private boolean hold; // A flag to indicate if the player chose to hold their card

    /**
     * Create a new client
     * @param name the name of the player
     */
    public Client(String name)
    {
        this.player = new Player(name);
        this.hold = false;
        this.deal = false;
    }

    /**
     * Set the GUI controls
     * @param highScore the pane that contains the high score
     * @param log the log that displays messages received from the server
     * @param dealerScore the score of the dealer
     * @param highScores the high scores which are displayed in the highScore pane
     * @param btn the button that takes the user back to the home page
     */
    public void set(AnchorPane highScore, TextArea log, Label dealerScore, Label highScores, Button btn)
    {
        this.highScorePane = highScore;
        this.log = log;
        this.dealerScore = dealerScore;
        this.highScores = highScores;
        this.homeBtn = btn;
    }

    /**
     * Set the player's details
     * @param containers the players card containers
     * @param scores the players scores
     * @param dealOrHold the button bar for Deal or Hold
     */
    public void setDetails(HBox[] containers, Label[] scores, Label[] playerLabels, HBox dealOrHold)
    {
        this.cardContainers = containers;
        this.scores = scores;
        this.playerLabels = playerLabels;
        this.dealOrHold = dealOrHold;
    }

    /**
     * Sets the holding flag of the client
     */
    public synchronized void hold()
    {
        this.hold = true;
    }

    /**
     * Sets the dealing flag of the client
     */
    public synchronized void deal()
    {
        this.deal = true;
    }

    /**
     * Informs the server that this client is ready and the game can start
     */
    public void ready()
    {
        send(READY);
    }

    /**
     * Request for the names of all the players
     */
    public void request()
    {
        send(ALL_NAMES);

        String[] names = receive().split(" ");
        String currentPlayer = this.player.name();

        int index = 0;
        for(int i = 0; i < names.length; ++i)
        {
            if(names[i].equalsIgnoreCase(currentPlayer))
            {
                index = i;
                break;
            }
        }

        if(index != 0)
        {
            String temp = names[index];
            names[index] = names[0];
            names[0] = temp;
        }

        this.playerNames = names;
        this.playerScores = new int[names.length];

        Platform.runLater(() ->
        {
            this.playerLabels[0].setText(names[0]);
            if(names.length > 1) // if there is more than 1 player
                this.playerLabels[1].setText(names[1]);
            if(names.length > 2) // if there are more than 2 players
                this.playerLabels[2].setText(names[2]);
            if(names.length > 3) // if there are more than 3 players
                this.playerLabels[3].setText(names[3]);
        });
    }

    /**
     * Connect this client to the server
     * @param address the IP address of the server
     * @param port the port in which the server is listening on
     * @return true if the client was connected to the server or false if otherwise
     */
    public boolean connect(String address, int port)
    {
        try
        {
            this.socket = new Socket(address, port); // Create a new server
            this.writer = new PrintWriter(socket.getOutputStream());
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            send(PLAYER_NAME + " " + this.player.name()); // Send the name of this client to the server
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Disconnects this client from the server
     */
    public void disconnect()
    {
        if(this.socket == null)
            return;

        try
        {
            this.writer.close();
            this.reader.close();
            this.socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Send a message to the server
     * @param message the message being sent
     */
    public synchronized void send(String message)
    {
        if(this.writer == null)
            return;

        this.writer.println(message);
        this.writer.flush();
    }

    /**
     * Receive a message from the server
     * @return the message received from the server
     */
    public synchronized String receive()
    {
        try
        {
            return this.reader.readLine();
        }
        catch(IOException e)
        {
            System.err.println(e.getMessage());
            return NONE;
        }
    }

    @Override
    public void run()
    {
        while(true)
        {
            String message = receive(); // Receive a message from the server
            boolean wasNull = message == null;
            String response = parse(message); // parse the message from the server and get the response
            if(wasNull || message.equals(QUIT))
                return;
            send(response); // send the response to the server
        }
    }

    /**
     * Search for the index of a player
     * @param name the name of the player being searched
     * @return the index of the player or -1 if the player was not found
     */
    private int search(String name)
    {
        for(int i = 0; i < this.playerNames.length; i++)
        {
            String playerName = this.playerNames[i];
            if(name.equalsIgnoreCase(playerName))
                return i;
        }
        return -1;
    }

    /**
     * Parse the message from the server
     * @param message the message from the server
     * @return the response from this client
     */
    private String parse(String message)
    {
        boolean localScores = false; // Display the local scores only if there is an error with the client machine

        if(message == null) // A null message means an error occurred with the client machine, so end the round, display the local scores and conclude the game
        {
            message = BROADCAST + " " + END_ROUND + " " + GAME_OVER;
            localScores = true;
        }

        if(message.startsWith(ASK)) // If the client is being asked whether to Deal or Hold
        {
            Platform.runLater(() -> this.dealOrHold.setVisible(true));
            if(this.deal) // If the dealing flag was set
            {
                this.deal = false; // Clear the flag
                return DEAL; // return Deal
            }
            else if(this.hold) // if the holding flag was set
            {
                this.hold = false; // Clear the flag
                return HOLD; // return Hold
            }
        }
        else if(message.contains(BROADCAST)) // If it was a broadcast
        {
            String broadcast = message.substring(10); // Extract the broadcast
            if(broadcast.contains(MESSAGE)) // If it was a message
                Platform.runLater(() -> this.log.appendText(broadcast.substring(8) + '\n')); // append it to the log
            if(broadcast.contains(ASK)) // If it was an ask
                Platform.runLater(() -> this.log.appendText(broadcast.substring(4) + '\n')); // append it to the log
            if(broadcast.contains(DEALER_SCORE)) // If it was a dealer score
                Platform.runLater(() -> this.dealerScore.setText("Dealer's Score: " + broadcast.substring(12))); // set the dealer score label
            if(broadcast.contains(WIN))
            {
                String winner = broadcast.split(" ")[1];
                if(winner.equals(ALL))
                {
                    for(int i = 0; i < this.playerScores.length; ++i)
                        this.playerScores[i] += 1;
                }
                else
                {
                    int index = Integer.parseInt(winner);
                    if(index != -1)
                        this.playerScores[index] += 1;
                }
            }
            if(broadcast.contains(START_ROUND)) // if a new round is starting
                Platform.runLater(() ->
                {
                    this.highScorePane.toBack(); // Hide the high score pane
                    this.dealerScore.setText("Dealer's Score: 0"); // Clear the dealer score
                    this.log.clear(); // Clear the log
                    this.dealOrHold.setVisible(false); // Hide the dealOrHold box
                    // Remove all the cards for all players
                    for(HBox hbox : this.cardContainers)
                        hbox.getChildren().clear();
                    // Clear all the scores for all players
                    for(Label score : this.scores)
                        score.setText("");
                    this.player.reset(); // Reset the score of the player to 0
                });
            if(broadcast.contains(END_ROUND)) // if a round is ending
                Platform.runLater(() -> this.dealOrHold.setVisible(false)); // Hide the dealOrHold Box
            if(broadcast.contains(HIGH_SCORES)) // if it was the high scores
            {
                String[] details = broadcast.substring(11).split(" "); // Extract the high scores
                Platform.runLater(() ->
                {
                    this.highScorePane.toFront(); // Show the high score pane
                    this.highScores.setText(""); // Clear the previous text

                    AnimationFX fade = new FadeIn(this.highScores); // Play a fade in animation
                    fade.setOnFinished(e ->
                    {
                        for(int i = 0; i < details.length; i += 2) // for each high score
                        {
                            String previous = this.highScores.getText();
                            this.highScores.setText(previous + '\n' + details[i] + "\t\t" + details[i + 1]); // display the player name and the score
                        }
                    });
                    fade.play(); // play the animation
                });
            }
            if(localScores)
            {
                Platform.runLater(() ->
                {
                    this.highScorePane.toFront(); // Show the high score pane
                    this.highScores.setText(""); // Clear the previous text

                    AnimationFX fade = new FadeIn(this.highScores); // Play a fade in animation
                    fade.setOnFinished(e ->
                    {
                        for(int i = 0; i < this.playerNames.length; ++i)
                        {
                            String previous = this.highScores.getText();
                            this.highScores.setText(previous + '\n' + this.playerNames[i] + "\t\t" + this.playerScores[i]);
                        }
                    });
                    fade.play(); // play the animation
                });
            }
            if(broadcast.contains(GAME_OVER)) // if the game is over
                Platform.runLater(() -> this.homeBtn.setVisible(true)); // Show the home button since it was hidden initially
            if(broadcast.contains(DEAL_CARD)) // if we are being dealt a card
            {
                // Extract the name of the player being dealt a card
                int start = broadcast.indexOf(" "), end = broadcast.indexOf(" ", start + 1);
                String playerName = broadcast.substring(start + 1, end);

                // While there are still cards being dealt
                while(!message.contains(END) && !message.isBlank())
                {
                    String[] cardDetails = message.split(" "); // Get the details of the card
                    int cardScore = Integer.parseInt(cardDetails[5]);
                    Card card = new Card(cardDetails[4], cardDetails[3], cardScore); // Create a new card

                    Platform.runLater(() ->
                    {
                        int finalIndex = search(playerName); // Search for the player's index
                        if(finalIndex != -1) // if the index is valid
                        {
                            if(finalIndex == 0) // it means the current player is the one being dealt a card
                                this.player.add(cardScore); // add the score of the card to the player

                            this.cardContainers[finalIndex].getChildren().add(card); // add the card to the card containers
                            this.scores[finalIndex].setText("" + this.player.score()); // update the player's score
                        }
                    });

                    message = receive(); // Receive more messages from the server
                }
            }
        }
        return NONE;
    }
}
