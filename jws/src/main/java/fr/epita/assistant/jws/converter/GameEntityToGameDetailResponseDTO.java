package fr.epita.assistant.jws.converter;

import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.entity.PlayerEntity;
import fr.epita.assistant.jws.presentation.rest.response.GameDetailResponseDTO;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class GameEntityToGameDetailResponseDTO {

    public GameDetailResponseDTO convert(GameEntity entity) throws IOException {
        return new GameDetailResponseDTO(
                entity.starttime,
                entity.state,
                entity.players.stream().map(this::convertPlayer).collect(Collectors.toList()),
                getMap(entity),
                entity.id
        );
    }

    public GameDetailResponseDTO.PlayerS convertPlayer(PlayerEntity entity)
    {
        return new GameDetailResponseDTO.PlayerS(
                entity.id,
                entity.lives,
                entity.name,
                entity.posX,
                entity.posY
        );
    }

    public List<String> getMap(GameEntity entity)
    {
        Collections.sort(entity.map);
        List<String> res = new ArrayList<>();
        for (int i = 0; i < entity.map.size(); i++)
        {
            res.add(entity.map.get(i).line);
        }
        return res;
    }
}
