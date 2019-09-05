/**
 * alert-common
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
package com.synopsys.integration.alert.common.persistence.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;

@Component
public class FilePersistenceUtil {
    private final File parentDataDirectory;
    private final File secretsDirectory;
    private final Gson gson;

    @Autowired
    public FilePersistenceUtil(final AlertProperties alertProperties, final Gson gson) {
        this.gson = gson;
        String dataDirectory = "data/";
        if (StringUtils.isNotBlank(alertProperties.getAlertConfigHome())) {
            dataDirectory = String.format("%s/data", alertProperties.getAlertConfigHome());
        }
        this.parentDataDirectory = new File(dataDirectory);
        this.secretsDirectory = new File("/run/secrets");
    }

    public void writeToFile(final String fileName, final String content) throws IOException {
        writeToFile(createFile(fileName), content.getBytes(StandardCharsets.UTF_8));
    }

    public void writeToFile(String fileName, File sourceFile) throws IOException {
        writeToFile(createFile(fileName), Files.readAllBytes(sourceFile.toPath()));
    }

    public void writeToFile(File destination, byte[] data) throws IOException {
        // destination is a path to a file.  Make sure all parent directories exist before writing.
        destination.getParentFile().mkdirs();
        Files.write(destination.toPath(),data,StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void writeJsonToFile(final String fileName, final Object content) throws IOException {
        final String jsonString = gson.toJson(content);
        writeToFile(fileName, jsonString);
    }

    public String readFromFile(final String fileName) throws IOException {
        return readFromFile(parentDataDirectory, fileName);
    }

    public String readFromSecretsFile(final String secretsFileName) throws IOException {
        return readFromFile(secretsDirectory, secretsFileName);
    }

    private String readFromFile(final File parentDirectory, final String fileName) throws IOException {
        return Files.readString(createFile(parentDirectory, fileName).toPath(), StandardCharsets.UTF_8);
    }

    public <T> T readJsonFromFile(final String fileName, final Class<T> clazz) throws IOException {
        final String jsonString = readFromFile(fileName);
        return gson.fromJson(jsonString, clazz);
    }

    public boolean exists(final String fileName) {
        return exists(parentDataDirectory, fileName);
    }

    public boolean exists(final File parentDirectory, final String fileName) {
        final File file = createFile(parentDirectory, fileName);
        return file.exists();
    }

    public void delete(final String fileName) throws IOException {
        final File file = createFile(fileName);
        FileUtils.forceDelete(file);
    }

    private File createFile(final String fileName) {
        return createFile(parentDataDirectory, fileName);
    }

    private File createFile(final File parent, final String fileName) {
        return new File(parent, fileName);
    }
}
