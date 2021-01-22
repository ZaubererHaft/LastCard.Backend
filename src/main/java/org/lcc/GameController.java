package org.lcc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Date;
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

    private Object obj = new Object();

    @GetMapping("/create/{name}/{count}")
    public Game createGame(@PathVariable String name, @PathVariable int count) {
        String gameId = new UID().toString();
        Game game = new Game(name, gameId, port, count);

        String cmd = "./" + this.executable + " " + this.address + " " + this.port + " " + gameId + " " + name + " " + count;
        logger.info("running " + cmd);

        Thread t = new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec(cmd, null, new File(path));
                File f = new File(process.toString() + ".log");
                FileWriter fw = new FileWriter(f);

                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String s;
                while ((s = br.readLine()) != null)
                {
                    String msg = new Date() + ": " + s;
                    fw.write(msg + System.lineSeparator());
                    fw.flush();
                    logger.info(msg);
                    logger.debug("[NOTE] PROCESS HEALTH: " + process);
                }
                fw.close();
            } catch (Exception ex) {

                throw new RuntimeException(ex);
            }
        });
        t.start();
        try {
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
    public List<Game> index() {
        return games;
    }

    private static class Game {
        private final String name, id;
        private final int port, countPlayers;

        private Game(String name, String id, int port, int countPlayers) {
            this.name = name;
            this.id = id;
            this.port = port;
            this.countPlayers = countPlayers;
        }

        public String getName() {
            return this.name;
        }

        public String getId() {
            return this.id;
        }

        public int getPort() {
            return this.port;
        }

        public int getCountPlayers() {
            return this.countPlayers;
        }
    }
}