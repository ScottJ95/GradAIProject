/**
 * Class that holds the result after making a move.
 * The Move Result holds data such as if the move was successful, a message, if there's an additional move, etc.
 *
 *
 * @author Scott Jeffery
 * @version 3/25/2018
 */
public class MoveResult {

    private boolean completed;
    private String message;
    private boolean additionalMove;
    private boolean cannotMove;

    /**
     * Move Result constructor
     * @param completed true if move is a valid move
     * @param message message of the move
     * @param additionalMove true if there is an additional move that can be made
     * @param cannotMove true if a player now cannot move.
     */
    public MoveResult(boolean completed, String message, boolean additionalMove, boolean cannotMove){
        this.completed = completed;
        this.message = message;
        this.additionalMove = additionalMove;
        this.cannotMove = cannotMove;

    }

    /* GETTERS AND SETTERS*/
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAdditionalMove() {
        return additionalMove;
    }

    public void setAdditionalMove(boolean additionalMove) {
        this.additionalMove = additionalMove;
    }

    public void printResult(){
        System.out.println(this.message + "\n");

    }

    public boolean isCannotMove() {
        return cannotMove;
    }

    public void setCannotMove(boolean cannotMove) {
        this.cannotMove = cannotMove;
    }
}
