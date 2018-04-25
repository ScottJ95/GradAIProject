import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main Driver class. Handles user input and the overall game flow.
 *
 * @author Scott Jeffery
 * @version 4/17/2018
 */
public class Driver {
    private static Player currentPlayer;
    private static Board mainBoard;
    private static AI bot;
    private static AI bot2;
    private static Player player;
    private static ArrayList<Move> moveHistory;
    private static boolean finished = false;
    //Parameters for the bot matches.
    static int draws = 0;
    static int redWins = 0;
    static int blackWins = 0;
    static int games = 0;


    /**
     * Main driver method
     * Assumes perfect user input in some cases.
     *
     * @param args args
     * @throws Exception basic exception handling
     */
    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        System.out.println("\nWelcome to checkers!!!\n");
        System.out.println("Type BOT for a bot vs bot match. Or just press enter for a player match.");
        String decision = sc.nextLine().trim().toUpperCase();
        if (decision.equals("BOT")) {
            System.out.print("How many games would you like to simulate?: ");
            int maxGames = sc.nextInt();
            while (games < maxGames) {
                //Change debug to true if you want to see the search values.
                bot = new AI(Player.Side.BLACK, 4, false);
                bot2 = new AI(Player.Side.RED, 4, false);
                currentPlayer = bot;
                playBotMatch();
                games++;
            }
            System.out.println("Draws: " + draws);
            System.out.println("Red Wins: " + redWins);
            System.out.println("Black Wins: " + blackWins);
            return;

        }

        System.out.println("Choose a side: RED or BLACK");
        String side = sc.nextLine().trim().toUpperCase();
        boolean firstValid = false;
        while (!firstValid) {
            if (side.equals("RED")) {
                player = new Player(Player.Side.RED);
                currentPlayer = null;
                firstValid = true;
            } else if (side.equals("BLACK")) {
                player = new Player(Player.Side.BLACK);
                currentPlayer = player;
                firstValid = true;
            } else {
                System.out.println("Uh yeah that didn't work. Please try again.");
                System.out.println("Choose a side: RED or BLACK");
                side = sc.nextLine().trim().toUpperCase();

            }
        }

        System.out.println("Choose difficulty. 0:Random. 1:Easy 2:Medium 3:Hard 4:Harder.");
        int difficulty = sc.nextInt();
        while (difficulty < 0 && difficulty > 4) {
            System.out.println("That's not valid try again.\n");
            System.out.println("Choose difficulty. 0: Random. 1: Easy 2: Medium 3: Hard 4: Harder.");
            difficulty = sc.nextInt();
        }
        //Make debug true for debug output.
        bot = new AI(flipSide(player.getSide()), difficulty, false);

        if (currentPlayer == null) {
            currentPlayer = bot;
        }


        System.out.println("Choose Print Style: 1: X's and O's.  2: B's and R's.");
        int style = sc.nextInt();
        while(style < 1 && style > 2){
            System.out.println("That's not valid try again.\n");
            System.out.println("Choose Print Style: 1: X's and O's 2: B's and R's.");
            style = sc.nextInt();
        }
        //Value to use X's and O's instead of R and B.
        boolean useX = false;
        if(style == 1) {
            useX = true;
        }

        //Fixes scanner
        sc.nextLine();
        Thread.sleep(1000);
        System.out.println("Black always moves first. After that, players take turns.");
        System.out.println("If 30 moves are made with no captures, the Bot will offer to end the game.\n");
        mainBoard = new Board(useX);
        moveHistory = new ArrayList<Move>();
        int movesMade = 0;


