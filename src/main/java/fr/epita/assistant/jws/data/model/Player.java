package fr.epita.assistant.jws.data.model;

import fr.epita.assistant.jws.GameState;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "player")
@AllArgsConstructor @NoArgsConstructor @With @ToString
public class Player extends PanacheEntityBase {
    public @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    public int lives;
    public String name;
    public int posX;
    public int posY;
    public boolean canPutBomb;
    public LocalDateTime lastMovement;
    public boolean firstMovement;
    public @ManyToOne Game game;
}
