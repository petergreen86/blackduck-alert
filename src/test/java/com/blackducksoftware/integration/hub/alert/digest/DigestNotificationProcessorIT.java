/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.digest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatEvent;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.ConfiguredProjectEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionProjectRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class DigestNotificationProcessorIT {
    @Autowired
    private CommonDistributionRepository commonDistributionRepository;
    @Autowired
    private DistributionProjectRepository distributionProjectRepository;
    @Autowired
    private ConfiguredProjectsRepository configuredProjectsRepository;
    @Autowired
    private NotificationTypesActions<CommonDistributionConfigRestModel> notificationActions;
    @Autowired
    private DigestNotificationProcessor processor;

    @After
    public void cleanup() {
        commonDistributionRepository.deleteAll();
        distributionProjectRepository.deleteAll();
        configuredProjectsRepository.deleteAll();
        notificationActions.getDistributionNotificationTypeRepository().deleteAll();
        notificationActions.getNotificationTypeRepository().deleteAll();
    }

    @Test
    public void processNotificationDataBasicTestIT() {
        final Long distributionConfigId = 10L;
        final String distributionType = SupportedChannels.HIPCHAT;
        final String name = "Config Name";
        final String frequency = "REAL_TIME";
        final Boolean filterByProject = true;

        final String projectName = "Test Hub Project Name";
        final Date lastRan = new Date();
        final StatusEnum status = StatusEnum.FAILURE;

        final CommonDistributionConfigEntity commonDistributionConfigEntity = commonDistributionRepository.save(new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, frequency, filterByProject, lastRan, status));
        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(projectName));
        distributionProjectRepository.save(new DistributionProjectRelation(commonDistributionConfigEntity.getId(), configuredProjectEntity.getId()));

        final CommonDistributionConfigRestModel restModel = new CommonDistributionConfigRestModel(null, null, null, null, null, null, null, Arrays.asList("POLICY_VIOLATION"), lastRan, status);
        notificationActions.saveNotificationTypes(commonDistributionConfigEntity, restModel);

        final List<NotificationEntity> notificationList = new ArrayList<>();
        final NotificationEntity applicableNotification = new NotificationEntity("event_key_1", new Date(), "POLICY_VIOLATION", projectName, "", "", "", "Test Component", "Test Component Version", "Test Policy Rule Name", "Test Person",
                Collections.emptyList());
        final NotificationEntity nonApplicableNotification = new NotificationEntity("event_key_2", new Date(), "POLICY_VIOLATION", "Project that we don't care about", "", "", "", "Test Component", "Test Component Version",
                "Test Policy Rule Name", "Test Person", Collections.emptyList());
        notificationList.add(applicableNotification);
        notificationList.add(nonApplicableNotification);

        final List<AbstractChannelEvent> eventsCreated = processor.processNotifications(DigestTypeEnum.REAL_TIME, notificationList);
        assertEquals(1, eventsCreated.size());
        final AbstractChannelEvent event = eventsCreated.get(0);
        assertTrue(event instanceof HipChatEvent);
        assertEquals(commonDistributionConfigEntity.getId(), event.getCommonDistributionConfigId());
    }

    @Test
    public void processNotificationDataWithSameEventKeyTestIT() {
        final Long distributionConfigId = 10L;
        final String distributionType = SupportedChannels.HIPCHAT;
        final String name = "Config Name";
        final String frequency = "REAL_TIME";
        final Boolean filterByProject = true;

        final String eventKey = "event_key";
        final String projectName = "Test Hub Project Name";
        final String projectVersionName = "Test Hub Project Version Name";
        final Date lastRan = new Date();
        final StatusEnum status = StatusEnum.FAILURE;

        final CommonDistributionConfigEntity commonDistributionConfigEntity = commonDistributionRepository.save(new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, frequency, filterByProject, lastRan, status));
        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(projectName));
        distributionProjectRepository.save(new DistributionProjectRelation(commonDistributionConfigEntity.getId(), configuredProjectEntity.getId()));

        final CommonDistributionConfigRestModel restModel = new CommonDistributionConfigRestModel(null, null, null, null, null, null, null, Arrays.asList("POLICY_VIOLATION"), lastRan, status);
        notificationActions.saveNotificationTypes(commonDistributionConfigEntity, restModel);

        final List<NotificationEntity> notificationList = new ArrayList<>();
        final NotificationEntity applicableNotification = new NotificationEntity(eventKey, new Date(), "POLICY_VIOLATION", projectName, "", projectVersionName, "", "Test Component", "Test Component Version", "Test Policy Rule Name",
                "Test Person", Collections.emptyList());
        final NotificationEntity otherApplicableNotification = new NotificationEntity(eventKey, new Date(), "POLICY_VIOLATION", projectName, "", projectVersionName, "", "Test Component", "Test Component Version", "Test Policy Rule Name",
                "Test Person", Collections.emptyList());
        notificationList.add(applicableNotification);
        notificationList.add(otherApplicableNotification);

        final List<AbstractChannelEvent> eventsCreated = processor.processNotifications(DigestTypeEnum.REAL_TIME, notificationList);
        assertEquals(1, eventsCreated.size());
        final AbstractChannelEvent event = eventsCreated.get(0);
        assertTrue(event instanceof HipChatEvent);
        assertEquals(commonDistributionConfigEntity.getId(), event.getCommonDistributionConfigId());
    }

    @Test
    public void processNotificationDataWithNegatingTypesTestIT() {
        final Long distributionConfigId = 10L;
        final String distributionType = SupportedChannels.HIPCHAT;
        final String name = "Config Name";
        final String frequency = "REAL_TIME";
        final Boolean filterByProject = true;

        final String eventKey = "event_key";
        final String projectName = "Test Hub Project Name";
        final String projectVersionName = "Test Hub Project Version Name";
        final Date lastRan = new Date();
        final StatusEnum status = StatusEnum.FAILURE;

        final CommonDistributionConfigEntity commonDistributionConfigEntity = commonDistributionRepository.save(new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, frequency, filterByProject, lastRan, status));
        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(projectName));
        distributionProjectRepository.save(new DistributionProjectRelation(commonDistributionConfigEntity.getId(), configuredProjectEntity.getId()));

        final CommonDistributionConfigRestModel restModel = new CommonDistributionConfigRestModel(null, null, null, null, null, null, null, Arrays.asList("POLICY_VIOLATION", "POLICY_VIOLATION_CLEARED"), lastRan, status);
        notificationActions.saveNotificationTypes(commonDistributionConfigEntity, restModel);

        final List<NotificationEntity> notificationList = new LinkedList<>();
        final NotificationEntity applicableNotification = new NotificationEntity(eventKey, new Date(), "POLICY_VIOLATION", projectName, "", projectVersionName, "", "Test Component", "Test Component Version", "Test Policy Rule Name",
                "Test Person", Collections.emptyList());
        final NotificationEntity nonApplicableNotification = new NotificationEntity(eventKey, new Date(), "POLICY_VIOLATION_CLEARED", projectName, "", projectVersionName, "", "Test Component", "Test Component Version",
                "Test Policy Rule Name", "Test Person", Collections.emptyList());
        notificationList.add(applicableNotification);
        notificationList.add(nonApplicableNotification);

        final List<AbstractChannelEvent> eventsCreated = processor.processNotifications(DigestTypeEnum.REAL_TIME, notificationList);
        assertEquals(0, eventsCreated.size());
    }

}
