package Graphics;

import Client.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static java.lang.Math.abs;

/**
 * Ez a class egy játéktábla a GameWindow ablakban. Lehet az ellenfélé vagy a játékosé is.
 * n*n gombbal rendelkezik.
 */
public class Board extends GridPane {
    boolean enabled; //Ez alapján állítom hogy éppen meg lehet e nyomni a tábla gombjait
    Button[][] boardHandler; //Itt kezelem a gombokat
    Integer[][] ships; //Ez tárolja hogy hol vannak hajók. Ahol nincs hajó ott 0, ahol van ott a hajó id-ja
    Integer[][] shots; //Ez tárolja a lövéseket,
    // ahol 0 ott nem volt lövés, ahol -1 ott nem talált, ahol 1 ott talált a lövés
    int size; //A tábla mérete
    Client client; //Referencia a kliensre, hogy lehessen üzenetet küldeni a szervernek
    Coordinate whereToShoot; //A táblán aktuálisan kiválasztott koordináta a lövés fázisban


    /**
     * Inicializálja a mátrixokat, és kirajzolja a táblát
     * @param boardSize
     * @param client
     */
    Board(int boardSize, Client client) {
        this.client = client;
        this.size = boardSize;
        this.setPrefSize(boardSize * (300 / boardSize), boardSize * (300 / boardSize));
        ships = new Integer[boardSize][boardSize];
        shots = new Integer[boardSize][boardSize];
        boardHandler = new Button[boardSize][boardSize];
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                int finalI = row;
                int finalJ = col;
                Button button = new Button();
                boardHandler[col][row] = button;
                button.setPrefSize(300 / boardSize, 300 / boardSize);
                button.setStyle("-fx-background-color: #5295be;");
                this.add(button, col, row);
                ships[col][row] = 0;
                shots[col][row] = 0;
            }
        }
    }

    /**
     * Elindítja a hajók lerakásának fázisát.
     * A táblán egy gombot lenyomva megjelenik hogy az adott hosszú
     * hajót hova lehet lerakni. Ha ide kattintunk akkor leteszi a hajót,
     * ha nem akkor újra kijelöli hogy hova lehet tenni a hajót.
     * @param sizeOfShip  A hajó hossza
     * @param isSubmarine Tengeralattjárót rakunk le vagy nem
     *                    (Akkor igaz amikor egy hajót leraktunk és újra felvesszük)
     */
    void waitForPlacement(int sizeOfShip, boolean isSubmarine) {
        enabled = true;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int finalJ = j;
                int finalI = i;
                boardHandler[i][j].setOnAction(event -> placeShip1(new Coordinate(finalI, finalJ), sizeOfShip, isSubmarine));
            }
        }
    }

    /**
     * Megjeleníti hogy egy adott koordinátáról hova lehet lehelyezni a hajó másik felét.
     * Ahova lehelyezhető a hajó oda az adott buttonoknak setOnActionként megadja, hogy ha
     * rájuk kattint a felhasználó akkor legyen letéve a hajó. A többi button placeShip1 fázisban marad
     * @param c1 A koordináta ahova a hajó első fele kerül
     * @param sizeOfShip A hajó mérete
     * @param isSubmarine Tengeralattjárót rakunk le vagy nem
     *      *                    (Akkor igaz amikor egy hajót leraktunk és újra felvesszük)
     */
    void placeShip1(Coordinate c1, int sizeOfShip, boolean isSubmarine) {
        //Megnézem minden pozícióra, hogy oda kerülhet e a hajó másik fele
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Coordinate c2 = new Coordinate(i, j);
                //Ha kerülher ide:
                if (((abs(c1.x - i) == 0 && abs(c1.y - j) == sizeOfShip - 1) ||
                        (abs(c1.x - i) == sizeOfShip - 1 && abs(c1.y - j) == 0)) && !shipInWay(c1, c2)) {
                    boardHandler[i][j].setDisable(false);
                    boardHandler[i][j].setStyle("-fx-background-color: #5295be;");
                    boardHandler[i][j].setOnAction(event -> {
                        try {
                            placeShip2(c1, c2, sizeOfShip, isSubmarine);
                        } catch (JsonProcessingException e) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    });
                    //Ha nem kerülhet ide:
                } else {
                    int finalI = i;
                    int finalJ = j;
                    if (ships[i][j] == 0) {
                        boardHandler[i][j].setStyle("-fx-background-color: #b1c8e0;");
                    } else {
                        boardHandler[i][j].setStyle("-fx-background-color: #737486;");
                    }
                    boardHandler[i][j].setOnAction(event -> placeShip1(new Coordinate(finalI, finalJ), sizeOfShip,isSubmarine));
                }
            }
        }
    }

    /**
     * Megmondja hogy két egy vonalra eső koordináta közötti egyenes úton van e hajó
     * @param c1
     * @param c2
     * @return
     */
    boolean shipInWay(Coordinate c1, Coordinate c2) {
        for (Coordinate c : Coordinate.coordinatesBetween(c1, c2)) {
            if (ships[c.x][c.y] != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Elküldi a szervernek a lerakott hajó adatait
     * @param c1
     * @param c2
     * @param sizeOfShip
     * @param isSubmarine
     * @throws JsonProcessingException
     */
    void placeShip2(Coordinate c1, Coordinate c2, int sizeOfShip, boolean isSubmarine) throws JsonProcessingException {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                boardHandler[i][j].setOnAction(null);
            }
        }
        if(!isSubmarine) {
            client.sendLine("Placing ship");
        }
        else{
            client.sendLine("Placing submarine");
        }
        client.sendLine(Client.toJSON(c1));
        client.sendLine(Client.toJSON(c2));
        client.sendLine(Client.toJSON(sizeOfShip));
    }

    /**
     * Frissíti a táblát. Ahol nincs hajó ott kék lesz a pálya, ahol van ott szürke.
     * Ahova lőttek már ott megjelenik egy piros x ha találat volt
     * és egy világos o ha nem talált a lövés.
     * @param shipsNew A hajók új elhelyezkedése
     * @param shotsNew A lövések új elhelyezkedése
     */
    void refreshBoard(Integer[][] shipsNew, Integer[][] shotsNew) {
        ships = shipsNew;
        shots = shotsNew;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Button button = boardHandler[i][j];
                int shipValue = ships[i][j];
                int shotValue = shots[i][j];

                if (shipValue == 0) {
                    button.setStyle("-fx-background-color: #5295be;");
                } else {
                    button.setStyle("-fx-background-color: gray;");
                }

                if (shotValue == -1) {
                    button.setText("o");
                    button.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
                    button.setTextFill(Color.BEIGE);
                } else if (shotValue == 1) {
                    button.setText("x");
                    button.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                    button.setTextFill(Color.DARKSALMON);
                }
                else if(shotValue==0){
                    button.setText(null);
                }
            }
        }
        //ez a kódrészlet majdnem megoldotta hogy
        // legyen a hajóknak körvonala, de nem működött és nem volt időm kibogozni
        /*String[] gridLineColors = new String[4]; // Array to store grid line colors (top, right, bottom, left)

        // Iterate through the ships matrix
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int shipValue = ships[i][j];

                // Check neighbors: top, right, bottom, left
                int[][] neighbors = {{j - 1, i}, {j, i + 1}, {j + 1, i}, {j, i - 1}};

                for (int k = 0; k < 4; k++) {
                    int neighborRow = neighbors[k][0];
                    int neighborCol = neighbors[k][1];

                    // Check if neighbor is within bounds
                    if (neighborRow >= 0 && neighborRow < size && neighborCol >= 0 && neighborCol < size) {
                        int neighborValue = ships[neighborRow][neighborCol];

                        // Set grid line color based on neighbor values
                        if (shipValue == neighborValue) {
                            gridLineColors[k] = "gray";
                        } else {
                            gridLineColors[k] ="red";
                        }
                    } else {
                        // Neighbor is outside the bounds, set grid line color to black
                        gridLineColors[k] = "black";
                    }
                }
                String Style=("-fx-border-color: " +
                        gridLineColors[0] + " " +
                        gridLineColors[1] + " " +
                        gridLineColors[2] + " " +
                        gridLineColors[3] + ";");
                //System.out.println(Style);

                // Set grid lines of the button based on gridLineColors
                boardHandler[i][j].setStyle(
                        boardHandler[i][j].getStyle()+ Style);
            }
        }*/
    }

    public void disableButtons() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                boardHandler[i][j].setOnAction(null);
            }
        }
    }

    /**
     * Beállítja a gombok eseménykezelését ki lehessen választani, hogy hova lő a játékos
     */
    public void waitForShoot() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int finalI = i;
                int finalJ = j;
                boardHandler[i][j].setOnAction(event -> {
                    if (enabled) {
                        if (whereToShoot != null) {
                            boardHandler[whereToShoot.x][whereToShoot.y].setStyle("-fx-background-color: #5295be;");
                        }
                        whereToShoot = new Coordinate(finalI, finalJ);
                        boardHandler[finalI][finalJ].setStyle("-fx-background-color: #d8e9f6;");
                    }
                });
            }
        }
    }

    /**
     * Elküldi a szervernek hogy hová lő a játékos
     * @return Hogy el lett e küldve a lövés a szervernek
     * @throws JsonProcessingException
     */
    public boolean shoot()throws JsonProcessingException{
        if(whereToShoot!=null){
            client.sendLine("Shooting");
            client.sendLine(Graphics.toJSON(whereToShoot));
            boardHandler[whereToShoot.x][whereToShoot.y].setStyle("-fx-background-color: #5295be;");
            whereToShoot=null;
            return true;
        }
        return false;
    }

    /**
     * Hajó mozgatása
     * Elküldi a szervernek hogy melyik hajót mozgatja a felhsználó
     */
    public void moveSubmarine() {
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                int finalI = i;
                int finalJ = j;
                boardHandler[i][j].setOnAction(event->{
                    client.sendLine("Moving submarine");
                    try {
                        client.sendLine(Graphics.toJSON(new Coordinate(finalI, finalJ)));
                    } catch (JsonProcessingException e) {
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                    for(int k=0;k<size;k++) {
                        for (int l = 0; l < size; l++) {
                            boardHandler[k][l].setOnAction(null);
                        }
                    }
                });
            }
        }
    }
}
