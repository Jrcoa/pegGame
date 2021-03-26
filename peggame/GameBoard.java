package peggame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameBoard implements PegGame{

    private Map<Location, Boolean> board;
    private GameState state;
    private int rows;
    private int cols;
    public GameBoard(int rows, int cols){
        this.rows = rows;
        this.cols = cols;
        this.board = new HashMap<>();
        this.state = GameState.NOT_STARTED;

        for(int rowS = 0; rowS < rows; rowS++){
            for(int colS=0; colS <cols; colS++){
                Location location = new Location(rowS, colS);
                this.board.put(location, false);
            }
        }
    }
    public GameBoard(){
        this(4, 4);
    }
    @Override
    public Collection<Move> getPossibleMoves() {
        Set<Location> locs = board.keySet();
        Collection<Move> possibleMoves = new ArrayList<>();
        for (Location loc : locs){
            possibleMoves.addAll(getMoves(loc));
        }
        return possibleMoves;
    }

    @Override
    public GameState getGameState() {
        return this.state;
    }

    @Override
    public void makeMove(Move move) throws PegGameException {
        Location from = move.getFrom();
        Location to = move.getTo();

        Location middle = new Location((to.getRow() + from.getRow())/2 , (to.getCol() + from.getCol()) / 2);

        if(!board.containsKey(from) || !board.containsKey(to)){ //Checks if move is in board
            throw new PegGameException("Invalid move");
        }else if(!hasPeg(from)){
            throw new PegGameException("There is no peg to move");
        }else if(hasPeg(to)){
            throw new PegGameException("There is already a peg there");
        }else if(!hasPeg(middle)){
            throw new PegGameException("There is no peg to jump over");
        }else{
            removePeg(from);
            addPeg(to);
            removePeg(middle);
        }
    }

    public Collection<Move> getMoves(Location location){
        Collection<Move> moves = new ArrayList<>();
        String[] commands = {"L","R", "B", "T", "TR", "TL", "BR", "BL"};
        for (String command : commands){
            try{
                Location neighbor = location.getNeighbor(command);
                if (board.get(neighbor) && !board.get(neighbor.getNeighbor(command))){
                    moves.add(new Move(location, neighbor.getNeighbor(command)));
                }
            } catch (Exception e){
                continue;
            }
        }
        return moves;
    }

    public void addPeg(Location to){

        if(board.containsKey(to)){
            board.put(to, true);
        }
    }

    public void removePeg(Location from){

        if(board.containsKey(from)){
            board.put(from, false);
        }
    }
    public boolean hasPeg(Location location){
        return board.get(location);
    }
    
    @Override
    public String toString(){
        Set<Location> holes= board.keySet();
        List<Location> holelist = new ArrayList<>();
        for(Location hole: holes){
            holelist.add(hole);
        }
        holelist.sort( (a,b) -> (a.hashCode() - b.hashCode()));


        int index = 0;        
        String string = "";
        for(Location hole: holelist){
            if(board.get(hole) == true){
                string += "{.}";
            }
            else{
                string += "{o}";
            }
            index ++;
            if(index % cols == 0){

                string += "\n";
            }
        }
        return string;
    }
    public void updateGameState(GameState state){
        this.state = state;
    }
    
}
