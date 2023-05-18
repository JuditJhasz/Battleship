package Server;

import java.util.HashMap;

/**
 * A kliens is rendelkezik ilyen osztállyal, hogy JSON formátumban lehessen küldeni a socketen
 */
public class Settings{
    public int size_n; //Tábla mérete
    public int size_m; //tábla mérete
    public HashMap<Integer,Integer> ships; //Size, number
    public Settings(){

    }

    public Settings(int size_n, int size_m, HashMap<Integer, Integer> ships) {
        this.size_n = size_n;
        this.size_m = size_m;
        this.ships = ships;
    }
};
