package client;

/**
 * This class represents a player
 */
public class Player
{
    private final String name; // The player's name
    private int score; // The player's score

    /**
     * Create a player object
     * @param name the name of the player
     */
    public Player(String name)
    {
        this.name = name;
        this.score = 0;
    }

    /**
     * Get a player's name
     * @return the name of this player
     */
    public String name()
    {
        return this.name;
    }

    /**
     * Add to this player's score
     * @param score the score to be added to this players'
     */
    public void add(int score)
    {
        this.score += score;
    }

    /**
     * Get a player's score
     * @return the score of this player
     */
    public int score()
    {
        return this.score;
    }

    /**
     * Clear the score of a player, resetting it to 0
     */
    public void reset()
    {
        this.score = 0;
    }

}

