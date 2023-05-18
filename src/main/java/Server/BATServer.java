package Server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Ez az osztály hozza létre a kapcsolatot a klienssel.
 * Folyamatosan várja egy szálon a klienseket, és mindegyiknek
 * létrehoz egy BAT típusú példányt ami új szálon kommunikál vele.
 * Leállításkor elmenti a felhasználók adatait egy fájlba JSON formátumba (users.json)
 */
public class BATServer implements Runnable{
    Games games; //A létező játékokat tartalmazza
    ConcurrentHashMap<String, User> users; //<username, user>
    protected ServerSocket serverSocket;
    public static final int PORT_NUMBER = 1023;
    public BATServer() throws IOException {
        games=new Games();
        users=new ConcurrentHashMap<String,User>();
        serverSocket = new ServerSocket(PORT_NUMBER);
        load();
    }

    public void close() throws IOException {
        serverSocket.close();
        saveData();
    }

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {saveData();System.out.println("closing BATServer");}));
        try {
            while (! Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                try {
                    new Thread(new BAT(clientSocket,this)).start();
                } catch (IOException e) {
                    System.err.println("Failed to communicate with client!"); //TODO
                }
            }
        } catch (IOException e) {
            saveData();
            System.out.println("Accept failed!"); //TODO
        }
        saveData();
        System.out.println("closing BATServer");
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    void saveData(){
        System.out.println("Saving data");
        saveUsers();
    }

    public static void main(String[] args) {
        try {
            new Thread(new BATServer()).start();
            System.out.println("BATServer launched");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void saveUsers(){
        System.out.println("Saving users");
        try (FileWriter fileWriter = new FileWriter("users.json")) {
            for(Map.Entry<String,User>U: users.entrySet()) {
                String JSONUser =toJSON( U.getValue());
                fileWriter.write(JSONUser+"\n");
            }
            //System.out.println("JSON data written to file successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing JSON to file.");
            e.printStackTrace();
        }
    }
    public void load(){
        System.out.println("Loading data");
        loadUsers();
    }
    public void loadUsers(){
        System.out.println("Loading users");
        try {
            BufferedReader reader = new BufferedReader(new FileReader("users.json"));
            String line = reader.readLine();
            while (line != null) {
                ObjectMapper mapper = new ObjectMapper();
                User newUser = mapper.readValue(line, User.class);
                users.put(newUser.username,newUser);
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            System.out.println("Error while loading users");
            e.printStackTrace();
        }
    }
    public static String toJSON(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String jsonString = mapper.writeValueAsString(object);
        return jsonString;

    }
    public void removeGame(Game game){
        games.removeGame(game);
    }


}
