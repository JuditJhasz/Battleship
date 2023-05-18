package Graphics;

import java.util.HashMap;
import java.util.Map;

/**
 * Ahhoz kell, hogy JSON formátumból a kliens oldalon objektumot lehessen létrehozni
 */
public class Settings{
    public int size_n;
    public int size_m;
    public HashMap<Integer,Integer> ships; //Size, number
    public Settings(){

    }

    public Settings(int size_n, int size_m, HashMap<Integer, Integer> ships) {
        this.size_n = size_n;
        this.size_m = size_m;
        this.ships = ships;
    }
    public String toString(){
        String s=new String("Size of the board: "+ this.size_n+ "x"+this.size_m+"; ships: ");
        for(Map.Entry<Integer,Integer>ship:ships.entrySet()){
            s=s+"; size: "+ship.getKey()+"; pieces "+ship.getValue()+",";
        }
        return s;
    }
};
