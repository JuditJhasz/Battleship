package Server;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Egy játékos aktuális játékának belsü állapota
 */
public class UserGameState {
    boolean readyToStart=false;
    int boardSize; //A tábla mérete
    int enemyId=1; //A következőleg lerakható ellenfél hajó id-ja
    int id=1; //A következőleg lerakható saját hajó id-ja
    int[][] enemyShots; //Tárolja hogy hova lőtt az ellenfél és milyen eredménnyel:
                        //0 ha nem lőtt, -1 ha nem talált, 1 ha talált
    int[][] playerShips; //Jelzi hogy hol vannak a játékos saját hajói
                            //0 ha nincs hajó, id ha van
    int[][] playerShots;   //Jelzi hogy az adott játékos hova lőtt és milyen eredménnyel
    int[][] enemyShips; //Jelzi hogy hol vannak az ellenfél hajói
    HashMap<Integer,Integer>shipsToPlace; //size, number
    HashMap<Integer, Ship>shipsPlaced;//id, size, position, parts_ok
    BAT bat;
    User user;
    Game game;
    int shipsOkay=0; //A nem kilőtt és lerakott hajók száma
    int submarineID; //A felvett de még nem lerakott, "tengeralattjárót" jegyzi meg
    UserGameState(Settings settings,BAT bat,User user, Game game){
        this.user=user;
        this.game=game;
        this.bat=bat;
        boardSize=settings.size_m;
        shipsToPlace=new HashMap<>();
        shipsToPlace.putAll(settings.ships);
        shipsPlaced=new HashMap<>();
        enemyShips=new int[boardSize][boardSize];
        enemyShots=new int[boardSize][boardSize];
        playerShips=new int[boardSize][boardSize];
        playerShots=new int[boardSize][boardSize];
        for(int i=0;i<boardSize;i++){
            for(int j=0;j<boardSize;j++){
                enemyShips[i][j]=0;
                enemyShots[i][j]=0;
                playerShips[i][j]=0;
                playerShots[i][j]=0;
            }
        }
    }

    /**
     * Lerak egy hajót a playerShips mátrixba
     * @param coordinate1 //A hajó egyik végének koordinátája
     * @param coordinate2 //A hajó másik végének koordinátája
     * @param size         //A hajó mérete
     * @return             //Hogy a hajó letehető e az adott helyre (Nem ütközik e másik hajóval)
     * @throws IOException
     */
    public boolean placeShip(Coordinate coordinate1,Coordinate coordinate2, int size)throws IOException {
        shipsOkay++;
        shipsToPlace.computeIfPresent(size,(k,v)->v-1);
        shipsPlaced.put(id,new Ship(size,id,coordinate1,coordinate2));
        Vector<Coordinate>shipPos=Coordinate.coordinatesBetween(coordinate1,coordinate2);
        for(Coordinate c:shipPos ){
            playerShips[c.x][c.y]=id;
        }
        id++;
        refreshPlayerBoard();
        bat.sendLine("Update ships to place");
        bat.sendLine(BATServer.toJSON(shipsToPlace));
        for(Map.Entry<Integer,Integer>ships: shipsToPlace.entrySet()){
            if(ships.getValue()!=0){
                return false;
            }
        }
        bat.sendLine("All ships placed");
        this.readyToStart=true;
        return true;
    }
    void refreshPlayerBoard()throws IOException {
        bat.sendLine("Refresh player board");
        bat.sendLine(BATServer.toJSON(playerShips));
        bat.sendLine(BATServer.toJSON(playerShots));
    }
    void refreshEnemyBoard()throws IOException{
        bat.sendLine("Refresh enemy board");
        bat.sendLine(BATServer.toJSON(enemyShips));
        bat.sendLine(BATServer.toJSON(enemyShots));
    }
    void refreshAll()throws IOException{
        refreshEnemyBoard();
        refreshPlayerBoard();
    }

    /**
     * Berakja az ellenfél hajóját az enemyShip mátrixba: Amikor sikerült kilőni egy hajót
     * @param c1
     * @param c2
     */
    public void placeEnemyShip(Coordinate c1, Coordinate c2){
        Vector<Coordinate>shipPos=Coordinate.coordinatesBetween(c1,c2);
        for(Coordinate c:shipPos ){
            enemyShips[c.x][c.y]=id;
        }
        enemyId++;
    }

