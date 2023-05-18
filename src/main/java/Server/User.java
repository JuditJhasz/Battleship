package Server;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Vector;

/**
 * Egy felhasználót reprezentál, rendelkezik az adataival,
 * és egy userGameState-el ami tudja a játékos
 * állását az aktuális játékban
 */
public class User {
    @JsonIgnore
    boolean active;

    public String username;
    @JsonIgnore
    int gameID;
    @JsonIgnore
    UserGameState userGameState;
    @JsonIgnore
    Game game;
    public String password;
    @JsonIgnore
    BAT bat;

    public Vector<Statistic>statistics;
    public void addBAT(BAT bat){
        this.bat=bat;
    }
    public User(){}

    public User(String username, String password,BAT bat) {
        statistics=new Vector<>();
        this.username = username;
        this.password = password;
        this.bat=bat;
    }
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password=" + password +
                '}';
    }
    public String toJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String jsonString = mapper.writeValueAsString(this);
        return jsonString;

    }
    public void setActive(boolean active){
        this.active=active;
    }
    public void turn()throws IOException{
        bat.sendLine("Your turn");
    }
    public void win(String note)throws IOException{
        sendLine("Won");
        sendLine(note);
    }
    public void lose(String note)throws IOException{
        sendLine("Lost");
        sendLine(note);
    }

    /**
     * Akkor hívódik meg ha a másik játékos kilépett a játékból. ekkor nyert a bentmaradó játékos
     */
    public void otherPlayerLeft() {
        try {
            bat.otherPlayerLeft();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    public User otherUser(){
        return this.game.otherUser(this);
    }
    void sendLine(String line) throws IOException {
        bat.sendLine(line);
    }
    void addGameState(Settings settings){
        userGameState=new UserGameState(settings,bat,this, game);
    }

    /**
     * Lerak egy hajót a saját táblájára
     * @param c1 A hajó egyik koordinátája
     * @param c2 A hajó másik  koordinátája
     * @param shipSize A hajó mérete
     * @return Hogy elfogytak e a játékos lerakható hajói
     * @throws IOException
     */
    boolean placeShip(Coordinate c1, Coordinate c2, int shipSize)throws IOException{
        boolean isReady=userGameState.placeShip(c1,c2,shipSize);
        if(isReady){
            if(game.otherUser(this).isReadyToStart()){
                game.startGame();
            }
        }
        return isReady;
    }
    boolean isReadyToStart(){
        return this.userGameState.readyToStart;
    }

    public void waitForTurn() throws IOException {
        bat.sendLine("Waiting for other user's turn");
    }
    public void shoot(Coordinate c){
        game.otherUser(this).getShot(c);
    }
    public void getShot(Coordinate c){
        userGameState.getShot(c);
    }

    /**
     * Ez a függvény hívódik meg ha az aktuális felhasználó a lövésével elsüllyesztette a másik
     * felhasználó hajóját
     * @param c1 Az elsüllyesztett hajó egyik koordinátája
     * @param c2 Az elsüllyesztett hajó másik koordinátája
     */
    public void shotOutShip(Coordinate c1, Coordinate c2){
        userGameState.placeEnemyShip(c1,c2);
    }

    /**
     * Ezt hívhatja meg a másik felhasználó hogy megüzenje hogy hogy sikerült a lövés (talált, nem talált)
     * @param c1 a lövés helye
     * @param isAHit Információ arról hogy talált e a lövés: 1 ha igen, -1 ha nem
     *               (A 0 azt jelöli ha nem volt lövés, ezért nem boolean)
     */
    public void answerToShoot(Coordinate c1, int isAHit){
        userGameState.answerToShoot(c1, isAHit);
    }

    public void refreshEnemyBoard() throws IOException {
        userGameState.refreshEnemyBoard();
    }
    public void addStatistic(Statistic s){
        statistics.add(s);
    }

    /**
     * Függvény ami felvesz egy hajót (submarine)
     * @param coordinate A hajó egy tetszőleges pontjának koordinátája
     */
    public void moveSubmarine(Coordinate coordinate) {
        userGameState.moveSubmarine(coordinate);
    }

    /**
     * Függvény ami leteszi a tengeralattjárót
     * @param c1 A tengeralattjáró egyik végének koordinátája
     * @param c2 A tengeralattjáró másik végének koordinátája
     */
    public void placeSubmarine(Coordinate c1, Coordinate c2) {
        try {
            userGameState.placeSubmarine(c1,c2);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


}
