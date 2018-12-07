/**
 * blackduck-alert
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
package com.synopsys.integration.alert.database.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.synopsys.integration.alert.database.entity.NotificationContent;

public interface NotificationContentRepository extends JpaRepository<NotificationContent, Long> {
    @Query("SELECT entity FROM NotificationContent entity WHERE entity.createdAt BETWEEN ?1 AND ?2 ORDER BY created_at, provider_creation_time asc")
    List<NotificationContent> findByCreatedAtBetween(final Date startDate, final Date endDate);

    @Query("SELECT entity FROM NotificationContent entity WHERE entity.createdAt < ?1 ORDER BY created_at, provider_creation_time asc")
    List<NotificationContent> findByCreatedAtBefore(final Date date);

    @Query(value = "SELECT entity FROM NotificationContent entity WHERE entity.id IN (SELECT notificationId FROM entity.auditNotificationRelations WHERE entity.id = notificationId)")
    Page<NotificationContent> findAllSentNotifications(final Pageable pageable);

    //    @Query(value = "SELECT entity FROM NotificationContent entity WHERE LOWER(?1) LIKE LOWER(entity.provider)  OR "
    //                       + "LOWER(?1) LIKE LOWER(entity.notificationType)  OR "
    //                       + "LOWER(?1) LIKE LOWER(entity.content)  OR "
    //                       + "LOWER(?1) LIKE LOWER(entity.createdAt)  OR "
    //                       + "entity.id IN (SELECT relation FROM entity.auditNotificationRelations relation WHERE entity.id = relation.notificationId AND  relation.auditEntryId IN "
    //                       + "(SELECT audit FROM relation.auditEntryEntity audit WHERE relation.auditEntryId = audit.id AND "
    //                       + "LOWER(?1) LIKE LOWER(audit.timeLastSent)  OR  "
    //                       + "LOWER(?1) LIKE LOWER(audit.status)  OR audit.commonConfigId IN "
    //                       + "(SELECT commonConfig FROM audit.commonDistributionConfigEntity commonConfig WHERE audit.commonConfigId = commonConfig.id AND "
    //                       + "LOWER(?1) LIKE LOWER(commonConfig.name)  OR "
    //                       + "LOWER(?1) LIKE LOWER(commonConfig.distributionType)  "
    //                       + "))) ")
    //    Page<NotificationContent> findMatching(String searchTerm, final Pageable pageable);
}
