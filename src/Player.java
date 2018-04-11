/**
 * The player class represents the player who will be playing against the A.I.
 * Players keep track of if its their turn, what side they are on, and players can make moves on the board.
 * @author  Scott Jeffery
 * @version 2/22/2018
 */
public class Player {

    public enum Side{
        BLACK, RED
    }
    private Side side;

    /**
     * Player constructor
     * @param side side of the player. Either BLACK or RED
     */
    public Player(Side side){
        this.side = side;
    }
    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }
}
