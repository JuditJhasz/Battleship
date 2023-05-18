package Graphics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import Client.Client;

import java.util.HashMap;
import java.util.Map;

/**
 * Ez a class létrehoz egy felületet ahonnan az alábbi lehetőségek közül lehet választani:
 *          Új játék létrehozása
 *          Kapcsolódás már meglévő játékhoz
 *          Már meglévő játékok listájának frissítése
 *          Játékos előzméényeinek megtekintése
 *
 */
public class ConnectionWindow extends GridPane {
    Client client;
    Graphics app;
    ComboBox<String> otherPlayerName = new ComboBox<>(); //A másik játékot létrehozó játékos neve
    // Create Buttons
    Button connect = new Button("Connect");
    Button newGame = new Button("New game");
    Button profile = new Button("Profile");
    Button refresh =new Button("Refresh games");

    HashMap<String,Settings>actualGames; //Játékosnév, beállítások
    Text gameSettings = new Text(); //Kiírja a kiválasztott játék beállításait
    public ConnectionWindow(Graphics app,Client  client){
        this.app=app;
        this.client=client;
        otherPlayerName.setPromptText("Select an opponent");

        // Create GridPane layout and add components
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(10));
        this.add(otherPlayerName, 0, 0);
        this.add(connect, 0, 1);
        this.add(newGame, 1, 1);
        this.add(refresh,1,2);
        this.add(profile, 0, 2);
        this.add(gameSettings, 0, 3, 2, 1);

        connect.setOnAction(event->{ //A connect gomb megnyomására elküldi a szervernek a játék adatait
            //Amihez csatlakozni akar a felhasználó
            if(otherPlayerName.getValue()!=null && otherPlayerName.getValue()!="Select an opponent") {
                client.sendLine("Connect");
                client.sendLine(otherPlayerName.getValue());
            }else{
                gameSettings.setText("Please choose an opponent from the list above");
            }
        });
        newGame.setOnAction(event->client.sendLine("New Game"));
        refresh.setOnAction(event->client.sendLine("Refresh"));
        profile.setOnAction(event->client.sendLine("Profile"));
    }
    //Frissíti a legördülő lista tartalmát, ami a várakozó játékokat tartalmazza
    public void refreshGames(HashMap<String,String>settings){
        otherPlayerName.getItems().clear();
        actualGames=new HashMap<>();
        for(Map.Entry<String,String> setting:settings.entrySet()){
            otherPlayerName.getItems().add(setting.getKey());
            String JSONString=setting.getValue();
            ObjectMapper mapper=new ObjectMapper();
            try {
                Settings newSetting=mapper.readValue(JSONString,Settings.class);
                actualGames.put(setting.getKey(),newSetting);
            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        otherPlayerName.setOnAction(event->showSettings());
    }
    //Kiírja a játékoshoz tartozó játékbeállításokat
    public void showSettings(){
        if(actualGames.get(otherPlayerName.getValue())!=null){
            gameSettings.setText(actualGames.get(otherPlayerName.getValue()).toString());
        }
    }
    //Ha nem sikerült a játékhoz kapcsolódás akkor visszajelez
    public void connectionError(String errorMessage){
        gameSettings.setText(errorMessage);
    }
}

