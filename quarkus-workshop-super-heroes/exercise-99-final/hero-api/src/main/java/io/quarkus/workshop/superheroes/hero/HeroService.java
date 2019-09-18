package io.quarkus.workshop.superheroes.hero;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.List;

import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(SUPPORTS)
public class HeroService {

    public List<Hero> getAllHeroes() {
        return Hero.listAll();
    }

    public Hero getHero(String name) {
        return Hero.findByName(name);
    }

    @Transactional(REQUIRED)
    public Hero createHero(Hero hero) {
        Hero.persist(hero);
        return hero;
    }
}
