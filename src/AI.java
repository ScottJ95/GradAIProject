import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * The A.I. Class represents the A.I. player
 * The A.I. Player decides on their move using minimax with alpha beta pruning.
 * The A.I. player can have multiple behaviors that decide it's difficulty.
 * TODO Cleanup Debug Lines and General Code Cleanup.
 *
 * @author Scott J
 * @version 4/6/2018
 */
public class AI extends Player {
    private int maxDepth;
    private int botDifficulty;
    private boolean debug;
    //The last position of a skip move if the bot can make an additional move.
    //If they are both -1, the bot cannot make an additional move.
    private int jumpCol = -1;
    private int jumpRow = -1;
    //time limit for searching.
    //NOTE: THE TIME LIMIT IS HIGHLY VARIABLE BECAUSE OF DIFFERENT STUFF.
    private long timeLimit = 5000; //milliseconds
    private HashMap<Board, ITState> transTable;


    /**
     * Constructor for the AI
     *
     * @param side       side the AI is playing on
     * @param difficulty difficulty of the AI value of 1-3
     * @param debug true if you want to enable debug output.
     */
    public AI(Side side, int difficulty, boolean debug) {
        super(side);
        switch (difficulty) {
            //Random
            case 0:
                break;
            //1 - 3 use Standard Alpha Beta with static depth limiting.
            case 1:
                this.maxDepth = 5;
                break;
            case 2:
                this.maxDepth = 7;
                break;
            case 3:
                this.maxDepth = 9;
                break;
            //It Deepening
            case 4:
                this.maxDepth = 100;
                break;
            //Should never be reached.
            default:
                this.maxDepth = 7;
                break;
        }
        this.botDifficulty = difficulty;
        this.debug = debug;
    }

    /**
     * Have the A.I. make a move on the board.
     * Also needs to take into account if the A.I. can make an additional move!
     *
     * @param board       board to use
     * @param moveHistory moveHistory to keep track of. Should be given from the driver.
     * @return MoveResult of the A.I.'s move
     */
    public MoveResult makeMove(Board board, ArrayList<Move> moveHistory) {
        //Every time we decide on a move, we have to reset the transition table.
        //The transition table stores the history of the previous iteration of deepening for move ordering.
        transTable = new HashMap<Board, ITState>();
        Move bestMove;
        System.out.println("I'm thinking...");
        //Figure out the best move based on the difficulty setting.
        if (this.botDifficulty == 4) {
            bestMove = findBestMoveITDeepening(board);
        }
        //Random move
        else if (this.botDifficulty == 0) {
            bestMove = findRandomMove(board);
        } else {
            bestMove = findBestMove(board);
        }

        MoveResult result = board.makeMove(bestMove, this.getSide());
        if (result == null) {
            System.out.println("This should not be reached");
        }

        //Check to see if we need to make another move.
        if (result.isAdditionalMove()) {
            //Let's keep track of where our piece ended up.
            this.jumpCol = bestMove.getEndCol();
            this.jumpRow = bestMove.getEndRow();
        }
        moveHistory.add(bestMove);
        return result;
    }

    /**
     * Decide on a random move and just return that random move.
     * Used when bot difficulty is 0
     *
     * @param board board to use
     * @return random move
     */
    private Move findRandomMove(Board board) {
        ArrayList<Move> moves;

        //Get all the moves for the root node.
        if (this.jumpRow == -1 && this.jumpCol == -1) {
            moves = board.getAllPossibleMoves(this.getSide());
        }

        //We are in an additional move. Reset skip position.
        else {
            moves = board.getAllPossibleMoves(this.getSide());
            this.jumpCol = -1;
            this.jumpRow = -1;
        }

        //We can't make a move. We have lost.
        if (moves.isEmpty()) {
            return null;
        }

        /* This is the only move we can make. End the search*/
        if (moves.size() == 1) {
            System.out.println("Forced to jump. Here is my move: " + moves.get(0).toString());
            return moves.get(0);
        }

        //Return random move
        Random rand = new Random();
        return moves.get(rand.nextInt(moves.size()));
    }

