package Server;


import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ez az osztály tartja számon hogy milyen játékok futnak vagy várakoznak éppen
 */
public class Games {
    //gamesRunning
    ConcurrentHashMap<String, Game> gamesRunning; //username, game
    ConcurrentHashMap<String, Game>gamesWaiting; //username, game
    public Games(){

        gamesWaiting=new ConcurrentHashMap<>();
        gamesRunning=new ConcurrentHashMap<>();
    }
    public void addGameWaiting(Game game) throws JsonProcessingException {
        gamesWaiting.put(game.user1.username,game);
    }
    public void addGameRunning(Game game){//TODO?
        gamesRunning.put(game.user1.username,game);
        gamesRunning.put(game.user2.username,game);
    }
    User otherUser(Game game, User user){
       return game.otherUser(user);
    }
    User otherUser(Game game, String username){
        if (Objects.equals(game.user1.username, username)){
            return game.user2;
        }
        else{
            return game.user1;
        }
    }
    //Lekezeli ha egy felhasználó kilépett,
    // megnézi hogy volt-e futó vagy várakozó játéka, és ha volt akkor eltávolítja
    public User userExited(String username){
        Game game=gamesRunning.get(username);
        if(game!=null){
            gamesRunning.remove(game.user1.username);
            gamesRunning.remove(game.user2.username);
            return otherUser(game, username);
        }
        if(gamesWaiting.get(username)!=null){
            User user=gamesWaiting.remove(username).getUser1();
        }
        return null;
    }
    //Eltávolítja az adott játékot a futó és várakozó játékok közül
    public void removeGame(Game game ){
        if(game!=null){
            gamesRunning.remove(game.user1.username);
            gamesRunning.remove(game.user2.username);
        }
        if(gamesWaiting.get(game.user1.username)!=null){
            gamesWaiting.remove(game.user1.username).getUser1();
        }
        if(gamesWaiting.get(game.user2.username)!=null){
            gamesWaiting.remove(game.user2.username).getUser1();
        }
    }

}