        while (!finished) {
            int numRed = mainBoard.getTotalRed();
            int numBlack = mainBoard.getTotalBlack();
            System.out.println(mainBoard.toString());
            printMoveHistory();
            //System.out.println("Heuristic: " + bot.heuristic(mainBoard, bot.getSide()));
            System.out.println("Black Pieces left: " + mainBoard.getTotalBlack());
            System.out.println("Red Pieces left: " + mainBoard.getTotalRed());
            Thread.sleep(1500);

            if (movesMade >= 30) {
                System.out.println("The bot would like to offer a draw.");
                System.out.print("Do you accept? YES or NO? ");
                boolean inputValid = false;
                while (!inputValid) {
                    String in = sc.nextLine().trim().toUpperCase();
                    if (in.equals("YES")) {
                        System.out.println("The game has ended!");
                        finished = true;
                        inputValid = true;
                        System.out.println("Thanks for playing!");
                    } else if (in.equals("NO")) {
                        System.out.println("Ok, returning to game.");
                        movesMade = 0;
                        inputValid = true;
                    } else {
                        System.out.println("Error try again. YES or NO?");
                    }
                }
            }
            if(useX){
                if(currentPlayer.getSide().equals("RED")){
                    System.out.println("It's " + currentPlayer.getSide() + "'s (X's) turn\n");
                }
                else{
                    System.out.println("It's " + currentPlayer.getSide() + "'s (O's) turn\n");
                }

            }
            else{
                System.out.println("It's " + currentPlayer.getSide() + "'s turn\n");
            }
            //System.out.println("Moves Made Since Last Capture: " + movesMade + "\n");
            MoveResult result = null;
            Move lastMove = null;

            //Bot make a move.
            if (currentPlayer.getSide() == bot.getSide()) {
                result = bot.makeMove(mainBoard, moveHistory);
                finished = isGameFinished();
                checkResult(result, null);

                int newRed = mainBoard.getTotalRed();
                int newBlack = mainBoard.getTotalBlack();

                if (newRed != numRed || newBlack != numBlack) {
                    movesMade = 0;
                } else {
                    movesMade++;
                }
            }

            //Player make a move.
            else {
                boolean valid = false;
                while (!valid) {

                    System.out.print("Pick a piece (ex: A0) or type nothing to end the game: ");
                    char[] startMove = sc.nextLine().trim().toUpperCase().toCharArray();

                    if (startMove.length == 0) {
                        System.out.println("The game has ended.");
                        System.out.println("Thanks for playing!");
                        finished = true;
                        break;
                    }

                    if (isMoveInput(startMove)) {
                        System.out.print("Pick a spot to move (ex: A0): ");
                        char[] endMove = sc.nextLine().trim().toUpperCase().toCharArray();

                        if (isMoveInput(endMove)) {
                            int[] moveInput = parseMove(startMove, endMove);
                            //MOVE IS COL,ROW BECAUSE OF HOW THE BOARD IS PRINTED
                            lastMove = new Move(moveInput[0], moveInput[1], moveInput[2], moveInput[3]);
                            result = mainBoard.makeMove(lastMove, currentPlayer.getSide());
                            finished = isGameFinished();
                        } else {
                            System.out.println("Error, try again");
                        }
                    } else {
                        System.out.println("Error, try again");
                    }
                    valid = checkResult(result, lastMove);
                }
                int newRed = mainBoard.getTotalRed();
                int newBlack = mainBoard.getTotalBlack();

                if (newRed != numRed || newBlack != numBlack) {
                    movesMade = 0;
                } else {
                    movesMade++;
                }
            }
        }
    }

    /**
     * Play a bot vs bot match.
     */
    private static void playBotMatch() throws InterruptedException {
        Thread.sleep(1500);
        System.out.println("Black always moves first. After that, players take turns.");
        mainBoard = new Board();
        moveHistory = new ArrayList<Move>();
        //Number of moves made before we decide on a draw.
        int movesMade = 0;
        finished = false;

        while (!finished) {
            int numRed = mainBoard.getTotalRed();
            int numBlack = mainBoard.getTotalBlack();
            System.out.println(mainBoard.toString());
            printMoveHistory();
            //System.out.println("Heuristic: " + bot.heuristic(mainBoard, bot.getSide()));
            System.out.println("Black Pieces left: " + mainBoard.getTotalBlack());
            System.out.println("Red Pieces left: " + mainBoard.getTotalRed());
            Thread.sleep(200);
            System.out.println("Moves made: " + movesMade);
            //Check if the game should end in a draw.
            if (movesMade >= 50) {
                System.out.println("The bots have ended the game in a draw.");
                System.out.println("Checking the scores now.");
                if (mainBoard.getTotalBlack() > mainBoard.getTotalRed()) {
                    System.out.println("Black wins!!");
                    draws++;
                    blackWins++;
                } else if (mainBoard.getTotalRed() > mainBoard.getTotalBlack()) {
                    System.out.println("Red wins!!");
                    draws++;
                    redWins++;
                } else {
                    System.out.println("This match has ended in a draw!");
                    draws++;
                }
                finished = true;
                break;
            }
            System.out.println("It's " + currentPlayer.getSide() + "'s turn\n");

            MoveResult result = null;
            Move lastMove = null;

            //Bot1 make a move.
            if (currentPlayer.getSide() == bot.getSide()) {
                result = bot.makeMove(mainBoard, moveHistory);
                finished = isGameFinished();
                checkResult(result, null);
                if (result.isCompleted() && !result.isAdditionalMove()) {
                    currentPlayer = bot2;
                }
                int newRed = mainBoard.getTotalRed();
                int newBlack = mainBoard.getTotalBlack();

                if (newRed != numRed || newBlack != numBlack) {
                    movesMade = 0;
                } else {
                    movesMade++;
                }
            }

            //Bot2 make a move
            else {
                result = bot2.makeMove(mainBoard, moveHistory);
                finished = isGameFinished();
                checkResult(result, null);
                if (result.isCompleted() && !result.isAdditionalMove()) {
                    currentPlayer = bot;
                }
                currentPlayer = bot;

                int newRed = mainBoard.getTotalRed();
                int newBlack = mainBoard.getTotalBlack();

                if (newRed != numRed || newBlack != numBlack) {
                    movesMade = 0;
                } else {
                    movesMade++;
                }
            }
        }
    }

    /**
     * Checks to see if the game is finished
     *
     * @return true if game is over.
     */
    private static boolean isGameFinished() {
        boolean finished = false;

        if (mainBoard.getTotalBlack() == 0) {
            System.out.println(mainBoard.toString());
            System.out.println("No more black pieces! Red wins!");
            finished = true;
            redWins++;
        } else if (mainBoard.getTotalRed() == 0) {
            System.out.println(mainBoard.toString());
            System.out.println("No more red pieces! Black wins!");
            finished = true;
            blackWins++;
        } else if (mainBoard.getAllPossibleMoves(Player.Side.BLACK).isEmpty()) {
            System.out.println(mainBoard.toString());
            System.out.println("Black cannot move! Red Wins!");
            finished = true;
            blackWins++;
        } else if (mainBoard.getAllPossibleMoves(Player.Side.RED).isEmpty()) {
            System.out.println(mainBoard.toString());
            System.out.println("Red cannot move! Black Wins!");
            finished = true;
            redWins++;
        }
        return finished;
    }

    /**
     * Check the result of the last move made on the board
     *
     * @param result   result of the previous move
     * @param lastMove last move that was made
     * @return true if the move was valid.
     * @throws InterruptedException exception for thread sleep.
     */
    private static boolean checkResult(MoveResult result, Move lastMove) throws InterruptedException {
        boolean valid = false;
        if (result != null && result.isCompleted() && !result.isAdditionalMove()) {
            if (currentPlayer == player) {
                moveHistory.add(lastMove);
            }
            valid = true;
            result.printResult();

            if (currentPlayer == player) {
                currentPlayer = bot;
            } else {
                currentPlayer = player;
            }
        } else if (result != null && result.isCompleted() && result.isAdditionalMove()) {
            if (currentPlayer == player) {
                moveHistory.add(lastMove);
            }
            System.out.println(mainBoard.toString());
            printMoveHistory();
            result.printResult();
            Thread.sleep(2000);

        } else if (result != null && result.isCannotMove()) {
            result.printResult();
            finished = true;
            return true;
        } else if (result != null) {
            result.printResult();
        }
        return valid;
    }

    /**
     * Check to make sure the input is correct format.
     *
     * @param move move to process
     * @return true if move is valid
     */
    private static boolean isMoveInput(char[] move) {
        if (move.length != 2) {
            return false;
        }
        if (move[0] >= 'A' && move[0] <= 'H') {
            if (move[1] >= '0' && move[1] <= '7') {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Parse the move made by the input
     *
     * @param startMove start move
     * @param endMove   end move
     * @return coordinates to use for the move. int[]
     */
    private static int[] parseMove(char[] startMove, char[] endMove) {
        int[] result = new int[4];

        result[0] = startMove[0] - 65;
        result[1] = Character.getNumericValue(startMove[1]);
        result[2] = endMove[0] - 65;
        result[3] = Character.getNumericValue(endMove[1]);
        return result;
    }

    /**
     * Print the current move history.
     */
    private static void printMoveHistory() {
        System.out.print("Move History: ");
        for (Move move : moveHistory) {
            if (move != null) {
                System.out.print(move.toString() + " ");
            }
        }
        System.out.println("");
    }

    /**
     * Flip the entered side
     *
     * @param side side to flip
     * @return flipped side
     */
    private static Player.Side flipSide(Player.Side side) {
        if (side == Player.Side.BLACK) {
            return Player.Side.RED;
        } else {
            return Player.Side.BLACK;
        }
    }
}