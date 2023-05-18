package Graphics;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Objects;
import Client.*;
import javafx.scene.text.Text;

/**
 * Ez az osztály kezeli az ablakot ahol új játékot lehet indítani
 */
public class NewGameWindow extends GridPane {

    private ComboBox<String> boardSizeComboBox;
    private ComboBox<String> shipsComboBox;
    private Button launchGameButton;
    private Text errorField=new Text();
    Graphics app;
    Client client;

    public NewGameWindow(Graphics app, Client client) {
        this.app=app;
        this.client=client;
        // Set GridPane properties
        setPadding(new Insets(10));
        setHgap(10);
        setVgap(10);

        // Create board size ComboBox
        boardSizeComboBox = new ComboBox<>();
        boardSizeComboBox.getItems().addAll("6*6","8*8", "10*10");
        boardSizeComboBox.setPromptText("Select Board Size");

        // Create ships ComboBox
        shipsComboBox = new ComboBox<>();
        shipsComboBox.setPromptText("Select Ships");

        // Create Launch Game button
        launchGameButton = new Button("Launch Game");

        // Add components to GridPane
        add(boardSizeComboBox, 0, 0);
        add(shipsComboBox, 1, 0);
        add(launchGameButton, 0, 1);
        add(errorField,0,2);
        // Update ships ComboBox based on board size selection
        boardSizeComboBox.setOnAction(event -> updateShipsComboBox());
        launchGameButton.setOnAction(event-> {
            try {
                launchGame();
            } catch (JsonProcessingException e) {
                System.out.println("Error: Could not convert settings to JSON");
                e.printStackTrace();
            }
        });
    }
    public void launchGame() throws JsonProcessingException {
        String boardSizeString=boardSizeComboBox.getValue();
        String shipsString=shipsComboBox.getValue();
        if(!Objects.equals(boardSizeString, "Select Board Size") &&
                !Objects.equals(shipsString, "Select Ships")&&
                !Objects.equals(shipsString, null)){
            Settings settings;
            HashMap<Integer, Integer> ships=parseShipSizes(shipsString);
            int n,m;
            String[] parts = boardSizeString.split("\\*");
            n=Integer.parseInt(parts[0]);
            m=Integer.parseInt(parts[1]);
            settings=new Settings(n,m,ships);
            String message=Graphics.toJSON(settings);
            client.sendLine(message);
            app.gameLaunched();
        }
        else{
            errorField.setText("Please choose settings.");
        }
    }

    public static HashMap<Integer, Integer> parseShipSizes(String shipSizes) {
        HashMap<Integer, Integer> shipMap = new HashMap<>();

        // Remove any leading/trailing spaces and split the string by commas
        String[] shipTokens = shipSizes.trim().split(",\\s*");

        for (String token : shipTokens) {
            // Split each token into the number of pieces and the length
            String[] parts = token.split("\\s+");
            int pieces = Integer.parseInt(parts[0]);
            int length = Integer.parseInt(parts[parts.length - 2]);
            shipMap.put(length, pieces);
        }

        return shipMap;
    }

    private void updateShipsComboBox() {
        String selectedBoardSize = boardSizeComboBox.getValue();

        // Clear existing options
        shipsComboBox.getItems().clear();

        // Add options based on board size selection
        if (selectedBoardSize.equals("6*6")) {
            shipsComboBox.getItems().add("1 pieces 2 long");
            shipsComboBox.getItems().add("1 pieces 4 long, 1 pieces 3 long");
            shipsComboBox.getItems().add("1 pieces 4 long, 3 pieces 3 long, 3 pieces 2 long");
            shipsComboBox.getItems().add("3 pieces 4 long, 3 pieces 3 long");
            shipsComboBox.getItems().add("2 pieces 4 long, 1 pieces 3 long, 2 pieces 2 long");
        }else if (selectedBoardSize.equals("8*8")) {
            shipsComboBox.getItems().add("1 pieces 5 long, 2 pieces 4 long, 3 pieces 3 long, 3 pieces 2 long");
            shipsComboBox.getItems().add("2 pieces 5 long, 2 pieces 4 long, 3 pieces 3 long");
            shipsComboBox.getItems().add("1 pieces 6 long, 2 pieces 4 long, 3 pieces 3 long, 2 pieces 2 long");
        } else if (selectedBoardSize.equals("10*10")) {
            shipsComboBox.getItems().add("2 pieces 5 long, 2 pieces 4 long, 3 pieces 3 long, 3 pieces 2 long");
            shipsComboBox.getItems().add("1 pieces 5 long, 2 pieces 4 long, 3 pieces 3 long,5 pieces 2 long");
            shipsComboBox.getItems().add("1 pieces 7 long, 2 pieces 5 long, 3 pieces 4 long, 2 pieces 3 long");
        }
    }

    public Button getLaunchGameButton() {
        return launchGameButton;
    }
}
