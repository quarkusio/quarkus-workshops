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

import io.quarkus.logging.Log;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.resteasy.reactive.client.api.ClientLogger;

@ApplicationScoped
public class RequestLogger implements ClientLogger {

    @Override
    public void setBodySize(int bodySize) {
    }

    @Override
    public void logResponse(HttpClientResponse response, boolean redirect) {
        Log.infof("%s - %s - %d", response.request().getMethod().name(), response.request().absoluteURI(), response.statusCode());
    }

    @Override
    public void logRequest(HttpClientRequest request, Buffer body, boolean omitBody) {
    }
}