    /**
     * Decide and return the best possible move to make using
     * Initializes the minimax algorithm using max depth limiting.
     *
     * @param board board to use
     * @return best possible move for the A.I.
     */
    private Move findBestMove(Board board) {
        //Initialize values
        int bestScore = Integer.MIN_VALUE;
        Move bestMove;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int depth = maxDepth;
        ArrayList<Move> moves;
        ArrayList<Integer> scores;

        //Get all the moves for the root node.
        if (this.jumpRow == -1 && this.jumpCol == -1) {
            moves = board.getAllPossibleMoves(this.getSide());
        }

        //We are in an additional move. Reset skip position.
        else {
            moves = board.getAllPossibleMoves(this.getSide());
            this.jumpCol = -1;
            this.jumpRow = -1;
        }

        //We can't make a move. We have lost.
        if (moves.isEmpty()) {
            return null;
        }

        /* This is the only move we can make. End the search*/
        if (moves.size() == 1) {
            System.out.println("Forced to jump. Here is my move: " + moves.get(0).toString());
            return moves.get(0);
        }

        long timeStamp = System.currentTimeMillis();
        //System.out.println("Current Timestamp: " + timeStamp + "ms");

        //NORMAL MOVE SETTING.
        //Now let's go through each move.
        scores = new ArrayList<>();
        moves = board.getAllPossibleMoves(this.getSide());
        for (Move move : moves) {
            // System.out.println(move.toString());
            Board tempBoard = Board.clone(board);
            tempBoard.makeMove(move, this.getSide());
            //Run minimax on the child, and add the score to the list.
            scores.add(minimax(tempBoard, depth - 1, flipSide(this.getSide()), alpha, beta, false));
            long timestampTemp = System.currentTimeMillis();
            long timeDiff = timestampTemp - timeStamp;
            //System.out.println("Current Timestamp: " + timeStamp + "ms. Total time: " + timeDiff + "ms");
        }

        bestScore = getBestMove(moves, scores);

        //Pick tied move at random.
        Random rand = new Random();
        bestMove = moves.get(rand.nextInt(moves.size()));
        if (debug) {
            System.out.println("Best move is: " + bestMove.toString() + ", with a score of: " + bestScore);
            System.out.println("Time spent: " + (System.currentTimeMillis() - timeStamp));
        }
        System.out.println("My move is: " + bestMove.toString());
        return bestMove;
    }

