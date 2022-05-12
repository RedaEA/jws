package fr.epita.assistant.jws.domain.service;

import fr.epita.assistant.jws.GameState;
import fr.epita.assistant.jws.converter.GameToGameEntity;
import fr.epita.assistant.jws.data.model.Game;
import fr.epita.assistant.jws.data.model.MyMap;
import fr.epita.assistant.jws.data.model.Player;
import fr.epita.assistant.jws.data.repository.GameRepository;
import fr.epita.assistant.jws.data.repository.MyMapRepository;
import fr.epita.assistant.jws.data.repository.PlayerRepository;
import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.entity.PlayerEntity;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class GameService {
    @Inject GameRepository gameRepository;
    @Inject GameToGameEntity converter;
    @Inject PlayerRepository playerRepository;
    @Inject MyMapRepository myMapRepository;

    @Transactional
    public List<GameEntity> getAllGames()
    {
        var games = gameRepository.findAll();
        return games.stream().map(game -> converter.convertGame(game)).collect(Collectors.toList());
    }

    public List<MyMap> getMap(Game game) throws IOException {
        List<MyMap> l_map = new ArrayList<>();
        String path = System.getenv("JWS_MAP_PATH");
        File file = new File(path);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();

        while(line != null)
        {
            var res = new MyMap().withLine(line).withGame(game);
            myMapRepository.persist(res);
            l_map.add(res);
            line = br.readLine();
        }
        return l_map;
    }

    @Transactional
    public GameEntity createNewGame(String playerName) throws IOException {
        var game = new Game()
                .withState(GameState.STARTING)
                .withPlayers(new ArrayList<>())
                .withMap(new ArrayList<>())
                .withStarttime(LocalDateTime.now());
        gameRepository.persist(game);
        val playerModel = new Player()
                .withName(playerName)
                .withLives(3)
                .withGame(game)
                .withPosX(1)
                .withPosY(1)
                .withCanPutBomb(true)
                .withFirstMovement(true);
        playerRepository.persist(playerModel);
        game.players.add(playerModel);
        game.map = getMap(game);
        return converter.convertGame(game);
    }

    @Transactional
    public GameEntity getGameById(Long id)
    {
        var model = gameRepository.findById(id);
        if (model == null)
        {
            return null;
        }
        return converter.convertGame(model);
    }

    @Transactional
    public GameEntity addPlayer(final String name, Long gameId)
    {
        var gameModel = gameRepository.findById(gameId);
        if (gameModel == null || gameModel.players.size() >= 4 || gameModel.state != GameState.STARTING)
        {
            return null;
        }

        val playerModel = new Player()
                .withName(name)
                .withLives(3)
                .withGame(gameModel)
                .withCanPutBomb(true)
                .withFirstMovement(true);
        if(gameModel.players.size() == 0)
        {
            playerModel.posX = 1;
            playerModel.posY = 1;
        }
        else if(gameModel.players.size() == 1)
        {
            playerModel.posX = 15;
            playerModel.posY = 1;
        }
        else if (gameModel.players.size() == 2)
        {
            playerModel.posX = 1;
            playerModel.posY = 13;
        }
        else if (gameModel.players.size() == 3)
        {
            playerModel.posX = 15;
            playerModel.posY = 13;
        }
        else
        {
            return null;
        }
        playerRepository.persist(playerModel);
        gameModel.players.add(playerModel);
        return converter.convertGame(gameModel);
    }

    @Transactional
    public GameEntity start(Long gameId)
    {
        var model = gameRepository.findById(gameId);
        if (model == null)
        {
            return null;
        }

        if (model.state == GameState.STARTING && model.players.size() > 1) {
            model.state = GameState.RUNNING;
        }
        return converter.convertGame(model);
    }

    @Transactional
    public String getBloc(int posX, int posY,  Game gameModel) throws IOException {
        List<MyMap> current = gameModel.map;
        Collections.sort(current);
        int y = 0;

        StringBuffer real = new StringBuffer();
        while (y < current.get(posY).line.length())
        {
            int x = Integer.parseInt(current.get(posY).line.substring(y, y+1));
            y++;
            String myChar = current.get(posY).line.substring(y, y + 1);
            int z = 0;
            while (z < x)
            {
                real.append(myChar);
                z++;
            }
            y++;
        }
        y = 0;
        while (y < posX)
        {
            y++;
        }
        return real.substring(y, y+1);
    }

    @Transactional
    public GameEntity movePlayer(Long gameId, Long playerId, int posX, int posY) throws IOException {
        var gameModel = gameRepository.findById(gameId);
        if (gameModel == null || gameModel.state != GameState.RUNNING)
        {
            return null;
        }
        if (!getBloc(posX, posY, gameModel).equals("G"))
        {
            return null;
        }
        if (gameModel.players.stream().noneMatch(player -> Objects.equals(player.id, playerId)))
        {
            return null;
        }
        for (var i = 0; i < gameModel.players.size(); i++)
        {
            if (gameModel.players.get(i).id.equals(playerId))
            {
                if (gameModel.players.get(i).lives == 0)
                {
                    return null;
                }
                gameModel.players.get(i).lastMovement = LocalDateTime.now();
                gameModel.players.get(i).firstMovement = false;
                gameModel.players.get(i).posX = posX;
                gameModel.players.get(i).posY = posY;
            }
        }
        return converter.convertGame(gameModel);
    }

    @Transactional
    public boolean existPlayerGame(Long playerId, Long gameId)
    {
        var gameModel = gameRepository.findById(gameId);
        var playerModel = playerRepository.findById(playerId);
        return gameModel != null && playerModel != null;
    }

    @Transactional
    public boolean canPlayerPutBomb(Long playerId)
    {
        var player = playerRepository.findById(playerId);
        return player.canPutBomb;
    }


    @Transactional
    public boolean canPlayerMove(Long playerId)
    {
        var player = playerRepository.findById(playerId);
        if (player.firstMovement == true)
        {
            player.firstMovement = false;
            return true;
        }
        else
        {
            int delay = Integer.parseInt(System.getenv("JWS_DELAY_MOVEMENT"));
            int tick = Integer.parseInt(System.getenv("JWS_TICK_DURATION"));

            if (LocalDateTime.now().isBefore(player.lastMovement.plusSeconds(delay * tick / 1000)))
            {
                return false;
            }
            return true;
        }
    }

    @Transactional
    public GameEntity putBomb(Long gameId, Long playerId, int posX, int posY) throws IOException{
        var gameModel = gameRepository.findById(gameId);
        var playerModel = playerRepository.findById(playerId);
        if (gameModel == null || gameModel.state != GameState.RUNNING || playerModel.lives <= 0)
        {
            return null;
        }
        if (!getBloc(posX, posY, gameModel).equals("G") || playerModel.posX != posX || playerModel.posY != posY)
        {
            return null;
        }
        Collections.sort(gameModel.map);
        String line = gameModel.map.get(posY).line;

        int y = 0;
        StringBuffer real = new StringBuffer();
        while (y < line.length())
        {
            int x = Integer.parseInt(line.substring(y, y+1));
            y++;
            String myChar = line.substring(y, y+1);
            int z = 0;
            while (z < x)
            {
                real.append(myChar);
                z++;
            }
            y++;
        }
        real.replace(posX, posX + 1, "B");
        StringBuffer rle = new StringBuffer();
        for (int i = 0; i < real.length(); i++)
        {
            int x = 0;
            String s = real.substring(i, i + 1);
            while (i + x < real.length() && s.equals(real.substring(i + x, i +1 + x)))
            {
                x++;
            }
            rle.append(x);
            rle.append(s);
            i += x - 1;
        }
        gameModel.map.get(posY).line = rle.toString();

        playerModel.canPutBomb = false;
        return converter.convertGame(gameModel);
    }

    @Transactional
    public void bombExplosion(int posX, int posY, Long gameId, Long playerId) throws InterruptedException {
        var gameModel = gameRepository.findById(gameId);
        int delay = Integer.parseInt(System.getenv("JWS_DELAY_BOMB"));
        int tick = Integer.parseInt(System.getenv("JWS_TICK_DURATION"));
        Thread.sleep(delay * tick);

        for (int i = 0; i < gameModel.players.size(); i++)
        {
            if (gameModel.players.get(i).posX == posX &&
                    (gameModel.players.get(i).posY == posY
                            || gameModel.players.get(i).posY == posY - 1
                            || gameModel.players.get(i).posY == posY + 1))
            {
                gameModel.players.get(i).lives -= 1;

            }
            else if (gameModel.players.get(i).posY == posY &&
                    (gameModel.players.get(i).posX == posX
                            || gameModel.players.get(i).posX == posX - 1
                            || gameModel.players.get(i).posX == posX + 1))
            {
                gameModel.players.get(i).lives -= 1;

            }
        }
        int playersAlive = 0;
        for (int y = 0; y < gameModel.players.size(); y++)
        {
            if (gameModel.players.get(y).lives > 0)
            {
                playersAlive++;
            }
        }
        Collections.sort(gameModel.map);
        String line1 = gameModel.map.get(posY - 1).line;
        String line2 = gameModel.map.get(posY).line;
        String line3 = gameModel.map.get(posY + 1).line;
        int y = 0;
        StringBuffer real1 = new StringBuffer();
        StringBuffer real2 = new StringBuffer();
        StringBuffer real3 = new StringBuffer();
        while (y < line1.length()) {
            int x = Integer.parseInt(line1.substring(y, y + 1));
            y++;
            String myChar = line1.substring(y, y + 1);
            int z = 0;
            while (z < x) {
                real1.append(myChar);
                z++;
            }
            y++;
        }
        y = 0;
        while (y < line2.length()) {
            int x = Integer.parseInt(line2.substring(y, y + 1));
            y++;
            String myChar = line2.substring(y, y + 1);
            int z = 0;
            while (z < x) {
                real2.append(myChar);
                z++;
            }
            y++;
        }
        y = 0;
        while (y < line3.length()) {
            int x = Integer.parseInt(line3.substring(y, y + 1));
            y++;
            String myChar = line3.substring(y, y + 1);
            int z = 0;
            while (z < x) {
                real3.append(myChar);
                z++;
            }
            y++;
        }
        if (real1.charAt(posX) == 'W') {
            real1.replace(posX, posX + 1, "G");
        }
        if (real3.charAt(posX) == 'W') {
            real3.replace(posX, posX + 1, "G");
        }
        real2.replace(posX, posX + 1, "G");
        if (real2.charAt(posX + 1) == 'W') {
            real2.replace(posX + 1, posX + 2, "G");
        }
        if (real2.charAt(posX - 1) == 'W') {
            real2.replace(posX - 1, posX, "G");
        }
        StringBuffer rle1 = new StringBuffer();
        StringBuffer rle2 = new StringBuffer();
        StringBuffer rle3 = new StringBuffer();
        for (int i = 0; i < real1.length(); i++) {
            int x = 0;
            String s = real1.substring(i, i + 1);
            while (i + x < real1.length() && s.equals(real1.substring(i + x, i + 1 + x))) {
                x++;
            }
            rle1.append(x);
            rle1.append(s);
            i += x - 1;
        }
        for (int i = 0; i < real2.length(); i++) {
            int x = 0;
            String s = real2.substring(i, i + 1);
            while (i + x < real2.length() && s.equals(real2.substring(i + x, i + 1 + x))) {
                x++;
            }
            rle2.append(x);
            rle2.append(s);
            i += x - 1;
        }
        for (int i = 0; i < real3.length(); i++) {
            int x = 0;
            String s = real3.substring(i, i + 1);
            while (i + x < real3.length() && s.equals(real3.substring(i + x, i + 1 + x))) {
                x++;
            }
            rle3.append(x);
            rle3.append(s);
            i += x - 1;
        }
        Collections.sort(gameModel.map);
        if (posY - 1 > 0) {
            gameModel.map.get(posY - 1).line = rle1.toString();
            Collections.sort(gameModel.map);
        }
        gameModel.map.get(posY).line = rle2.toString();
        Collections.sort(gameModel.map);
        if (posY + 1 < gameModel.map.size() - 1) {
            gameModel.map.get(posY + 1).line = rle3.toString();
        }
        Collections.sort(gameModel.map);
        if (playersAlive <= 1)
        {
            gameModel.state = GameState.FINISHED;
        }
        var playerModel = playerRepository.findById(playerId);
        playerModel.canPutBomb = true;
    }
}
