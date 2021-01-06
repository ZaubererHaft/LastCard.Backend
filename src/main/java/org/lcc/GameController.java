package org.lcc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;

@RestController
public class GameController {

    @Value("${server.address}")
    private String address;
    @Value("${application.executable}")
    private String executable;
    @Value("${application.path}")
    private String path;
    @Value("${application.port}")
    private int port = 49152;

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final List<Game> games = new ArrayList<>();

    @GetMapping("/create/{name}")
    public Game createGame(@PathVariable String name){
        String gameId = new UID().toString();
        Game game = new Game(name, gameId, port);
        try {

            String cmd = "./" + this.executable + " " + this.address + " " + this.port + " " + gameId + " " + name;

            logger.info("running " + cmd);
            Process process = Runtime.getRuntime()
                    .exec(cmd , null, new File(this.path));
            Thread.sleep(750);
        }
        catch(Exception ex)
        {
            throw new RuntimeException(ex);
        }
        this.port++;
        this.games.add(game);
        return game;
    }



    @GetMapping("/get")
    public List<Game> index(){
        return games;
    }

    private static class Game
    {
        private final String name, id;
        private final int port;

        private Game(String name, String id, int port) {
            this.name = name;
            this.id = id;
            this.port = port;
        }

        public String getName()
        {
            return this.name;
        }

        public String getId()
        {
            return this.id;
        }

        public int getPort()
        {
            return this.port;
        }
    }
}