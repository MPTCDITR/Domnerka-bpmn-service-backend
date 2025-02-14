package com.domnerka.featuredprocessdefinition;

import com.domnerka.dto.prpcess.BPMNProcessDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.camunda.bpm.engine.repository.ProcessDefinition;

@Entity
@Table(name = "featured_process_definitions")
@Data
public class FeaturedProcessDefinitionEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "`key`")
    private String key;

    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private Integer version;

    @Column(name = "resource")
    private String resource;

    @Column(name = "deployment_id")
    private String deploymentId;

    @Column(name = "diagram")
    private String diagram; // Assuming BLOB data for diagrams

    @Column(name = "suspended")
    private Boolean suspended;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "version_tag")
    private String versionTag;

    @Column(name = "history_time_to_live")
    private Integer historyTimeToLive;

    @Column(name = "is_startable_in_tasklist")
    private Boolean isStartableInTasklist;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    // set to form process definition
    public static FeaturedProcessDefinitionEntity fromProcessDefinition(ProcessDefinition processDefinition,
                                                                        BPMNProcessDto bpmnProcessDto) {
        FeaturedProcessDefinitionEntity entity = new FeaturedProcessDefinitionEntity();
        entity.setId(processDefinition.getId());
        entity.setDeploymentId(processDefinition.getDeploymentId());
        entity.setName(processDefinition.getName());
        entity.setKey(processDefinition.getKey());
        entity.setCategory(processDefinition.getCategory());
        entity.setDescription(bpmnProcessDto.getDescription());
        entity.setResource(processDefinition.getResourceName());
        entity.setVersion(processDefinition.getVersion());
        entity.setTenantId(processDefinition.getTenantId());
        entity.setDiagram(processDefinition.getDiagramResourceName());
        entity.setHistoryTimeToLive(processDefinition.getHistoryTimeToLive());
        entity.setSuspended(processDefinition.isSuspended());
        entity.setVersionTag(processDefinition.getVersionTag());
        entity.setIsStartableInTasklist(processDefinition.isStartableInTasklist());
        entity.setIsFeatured(bpmnProcessDto.getIsFeatured());

        return entity;
    }
}
