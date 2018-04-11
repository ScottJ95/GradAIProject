/**
 * A move is represented by the starting position and the ending position
 * A move must be a valid move in order to be applied to a board.
 *
 * @author Scott Jeffery
 * @version 3/2/2018
 */
public class Move {
    private int startRow;
    private int startCol;
    private int endRow;
    private int endCol;
    private boolean jumpMove;

    /**
     * Move Constructor
     * @param startCol start column of the move
     * @param startRow start row of the move
     * @param endCol end column of the move
     * @param endRow end row of the move.
     */
    public Move(int startCol, int startRow, int endCol, int endRow){
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        if(startRow + 1 == endRow || startRow - 1 == endRow){
            this.jumpMove = false;
        }
        else if(startRow + 2 == endRow || startRow - 2 == endRow){
            this.jumpMove = true;
        }

    }

    /*GETTERS AND SETTERS*/
    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public void setStartCol(int startCol) {
        this.startCol = startCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getEndCol() {
        return endCol;
    }

    public void setEndCol(int endCol) {
        this.endCol = endCol;
    }

    public boolean isJumpMove() {
        return jumpMove;
    }

    public void setJumpMove(boolean jumpMove) {
        this.jumpMove = jumpMove;
    }

    /**
     * Equals override
     * @param obj obj to check
     * @return true if they are equal
     */
    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }
        if(!(obj instanceof  Move)){
            return false;
        }
        Move checkMove = (Move) obj;
        if(this.getStartRow() == checkMove.getStartRow() && this.getStartCol() == checkMove.getStartCol()
                && this.getEndCol() == checkMove.getEndCol() && this.getEndRow() == checkMove.getEndRow()){
            return true;
        }
        return false;
    }

    /**
     * Clone method
     * @param move to clone
     * @return new cloned move
     */
    public Move clone(Move move){
        return new Move(move.getStartCol(), move.getStartRow(), move.getEndCol(), move.getEndRow());

    }

    /**
     * To String
     * @return move string
     */
    public String toString(){
        String result = "";
        result += (char) (65 + startCol);
        result+= startRow;
        result += (char) (65+ endCol);
        result += endRow;

        return result;
    }


}
