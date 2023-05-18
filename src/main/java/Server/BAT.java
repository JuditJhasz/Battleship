package Server;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Objects;

/**
 * A kapcsolat létrejötte után ez az osztály kommunikál a kliensel, üzenetet fogadni csak ő tud.
 * Van egy user felhasználót reprezentáló objektuma, aminek meghívja a függvényeit a kliens üzenetei
 * alapján.
 * Rajta keresztül lehet üzenetet küldeni a kliensnek.
 */
public class BAT implements Runnable {
    protected Socket clientSocket;
    protected BufferedReader clientReader;
    protected PrintWriter clientWriter;
    protected BATServer batServer;
    protected User user;


    public BAT(Socket clientSocket, BATServer batServer) throws IOException {
        this.batServer = batServer;
        this.clientSocket = clientSocket;
        this.clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.clientWriter = new PrintWriter(clientSocket.getOutputStream());
    }

    protected void sendLine(String line) throws IOException {
        clientWriter.print(line + "\r\n");
        clientWriter.flush();
    }

    protected boolean expect(String line) throws IOException {
        String clientLine = clientReader.readLine();
        return clientLine.equals(line);
    }

    public void run() {
        try {
            //Belépési fázis
            sendLine("Log in/ register phase");
            boolean loggedIn=false;
            while(!loggedIn) {
                String line = clientReader.readLine();
                switch (line) {
                    case "Log in":
                        loggedIn=logIn();
                        break;
                    case "Register":
                        loggedIn=register();
                        break;
                }
            }
            //Játékhoz csatlakozás
            boolean connected=false;
            sendLine("Connection phase");
            refresh();
            while(!connected){
                String line=clientReader.readLine();
                switch (line) {
                    case "Connect":
                        connect();
                        break;
                    case "Refresh":
                        refresh();
                        break;
                    case "New Game":
                        newGame();
                        break;
                    case"In a game":
                        connected=true;
                        break;
                    case"Profile":
                        sendLine("Profile");
                        sendLine(BATServer.toJSON(user.statistics));
                        break;
                }

            }
            //Hajók lerakása
            boolean shipsPlaced=false;
            while(!shipsPlaced){
                String line=clientReader.readLine();
                switch (line){
                    case "Placing ship":
                        shipsPlaced=placeShip();
                        break;
                }
            }
            //Játék fázis
            while(true){
                String line=clientReader.readLine();
                ObjectMapper mapper = new ObjectMapper();
                String coordinateString;
                Coordinate coordinate;
                switch (line) {
                    case "Shooting":
                        coordinateString = clientReader.readLine();
                        coordinate = mapper.readValue(coordinateString, Coordinate.class);
                        user.shoot(coordinate);
                        break;
                    case "Moving submarine":
                        coordinateString=clientReader.readLine();
                        coordinate = mapper.readValue(coordinateString, Coordinate.class);
                        user.moveSubmarine(coordinate);
                        break;
                    case "Placing submarine":
                        String coordinateString1=clientReader.readLine();
                        Coordinate coordinate1 = mapper.readValue(coordinateString1, Coordinate.class);
                        String coordinateString2=clientReader.readLine();
                        Coordinate coordinate2 = mapper.readValue(coordinateString2, Coordinate.class);
                        user.placeSubmarine(coordinate1,coordinate2);
                        break;

                }
            }
        } catch (SocketException e) { //Annak a kezelése ha megszakad a kapcsolat egy felhasználóval.
            //Ekkor a felhasználó automatikusan veszít ha játékban volt
            System.out.println("A client closed the connection");
            if (user != null) {
                user.setActive(false);
                User otherUser=batServer.games.userExited(user.username);
                if(otherUser!=null){
                    otherUser.otherPlayerLeft();
                }

            }
        }
        catch(IOException e){
            if (user != null) {
                user.setActive(false);
                User otherUser=batServer.games.userExited(user.username);
                if(otherUser!=null){
                    otherUser.otherPlayerLeft();
                }
            }
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    public boolean newGame() throws IOException{
        sendLine("Launch a game");
        String settingsString=clientReader.readLine();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Settings newSettings = mapper.readValue(settingsString, Settings.class);
            batServer.games.addGameWaiting(new Game(newSettings, user));
            return true;
        }catch(JsonMappingException e){
            System.out.println("exception while converting settings back");
            e.printStackTrace();
            return false;
        }
    }
    public void refresh() throws IOException{
        sendLine("Sending waiting games");
        sendGames();
        sendLine("End");
    }
    boolean connect() throws IOException{
        String usernameToConnect=clientReader.readLine();
        Game game=batServer.games.gamesWaiting.get(usernameToConnect);
        if(game==null){
            sendLine("Connection error");
            sendLine("Game already started, please press the refresh button");
            return false;
        }
        else{
            batServer.games.gamesWaiting.remove(usernameToConnect);
            game.addUser2(user);
            batServer.games.addGameRunning(game);
            sendLine("Connection was successful");
            game.launchGame();
            return true;
        }
    }

    boolean logIn() throws IOException {
        String userName = clientReader.readLine();
        String password = clientReader.readLine();
        if (batServer.users.containsKey(userName)) {
            if(Objects.equals(batServer.users.get(userName).password, password)){
                user=batServer.users.get(userName);
                user.addBAT(this);
                if(user.active){
                    sendLine("Log in/ Register error");
                    sendLine("User is already logged in");
                    user=null;
                    return false;
                }
                sendLine("Successful log in");
                user.setActive(true);
                return true;
            }
        }
        sendLine("Log in/ Register error");
        sendLine("Can not find user-password pair, please try again");
        return false;
    }
    boolean register() throws IOException{
        String userName = clientReader.readLine();
        String password = clientReader.readLine();
        if(batServer.users.containsKey(userName)){
            sendLine("Log in/ Register error");
            sendLine("Username already exists, please choose another one");
            return false;
        }
        user=new User(userName,password,this);
        user.setActive(true);
        batServer.users.put(userName,user);
        sendLine("Successful log in");
        return true;
    }
    void sendGames() throws IOException {
        if(!batServer.games.gamesWaiting.isEmpty()){
            for(Map.Entry<String,Game>game: batServer.games.gamesWaiting.entrySet()){
                sendLine(game.getValue().getUser1Name());
                sendLine(BATServer.toJSON(game.getValue().getSettings()));
            }
        }
    }
    void otherPlayerLeft()throws IOException{
        sendLine("The other player left the game");
        user.game.gameEnded(user,"One of the players left the game");
    }
    boolean placeShip() throws IOException{
        String c1JSON= clientReader.readLine();
        String c2JSON= clientReader.readLine();
        String sizeJSON=clientReader.readLine();
        ObjectMapper mapper = new ObjectMapper();
        try {
            Coordinate c1 = mapper.readValue(c1JSON, Coordinate.class);
            Coordinate c2 = mapper.readValue(c2JSON, Coordinate.class);
            int size = mapper.readValue(sizeJSON, int.class);
            return user.placeShip(c1,c2,size);
        }
        catch(JsonMappingException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    void removeGame(Game game){
        batServer.removeGame(game);
    }


}
