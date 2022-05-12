package fr.epita.assistant.jws.data.model;

import fr.epita.assistant.jws.GameState;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "game")
@AllArgsConstructor @NoArgsConstructor @With @ToString
public class Game extends PanacheEntityBase {
    public @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    public LocalDateTime starttime;
    public GameState state;
    public @OneToMany(cascade = CascadeType.ALL) List<MyMap> map;
    public @OneToMany(cascade = CascadeType.ALL) List<Player> players;
}
