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
package io.quarkus.workshop.superheroes.load.client;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/villains")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "villain")
public interface VillainProxy {
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    Response hello();

    @GET
    Response all();

    @GET
    @Path("/random")
    Response random();

    @POST
    Response create(Villain villain);

    @GET
    @Path("/{id}")
    Response get(@PathParam("id") String id);

    @DELETE
    @Path("/{id}")
    Response delete(@PathParam("id") String id);

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        return null;
    }
}
