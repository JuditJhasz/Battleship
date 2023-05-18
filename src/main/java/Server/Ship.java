package Server;

/**
 * Egy hajót reprezentál, tudja magáról hogy mekkora, hol van lerakva,
 * és hogy megsérült e valamint hogy hol
 */
public class Ship {
    Coordinate coord1;
    Coordinate coord2;
    int size;
    int ID;
    int injuries=0;
    boolean[]injuriesVector;

    public void getShot(){
        injuries++;
    }
    public boolean isShot(){
        for(int i=0;i<size;i++){
            if(!injuriesVector[i]){
                return false;
            }
        }
        return true;
    }
    public boolean isUnShot(){
        return injuries==0;
    }
    public Ship(int size, int ID,Coordinate c1, Coordinate c2) {
        coord1=c1;
        coord2=c2;
        this.size = size;
        this.ID = ID;
        injuriesVector=new boolean[size];
        for(int i=0;i<size;i++){
            injuriesVector[i]=false;
        }
    }
}
