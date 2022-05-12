package fr.epita.assistant.jws.data.model;

import fr.epita.assistant.jws.domain.entity.MyMapEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

import javax.persistence.*;

@Entity @Table(name = "myMap")
@AllArgsConstructor @NoArgsConstructor @With @ToString
public class MyMap extends PanacheEntityBase implements Comparable<MyMap>{
    public @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    public String line;
    public @ManyToOne Game game;

    @Override
    public int compareTo(MyMap o) {
        return (int)(this.id - o.id);
    }

}
