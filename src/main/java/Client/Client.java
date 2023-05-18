package Client;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import Graphics.Graphics;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Ez az osztály kezeli a szerverrel való kommunikácót,
 * attól függően hogy mit kap a szervertől hív meg függvényeket a Grafika.Grafika osztályból.
 * Rajta keresztül lehet üzenetet küldeni a szervernek, de üzenetet fogadni csak ő tud
 */
public class Client {
    int portNumber=1023;
    protected Socket clientSocket; //Socket a kapcsolathoz
    protected Graphics graphics;
    PrintWriter serverOutput;
    public String username; //A klienshez kapcsolódó username

    /**
     * A kliens osztály konstruktora, a grafika hívja meg.
     * Inicializálja a változókat. kezeli a kapcsolatokat.
     * @param host
     * @param graphics a futó grafikus interfacet kezeli
     * @throws IOException
     */
    public Client(String host, Graphics graphics) throws IOException {
        this.graphics=graphics;
        clientSocket = new Socket(host,  portNumber); //Kapcsolat létrehozása
        serverOutput= new PrintWriter(clientSocket.getOutputStream());
        final boolean[] CommunicationEnded = {false};
        // Az a szál ami figyeli hogy ír-e a szerver,
        // ez ellenőrzi azt is, hogy kell e zárni a kommmunikációt, és zárja ha igen
        Thread serverListener = new Thread(new Runnable(){
            final BufferedReader serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            @Override
            public void run() {
                String message=" ";
                while(true){
                    try {
                        message = getLine(serverInput);
                        handleMessage(message,serverInput); //Egy hatalmaaaas switch
                    } catch (IOException e) {
                        System.out.println("Connection failed with server");
                        System.out.println(message);
                        break;
                    }

                }
            }
        });
        serverListener.start();
    }

    /**
     * @param message
     * @param serverInput Ahhoz hogy ebben a függvényben lehessen olvasni a szervertől,
     *                    ugyanaz a szál mint a konstruktor
     * @throws IOException
     */
    private void handleMessage(String message,BufferedReader serverInput) throws IOException{
        String line;
        switch(message){
            case "Log in/ register phase":
                graphics.logIn();
                break;
            case "Log in/ Register error":
                String error=getLine(serverInput);
                graphics.logInError(error);
                break;
            case"Successful log in":
                graphics.succesfulLogIn();
                break;
            case"Connection phase":
                graphics.connectionPhase();
                break;
            case"Launch a game":
             graphics.newGame();
             break;
            case"Sending waiting games":
                getGameSettings(serverInput);
                break;
            case "Connection error":
                String errorMessage=getLine(serverInput);
                graphics.connectionError(errorMessage);
                break;
            case "Game launched":
                graphics.gameLaunched();
                break;
            case"Game phase":
                String JSONSettings=getLine(serverInput);
                sendLine("In a game");
                graphics.openGame(JSONSettings);
                break;
            case"Refresh player board":
                String playerShips=getLine(serverInput);
                String playerShots=getLine(serverInput);
                graphics.refreshPlayerBoard(playerShips,playerShots);
                break;
            case"Refresh enemy board":
                String enemyShips=getLine(serverInput);
                String enemyShots=getLine(serverInput);
                graphics.refreshEnemyBoard(enemyShips,enemyShots);
                break;
            case"Update ships to place":
                String shipsToPlace=getLine(serverInput);
                graphics.updateShipsToPlace(shipsToPlace);
                break;
            case "All ships placed":
                graphics.shipsPlaced();
                break;
            case "Your turn":
                graphics.yourTurn();
                break;
            case "Waiting for other user's turn":
                graphics.waitForTurn();
                break;
            case"Won":
                line=getLine(serverInput);
                graphics.win(line);
                break;
            case"Lost":
                line=getLine(serverInput);
                graphics.lose(line);
                break;
            case"Profile":
                line= getLine(serverInput);
                graphics.showProfile(line);
                break;
            case"Place submarine":
                String shipSize=getLine(serverInput);
                graphics.placeSubmarine(Integer.parseInt(shipSize));
                break;

        }
    }

    /**
     * Várja a játék beállításait és még JSON formában átadja a graphics-nak
     * @param serverInput
     * @throws IOException
     */
    private void getGameSettings(BufferedReader serverInput) throws IOException {
        String user1Name= getLine(serverInput);
        HashMap<String, String>allSettings=new HashMap<>();
        String end="End";
        while(!Objects.equals(user1Name, end)){
            String settings= getLine(serverInput);
            allSettings.put(user1Name,settings);
            user1Name=getLine(serverInput);
        }
        graphics.refreshGames(allSettings);
    }

    /**
     * Olvasás a szerver felől, automatikusan logol a konzolra
     * @param serverInput
     * @return
     * @throws IOException
     */
    private String getLine(BufferedReader serverInput) throws IOException{
        String message= serverInput.readLine();
        System.out.println("Server: "+ message);
        return message;
    }

    /**
     * Üzenet küld a szervernek, automatikusan logolva a konzolra
     * @param line
     */
    public void sendLine(String line){
        System.out.println(line);
        serverOutput.print(line + "\r\n");
        serverOutput.flush();
    }

    /**
     * Json-né alakít tetszőleges objektumot
     * @param object --ezt alakítja JSONné
     * @return String JSON
     * @throws JsonProcessingException
     */
    public static String toJSON(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String jsonString = mapper.writeValueAsString(object);
        return jsonString;
    }


}
