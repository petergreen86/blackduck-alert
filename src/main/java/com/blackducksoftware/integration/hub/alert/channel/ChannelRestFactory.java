/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.alert.channel;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChannelRestFactory {
    private final RestConnection restConnection;
    private final Logger logger;

    public ChannelRestFactory(final RestConnection restConnection, final Logger logger) {
        this.restConnection = restConnection;
        this.logger = logger;
    }

    public Request createRequest(final List<String> urlSegments, final String jsonBody, final Map<String, String> requestProperties) {
        final HttpUrl httpUrl = restConnection.createHttpUrl(urlSegments);
        return createRequest(httpUrl, jsonBody, requestProperties);
    }

    public Request createRequest(final String url, final String jsonBody, final Map<String, String> requestProperties) {
        final HttpUrl httpUrl = restConnection.createHttpUrl(url);
        return createRequest(httpUrl, jsonBody, requestProperties);
    }

    public Request createRequest(final HttpUrl httpUrl, final String jsonBody, final Map<String, String> requestProperties) {
        final RequestBody body = restConnection.createJsonRequestBody(jsonBody);
        final Request request = restConnection.createPostRequest(httpUrl, requestProperties, body);

        return request;
    }

    public void createResponse(final Request request) {
        Response response = null;
        try {
            logger.info("Attempting to send message...");
            response = restConnection.createResponse(request);
            logger.info("Successfully sent a message!");
            if (logger.isTraceEnabled()) {
                logger.trace("Response: " + response.toString());
            }
        } catch (final IntegrationException e) {
            logger.error("There was a problem generating a response", e);
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }
}
