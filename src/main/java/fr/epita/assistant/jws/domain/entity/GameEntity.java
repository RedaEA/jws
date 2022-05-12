package fr.epita.assistant.jws.domain.entity;

import fr.epita.assistant.jws.GameState;
import fr.epita.assistant.jws.data.model.MyMap;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor @With @ToString
public class GameEntity {
    public Long id;
    public LocalDateTime starttime;
    public GameState state;
    public List<MyMapEntity> map;
    public List<PlayerEntity> players;
}
