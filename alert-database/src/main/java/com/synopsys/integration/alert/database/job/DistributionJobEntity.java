package com.synopsys.integration.alert.database.job;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.database.job.email.EmailJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsEntity;
import com.synopsys.integration.alert.database.job.msteams.MSTeamsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.slack.SlackJobDetailsEntity;

@Entity
@Table(schema = "alert", name = "distribution_jobs")
public class DistributionJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "name")
    private String name;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "distribution_frequency")
    private String distributionFrequency;

    @Column(name = "processing_type")
    private String processingType;

    @Column(name = "channel_descriptor_name")
    private String channelDescriptorName;

    @OneToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private BlackDuckJobDetailsEntity blackDuckJobDetails;

    @OneToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private AzureBoardsJobDetailsEntity azureBoardsJobDetails;

    @OneToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private EmailJobDetailsEntity emailJobDetails;

    @OneToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private JiraCloudJobDetailsEntity jiraCloudJobDetails;

    @OneToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private JiraServerJobDetailsEntity jiraServerJobDetails;

    @OneToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private MSTeamsJobDetailsEntity msTeamsJobDetails;

    @OneToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id", insertable = false, updatable = false)
    private SlackJobDetailsEntity slackJobDetails;

    public DistributionJobEntity() {
    }

    public DistributionJobEntity(UUID jobId, String name, Boolean enabled, String distributionFrequency, String processingType, String channelDescriptorName) {
        this.jobId = jobId;
        this.name = name;
        this.enabled = enabled;
        this.distributionFrequency = distributionFrequency;
        this.processingType = processingType;
        this.channelDescriptorName = channelDescriptorName;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDistributionFrequency() {
        return distributionFrequency;
    }

    public void setDistributionFrequency(String distributionFrequency) {
        this.distributionFrequency = distributionFrequency;
    }

    public String getProcessingType() {
        return processingType;
    }

    public void setProcessingType(String processingType) {
        this.processingType = processingType;
    }

    public String getChannelDescriptorName() {
        return channelDescriptorName;
    }

    public void setChannelDescriptorName(String channelDescriptorName) {
        this.channelDescriptorName = channelDescriptorName;
    }

    public BlackDuckJobDetailsEntity getBlackDuckJobDetails() {
        return blackDuckJobDetails;
    }

    public void setBlackDuckJobDetails(BlackDuckJobDetailsEntity blackDuckJobDetails) {
        this.blackDuckJobDetails = blackDuckJobDetails;
    }

    public AzureBoardsJobDetailsEntity getAzureBoardsJobDetails() {
        return azureBoardsJobDetails;
    }

    public void setAzureBoardsJobDetails(AzureBoardsJobDetailsEntity azureBoardsJobDetails) {
        this.azureBoardsJobDetails = azureBoardsJobDetails;
    }

    public EmailJobDetailsEntity getEmailJobDetails() {
        return emailJobDetails;
    }

    public void setEmailJobDetails(EmailJobDetailsEntity emailJobDetails) {
        this.emailJobDetails = emailJobDetails;
    }

    public JiraCloudJobDetailsEntity getJiraCloudJobDetails() {
        return jiraCloudJobDetails;
    }

    public void setJiraCloudJobDetails(JiraCloudJobDetailsEntity jiraCloudJobDetails) {
        this.jiraCloudJobDetails = jiraCloudJobDetails;
    }

    public JiraServerJobDetailsEntity getJiraServerJobDetails() {
        return jiraServerJobDetails;
    }

    public void setJiraServerJobDetails(JiraServerJobDetailsEntity jiraServerJobDetails) {
        this.jiraServerJobDetails = jiraServerJobDetails;
    }

    public MSTeamsJobDetailsEntity getMsTeamsJobDetails() {
        return msTeamsJobDetails;
    }

    public void setMsTeamsJobDetails(MSTeamsJobDetailsEntity msTeamsJobDetails) {
        this.msTeamsJobDetails = msTeamsJobDetails;
    }

    public SlackJobDetailsEntity getSlackJobDetails() {
        return slackJobDetails;
    }

    public void setSlackJobDetails(SlackJobDetailsEntity slackJobDetails) {
        this.slackJobDetails = slackJobDetails;
    }

}