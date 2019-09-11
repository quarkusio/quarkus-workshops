package io.quarkus.workshop.superheroes.vilain;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(SUPPORTS)
public class VillainService {

    public List<Villain> getAllVilains() {
        return Villain.listAll();
    }

    public Villain getVilain(String name) {
        return Villain.findByName(name);
    }
}
