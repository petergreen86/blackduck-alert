/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.exception;

import java.util.UUID;

import com.synopsys.integration.alert.common.exception.AlertException;

public class AlertJobMissingException extends AlertException {
    private static final long serialVersionUID = -1163748183484212814L;

    private final UUID missingUUID;

    public AlertJobMissingException(final String message, final UUID missingUUID) {
        super(message);
        this.missingUUID = missingUUID;
    }

    public UUID getMissingUUID() {
        return missingUUID;
    }
}