    /**
     * Ezt hívja meg a másik felhasználó amikor lő
     * @param c A lövés koordinátája
     */
    void getShot(Coordinate c){
        User otherUser=game.otherUser(user);
        if(playerShips[c.x][c.y]!=0){
            Ship shipShot=shipsPlaced.get(playerShips[c.x][c.y]);
            Vector<Coordinate>coordinates=Coordinate.coordinatesBetween(shipShot.coord1,shipShot.coord2);
            for (int i = 0; i <coordinates.size() ; i++) {
                if(c.x==coordinates.get(i).x && c.y==coordinates.get(i).y){
                    shipShot.injuriesVector[i]=true;
                }
            }
            this.playerShots[c.x][c.y]=1;
            Ship ship=shipsPlaced.get(playerShips[c.x][c.y]);
            ship.getShot();
            otherUser.answerToShoot(c,1);
            if(ship.isShot()){
                otherUser.shotOutShip(ship.coord1,ship.coord2);
                shipsOkay--;
                if(shipsOkay==0){
                    try {
                        otherUser.refreshEnemyBoard();
                        refreshPlayerBoard();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    game.gameEnded(otherUser,"All the ships has been sunk");
                    return;
                }
            }
        }
        else{
            this.playerShots[c.x][c.y]=-1;
            otherUser.answerToShoot(c,-1);
        }
        try {
            game.changeTurn();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            otherUser.refreshEnemyBoard();
            refreshPlayerBoard();
        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();
        }
    }

    /**
     * Ezt hívhatja meg a másik felhasználó hogy megüzenje hogy hogy sikerült a lövés (talált, nem talált)
     * @param c1 a lövés helye
     * @param isAHit Információ arról hogy talált e a lövés: 1 ha igen, -1 ha nem
     *               (A 0 azt jelöli ha nem volt lövés, ezért nem boolean)
     */
    public void answerToShoot(Coordinate c1, int isAHit) {
        enemyShots[c1.x][c1.y]=isAHit;
    }
    /**
     * Függvény ami felvesz egy hajót (submarine)
     * @param c A hajó egy tetszőleges pontjának koordinátája
     */
    public void moveSubmarine(Coordinate c) {
        int id=playerShips[c.x][c.y];
        submarineID=id;
        Ship ship=shipsPlaced.get(id);
        User otherUser=user.game.otherUser(user);
        otherUser.userGameState.removeEnemySubmarineShots(ship.coord1,ship.coord2);
        Vector<Coordinate>coordinates=Coordinate.coordinatesBetween(ship.coord1,ship.coord2);
        for(Coordinate coordinate:coordinates){
            playerShips[coordinate.x][coordinate.y]=0;
            playerShots[coordinate.x][coordinate.y]=0;
        }
        try {
            refreshPlayerBoard();
            bat.sendLine("Place submarine");
            bat.sendLine(Integer.toString(ship.size));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Eltávolítja a playerShots mátrixból az elenfél
     * éppen mozgatott tengeralattjáróját ért lövéseket
     * @param c1 A tengeralattjáró egyik végének koordinátája
     * @param c2 A tengeralattjáró másik végének koordinátája
     */
    private void removeEnemySubmarineShots(Coordinate c1, Coordinate c2) {
        Vector<Coordinate>shipPos=Coordinate.coordinatesBetween(c1,c2);
        for(int i=0;i<shipPos.size();i++){
           enemyShots[shipPos.get(i).x][shipPos.get(i).y]=0;
        }
    }

    /**
     * Függvény ami leteszi a tengeralattjárót
     * @param c1 A tengeralattjáró egyik végének koordinátája
     * @param c2 A tengeralattjáró másik végének koordinátája
     */
    public void placeSubmarine(Coordinate c1,Coordinate c2) throws IOException {
        Ship submarine=shipsPlaced.get(submarineID);
        Vector<Coordinate>shipPos=Coordinate.coordinatesBetween(c1,c2);
        for(int i=0;i<shipPos.size();i++){
            playerShips[shipPos.get(i).x][shipPos.get(i).y]=submarineID;
            if(submarine.injuriesVector[i]){
                playerShots[shipPos.get(i).x][shipPos.get(i).y]=1;
            }else{
                playerShots[shipPos.get(i).x][shipPos.get(i).y]=0;
            }
        }
        User otherUser=user.game.otherUser(user);
        if(submarine.isShot()){
            otherUser.userGameState.removeEnemyShip(submarine.coord1,submarine.coord2);
            otherUser.userGameState.placeEnemySubmarine(c1,c2,submarine.injuriesVector,submarineID);
        }else{
            otherUser.userGameState.placeEnemyShots(c1,c2,submarine.injuriesVector);
        }
        submarine.coord1=c1;
        submarine.coord2=c2;
        refreshPlayerBoard();
        user.game.changeTurn();
    }

    /**
     * Amikor az ellenfél mozgatta egy hajóját, lerakja az új helyre a hajót ért lövéseket
     * az enemyShots mátrixba
     * @param c1
     * @param c2
     * @param injuries
     */
    private void placeEnemyShots(Coordinate c1, Coordinate c2, boolean[] injuries) {
        System.out.println("injuries of ship:");
        Vector<Coordinate>coordinates=Coordinate.coordinatesBetween(c1,c2);
        for (int i=0;i<coordinates.size();i++) {
            System.out.println(injuries[i]+ ": pos: "+ i);
            if(injuries[i]==false) {
                enemyShots[coordinates.get(i).x][coordinates.get(i).y] = 0;
            }else{
                enemyShots[coordinates.get(i).x][coordinates.get(i).y] = 1;
            }

        }
        try {
            refreshEnemyBoard();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ha az ellenfél mozgatja a teljesen kilőtt tengeralattjáróját akkor
     * lerakja egy új helyre az enemyShips mátrixban
     * @param c1
     * @param c2
     * @param injuries
     * @param submarineID
     */
    private void placeEnemySubmarine(Coordinate c1, Coordinate c2, boolean[] injuries, int submarineID) {
        Vector<Coordinate>coordinates=Coordinate.coordinatesBetween(c1,c2);
        for (int i=0;i<coordinates.size();i++) {
            enemyShips[coordinates.get(i).x][coordinates.get(i).y]=id;
            if(injuries[i]==false) {
                enemyShots[coordinates.get(i).x][coordinates.get(i).y] = 0;
            }else{
                enemyShots[coordinates.get(i).x][coordinates.get(i).y] = 1;
            }
        }
        try {
            refreshEnemyBoard();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ha az ellenfél mozgat egy teljesen kilőtt hajót
     * @param c1
     * @param c2
     */
    private void removeEnemyShip(Coordinate c1, Coordinate c2) {
        Vector<Coordinate>coordinates=Coordinate.coordinatesBetween(c1,c2);
        for (Coordinate c:coordinates) {
            enemyShips[c.x][c.y]=0;
            enemyShots[c.x][c.y]=0;
        }
    }

}
