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
package com.synopsys.integration.alert.channel.email;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model2.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.provider.Provider;

@Component
public class EmailAddressHandler {
    private final List<Provider> providers;

    @Autowired
    public EmailAddressHandler(final List<Provider> providers) {
        this.providers = providers;
    }

    public FieldAccessor updateEmailAddresses(final String providerName, final MessageContentGroup contentGroup, final FieldAccessor originalAccessor) {
        if (StringUtils.isBlank(providerName)) {
            return originalAccessor;
        }
        return providers
                   .stream()
                   .filter(provider -> provider.getName().equals(providerName))
                   .findFirst()
                   .map(Provider::getEmailHandler)
                   .map(emailHandler -> emailHandler.updateFieldAccessor(contentGroup, originalAccessor))
                   .orElse(originalAccessor);
    }

}
