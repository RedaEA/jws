package fr.epita.assistant.jws.data.repository;

import fr.epita.assistant.jws.data.model.MyMap;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyMapRepository implements PanacheRepositoryBase<MyMap, Long> {
}
