package Client;

import java.util.Vector;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
 * Ez a class arra van, hogy a szervertől érkező
 * JSON formátumú koordinátákat vissza tudjam alakítani objektummá.
 */
public class Coordinate {
    public int x;
    public int y;
    public Coordinate(int x, int y){
        this.x=x;
        this.y=y;
    }
    public Coordinate(){}

    /**
     * Felsorol minden koordinátát két egyvonalon lévő koordináta között.
     * @param a első koordináta
     * @param b második koordináta
     * @return A köztes koordinátákat tartalmazó vektor, benne van a és b is
     */
    static public Vector<Coordinate>coordinatesBetween(Coordinate a, Coordinate b){
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
        return new Vector<Coordinate>();
    }
}
