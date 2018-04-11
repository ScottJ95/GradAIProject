/** State data class used for iterative deepening
 * Created by Scott J on 3/19/2018.
 */
public class ITState {
    private Board board;
    private Move move;
    private int score;
    private int depth;


    /**
     * State data class constructor
     * @param board board to store
     * @param move best move of this board
     * @param score score of this board
     * @param depth depth of this board
     */
    public ITState(Board board, Move move, int score, int depth) {
        this.board = board;
        this.score = score;
        this.depth = depth;
        this.move = move;
    }

    /*Getters and Setters*/
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }
}