    /**
     * Decide and return the best possible move to make using iterative deepening.
     * Initializes the minimax algorithm with iterative deepening for dynamic depth limiting.
     *
     * @param board board to use
     * @return best possible move for the A.I.
     */
    private Move findBestMoveITDeepening(Board board) {
        //Initialize values
        int bestScore;
        Move bestMove;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int timeRange = 750;
        int depth = 1;
        ArrayList<Move> moves;

        //Get all the moves for the root node.
        if (this.jumpRow == -1 && this.jumpCol == -1) {
            moves = board.getAllPossibleMoves(this.getSide());
        }

        //We are in an additional move. Reset jump position.
        else {
            moves = board.getAllPossibleMoves(this.getSide());
            this.jumpCol = -1;
            this.jumpRow = -1;
        }

        //We can't make a move. We have lost.
        if (moves.isEmpty()) {
            return null;
        }

        /* This is the only move we can make. End the search*/
        if (moves.size() == 1) {
            System.out.println("Forced to jump. Here is my move: " + moves.get(0).toString());
            return moves.get(0);
        }

        //Intialize time tracking.
        long timeStamp = System.currentTimeMillis();
        long timeSpent;
        //System.out.println("Current Timestamp: " + timeStamp + "ms");
        boolean outOfTime = false;
        ArrayList<Integer> scores;
        ArrayList<Move> searched;
        //BestScore and BestMove.
        bestMove = null;

        //Start main loop
        while (!outOfTime && depth <= this.maxDepth) {
            //We initialize these here in the loop because if we have enough time to run the algorithm again...
            //We need to overwrite the new values.
            searched = new ArrayList<>();
            scores = new ArrayList<>();

            //We already generated the children in the previous iteration.
            //Let's search the best move first.
            if (depth != 1 && bestMove != null) {
                Board tempBoard = Board.clone(board);
                tempBoard.makeMove(bestMove, this.getSide());
                int score = minimaxITDeepening(tempBoard, depth - 1, flipSide(this.getSide()),
                        alpha, beta, false);
                scores.add(score);
                searched.add(bestMove);
                timeSpent = System.currentTimeMillis() - timeStamp;
                if (timeSpent >= timeLimit - timeRange) {
                    System.out.println("My move is: " + bestMove.toString());
                    return bestMove;
                }
            }

            for (Move move : moves) {
                if (!move.equals(bestMove)) {
                    Board tempBoard = Board.clone(board);
                    tempBoard.makeMove(move, this.getSide());
                    //Run minimax on the child, and add the score to the list.
                    int score = minimaxITDeepening(tempBoard, depth - 1, flipSide(this.getSide()),
                            alpha, beta, false);
                    scores.add(score);
                    searched.add(move);
                    timeSpent = System.currentTimeMillis() - timeStamp;
                    if (timeSpent >= timeLimit - timeRange) {
                        System.out.println("My move is: " + bestMove.toString());
                        return bestMove;
                    }
                }
            }

            bestScore = getBestMove(searched, scores);
            //Pick tied move at random.
            Random rand = new Random();
            bestMove = searched.get(rand.nextInt(searched.size()));
            timeSpent = System.currentTimeMillis() - timeStamp;
            if (debug) {
                System.out.println("Best move at depth " + depth + " : " + bestMove.toString() +
                        ", with a score of: " + bestScore);
                System.out.println("Time spent: " + timeSpent + "ms");
            }
            if (timeSpent >= timeLimit - timeRange) {
                outOfTime = true;
            }
            depth++;
        }//end of main loop.
        System.out.println("My move is: " + bestMove.toString());
        return bestMove;
    }


