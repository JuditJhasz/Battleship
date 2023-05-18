package Server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

/**
 * Ez az osztály kezeli a játékot, azokat az információkat amik mind a két játékosra érvényesek
 * pl beállítások.
 * Van benne referencia a két játékosra és van egy függvény
 * ami egy játékos alapján megadja a másik játlkost
 */
public class Game {

    Settings settings;
    @JsonIgnore
    User userInTurn=null;
    @JsonIgnore
    User user1;  //Az a játékos aki a játékot indította
    @JsonIgnore
    User user2; //Az utólag csatlakozó játékos
    @JsonIgnore
    boolean over=false;
    @JsonProperty("username")
    public String getUser1Name(){
        return user1.username;
    }

    public Settings getSettings() {
        return settings;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public Game(int size_n, int size_m, User user1, HashMap<Integer, Integer> ships) {
        settings= new Settings(size_n,size_m,ships);
        this.user1 = user1;
        user1.gameID=2;
        user1.game=this;
    }
    public Game(Settings settings, User user1) {
        this.settings= settings;
        this.user1 = user1;
        user1.gameID=2;
        user1.game=this;
    }

    /**
     * Akkor hívódik meg amikor egy elindított játékhoz csatlakozik egy második játékos
     * @param user2 A csatlakozó játékos
     */
    public void addUser2(User user2){
        this.user2 = user2;
        user2.gameID=1;
        user2.game=this;
    }
    public void gamePlay(){
        while(!over){
            PlayerTurn(user1);
            PlayerTurn(user2);
        }
    }
    void PlayerTurn(User user){

    }
    public String toJSon() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String jsonString = mapper.writeValueAsString(this);
        return jsonString;
    }

    public User otherUser(User user){
        if(this.user1==user){
            return user2;
        }
        return user1;
    }
    void launchGame()throws IOException {
        user1.sendLine("Game phase");
        user2.sendLine("Game phase");
        String JSONsettings=BATServer.toJSON(settings);
        user1.sendLine(JSONsettings);
        user2.sendLine(JSONsettings);
        user1.addGameState(settings);
        user2.addGameState(settings);
    }

    public void startGame() throws IOException{
        user1.turn();
        user2.waitForTurn();
        userInTurn=user1;
    }
    public void changeTurn() throws IOException {
        userInTurn.waitForTurn();
        userInTurn=otherUser(userInTurn);
        userInTurn.turn();
    }
    public void gameEnded(User winner,String note) {
        User loser=otherUser(winner);
        try {
            winner.win(note);
            loser.lose(note);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        winner.bat.removeGame(this);
        Statistic w=new Statistic(loser.username, false,note);
        Statistic l=new Statistic(winner.username,true, note);
        winner.addStatistic(w);
        loser.addStatistic(l);
        winner.userGameState=null;
        winner.game=null;
        loser.userGameState=null;
        loser.game=null;

    }

}
