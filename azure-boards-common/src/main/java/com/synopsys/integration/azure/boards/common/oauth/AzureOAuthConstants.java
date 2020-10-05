/**
 * azure-boards-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.azure.boards.common.oauth;

public class AzureOAuthConstants {
    public static final String DEFAULT_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    public static final String DEFAULT_CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    public static final String REQUEST_BODY_FIELD_ASSERTION = "assertion";
    public static final String REQUEST_BODY_FIELD_CLIENT_ASSERTION_TYPE = "client_assertion_type";
    public static final String REQUEST_BODY_FIELD_CLIENT_ASSERTION = "client_assertion";
    public static final String REQUEST_BODY_FIELD_REDIRECT_URI = "redirect_uri";

    private AzureOAuthConstants() {
        // hiding constructor only constants.
    }
}