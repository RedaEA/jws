package fr.epita.assistant.jws.converter;

import fr.epita.assistant.jws.data.model.Game;
import fr.epita.assistant.jws.data.model.MyMap;
import fr.epita.assistant.jws.data.model.Player;
import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.entity.MyMapEntity;
import fr.epita.assistant.jws.domain.entity.PlayerEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.stream.Collectors;

@ApplicationScoped
public class GameToGameEntity {
    public GameEntity convertGame(Game g)
    {
        return new GameEntity(
          g.id,
          g.starttime,
          g.state,
          g.map.stream().map(this::convertMap).collect(Collectors.toList()),
          g.players.stream().map(this::convertPlayer).collect(Collectors.toList())
        );
    }

    public PlayerEntity convertPlayer(Player p)
    {
        return new PlayerEntity(
          p.id,
          p.lives,
          p.name,
          p.posX,
          p.posY,
                p.canPutBomb,
                p.lastMovement,
                p.firstMovement
        );
    }

    public MyMapEntity convertMap(MyMap m)
    {
        return new MyMapEntity(
                m.id,
                m.line
        );
    }
}
