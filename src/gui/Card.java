package gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * This class represents the visual appearance of a card
 */
public class Card extends AnchorPane
{

    /**
     * Create a visual card
     * @param suit the suit of the card
     * @param value the value of the card
     * @param score the score of the card
     */
    public Card(String suit, String value, int score)
    {
        setStyle("-fx-background-color:  rgb(142, 32, 26); -fx-background-radius: 5 5 5 5;");
        setPrefWidth(120.0);
        setMinWidth(120.0);
        setPrefHeight(160.0);
        setMinHeight(160.0);

        Font font = new Font("Consolas Bold", 16);

        String displayedNumber = switch (value)
        {
            case "Ace" -> "A";
            case "Jack" -> "J";
            case "Queen" -> "Q";
            case "King" -> "K";
            default -> Integer.toString(score);
        };

        Label topRightNum = new Label(displayedNumber);
        topRightNum.setTextFill(Color.WHITE);
        topRightNum.setFont(font);
        topRightNum.setLayoutX(100.0);
        topRightNum.setLayoutY(10.0);

        Label bottomLeftNum = new Label(displayedNumber);
        bottomLeftNum.setTextFill(Color.WHITE);
        bottomLeftNum.setFont(font);
        bottomLeftNum.setLayoutX(10.0);
        bottomLeftNum.setLayoutY(135.0);

        Label center = new Label("Of");
        center.setTextFill(Color.WHITE);
        center.setFont(font);
        center.setAlignment(Pos.CENTER);
        center.setPrefWidth(120.0);
        center.setMinWidth(120.0);
        center.setLayoutY(65.0);

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.WHITE);
        valueLabel.setFont(font);
        valueLabel.setAlignment(Pos.CENTER);
        valueLabel.setPrefWidth(120.0);
        valueLabel.setMinWidth(120.0);
        valueLabel.setLayoutY(30.0);

        Label suitLabel = new Label(suit);
        suitLabel.setTextFill(Color.WHITE);
        suitLabel.setFont(font);
        suitLabel.setAlignment(Pos.CENTER);
        suitLabel.setPrefWidth(120.0);
        suitLabel.setMinWidth(120.0);
        suitLabel.setLayoutY(100.0);

        getChildren().addAll(topRightNum, bottomLeftNum, center, suitLabel, valueLabel);
    }
}
