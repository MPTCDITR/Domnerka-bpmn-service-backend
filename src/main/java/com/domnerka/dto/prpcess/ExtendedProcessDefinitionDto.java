package com.domnerka.dto.prpcess;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;

import java.util.Date;

public class ExtendedProcessDefinitionDto extends ProcessDefinitionDto {
    private Date createdAt;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public static ExtendedProcessDefinitionDto fromProcessDefinition(ProcessDefinition definition) {
        ExtendedProcessDefinitionDto dto = new ExtendedProcessDefinitionDto();
        dto.id = definition.getId();
        dto.key = definition.getKey();
        dto.category = definition.getCategory();
        dto.description = definition.getDescription();
        dto.name = definition.getName();
        dto.version = definition.getVersion();
        dto.resource = definition.getResourceName();
        dto.deploymentId = definition.getDeploymentId();
        dto.diagram = definition.getDiagramResourceName();
        dto.suspended = definition.isSuspended();
        dto.tenantId = definition.getTenantId();
        dto.versionTag = definition.getVersionTag();
        dto.historyTimeToLive = definition.getHistoryTimeToLive();
        dto.isStartableInTasklist = definition.isStartableInTasklist();
        return dto;
    }
}
