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
import io.quarkus.workshop.superheroes.load.client.Hero;
import io.quarkus.workshop.superheroes.load.client.HeroProxy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import net.datafaker.Faker;
import net.datafaker.providers.base.Superhero;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class HeroScenario extends Scenario<Hero> {
    private static int NB_HEROES = 941;

    @RestClient
    HeroProxy heroProxy;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    @Scheduled(every = "${hero-scenario.every.expr}", delay = 2, delayUnit = TimeUnit.SECONDS)
    @Override
    public void run() {
        run(random);
    }

    // tag::adocScenario[]
    protected List<Supplier<Response>> callSupplier () {
        Supplier<Response> helloCall = () -> heroProxy.hello();
        Supplier<Response> allCall = () -> heroProxy.all();
        Supplier<Response> randomCall = () -> heroProxy.random();
        Supplier<Response> getCall = () -> heroProxy.get(idParam());
        Supplier<Response> createCall = () -> heroProxy.create(create());
        Supplier<Response> deleteCall = () -> heroProxy.delete(idParam());
        return Stream.of(allCall, helloCall, randomCall, getCall, createCall, deleteCall).collect(Collectors.toList());
    }
    // end::adocScenario[]

    private static String idParam() {
        return ThreadLocalRandom.current().nextInt(0, NB_HEROES + 1) + "";
    }

    protected Hero create() {
        final Superhero fakeHero = faker.superhero();
        Hero hero = new Hero();
        if (Math.random() * 100 < 95) {
            hero.name = fakeHero.name();
            hero.otherName = faker.funnyName().name();
            hero.level = faker.number().numberBetween(1, 20);
            hero.picture = faker.internet().url();
            hero.powers = fakeHero.power();
        } else {
            hero.otherName = faker.funnyName().name();
            hero.level = 0;
            hero.picture = faker.internet().url();
            hero.powers = fakeHero.power();
        }
        return hero;
    }
}
