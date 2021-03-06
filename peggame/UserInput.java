package peggame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.crypto.BadPaddingException;

import peggame.GameBoard.BoardType;
import peggame.backtracker.Backtracker;
import peggame.backtracker.Configuration;


public class UserInput{
    /**
     * Constructor for extant board
     */
    private PegGame board;
    public UserInput(PegGame board){
        this.board = board;//the board for the userinput class to manipulate
    }

    /**
     * Constructor to create class without board
     * (Mainly used for when reading board from file)
     */
    public UserInput(){
    
    }

    /**
     * Getter for userinput board
     * @return Returns the userinput gameboard
     */
    public PegGame getBoard() {
        return board;
    }


    /**
     * Sets the board to the board found in a text document
     * Gets the number of rows from the first line of document
     * Gets the number of colums from the length of the second line
     * Creates a new board, which is defaultly empty
     * Fills any location with a o in the text document with a peg
     * @param filename File location of text version of game board
     */
    public void initiateboard(String filename){
        //use the filename to add pegs to the board corresponding to file contents.
        //maybe return a tostring of the new board.(good for testing/debugging etc)

        GameBoard board = null;
        try(
            FileReader file = new FileReader(filename);
            BufferedReader reader = new BufferedReader(file);
        ){
            String rowString = reader.readLine();
            int row = Integer.parseInt(rowString);
            String line = reader.readLine();
            int col = line.length();
            reader.mark(col*2);
            String second = reader.readLine();
            int sec = second.length();
            if(sec-col==1){
                board = new GameBoard(row);
            } else{
                board = new GameBoard(row, col);
            }

            reader.reset();
            int countRow = 0;
            while (true){
                if (line == null){
                    break;
                } else{
                    String[] pegs = line.split("");
                    for (int i=0; i<pegs.length; i++){
                        if(pegs[i].equals("o")){
                            board.addPeg(new Location (countRow, i));
                        }
                    }
                }
                line = reader.readLine();
                countRow += 1;
            }


        }catch(IOException e){
            System.out.println("Bad Filename");
        }
        board.updateGameState(GameState.IN_PROGRESS);
        this.board = board;
        board.analyzeState();

    }

    /**
     * Simple method for intrepreting different commands
     * @param s Provided command
     * @return Boolean used for quitting
     */
    public boolean interpretCommand(String s){
        String[] multiple = s.split(" ");
    
        if(s.equals("help")){
            printCommands();
        }
        else if (multiple[0].equals("move")){//if first word is "move", process logic to make a move
            int r1 = Integer.parseInt(multiple[1]);
            int c1 = Integer.parseInt(multiple[2]);
            int r2 = Integer.parseInt(multiple[3]);
            int c2 = Integer.parseInt(multiple[4]);
            Location from = new Location(r1, c1);
            Location to = new Location(r2, c2);

            Move move = new Move(from, to);
            
            try{
                board.makeMove(move);

            }catch(PegGameException e){
                System.out.println(e);
            }
           
           
        }
        else if(s.equals("hint")){
            //manually making a copy without copying moves, so we can pull the first one
            GameBoard[] ref = {new GameBoard(board.getCols())};
            if(board.getShape() == BoardType.RECTANGLE){
                ref[0] = new GameBoard(board.getRows(), board.getCols());
            }
            GameBoard copy = ref[0];
            board.getBoard().forEach((loc, bool) -> copy.getBoard().put(loc,bool));
            copy.setNumPegs(board.getNumPegs());

            //solve the board
            Backtracker bt = new Backtracker(false);
            Configuration sol = bt.solve(new PeggameSolver(copy));
            if(sol == null){
                System.out.println("This game is unwinnable. Have a nice day.");
                return true;
            }
            Collection<Move> solvingmoves = sol.getsolution();
            Iterator<Move> solmoves = solvingmoves.iterator();
            System.out.println(solmoves.next());
        }
        else if(s.equals("quit")){
            System.out.println("quitting the program...");
            board.updateGameState(GameState.NOT_STARTED);
            return false;
        }
        //make all the moves the solving backtracker algorithm finds
        else if(s.equals("solve")){
            GameBoard[] ref = {new GameBoard(board.getCols())};
            if(board.getShape() == BoardType.RECTANGLE){
                ref[0] = new GameBoard(board.getRows(), board.getCols());
            }
            GameBoard copy = ref[0];
            board.getBoard().forEach((loc, bool) -> copy.getBoard().put(loc,bool));
            copy.setNumPegs(board.getNumPegs());//manually making a copy without copying moves

            Backtracker bt = new Backtracker(false);
            Configuration sol = bt.solve(new PeggameSolver(copy));
            if(sol == null){
                System.out.println("There is no way for you solve this board.");
                return true;
            }
            Collection<Move> solvingmoves = sol.getsolution();
            for(Move move : solvingmoves){
                try{//catch exception for make move. make each move in list
                    System.out.println(move);
                    board.makeMove(move);
                    if(board.getNumPegs() > 1){
                        System.out.println(board);
                    }
                }catch(PegGameException e){
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * Help command - Prints out all possible commands, how to use them and what they do
     */
    public static void printCommands(){
        System.out.println("Available Commands: ");
        System.out.println("help - displays this message");
        System.out.println("move r1 c1 r2 c2 - attempts to move a peg from r1 c1 to r2 c2 on the board.");
        System.out.println("hint - displays a move to help you solve.");
        System.out.println("quit - quits the game.");
        System.out.println("solve - solves the game for you.");
    }
}