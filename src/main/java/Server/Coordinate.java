package Server;

import java.util.Vector;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
 * A koordinátákat kezelő osztály, létezik egy ugyanilyen a kliens oldalon is, hogy le
 */
public class Coordinate {
    public int x;
    public int y;
    Coordinate(){}
    Coordinate(int x, int y){
        this.x=x;
        this.y=y;
    }
    static Vector<Coordinate>coordinatesBetween(Coordinate a, Coordinate b){
        Vector<Coordinate>coordinatesBetween=new Vector<>();
        if(a.x==b.x){
            int smaller=min(a.y,b.y);
            int bigger=max(a.y,b.y);
            for (int i=smaller;i<=bigger;i++){
                coordinatesBetween.add(new Coordinate(a.x,i));
            }
            return coordinatesBetween;
        }
        if(a.y==b.y){
            int smaller=min(a.x,b.x);
            int bigger=max(a.x,b.x);
            for (int i=smaller;i<=bigger;i++){
                coordinatesBetween.add(new Coordinate(i,a.y));
            }
            return coordinatesBetween;
        }
        return null;
    }
}
