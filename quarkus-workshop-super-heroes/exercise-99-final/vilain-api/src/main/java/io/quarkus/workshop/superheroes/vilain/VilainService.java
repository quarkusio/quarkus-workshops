package io.quarkus.workshop.superheroes.vilain;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(SUPPORTS)
public class VilainService {

    public List<Vilain> getAllVilains() {
        return Vilain.listAll();
    }

    public Vilain getVilain(String name) {
        return Vilain.findByName(name);
    }
}