    /**
     * Get the best move out of the list of searched moves and scores.
     * Assumed that Searched[i] corresponds to scores[i]
     * This method also will alter searched and scores.
     *
     * @param searched moves that were searched
     * @param scores   scores of these moves
     * @return best score found in these moves
     */
    private int getBestMove(ArrayList<Move> searched, ArrayList<Integer> scores) {
        int bestScore = Integer.MIN_VALUE;

        //We are max, so we want the highest out of all of the scores we saw.
        //If a score is tied, let's just pick one at random.
        //Easiest way I found to do this is to just find the best score, and extract all the ones that aren't tied.
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i) > bestScore) {
                bestScore = scores.get(i);
            }
        }

        //Filter scores to find tied ones
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i) < bestScore) {
                scores.remove(i);
                searched.remove(i);
                i--;
            }
        }
        return bestScore;
    }

    /**
     * Perform the minimax algorithm on the specified board with alpha beta pruning.
     * Fail-Soft Implementation. Just used the common pseudo code that is found pretty much everywhere.
     *
     * @param board current board state
     * @param depth depth
     * @param alpha alpha value
     * @param beta  beta value
     * @param isMax true if a maxNode
     * @return result of minimax
     */
    private int minimax(Board board, int depth, Side side, int alpha, int beta, boolean isMax) {
        int result;
        if (depth == 0 || board.isGameOver()) { //Max Depth
            return quiesce(board, side, alpha, beta, isMax);
        }

        //Get all the possible moves
        ArrayList<Move> moves = board.getAllPossibleMoves(side);

        //Max node
        if (isMax) {
            result = Integer.MIN_VALUE;
            //Going from right to left, get all the moves.
            for (Move move : moves) {
                //Generate child
                boolean nextMax = isMax;
                Side nextSide = side;
                Board tempBoard = Board.clone(board);
                MoveResult moveResult = tempBoard.makeMove(move, side);

                //It's still max's turn.
                if (moveResult.isAdditionalMove()) {
                    nextSide = flipSide(side);
                    nextMax = !isMax;
                    //System.out.println("Double Jump Found: " + nextSide + ", " + nextMax);
                }
                result = Math.max(result, minimax(tempBoard, depth - 1, flipSide(nextSide), alpha, beta, !nextMax));
                alpha = Math.max(alpha, result);

                if (beta <= alpha) {
                    // System.out.println("Cut off at Max: " + depth + ", " + "Alpha: " + alpha + ", Beta: " + beta);
                    return result;
                }
            }
            return result;
        }

        //Min node
        else {
            result = Integer.MAX_VALUE;
            for (Move move : moves) {
                //Generate child
                boolean nextMax = isMax;
                Side nextSide = side;
                Board tempBoard = Board.clone(board);
                MoveResult moveResult = tempBoard.makeMove(move, side);
                //It's still min's turn.
                if (moveResult.isAdditionalMove()) {
                    nextSide = flipSide(side);
                    nextMax = !isMax;
                    // System.out.println("Double Jump Found: " + nextSide + ", " + nextMax);
                }

                result = Math.min(result, minimax(tempBoard, depth - 1, flipSide(nextSide), alpha, beta, !nextMax));
                beta = Math.min(beta, result);

                if (beta <= alpha) {
                    //  System.out.println("Cut off at Min at: " + depth + " Alpha: " + alpha + " Beta: " + beta);
                    return result;
                }
            }
            return result;
        }
    }

    /**
     * Perform the minimax algorithm on the specified board using iterative deepening.
     * The algorithm searches using "Best Move First" from previous iterations.
     * Modification of common fail-soft alpha-beta to fit the needs
     *
     * @param board current board state
     * @param depth depth
     * @param alpha alpha value
     * @param beta  beta value
     * @param isMax true if a maxNode
     * @return result of minimax
     */
    private int minimaxITDeepening(Board board, int depth, Side side, int alpha, int beta, boolean isMax) {
        int result;

        //Perform Queisence Search on this board.
        if (depth == 0 || board.isGameOver()) {
            return quiesce(board, side, alpha, beta, isMax);
        }

        /*
            This algorithm is based off a number of articles I was reading online.
            It performs "Best Move First" using iterative deepening.
            If the cut off is a fail-high or an exact, we store the best move.
            If the cut off is a fail-low, we DO NOT store the best move.
            If there exists a best move on the board inside the hash table, use that best move.
         */
        else {
            ArrayList<Move> moves = board.getAllPossibleMoves(side);
            Move bestMove = null;


            //Search best move first, if it exists.
            if (transTable.containsKey(board) && transTable.get(board).getMove() != null) {
                bestMove = transTable.get(board).getMove();
            }

            //There's a lot of repeated code here, but I couldn't figure out how to break it up.
            //I wanted to avoid potential stack overflow errors.
            //Max node
            if (isMax) {
                result = Integer.MIN_VALUE;

                //Search best move first.
                if (bestMove != null) {
                    Side nextSide = side;
                    boolean nextMax = isMax;
                    Board bestBoard = Board.clone(board);
                    MoveResult moveResult = bestBoard.makeMove(bestMove, side);

                    //It's still max's turn.
                    if (moveResult.isAdditionalMove()) {
                        nextSide = flipSide(side);
                        nextMax = !isMax;
                        // System.out.println("Double Jump Found: " + nextSide + ", " + nextMax);
                    }

                    result = Math.max(result, minimaxITDeepening(bestBoard, depth - 1, flipSide(nextSide),
                            alpha, beta, !nextMax));
                    alpha = Math.max(alpha, result);

                    if (beta <= alpha) {
                        //Beta cut off. store it.
                        transTable.put(board, new ITState(board, bestMove, result, depth));
                        return result;
                    }
                }
                for (Move move : moves) {
                    if (!move.equals(bestMove)) {
                        Side nextSide = side;
                        boolean nextMax = isMax;
                        Board tempBoard = Board.clone(board);
                        MoveResult moveResult = tempBoard.makeMove(move, side);

                        //It's still max's turn.
                        if (moveResult.isAdditionalMove()) {
                            nextSide = flipSide(side);
                            nextMax = !isMax;
                            //  System.out.println("Double Jump Found: " + nextSide + ", " + nextMax);
                        }

                        int tempResult = result;
                        result = Math.max(result, minimaxITDeepening(tempBoard, depth - 1, flipSide(nextSide),
                                alpha, beta, !nextMax));

                        //New best move found.
                        if (tempResult != result) {
                            bestMove = move;
                        }

                        alpha = Math.max(alpha, result);
                        if (beta <= alpha) {
                            //Beta cut off. Store it.
                            transTable.put(board, new ITState(board, move, result, depth));
                            return result;
                        }
                    }
                }
                //System.out.println("Result Max normal: " + result);
                //Exact value. Store it.
                transTable.put(board, new ITState(board, bestMove, result, depth));
                return result;
            }


            //Min node
            else {
                result = Integer.MAX_VALUE;
                //Search best move first.
                if (bestMove != null) {
                    Side nextSide = side;
                    boolean nextMax = isMax;
                    Board tempBoard = Board.clone(board);
                    MoveResult moveResult = tempBoard.makeMove(bestMove, side);

                    //It's still min's turn.
                    if (moveResult.isAdditionalMove()) {
                        nextSide = flipSide(side);
                        nextMax = !isMax;
                        // System.out.println("Double Jump Found: " + nextSide + ", " + nextMax);
                    }

                    result = Math.min(result, minimaxITDeepening(tempBoard, depth - 1, flipSide(nextSide),
                            alpha, beta, !nextMax));
                    beta = Math.min(beta, result);

                    if (beta <= alpha) {
                        //Alpha cut off, NULL MOVE STORE.
                        transTable.put(board, new ITState(tempBoard, null, result, depth));
                        return result;
                    }
                }
                for (Move move : moves) {
                    if (!move.equals(bestMove)) {
                        Side nextSide = side;
                        boolean nextMax = isMax;
                        Board tempBoard = Board.clone(board);
                        MoveResult moveResult = tempBoard.makeMove(move, side);

                        //It's still mins move.
                        if (moveResult.isAdditionalMove()) {
                            nextSide = flipSide(side);
                            nextMax = !isMax;
                            // System.out.println("Double Jump Found: " + nextSide + ", " + nextMax);
                        }
                        int tempResult = result;
                        result = Math.min(result, minimaxITDeepening(tempBoard, depth - 1, flipSide(nextSide),
                                alpha, beta, !nextMax));

                        //New best move found.
                        if (tempResult != result) {
                            bestMove = move;
                        }
                        beta = Math.min(beta, result);

                        if (beta <= alpha) {
                            //Alpha cut off. NULL MOVE STORE.
                            transTable.put(board, new ITState(board, null, result, depth));
                            return result;
                        }
                    }
                }

                //Exact result. Store it.
                transTable.put(board, new ITState(board, bestMove, result, depth));
                return result;
            }
        }
    }

    /**
     * Perform Quiescence search on a leaf node.
     * If there is a jump move present, we examine all jump moves until there are no more possible jumps.
     * Otherwise, we simply return the normal heuristic if the node is "quiet"
     *
     * @param board board to check
     * @param side  side of the player
     * @param alpha alpha value
     * @param beta  beta value
     * @param isMax true if maximizing player
     * @return result of the Quiescence search
     */
    private int quiesce(Board board, Player.Side side, int alpha, int beta, boolean isMax) {

        int result = heuristic(board, this.getSide());
        ArrayList<Move> moves = board.getAllPossibleJumpMoves(side);
        //No jumps, this position is quiet.
        if (moves.isEmpty()) {
            return result;
        }

        //Search the jumps, let's see what we got.
        //I can seperate min and max much nicer here because result has been estimated from the Heuristic.
        for (Move move : moves) {
            //Make the move and figure out who's going next
            Board tempBoard = Board.clone(board);
            MoveResult moveResult = tempBoard.makeMove(move, side);
            boolean nextMax = !isMax;
            Side nextSide = flipSide(side);

            //Keep the side the same. We are going again.
            if (moveResult.isAdditionalMove()) {
                nextMax = !nextMax;
                nextSide = side;
            }

            if (isMax) {
                result = Math.max(result, quiesce(tempBoard, flipSide(nextSide), alpha, beta, !nextMax));
                alpha = Math.max(alpha, result);
                if (beta <= alpha) {
                    return result;
                }
            } else {
                result = Math.min(result, quiesce(tempBoard, flipSide(nextSide), alpha, beta, !nextMax));
                beta = Math.min(beta, result);
                if (beta <= alpha) {
                    return result;
                }
            }

        }
        return result;
    }

    /**
     * Performs the heurstic function on a specified board.
     * The side parameter is the side that will be considered as the Max player.
     *
     * @param board board to use
     * @param side  side to use for Max
     * @return heuristic score of the board as an int
     */
    public int heuristic(Board board, Player.Side side) {
        return this.heuristic3(board, side);
    }

    /**
     * First Heuristic Function
     * Gives a score based on the number of pieces on each side.
     *
     * @param board board to score
     * @param side  max player.
     * @return h1(board) as an int
     */
    private int heuristic1(Board board, Player.Side side) {
        int score = 0;
        int timesRed = 1;
        int timesBlack = -1;

        if (side == Player.Side.BLACK) {
            timesRed = -1;
            timesBlack = 1;
        }
        //+1 if RED, -1 if black
        score = score + (timesRed * board.getNumRed() * 5);
        score = score + (timesRed * board.getNumRedKing() * 15);
        score = score + (timesBlack * board.getNumBlack() * 5);
        score = score + (timesBlack * board.getNumBlackKing() * 15);

        return score;
    }

    /**
     * Second Heuristic Function
     * Scores the board based on the jumps that can be made by both players.
     * More emphasis is put on if a king is captured over a normal piece.
     *
     * @param board board to use.
     * @param side  max player
     * @return h2(board) as an int
     */
    private int heuristic2(Board board, Player.Side side) {
        int score = 0;

        //Now we need jump moves for the maximizing side.
        ArrayList<Move> maxJumpMoves = board.getAllPossibleJumpMoves(side);
        for (Move jump : maxJumpMoves) {
            int midRow = (jump.getStartRow() + jump.getEndRow()) / 2;
            int midCol = (jump.getStartCol() + jump.getEndCol()) / 2;
            Board.Piece midPiece = board.getPiece(midRow, midCol);

            //We jumped a king. Give it more points.
            //Like before, we give kings a score of X3 of normal piece.
            if (midPiece == Board.Piece.BLACK_KING || midPiece == Board.Piece.RED_KING) {
                score = score + 6;
            } else {
                score = score + 2;
            }
        }
        Player.Side opponent;

        if (side == Player.Side.BLACK) {
            opponent = Player.Side.RED;
        } else {
            opponent = Player.Side.BLACK;
        }

        ArrayList<Move> minJumpMoves = board.getAllPossibleJumpMoves(opponent);
        for (Move jump : minJumpMoves) {
            int midRow = (jump.getStartRow() + jump.getEndRow()) / 2;
            int midCol = (jump.getStartCol() + jump.getEndCol()) / 2;
            Board.Piece midPiece = board.getPiece(midRow, midCol);

            //We jumped a king. Give it more points.
            //Like before, we give kings a score of X3 of normal piece.
            if (midPiece == Board.Piece.BLACK_KING || midPiece == Board.Piece.RED_KING) {
                score = score - 6;
            } else {
                score = score - 2;
            }
        }

        //Now we need all additional jump moves for each side.
        score = score + board.getAdditionalJumpMoves(board, maxJumpMoves, side);
        score = score - board.getAdditionalJumpMoves(board, minJumpMoves, opponent);
        return score;
    }

    /**
     * Heuristic 3
     * H1 + H2 + The following
     * Player's piece on opponent side:
     * Opponent piece on our side:
     * Piece in opponent's end, Opponent piece in our end
     * Piece on "edge" (outer edges, not counting the end rows)
     * Player piece on our side:, or Piece on Opponent's side.
     *
     * @param board      board to use
     * @param playerSide player to act as max
     * @return result of heuristic 3 as an int
     */
    private int heuristic3(Board board, Player.Side playerSide) {
        //H1 + H2
        int result = heuristic1(board, playerSide);
        result = result + heuristic2(board, playerSide);

        //Red starts at 0, black is at 7.
        //Player's side calculations.
        //Check the end rows
        //First, check the Red End Row.
        int row = 0;
        result = result + checkEndRow(row, board, playerSide);
        //Black End Row
        row = 7;
        result = result + checkEndRow(row, board, playerSide);

        //Check Edges: Column 0 and Column 7
        //Ignore end corners, because we already counted those.
        //Column 0.
        int col = 0;
        result = result + checkEdges(col, board, playerSide);
        //Column 7.
        col = 7;
        result = result + checkEdges(col, board, playerSide);

        //Check Territories.
        //Black Side = [4,1-7] -> [6, 1-7]
        //Red side = [0, 1-7= -> [3, 1-7]
        //Check black side
        result = result + checkTerritory(board, playerSide, true);
        //Check red side
        result = result + checkTerritory(board, playerSide, false);
        return result;
    }

    /**
     * Check the end rows of each side
     * See Heuristic 3 for details.
     *
     * @param row        row to check. 0 for Red, 7 for Black. Any other value will return -1
     * @param board      board to use
     * @param playerSide side of player being evaluated.
     * @return result of the check as an int
     */
    private int checkEndRow(int row, Board board, Player.Side playerSide) {
        //Red Row.
        int val1 = 1; //We are red and piece is red
        int val2 = -4;//We are red and piece is black
        int val3 = -1; //We are black and piece is red
        int val4 = 4;//We are black and piece is black.
        //Values defaulted to red row.
        int result = 0;

        //Error.
        if (row != 0 && row != 7) {
            throw new IllegalArgumentException("Error on Check End Row. Row Num = " + row);
        }

        //Black row.
        else if (row == 7) {
            val1 = 4;  //We are red and piece is red
            val2 = -1; //We are red and piece is black.
            val3 = -4; //We are black and piece is red.
            val4 = 1; //We are black and piece is black.
        }

        //We don't care about kings because if a king is in the end row it doesn't do much.
        //If a normal piece is in an end row however, it makes a HUGE difference.
        for (int j = 0; j < 8; j++) {
            Board.Piece currPiece = board.getPiece(row, j);
            if (playerSide == Player.Side.RED) {
                if (currPiece == Board.Piece.RED) {
                    result = result + val1;
                } else if (currPiece == Board.Piece.BLACK) {
                    result = result + val2;
                }
            } else {
                //Flipped.
                if (currPiece == Board.Piece.RED) {
                    result = result + val3;
                } else if (currPiece == Board.Piece.BLACK) {
                    result = result + val4;
                }
            }
        }
        return result;
    }

    /**
     * Check the edges of the board. These are the 2 outside columns of the board.
     * See Heuristic 3 for details.
     *
     * @param column     column to use. Either 0 or 7.
     * @param board      board to use
     * @param playerSide side to check
     * @return result of the evaluation as an int.
     */
    private int checkEdges(int column, Board board, Player.Side playerSide) {
        int i = 1;
        int result = 0;
        if (column != 0 && column != 7) {
            throw new IllegalArgumentException("Error at checkEdges. Column: " + column);
        }
        while (i < 7) {
            Board.Piece currPiece = board.getPiece(i, column);
            if (playerSide == Player.Side.RED) {
                switch (currPiece) {
                    case RED:
                        result = result + 2;
                        break;
                    case RED_KING:
                        result = result + 4;
                        break;
                    case BLACK:
                        result = result - 2;
                        break;
                    case BLACK_KING:
                        result = result - 4;
                        break;
                }
            } else {
                switch (currPiece) {
                    case RED:
                        result = result - 2;
                        break;
                    case RED_KING:
                        result = result - 4;
                        break;
                    case BLACK:
                        result = result + 2;
                        break;
                    case BLACK_KING:
                        result = result + 4;
                        break;
                }
            }
            i++;
        }
        return result;
    }

    /**
     * Check the territories for both sides that doesn't include the edges and final row.
     * If a friendly piece is in enemy territory, give it 2 points.
     *
     * @param board      board to use
     * @param playerSide side of the max player
     * @param isBlack    if its the black or red territory
     * @return result of the territory score as an int
     */
    private int checkTerritory(Board board, Side playerSide, boolean isBlack) {
        int result = 0;
        //Black Territory
        if (isBlack) {
            int row = 4;
            int endRow = 6;
            int endCol = 6;

            while (row <= endRow) {
                int col = 1;

                while (col <= endCol) {
                    Board.Piece currPiece = board.getPiece(row, col);
                    //Since King's can move freely, we won't count kings in this count
                    if (playerSide == Side.RED && currPiece == Board.Piece.RED) {
                        result = result + 2;
                    } else if (playerSide == Side.BLACK && currPiece == Board.Piece.RED) {
                        result = result - 2;
                    }
                    col++;
                }
                row++;
            }
        }

        //Red Territory
        else {
            int row = 1;
            int endRow = 3;
            int endCol = 6;

            while (row <= endRow) {
                int col = 1;

                while (col <= endCol) {
                    Board.Piece currPiece = board.getPiece(row, col);
                    //Since King's can move freely, we won't count kings in this count.
                    if (playerSide == Side.BLACK && currPiece == Board.Piece.BLACK) {
                        result = result + 2;
                    } else if (playerSide == Side.RED && currPiece == Board.Piece.BLACK) {
                        result = result - 2;
                    }
                    col++;
                }
                row++;
            }
        }
        return result;
    }

    /**
     * Flip the entered side
     *
     * @param side side to flip
     * @return flipped side
     */
    private Player.Side flipSide(Player.Side side) {
        if (side == Side.BLACK) {
            return Side.RED;
        } else {
            return Side.BLACK;
        }
    }

    /*GETTERS AND SETTERS*/
    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getBotDifficulty() {
        return botDifficulty;
    }

    public void setBotDifficulty(int botDifficulty) {
        this.botDifficulty = botDifficulty;
    }

    public int getJumpCol() {
        return jumpCol;
    }

    public void setJumpCol(int jumpCol) {
        this.jumpCol = jumpCol;
    }

    public int getJumpRow() {
        return jumpRow;
    }

    public void setJumpRow(int jumpRow) {
        this.jumpRow = jumpRow;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public HashMap<Board, ITState> getTransTable() {
        return transTable;
    }

    public void setTransTable(HashMap<Board, ITState> transTable) {
        this.transTable = transTable;
    }
}