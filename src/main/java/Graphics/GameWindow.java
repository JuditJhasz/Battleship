package Graphics;

import Client.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 *Ez az ablak jeleníti meg a futó játékot, és kezeli az ezzel kapcsolatos opciókat:
 *  hajó mozgatása,
 *  lövés
 *  hajók lerakása
 */
public class GameWindow extends GridPane {
    int SubmarineMoves=1; //Hányszor lehet hajót mozgatni. Küldhetné a szerver de már nem volt időm megcsinálni
    private int boardSize; //A tábla mérete
    private Map<Integer, Integer> ships; //Lerakható hajók:<méret,darab>
    private Board enemyBoard; //Az ellenfél találatait jelző tábla
    private Board playerBoard; //Saját tábla
    private VBox shipSelectionBox; //Itt lehet hajót választani letevéshez
    HashMap<Integer, CheckBox> shipChooserHandler; //Legördülő lista hajó kiválasztásához
    Text turnMessage=new Text(); //Itt jelzem a játékkal kapcsolatos üzeneteket
    Settings settings; //A játék beállításai
    Graphics graphics;
    Client client;
    int sizeSelected; //A kiválasztott leteendő hajó mérete
    Button shoot=new Button("Shoot");
    Button submarine =new Button("Move submarine");

    //Inicializáció
    public GameWindow(Settings settings,Client client, Graphics graphics) {
        this.settings=settings;
        this.client=client;
        this.graphics=graphics;
        this.boardSize = settings.size_n;
        this.ships = settings.ships;
        this.enemyBoard = new Board(boardSize,client);
        this.playerBoard = new Board(boardSize,client);
        this.shipSelectionBox = new VBox();
        this.shipChooserHandler=new HashMap<>();
        this.setHgap(30);
        this.setVgap(30);

        initializeGridPanes();
        createShipSelection();

        // Set the layout for the GameWindow
        setPadding(new Insets(10));
        //setSpacing(10);

        // Add the components to the GameWindow
        this.add(new Text("The opponent's board"),0,0);
        this.add(new Text("Your board"),1,0);
        this.add(enemyBoard,0,1);
        this.add(playerBoard,1,1);
        this.add(shipSelectionBox,1,2);
    }

    public void yourTurn() {
        enemyBoard.enabled=true;
        shoot.setDisable(false);
        if(SubmarineMoves>0) {
            submarine.setDisable(false);
        }
        turnMessage.setText("Your turn");
    }
    public void waitForTurn() {
        turnMessage.setText("Waiting for the other player to shoot");
        shoot.setDisable(true);
        submarine.setDisable(true);
        enemyBoard.enabled=false;
    }

    private void initializeGridPanes() {
        // Set the layout for the GridPanes
        enemyBoard.setAlignment(Pos.CENTER);
        enemyBoard.setStyle( "-fx-grid-lines-visible: true;" );
        enemyBoard.setHgap(1);
        enemyBoard.setVgap(1);

        playerBoard.setAlignment(Pos.CENTER);
        playerBoard.setHgap(1);
        playerBoard.setVgap(1);
        playerBoard.setStyle( "-fx-grid-lines-visible: true;" );

        // Set the column and row constraints for the GridPanes
        for (int i = 0; i < boardSize; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / boardSize);
            enemyBoard.getColumnConstraints().add(colConstraints);
            playerBoard.getColumnConstraints().add(colConstraints);
        }

    }




    private void createShipSelection() {
        // Set the layout for the shipSelectionBox
        shipSelectionBox.setAlignment(Pos.TOP_LEFT);
        shipSelectionBox.setSpacing(5);
        shipSelectionBox.setPadding(new Insets(10));

        // Create checkboxes for ship selection
        for (Map.Entry<Integer, Integer> entry : ships.entrySet()) {
            int shipSize = entry.getKey();
            int shipCount = entry.getValue();

            CheckBox checkBox = new CheckBox();
            checkBox.setId(Integer.toString(shipSize));
            checkBox.setOnAction(event->checkBoxHit(checkBox));

            shipChooserHandler.put(shipSize,checkBox);
            checkBox.setDisable(shipCount == 0);

            Label label = new Label("length: "+shipSize+" ("+shipCount+" pieces)");

            // Add checkbox and label to the shipSelectionBox
            shipSelectionBox.getChildren().addAll(checkBox, label);
        }
    }
    void checkBoxHit(CheckBox checkBox){
        if(checkBox.isSelected()){
            sizeSelected=Integer.parseInt(checkBox.getId());
            playerBoard.waitForPlacement(sizeSelected, false);
            for(Map.Entry<Integer, CheckBox>checkBoxEntry: shipChooserHandler.entrySet()){
               if(checkBox.getId()!=checkBoxEntry.getValue().getId()){
                   checkBoxEntry.getValue().setSelected(false);
               }
            }
        }
    }
    void refreshPlayerBoard(Integer[][]playerShips, Integer[][]playerShots){
        playerBoard.refreshBoard(playerShips,playerShots);
    }
    void refreshEnemyBoard(Integer[][]ships, Integer[][]shots){
        enemyBoard.refreshBoard(ships,shots);
    }
    void refreshShipsToPlace(HashMap<String,Integer>ships){
        this.ships=new HashMap<>();
        shipSelectionBox.getChildren().clear();
        for (Map.Entry<String, Integer> entry : ships.entrySet()) {
            Integer shipSize=Integer.parseInt(entry.getKey());
            Integer shipCount = entry.getValue();
            this.ships.put(shipSize,shipCount);

            CheckBox checkBox = new CheckBox();
            checkBox.setId(Integer.toString(shipSize));
            checkBox.setOnAction(event->checkBoxHit(checkBox));

            shipChooserHandler.put(shipSize,checkBox);
            checkBox.setDisable(shipCount == 0);

            Label label = new Label("length: "+shipSize+" ("+shipCount+" pieces)");

            // Add checkbox and label to the shipSelectionBox
            shipSelectionBox.getChildren().addAll(checkBox, label);
        }
    }

    public void shipsPlaced() {
        shipSelectionBox.getChildren().clear();
        this.add(submarine,1,3);
        submarine.setDisable(true);
        submarine.setOnAction(event->{
            enemyBoard.enabled=false;
            shoot.setDisable(true);
            this.playerBoard.moveSubmarine();});
        playerBoard.disableButtons();
        shipSelectionBox.getChildren().add(turnMessage);
        turnMessage.setText("Waiting for game to start");
        this.add(shoot,0,2);
        shoot.setDisable(true);
        shoot.setOnAction(event-> {
            submarine.setDisable(true);
            try {
                shoot();
            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        });
        enemyBoard.waitForShoot();
        enemyBoard.enabled=false;
    }
    public void shoot()throws JsonProcessingException {
        enemyBoard.shoot();
    }

    public void win(String line) {
        shipSelectionBox.getChildren().clear();
        shipSelectionBox.getChildren().add(new Text("You won!: "+line));
        shoot.setDisable(true);
        //shoot.setText(Profile);
    }

    public void lose(String line) {
        shipSelectionBox.getChildren().clear();
        shipSelectionBox.getChildren().add(new Text("You lost :( "+line));
        shoot.setDisable(true);
    }

    public void placeSubmarine(int shipSize) {
        playerBoard.waitForPlacement(shipSize,true);
        SubmarineMoves--;
    }
}
