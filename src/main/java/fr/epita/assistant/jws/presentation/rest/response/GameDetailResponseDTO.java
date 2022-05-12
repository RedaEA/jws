package fr.epita.assistant.jws.presentation.rest.response;

import fr.epita.assistant.jws.GameState;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.List;


@Value @With
public class GameDetailResponseDTO {
    public LocalDateTime startTime;
    public GameState state;
    public List<PlayerS> players;
    public List<String> map;
    public Long id;

    @AllArgsConstructor @NoArgsConstructor
    public static class PlayerS
    {
        public Long id;
        public int lives;
        public String name;
        public int posX;
        public int posY;
    }
}
