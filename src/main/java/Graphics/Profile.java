package Graphics;

import Server.Statistic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Vector;


/**
 * Ez az osztály kezeli az ablakot ami kiírja a játékos előzményeit
 */
public class Profile extends GridPane {
    String statistics;
    Label showStatistics=new Label("Nothing saved to profile yet");
    Button cancel=new Button("Cancel");
    Graphics graphics;

    Profile (String statistics,Graphics graphics){
        this.statistics=statistics;
        this.graphics=graphics;
        showStatistics.setText(JsonToString(statistics));
        if(showStatistics.getText().length()==0){
            showStatistics.setText("Nothing saved to profile yet");
        }
        this.add(showStatistics,0,0);
        this.add(cancel,0,1);
        cancel.setOnAction(event->graphics.backToConnect());
    }
    String JsonToString(String stat){
        ObjectMapper mapper=new ObjectMapper();
        try {
            //Vector<HashMap<String,String>> statisticsVector=mapper.readValue(stat,Vector.class);
            Vector<HashMap<String,String>> statisticsVector=mapper.readValue(stat,Vector.class);
            String s=new String();
            for(int i=0;i<statisticsVector.size();i++){
                Integer i2=i+1;
                s= s+"Game: "+i2.toString()+"\n"+"opponent: "+statisticsVector.elementAt(i).get("opponent")+"\n"+
                        "result: "+ statisticsVector.elementAt(i).get("result")+"\n"+
                        "Note: "+statisticsVector.elementAt(i).get("note")+"\n";
            }
            return s;
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return "Statistics could not be accessed";
    }

}
