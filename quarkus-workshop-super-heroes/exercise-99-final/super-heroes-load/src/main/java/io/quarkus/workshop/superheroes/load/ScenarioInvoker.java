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
package io.quarkus.workshop.superheroes.load;

import com.github.javafaker.Faker;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.lang.String.format;

public abstract class ScenarioInvoker implements Runnable {

    private static Logger LOGGER = Logger.getLogger(ScenarioInvoker.class.getName());

    protected Faker faker = new Faker();

    private WeightedRandomResult<Endpoint> endpointsToExecute = new WeightedRandomResult<>(getEndpoints());

    protected abstract String getTargetUrl();

    protected abstract List<Endpoint> getEndpoints();

    @Override
    public void run() {
        while (true) {
            try {
                final Endpoint endpoint = endpointsToExecute.get();
                final WebTarget webTarget = ClientBuilder.newClient().target(getTargetUrl())
                    .path(endpoint.getPath())
                    .resolveTemplates(endpoint.getTemplates());
                final Response response = webTarget.request().method(endpoint.getMethod(), endpoint.getEntity());
                LOGGER.info(format("%s - %s - %d", endpoint.getMethod(), webTarget.getUri(), response.getStatus()));
                sleep();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
