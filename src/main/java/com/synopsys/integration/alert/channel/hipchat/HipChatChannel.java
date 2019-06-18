/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.hipchat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.ChannelFreemarkerTemplatingService;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.rest.RestChannelUtility;
import com.synopsys.integration.alert.common.AlertConstants;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model2.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.database.api.DefaultAuditUtility;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Component(value = HipChatChannel.COMPONENT_NAME)
public class HipChatChannel extends DistributionChannel {
    public static final String COMPONENT_NAME = "channel_hipchat";
    public static final int MESSAGE_SIZE_LIMIT = 8000;
    private final Logger logger = LoggerFactory.getLogger(HipChatChannel.class);
    private final RestChannelUtility restChannelUtility;
    private final ChannelFreemarkerTemplatingService freemarkerTemplatingService;

    @Autowired
    public HipChatChannel(final Gson gson, final DefaultAuditUtility auditUtility, final RestChannelUtility restChannelUtility,
        final ChannelFreemarkerTemplatingService freemarkerTemplatingService) {
        super(gson, auditUtility);
        this.restChannelUtility = restChannelUtility;
        this.freemarkerTemplatingService = freemarkerTemplatingService;
    }

    @Override
    public String sendMessage(final DistributionEvent event) throws IntegrationException {
        final List<Request> requests = createRequests(event);
        restChannelUtility.sendMessage(requests, event.getDestination());
        return "Successfully sent HipChat message";
    }

    public List<Request> createRequests(final DistributionEvent event) throws IntegrationException {
        final FieldAccessor fieldAccessor = event.getFieldAccessor();
        final String apiKey = fieldAccessor.getString(HipChatDescriptor.KEY_API_KEY).orElseThrow(() -> new AlertException("ERROR: Missing API key in the global HipChat config."));
        final String hostServer = fieldAccessor.getString(HipChatDescriptor.KEY_HOST_SERVER).orElseThrow(() -> new AlertException("ERROR: Missing the server URL in the global HipChat config."));
        final Integer roomId = fieldAccessor.getInteger(HipChatDescriptor.KEY_ROOM_ID).orElseThrow(() -> new AlertException("Room ID missing"));
        final Boolean notify = fieldAccessor.getBoolean(HipChatDescriptor.KEY_NOTIFY).orElse(false);
        final String color = fieldAccessor.getString(HipChatDescriptor.KEY_COLOR).orElse("Red");

        final String htmlMessage = createHtmlMessage(event.getContent());
        if (isChunkedMessageNeeded(htmlMessage)) {
            return createChunkedRequestList(hostServer, apiKey, roomId, notify, color, event.getProvider(), htmlMessage);
        } else {
            return Arrays.asList(createRequest(hostServer, apiKey, roomId, notify, color, htmlMessage));
        }
    }

    private boolean isChunkedMessageNeeded(final String htmlMessage) {
        if (StringUtils.isNotBlank(htmlMessage)) {
            return htmlMessage.length() > MESSAGE_SIZE_LIMIT;
        } else {
            return false;
        }
    }

    private List<Request> createChunkedRequestList(final String hostServer, final String apiKey, final Integer roomId, final Boolean notify, final String color, final String provider, final String htmlMessage) {
        final int contentLength = htmlMessage.length();
        logger.info("Message too large.  Creating chunks...");
        logger.info("Content length: {}", contentLength);
        final int additionPage = (contentLength % MESSAGE_SIZE_LIMIT == 0) ? 0 : 1;
        final int requestCount = (contentLength / MESSAGE_SIZE_LIMIT) + additionPage;
        logger.info("Number of requests to submit: {}", requestCount);
        final List<Request> requestList = new ArrayList<>(requestCount);
        int end = 0;
        int currentRequest = 1;
        while (end < contentLength) {
            logger.info("Creating request {} of {}", currentRequest, requestCount);
            final String contentTitle = String.format("%s (part %d of %d)<br/>", provider, currentRequest, requestCount);
            final int start = end;
            end = end + MESSAGE_SIZE_LIMIT;
            final String content;
            if (end > htmlMessage.length()) {
                content = htmlMessage.substring(start);
            } else {
                final String arbitrarilySplitMessage = htmlMessage.substring(start, end);
                final String lineBreak = "<br/>";
                final int lastBreak = arbitrarilySplitMessage.lastIndexOf("<br/>");
                if (lastBreak > 0) {
                    end = start + lastBreak + lineBreak.length();
                }
                content = htmlMessage.substring(start, end);
            }
            requestList.add(createRequest(hostServer, apiKey, roomId, notify, color, contentTitle + content));
            currentRequest++;
        }

        return requestList;
    }

    public Request createRequest(final String hostServer, final String apiKey, final Integer roomId, final Boolean notify, final String color, final String htmlMessage) {
        final String jsonString = getJsonString(htmlMessage, AlertConstants.ALERT_APPLICATION_NAME, notify, color);

        final String url = hostServer + "/v2/room/" + roomId + "/notification";

        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + apiKey);
        requestHeaders.put("Content-Type", "application/json");

        return restChannelUtility.createPostMessageRequest(url, requestHeaders, jsonString);
    }

    private String createHtmlMessage(final MessageContentGroup contentGroup) throws AlertException {
        try {
            final String templateDirectoryPath = freemarkerTemplatingService.getTemplatePath("hipchat");

            final HashMap<String, Object> model = new HashMap<>();
            model.put("content", contentGroup);

            final Configuration freemarkerConfig = freemarkerTemplatingService.createFreemarkerConfig(templateDirectoryPath);
            final Template template = freemarkerConfig.getTemplate("message_content.ftl");
            return freemarkerTemplatingService.getResolvedTemplate(model, template);
        } catch (final IOException | TemplateException e) {
            throw new AlertException(e);
        }
    }

    private String getJsonString(final String htmlMessage, final String from, final boolean notify, final String color) {
        final JsonObject json = new JsonObject();
        json.addProperty("message_format", "html");
        if (htmlMessage != null) {
            json.addProperty("message", htmlMessage);
        }
        if (from != null) {
            json.addProperty("from", from);
        }
        json.addProperty("notify", notify);
        if (color != null) {
            json.addProperty("color", color);
        }
        return json.toString();
    }

    @Override
    public String getDestinationName() {
        return COMPONENT_NAME;
    }
}
