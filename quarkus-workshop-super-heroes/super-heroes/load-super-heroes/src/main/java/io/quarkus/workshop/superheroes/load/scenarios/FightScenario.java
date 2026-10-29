/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkus.workshop.superheroes.load.scenarios;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.workshop.superheroes.load.client.Fight;
import io.quarkus.workshop.superheroes.load.client.FightsProxy;
import io.quarkus.workshop.superheroes.load.client.Hero;
import io.quarkus.workshop.superheroes.load.client.Villain;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import net.datafaker.Faker;
import net.datafaker.providers.base.Superhero;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class FightScenario extends Scenario<Fight> {
    private static int NB_FIGHT = 10;
    @RestClient
    FightsProxy fightsProxy;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    @Scheduled(every = "${fight-scenario.every.expr}")
    public void run() {
        run(random);
    }

    protected List<Supplier<Response>> callSupplier () {
        Supplier<Response> helloCall = () -> fightsProxy.hello();
        Supplier<Response> allCall = () -> fightsProxy.all();
        Supplier<Response> randomCall = () -> fightsProxy.random();
        Supplier<Response> getCall = () -> fightsProxy.get(idParam());
        Supplier<Response> createCall = () -> fightsProxy.create(create());
        return Stream.of(allCall, helloCall, randomCall, getCall, createCall).collect(Collectors.toList());
    }

    private static String idParam() {
        return ThreadLocalRandom.current().nextInt(0, NB_FIGHT + 1) + "";
    }

    @Override
    protected Fight create() {
        final Superhero fakeHero = faker.superhero();
        Fight fight = new Fight();
        fight.hero = new Hero();
        fight.villain = new Villain();
        if (Math.random() * 100 < 95) {
            fight.hero.name = fakeHero.name();
            fight.hero.otherName = faker.funnyName().name();
            fight.hero.level = faker.number().numberBetween(1, 20);
            fight.hero.picture = faker.internet().url();
            fight.hero.powers = fakeHero.power();
            fight.villain.name = fakeHero.name();
            fight.villain.otherName = faker.funnyName().name();
            fight.villain.level = faker.number().numberBetween(1, 20);
            fight.villain.picture = faker.internet().url();
            fight.villain.powers = fakeHero.power();
        }

        return fight;
    }

}
