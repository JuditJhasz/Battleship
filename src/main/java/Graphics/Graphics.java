package Graphics;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import Client.Client;

import java.io.IOException;
import java.util.HashMap;

/**
 * Ez a class kezeli az appot, hogy mikor milyen ablak legyen megjelenítve.
 * A kliens ezeket a függvényeket tudja csak meghívni.
 * Ez a class gondoskodik róla, hogy grafikai változtatás csak JAT szálon történjen.
 */
public class Graphics extends Application {

    Client client; //Referencia a klienshez
    LogInWindow logInWindow; //A belépést kezelő ablak
    ConnectionWindow connectionWindow; //A játékhoz csatlakozást kezelő ablak
    NewGameWindow newGameWindow; // Az új játék létrehozását kezelő ablak
    GameWindow gameWindow; //A futó játékot kezelő ablak
    Stage primaryStage;
    protected MenuBar menuBar; //Menü
    protected Menu mFile;
    protected MenuItem mExit; //Kijelentkezés
    public VBox root;


    /**
     * @param primaryStage
     * A JAT szál, kezdetben létrehozza a bejelentkező ablakot, és
     * inicializálja a beállításokat.
     */
    @Override
    public void start(Stage primaryStage)  {
        this.primaryStage=primaryStage;
        try {
            client=new Client("localhost",this);
        } catch (IOException e) {
            System.out.println("Error when initialising the client");
            System.out.println(e.getMessage());
        }
        root = new VBox(20);
        root.setFillWidth(true);
        primaryStage.setTitle("Battleship");
        primaryStage.setScene(new Scene(root, 1000,600));

        primaryStage.setResizable(true); // Az ablak ne legyen átméretezhető
        primaryStage.centerOnScreen(); // Az ablak a képernyő közepén legyen


        // A menüsor elkészítése
        menuBar = new MenuBar();
        mFile=new Menu("File");
        menuBar.getMenus().add(mFile);
        mExit=new MenuItem("Exit");
        mFile.getItems().addAll(mExit);

        root.getChildren().add(menuBar);
        root.getChildren().add(new Pane());

        // Eseménykezelés beállítása
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        mExit.setOnAction(event -> System.exit(0));

        changePane(new Pane(new Text("Can not connect to server")));
        // Az ablak megjelenítése
        primaryStage.show();
    }

    /**
     * JSONné alakít egy objektumot
     * @param object --ezt
     * @return
     * @throws JsonProcessingException
     */
    public static String toJSON(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String jsonString = mapper.writeValueAsString(object);
        return jsonString;

    }
    //Ablak megváltoztatása
    private void changePane(Pane newPane) {
        root.getChildren().set(1, newPane);
        VBox.setVgrow(newPane, Priority.ALWAYS);
    }

    public void logIn(){
        Graphics graphics=this;
        Runnable logInThread=new Runnable() {
            @Override
            public void run() {
                logInWindow=new LogInWindow(graphics, client);
                changePane(new Pane(logInWindow));
            }
        };
        Platform.runLater(logInThread);
    }
    public void connectionPhase(){
        Graphics graphics=this;
        Runnable logInThread=new Runnable() {
            @Override
            public void run() {
                connectionWindow=new ConnectionWindow(graphics,client);
                changePane(new Pane(connectionWindow));
            }
        };
        Platform.runLater(logInThread);
    }
    public void logInError(String error){
        Runnable logInThread=new Runnable() {
            @Override
            public void run() {
                logInWindow.logInError(error);
            }
        };
        Platform.runLater(logInThread);
    }

    public void succesfulLogIn(){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                logInWindow.succesfulLogIn();
            }
        };
        Platform.runLater(thread);
    }
    public void newGame(){
        Graphics graphics=this;
        Runnable logInThread=new Runnable() {
            @Override
            public void run() {
                newGameWindow=new NewGameWindow(graphics, client);
                changePane(new Pane(newGameWindow));
            }
        };
        Platform.runLater(logInThread);
    }
    public void refreshGames(HashMap<String,String> settings){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                connectionWindow.refreshGames(settings);
            }
        };
        Platform.runLater(thread);
    }
    public static void main(String[] args) {
        launch(args);
    }
    public void connectionError(String errorMessage){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                connectionWindow.connectionError(errorMessage);
            }
        };
        Platform.runLater(thread);
    }
    public void successfulConnection(String connectionMessage){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                changePane(new Pane(new Text(connectionMessage)));
            }
        };
        Platform.runLater(thread);
    }
    public void setTitle(String title){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                primaryStage.setTitle("Battleship, username:"+ title);
            }
        };
        Platform.runLater(thread);
    }
    public void gameLaunched(){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                changePane(new Pane(new Text("Waiting for another player to connect"))); //TODO
            }
        };
        Platform.runLater(thread);
    }
    public void openGame(String JSONSettings){
        Graphics graphics=this;
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Settings newSettings = mapper.readValue(JSONSettings, Settings.class);
                    gameWindow = new GameWindow(newSettings, client, graphics);
                    changePane(new Pane(gameWindow)); //TODO
                }
                catch(JsonProcessingException e){
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        Platform.runLater(thread);
    }
    public void refreshPlayerBoard(String JSONPlayerShips,String JSONPlayerShots){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                ObjectMapper mapper=new ObjectMapper();
                try {
                    Integer[][] playerShips=mapper.readValue(JSONPlayerShips,Integer[][].class);
                    Integer[][] playerShots=mapper.readValue(JSONPlayerShots,Integer[][].class);
                    gameWindow.refreshPlayerBoard(playerShips,playerShots);
                } catch (JsonProcessingException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        Platform.runLater(thread);
    }

    public void refreshEnemyBoard(String JSONEnemyShips,String JSONEnemyShots){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                ObjectMapper mapper=new ObjectMapper();
                try {
                    Integer[][] enemyShips=mapper.readValue(JSONEnemyShips,Integer[][].class);
                    Integer[][] enemyShots=mapper.readValue(JSONEnemyShots,Integer[][].class);
                    gameWindow.refreshEnemyBoard(enemyShips,enemyShots);
                } catch (JsonProcessingException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        Platform.runLater(thread);
    }
    public void updateShipsToPlace(String JSONShips){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                ObjectMapper mapper=new ObjectMapper();
                try {
                    HashMap<String, Integer> ships=mapper.readValue(JSONShips,HashMap.class);
                    gameWindow.refreshShipsToPlace(ships);
                } catch (JsonProcessingException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        Platform.runLater(thread);
    }
    public void shipsPlaced(){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                gameWindow.shipsPlaced();
            }
        };
        Platform.runLater(thread);
    }

    public void yourTurn() {
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                gameWindow.yourTurn();
            }
        };
        Platform.runLater(thread);
    }

    public void waitForTurn() {
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                gameWindow.waitForTurn();
            }
        };
        Platform.runLater(thread);
    }

    public void win(String line) {
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                gameWindow.win(line);
            }
        };
        Platform.runLater(thread);

    }

    public void lose(String line) {
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                gameWindow.lose(line);
            }
        };
        Platform.runLater(thread);
    }
    public void backToConnect(){
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                changePane(connectionWindow);
            }
        };
        Platform.runLater(thread);
    }

    public void showProfile(String line) {
        Graphics graphics=this;
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                changePane(new Profile(line,graphics));
            }
        };
        Platform.runLater(thread);
    }

    public void placeSubmarine(int shipSize) {
        Runnable thread=new Runnable() {
            @Override
            public void run() {
                gameWindow.placeSubmarine(shipSize);
            }
        };
        Platform.runLater(thread);
    }
}

