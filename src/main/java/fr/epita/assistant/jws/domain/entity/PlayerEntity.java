package fr.epita.assistant.jws.domain.entity;

import fr.epita.assistant.jws.data.model.Game;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

import java.time.LocalDateTime;


@AllArgsConstructor @NoArgsConstructor @With @ToString
public class PlayerEntity {
    public Long id;
    public int lives;
    public String name;
    public int posX;
    public int posY;
    public boolean canPutBomb;
    public LocalDateTime lastMovement;
    public boolean firstMovement;
}
