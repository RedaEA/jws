package fr.epita.assistant.jws.presentation.rest.response;

import fr.epita.assistant.jws.GameState;
import lombok.*;

@With @Value
public class GameListResponseDTO {
    Long id;
    int players;
    GameState state;
}
