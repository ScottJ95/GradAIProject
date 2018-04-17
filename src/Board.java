import java.util.ArrayList;
import java.util.Random;

/**
 * The Board Class represents the board and rules of the checkers game.
 * A Board is defined as a current board state and has information about the state
 * A Board can have a Move applied to it as long as the move is valid
 * When a move is applied to a board, the board changes state
 * The checkers board is straight checkers, which is an 8x8 board.
 *
 * @author Scott Jeffery
 * @version 4/17/2018
 */
public class Board {


    //Pieces for the board
    //Pieces don't need any info besides their type.... enums!! I always forget these.
    public enum Piece {
        BLACK, RED, BLACK_KING, RED_KING, EMPTY
    }

    private Piece[][] board;
    private int numRed;
    private int numBlack;
    private int numRedKing;
    private int numBlackKing;
    private int score;
    //Static Zobrist table usd for hashing. All board use this same table.
    static private int[][][] zobristTable = init_zobrist();

    /**
     * Initialize the Zobrist Table used for hashing
     *
     * @return zobristTable
     */
    private static int[][][] init_zobrist() {
        Random rand = new Random();

        zobristTable = new int[8][8][4];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int r = 0; r < 4; r++)
                    zobristTable[i][j][r] = rand.nextInt(Integer.MAX_VALUE);
            }
        }

        return zobristTable;
    }

    /**
     * Default Constructor
     */
    public Board() {
        createDefaultBoard();
        numRed = 12;
        numRedKing = 0;
        numBlack = 12;
        numBlackKing = 0;
    }

    /**
     * Constructor input is a board state
     *
     * @param board board state to intialize to
     */
    public Board(Piece[][] board) {

        this.board = board;
        countPieces();
    }


    /**
     * Test constructor
     * Pass it any boolean paramaeter to construct a test board
     *
     * @param test test value
     */
    public Board(boolean test) {
        this.board = new Piece[8][8];


        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 4 && j == 0) {
                    board[i][j] = Piece.BLACK;
                } else if (i == 3 && j == 1) {
                    board[i][j] = Piece.RED;
                } else if (i == 0 && j == 6) {
                    board[i][j] = Piece.RED;
                } else {
                    board[i][j] = Piece.EMPTY;
                }
            }
        }

        this.numRedKing = 1;
        this.numBlackKing = 2;
        this.numBlack = 1;
        this.numRed = 2;

    }

    /**
     * Clones a board and returns a new board.
     *
     * @param board board to copy
     * @return new board
     */
    public static Board clone(Board board) {
        Piece[][] newBoard = new Piece[board.getBoard().length][board.getBoard()[0].length];
        //Copy the array manually, clone just returns a reference to the same objects.
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                newBoard[i][j] = board.getBoard()[i][j];
            }
        }
        return new Board(newBoard);
    }

    /**
     * Create the default starting checkers board.
     */
    private void createDefaultBoard() {
        Piece[][] board = new Piece[8][8];

        for (int i = 0; i < 8; i++) {
            int j;
            //Red Pieces
            if (i == 0 || i == 2) {
                j = 0;
                while (j < 8) {
                    if (j % 2 == 0) {
                        board[i][j] = Piece.RED;
                    } else {
                        board[i][j] = Piece.EMPTY;
                    }
                    j++;
                }
            } else if (i == 1) {
                j = 0;
                while (j < 8) {
                    if (j % 2 == 1) {
                        board[i][j] = Piece.RED;
                    } else {
                        board[i][j] = Piece.EMPTY;
                    }
                    j++;
                }
            }

            //Black Pieces
            else if (i == 5 || i == 7) {
                j = 0;
                while (j < 8) {
                    if (j % 2 == 1) {
                        board[i][j] = Piece.BLACK;
                    } else {
                        board[i][j] = Piece.EMPTY;
                    }
                    j++;
                }
            } else if (i == 6) {
                j = 0;
                while (j < 8) {
                    if (j % 2 == 0) {
                        board[i][j] = Piece.BLACK;
                    } else {
                        board[i][j] = Piece.EMPTY;
                    }
                    j++;
                }
            } else {
                j = 0;
                while (j < 8) {
                    board[i][j] = Piece.EMPTY;
                    j++;
                }
            }
        }
        this.board = board;
    }

    /**
     * Make a move on the board with the specified move with the specified player
     * Returns a MoveResult with information about the move that was made.
     *
     * @param move move to make
     * @param side side to move
     * @return MoveResult result of the move made
     */
    public MoveResult makeMove(Move move, Player.Side side) {
        //First we need to check errors.
        if (move == null) {
            String message = "No move.";
            return new MoveResult(false, message, false, true);
        }
        //Check if there's a piece present.
        Piece checkPiece = getPiece(move.getStartRow(), move.getStartCol());
        if (checkPiece == Piece.EMPTY) {
            String message = "No piece there.";
            return new MoveResult(false, message, false, false);
        }

        //Check valid player is moving their piece.
        if (side == Player.Side.BLACK) {
            if (checkPiece == Piece.RED || checkPiece == Piece.RED_KING) {
                String message = "That's not your piece!";
                return new MoveResult(false, message, false, false);
            }
        }

        if (side == Player.Side.RED) {
            if (checkPiece == Piece.BLACK || checkPiece == Piece.BLACK_KING) {
                String message = "That's not your piece!";
                return new MoveResult(false, message, false, false);
            }
        }

        //Let's pull out our values
        //NOTE WE USE ROW,COL HERE INSTEAD OF COL,ROW THAT'S STORED IN A MOVE
        int startRow = move.getStartRow();
        int startCol = move.getStartCol();
        int endRow = move.getEndRow();
        int endCol = move.getEndCol();

        //Check if this move is valid.
        if (isValidMove(move, side)) {
            //We need to verify: Is it a normal move or a jump move?
            boolean jump = move.isJumpMove();
            if (!jump) {
                board[startRow][startCol] = Piece.EMPTY;
                board[endRow][endCol] = checkPiece;
            } else {
                board[startRow][startCol] = Piece.EMPTY;
                board[endRow][endCol] = checkPiece;

                //So we jumped over a piece, where is that piece we jumped over?
                //Mid point: x1 + x2 / 2
                int midRow = (startRow + endRow) / 2;
                int midCol = (startCol + endCol) / 2;
                Piece midPiece = getPiece(midRow, midCol);
                //Check what the piece is
                switch (midPiece) {
                    case RED:
                        numRed--;
                        break;
                    case RED_KING:
                        numRedKing--;
                        break;
                    case BLACK:
                        numBlack--;
                        break;
                    case BLACK_KING:
                        numBlackKing--;
                        break;
                }
                board[midRow][midCol] = Piece.EMPTY;
            }

            //Now let's check if the move turned a piece into a king.
            if (endRow == 0 && side == Player.Side.BLACK && board[endRow][endCol] == Piece.BLACK) {
                board[endRow][endCol] = Piece.BLACK_KING;
                numBlackKing++;
                numBlack--;
            } else if (endRow == board.length - 1 && side == Player.Side.RED && board[endRow][endCol] == Piece.RED) {
                board[endRow][endCol] = Piece.RED_KING;
                numRedKing++;
                numRed--;
            }

            //If the player made a jump move, we need to check to see if it is still the player's turn.
            boolean additionalMove = false;
            String message = "Move completed.";
            if (jump) {
                ArrayList<Move> jumpMoves = getPossibleJumpMoves(endRow, endCol, side);
                if (!jumpMoves.isEmpty()) {
                    additionalMove = true;
                    message = message + " Player must make another move.";
                }
            }
            return new MoveResult(true, message, additionalMove, false);
        } else {
            //It's possible a player cannot move.
            String message;
            if (side == Player.Side.BLACK) {
                if (getAllPossibleMoves(Player.Side.BLACK).isEmpty()) {
                    message = "Black cannot move, Red Wins!";
                    return new MoveResult(false, message, false, true);
                }
            } else {
                if (getAllPossibleMoves(Player.Side.RED).isEmpty()) {
                    message = "Red cannot move. Black wins!";
                    return new MoveResult(false, message, false, true);
                }

            }
            message = "This move is not allowed or you must jump somewhere.";
            return new MoveResult(false, message, false, false);
        }
    }

    /**
     * Checks to make sure that a specified move is valid.
     * If the player can make a jump move and the move is not a valid jump move, then the move cannot be made.
     *
     * @param move move to check
     * @return true if move is valid
     */
    private boolean isValidMove(Move move, Player.Side side) {
        //Let's see if there's a jump move first. If so, then the input move MUST be a jump move.
        //While this might seem redundent, I need a way to double check this.
        ArrayList<Move> possibleJumps = getAllPossibleJumpMoves(side);
        if (!possibleJumps.isEmpty()) {
            if (move.isJumpMove()) {
                if (possibleJumps.contains(move)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        //Otherwise, let's check all the other moves.
        else {
            ArrayList<Move> possibleMoves =
                    getPossiblePieceMoves(move.getStartRow(), move.getStartCol(), side);
            if (possibleMoves.contains(move)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Count the pieces of this current board and update the piece counts.
     */
    private void countPieces() {
        int redTot = 0;
        int redKingTot = 0;
        int blackTot = 0;
        int blackKingTot = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                Piece piece = board[i][j];
                switch (piece) {
                    case RED:
                        redTot++;
                        break;
                    case RED_KING:
                        redKingTot++;
                        break;
                    case BLACK:
                        blackTot++;
                        break;
                    case BLACK_KING:
                        blackKingTot++;
                        break;
                }
            }
        }
        this.numRed = redTot;
        this.numRedKing = redKingTot;
        this.numBlack = blackTot;
        this.numBlackKing = blackKingTot;
    }

    /**
     * Checks the current game state to determine if the game is over.
     * The game is over if one side has no more pieces or if one side cannot move.
     *
     * @return true if the game is over.
     */
    private boolean checkGameOver() {
        if (this.getTotalRed() == 0 || this.getTotalBlack() == 0) {
            return true;
        } else if (this.getAllPossibleMoves(Player.Side.RED).isEmpty()) {
            return true;
        } else if (this.getAllPossibleMoves(Player.Side.BLACK).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Generate and return the list of all possible moves for a specific side
     *
     * @param side side to use
     * @return ArrayList of all possible moves for the side
     */
    public ArrayList<Move> getAllPossibleMoves(Player.Side side) {
        ArrayList<Move> moves = new ArrayList<Move>();
        Piece normal;
        Piece king;

        moves.addAll(getAllPossibleJumpMoves(side));
        //If there is any jump moves, these are the only possible moves.
        if (!moves.isEmpty()) {
            return moves;
        }

        if (side == Player.Side.BLACK) {
            normal = Piece.BLACK;
            king = Piece.BLACK_KING;
        } else {
            normal = Piece.RED;
            king = Piece.RED_KING;
        }

        //Just brute force it.
        //For every single square, if a piece is there, get all of their possible moves.
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                Piece currPiece = getPiece(i, j);
                if (currPiece == normal || currPiece == king) {
                    if (getPossiblePieceMoves(i, j, side) != null) {
                        moves.addAll(getPossiblePieceMoves(i, j, side));
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Generate and return the list of all possible moves for a specific piece
     * Jump moves are forced.
     * If a jump move is possible returns only those possible jump moves.
     *
     * @param row  row pos to use
     * @param col  col pos to use
     * @param side side to use
     * @return ArrayList of all possible moves
     */
    private ArrayList<Move> getPossiblePieceMoves(int row, int col, Player.Side side) {
        //If a piece is normal, it has 2 possible moves maximum. If it's a king piece, it has 4 moves maximum.
        Piece currPiece = getPiece(row, col);

        if (currPiece == Piece.EMPTY) {
            System.out.println("CurrPiece is empty");
            return null;
        }

        ArrayList<Move> moves = new ArrayList<Move>();
        int newRow = 0;

        moves.addAll(getPossibleJumpMoves(row, col, side));
        if (!moves.isEmpty()) {
            return moves; //Jump moves are forced, so this is the only move you can make.
        }

        //Normal Pieces have 2 possible moves.
        if (side == Player.Side.BLACK && currPiece == Piece.BLACK) {
            newRow = row - 1;
            if (newRow >= 0) {
                moves.addAll(generateMoves(row, col, newRow));
            }
        } else if (side == Player.Side.RED && currPiece == Piece.RED) {
            newRow = row + 1;
            if (newRow < board.length) {
                moves.addAll(generateMoves(row, col, newRow));
            }
        }

        //King pieces, which have 4 possible moves.
        else if (currPiece == Piece.BLACK_KING || currPiece == Piece.RED_KING) {
            newRow = row - 1;
            if (newRow >= 0) {
                moves.addAll(generateMoves(row, col, newRow));
            }

            newRow = row + 1;
            if (newRow < board.length) {
                moves.addAll(generateMoves(row, col, newRow));
            }
        }
        return moves;
    }

    /**
     * Checks the moves that a specific piece can make and returns the list of valid moves.
     *
     * @param newRow row to use
     * @return list of generated moves
     */
    private ArrayList<Move> generateMoves(int row, int col, int newRow) {
        ArrayList<Move> moves = new ArrayList<Move>();

        //Test both moves
        int newCol = col - 1;
        if (newCol >= 0 && getPiece(newRow, newCol) == Piece.EMPTY) {
            moves.add(new Move(col, row, newCol, newRow));
        }

        newCol = col + 1;
        if (newCol < board.length && getPiece(newRow, newCol) == Piece.EMPTY) {
            moves.add(new Move(col, row, newCol, newRow));
        }
        return moves;
    }

    /**
     * Generate and return the list of possible jump moves for a specific piece
     *
     * @param row  row pos to use
     * @param col  col pose to use
     * @param side side to use
     * @return ArrayList of all possible jump moves
     */
    private ArrayList<Move> getPossibleJumpMoves(int row, int col, Player.Side side) {
        Piece currPiece = getPiece(row, col);

        ArrayList<Move> moves = new ArrayList<Move>();

        //Standard pieces can make 2 possible jumps, kings can make 4.
        //Standard pieces
        if (side == Player.Side.BLACK && getPiece(row, col) == Piece.BLACK) {
            int newRow = row - 2;
            if (newRow >= 0) {
                moves.addAll(generateJumpMoves(row, col, newRow, side));
            }
        } else if (side == Player.Side.RED && getPiece(row, col) == Piece.RED) {
            int newRow = row + 2;
            if (newRow < board.length) {
                moves.addAll(generateJumpMoves(row, col, newRow, side));
            }
        }

        //King pieces .
        else if (getPiece(row, col) == Piece.BLACK_KING || getPiece(row, col) == Piece.RED_KING) {
            int newRow = row + 2;
            if (newRow < board.length) {
                moves.addAll(generateJumpMoves(row, col, newRow, side));
            }
            newRow = row - 2;
            if (newRow >= 0) {
                moves.addAll(generateJumpMoves(row, col, newRow, side));
            }
        }
        return moves;
    }

    /**
     * Generate jump moves for a specific piece. Returning the list of all moves if valid.
     *
     * @param row    row
     * @param col    col
     * @param newRow new row
     * @return list of generated jump moves
     */
    private ArrayList<Move> generateJumpMoves(int row, int col, int newRow, Player.Side side) {
        ArrayList<Move> moves = new ArrayList<Move>();

        //Check top.
        int newCol = col + 2;
        //Make sure the move is valid.
        if (newRow < board.length && newRow >= 0 && newCol < board.length && newCol >= 0
                && getPiece(newRow, newCol) == Piece.EMPTY) {
            int midRow = (row + newRow) / 2;
            int midCol = (col + newCol) / 2;
            Piece midPiece = getPiece(midRow, midCol);

            //Make sure the midPiece is valid.
            if (side == Player.Side.BLACK && (midPiece == Piece.RED || midPiece == Piece.RED_KING)) {
                moves.add(new Move(col, row, newCol, newRow));
            } else if (side == Player.Side.RED && (midPiece == Piece.BLACK || midPiece == Piece.BLACK_KING)) {
                moves.add(new Move(col, row, newCol, newRow));
            }
        }

        //Check bottom.
        newCol = col - 2;
        if (newRow < board.length && newRow >= 0 && newCol < board.length && newCol >= 0
                && getPiece(newRow, newCol) == Piece.EMPTY) {
            int midRow = (row + newRow) / 2;
            int midCol = (col + newCol) / 2;
            Piece midPiece = getPiece(midRow, midCol);

            if (side == Player.Side.BLACK && (midPiece == Piece.RED || midPiece == Piece.RED_KING)) {
                moves.add(new Move(col, row, newCol, newRow));
            } else if (side == Player.Side.RED && (midPiece == Piece.BLACK || midPiece == Piece.BLACK_KING)) {
                moves.add(new Move(col, row, newCol, newRow));
            }
        }

        return moves;
    }

    /**
     * Generate and return the list of all possible jump moves for a specific player.
     *
     * @param side side to use
     * @return ArrayList of all possible jump moves
     */
    public ArrayList<Move> getAllPossibleJumpMoves(Player.Side side) {
        ArrayList<Move> moves = new ArrayList<Move>();
        Piece normal;
        Piece king;

        if (side == Player.Side.BLACK) {
            normal = Piece.BLACK;
            king = Piece.BLACK_KING;
        } else {
            normal = Piece.RED;
            king = Piece.RED_KING;
        }

        //Just brute force it....
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                Piece currPiece = getPiece(i, j);
                if (currPiece == normal || currPiece == king) {
                    moves.addAll(getPossibleJumpMoves(i, j, side));
                }
            }
        }
        return moves;
    }

    /**
     * Get and return the number of additional jump moves that can be made after the current jump move.
     *
     * @param board Board to use
     * @param jumpMoves jump moves list to use
     * @param side      side of player making the move
     * @return number of additional jump moves
     */
    public int getAdditionalJumpMoves(Board board, ArrayList<Move> jumpMoves, Player.Side side) {
        int result = 0;

        //For all possible moves
        for (Move jump : jumpMoves) {
            //Might be memory intensive
            //However this is the only way I could do this without it being overly complicated.
            Board tempBoard = Board.clone(board);
            Piece piece = getPiece(jump.getStartRow(), jump.getStartCol());
            MoveResult moveResult = tempBoard.makeMove(jump, side);

            //Check to not mess up the while loop.
            if (moveResult.isAdditionalMove()) {
                result += 2;
            }

            //While we can make an additional move.
            while (moveResult.isAdditionalMove()) {
                //Add to our result
                //Set up our new starting positions
                int newStartCol = jump.getEndCol();
                int newStartRow = jump.getEndRow();

                //Get the jump moves for that piece
                ArrayList<Move> tempJumps = getPossibleJumpMoves(newStartRow, newStartCol, side);
                //If the piece can still jump, make the first jump move.
                //If a piece has choosing jump moves after the first, we'll count it as one move.
                if (!tempJumps.isEmpty()) {
                    result += 2;
                    moveResult = tempBoard.makeMove(tempJumps.get(0), side);
                } else {
                    moveResult.setAdditionalMove(false);
                }
            }
        }
        return result;
    }

    /* GETTERS AND SETTERS */
    public Piece[][] getBoard() {
        return board;
    }

    public void setBoard(Piece[][] board) {
        this.board = board;
    }

    public boolean isGameOver() {
        return this.checkGameOver();
    }

    public int getTotalRed() {
        return numRed + numRedKing;
    }


    public int getTotalBlack() {
        return numBlack + numBlackKing;
    }

    public int getNumBlack() {
        return numBlack;
    }

    public int getNumRed() {
        return numRed;
    }


    public int getNumRedKing() {
        return numRedKing;
    }


    public int getNumBlackKing() {
        return numBlackKing;
    }

    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Hashcode override using the Zobrist hashing method
     * This was required to get hashmap to work correctly.
     *
     * @return hashcode of this object.
     */
    @Override
    public int hashCode() {
        int result = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != Piece.EMPTY) {
                    Piece piece = board[i][j];
                    switch (piece) {
                        case RED:
                            result ^= zobristTable[i][j][0];
                            break;
                        case RED_KING:
                            result ^= zobristTable[i][j][1];
                            break;
                        case BLACK:
                            result ^= zobristTable[i][j][2];
                            break;
                        case BLACK_KING:
                            result ^= zobristTable[i][j][3];
                            break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Equals method override.
     * Checks the hashcodes.
     *
     * @param o objects to check for equality
     * @return true of objects are equal
     */
    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (!o.getClass().equals(Board.class)) {
            return false;
        } else {
            if (o.hashCode() == this.hashCode()) {
                return true;
            }
        }
        return result;
    }

    /**
     * Returns a visual representation of this board.
     * Columns are labeled A - H and Rows are labeled 0 - 7
     *
     * @return String representation of the game board.
     */
    @Override
    public String toString() {
        String result = "";

        for (int i = 7; i >= 0; i--) {
            result += (i) + " ";
            for (int j = 0; j < board.length; j++) {
                result = result + "[";
                Piece piece = getPiece(i, j);
                switch (piece) {
                    case BLACK:
                        result += "b]";
                        break;
                    case BLACK_KING:
                        result += "B]";
                        break;
                    case RED:
                        result += "r]";
                        break;
                    case RED_KING:
                        result += "R]";
                        break;
                    case EMPTY:
                        result += " ]";
                        break;
                }
            }
            result += "\n";
        }

        result += "   A  B  C  D  E  F  G  H\n";
        return result;
    }
}
