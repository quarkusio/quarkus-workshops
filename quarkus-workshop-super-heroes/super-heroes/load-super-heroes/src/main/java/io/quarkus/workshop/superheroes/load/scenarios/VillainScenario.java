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
import io.quarkus.workshop.superheroes.load.client.Villain;
import io.quarkus.workshop.superheroes.load.client.VillainProxy;
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
public class VillainScenario extends Scenario<Villain> {
    private static int NB_VILLAIN = 570;

    @RestClient
    VillainProxy villainProxy;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    @Scheduled(every = "${villain-scenario.every.expr}")
    @Override
    public void run() {
        run(random);
    }

    protected List<Supplier<Response>> callSupplier () {
        Supplier<Response> helloCall = () -> villainProxy.hello();
        Supplier<Response> allCall = () -> villainProxy.all();
        Supplier<Response> randomCall = () -> villainProxy.random();
        Supplier<Response> getCall = () -> villainProxy.get(idParam());
        Supplier<Response> createCall = () -> villainProxy.create(create());
        Supplier<Response> deleteCall = () -> villainProxy.delete(idParam());
        return Stream.of(allCall, helloCall, randomCall, getCall, createCall, deleteCall).collect(Collectors.toList());
    }

    private static String idParam() {
        return ThreadLocalRandom.current().nextInt(0, NB_VILLAIN + 1) + "";
    }

    @Override
    protected Villain create() {
        final Superhero fakeHero = faker.superhero();
        Villain villain = new Villain();
        if (Math.random() * 100 < 95) {
            villain.name = fakeHero.name();
            villain.otherName = faker.funnyName().name();
            villain.level = faker.number().numberBetween(1, 20);
            villain.picture = faker.internet().url();
            villain.powers = fakeHero.power();
        } else {
            villain.otherName = faker.funnyName().name();
            villain.level = 0;
            villain.picture = faker.internet().url();
            villain.powers = fakeHero.power();
        }
        return villain;
    }
}
