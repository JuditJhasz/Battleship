package Server;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Egy játékos statisztikája egy játékról, kilépéskor mentődik
 */
public class Statistic {
    public String opponent;
    public String result;
    @JsonIgnore
    public boolean win;
    public String note;

    public Statistic(){}
    public Statistic(String opponent, boolean win, String note) {
        this.opponent = opponent;
        this.win = win;
        this.note = note;
        if(win){
            result="won";
        }
        else {
            result="lost";
        }
    }
}
