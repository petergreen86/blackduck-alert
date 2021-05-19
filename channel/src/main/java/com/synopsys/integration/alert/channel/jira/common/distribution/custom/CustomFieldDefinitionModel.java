/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common.distribution.custom;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class CustomFieldDefinitionModel extends AlertSerializableModel {
    private final String fieldId;
    private final String fieldType;
    private final @Nullable String fieldArrayItems;

    public CustomFieldDefinitionModel(String fieldId, String fieldType, @Nullable String fieldArrayItems) {
        this.fieldId = fieldId;
        this.fieldType = fieldType;
        this.fieldArrayItems = fieldArrayItems;
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getFieldArrayItems() {
        return fieldArrayItems;
    }

    //TODO
    //IDEA would have an abstract method "createResponseObject"

}
