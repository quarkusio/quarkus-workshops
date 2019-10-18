// tag::adocTransactional[]
package io.quarkus.workshop.superheroes.hero;

// end::adocTransactional[]
import org.eclipse.microprofile.config.inject.ConfigProperty;
// tag::adocTransactional[]
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.SUPPORTS;

@ApplicationScoped
@Transactional(REQUIRED)
public class HeroService {

    // tag::adocConfigProperty[]
    @ConfigProperty(name = "level.multiplier", defaultValue="1")
    int levelMultiplier;
    // end::adocConfigProperty[]

    @Transactional(SUPPORTS)
    public List<Hero> findAllHeroes() {
        return Hero.listAll();
    }

    @Transactional(SUPPORTS)
    public Hero findHeroById(Long id) {
        return Hero.findById(id);
    }

    @Transactional(SUPPORTS)
    public Hero findRandomHero() {
        Hero randomHero = null;
        while (randomHero == null) {
            randomHero = Hero.findRandom();
        }
        return randomHero;
    }

    // tag::adocPersistHero[]
    public Hero persistHero(@Valid Hero hero) {
        // tag::adocPersistHeroLevel[]
        hero.level = hero.level * levelMultiplier;
        // end::adocPersistHeroLevel[]
        Hero.persist(hero);
        return hero;
    }
    // end::adocPersistHero[]

    public Hero updateHero(@Valid Hero hero) {
        Hero entity = Hero.findById(hero.id);
        entity.name = hero.name;
        entity.otherName = hero.otherName;
        entity.level = hero.level;
        entity.picture = hero.picture;
        entity.powers = hero.powers;
        return entity;
    }

    public void deleteHero(Long id) {
        Hero hero = Hero.findById(id);
        hero.delete();
    }
}
// end::adocTransactional[]
