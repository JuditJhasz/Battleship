package Graphics;


import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import Client.Client;


/**
 * A bejelentkezéshez/ regisztrációhoz használt grafikus ablakot megjelenítő osztály
 *
 */
public class LogInWindow extends GridPane {
    Graphics app;
    VBox vbox;
    Client client;
    Button logInButton = new Button("Log in");
    Button registerButton = new Button("Register");

    TextField username = new TextField();
    TextField password = new TextField();

    Text errorField=new Text();
    public LogInWindow(Graphics app,Client client)  {
        this.client=client;
        this.app=app;
        initialise();
    }

    void initialise(){
        this.setWidth(app.root.getWidth());
        this.setPadding(new Insets(10));
        this.setHgap(10);
        this.setVgap(10);

        logInButton.setOnAction(event->logIn());
        registerButton.setOnAction(event->register());
        this.add(new Text("username"),0,0);
        this.add(new Text("password"),1,0);
        this.add(username, 0, 1);
        this.add(password, 1, 1);
        this.add(errorField, 0, 3,2,1);

        this.add(logInButton, 0, 2);
        this.add(registerButton, 1, 2);

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(25);
        this.getColumnConstraints().addAll(columnConstraints, columnConstraints, columnConstraints, columnConstraints);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(100);
        this.getRowConstraints().addAll(rowConstraints, rowConstraints, rowConstraints);

    }
    public void logIn(){
        if(username.getCharacters().isEmpty() || password.getCharacters().isEmpty()){
            errorField.setText("Please fill the username and password fields");
        }
        else{
            client.sendLine("Log in");
            client.sendLine(username.getCharacters().toString());
            client.sendLine(password.getCharacters().toString());
        }
    }

    public void register(){
        if(username.getCharacters().isEmpty() || password.getCharacters().isEmpty()){
            errorField.setText("Please fill the username and password fields");
        }
        else{
            client.sendLine("Register");
            client.sendLine(username.getCharacters().toString());
            client.sendLine(password.getCharacters().toString());
        }
    }
    public void logInError(String error){
        errorField.setText(error);
        System.out.println("Graphics: "+error);
    }
    public void succesfulLogIn(){

        client.username=username.getCharacters().toString();
        app.setTitle(client.username);
    }

}


