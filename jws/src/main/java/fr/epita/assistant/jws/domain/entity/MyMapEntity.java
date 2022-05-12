package fr.epita.assistant.jws.domain.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

@AllArgsConstructor @NoArgsConstructor @With @ToString
public class MyMapEntity implements Comparable<MyMapEntity>{
    public Long id;
    public String line;

    @Override
    public int compareTo(MyMapEntity o) {
        return (int)(this.id - o.id);
    }
}
