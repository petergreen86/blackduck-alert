/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.channel.jira.server.action.JiraServerGlobalValidationAction;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.ReadPageController;
import com.synopsys.integration.alert.common.rest.api.StaticConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

@RestController
@RequestMapping(AlertRestConstants.JIRA_SERVER_CONFIGURATION_PATH)
public class JiraServerGlobalConfigController implements StaticConfigResourceController<JiraServerGlobalConfigModel>, ValidateController<JiraServerGlobalConfigModel>, ReadPageController<AlertPagedModel<JiraServerGlobalConfigModel>> {
    private final JiraServerGlobalValidationAction jiraServerGlobalValidationAction;
    private final IJiraServerGlobalTestAction jiraServerGlobalTestAction;
    private final IJiraServerGlobalConfigAction jiraServerGlobalConfigAction;
    private final JiraServerDisablePluginAction jiraServerDisablePluginAction;

    @Autowired
    public JiraServerGlobalConfigController(
        JiraServerGlobalValidationAction jiraServerGlobalValidationAction, IJiraServerGlobalTestAction jiraServerGlobalTestAction, IJiraServerGlobalConfigAction jiraServerGlobalConfigAction,
        JiraServerDisablePluginAction jiraServerDisablePluginAction
    ) {
        this.jiraServerGlobalValidationAction = jiraServerGlobalValidationAction;
        this.jiraServerGlobalTestAction = jiraServerGlobalTestAction;
        this.jiraServerGlobalConfigAction = jiraServerGlobalConfigAction;
        this.jiraServerDisablePluginAction = jiraServerDisablePluginAction;
    }

    @Override
    public JiraServerGlobalConfigModel getOne(UUID id) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigAction.getOne(id));
    }

    @Override
    public AlertPagedModel<JiraServerGlobalConfigModel> getPage(Integer pageNumber, Integer pageSize, String searchTerm) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigAction.getPaged(pageNumber, pageSize));
    }

    @Override
    public JiraServerGlobalConfigModel create(JiraServerGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigAction.create(resource));
    }

    @Override
    public void update(UUID id, JiraServerGlobalConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigAction.update(id, resource));
    }

    @Override
    public ValidationResponseModel validate(JiraServerGlobalConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalValidationAction.validate(requestBody));
    }

    @Override
    public void delete(UUID id) {
        ResponseFactory.createContentResponseFromAction(jiraServerGlobalConfigAction.delete(id));
    }

    @PostMapping("/test")
    public ValidationResponseModel test(/*@RequestBody*/ JiraServerGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(jiraServerGlobalTestAction.testWithPermissionCheck(resource));
    }

    @PostMapping("disable-plugin")
    public ValidationResponseModel disablePlugin(JiraServerGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(jiraServerDisablePluginAction.disablePlugin(resource));
    }

}
