package com.domnerka.bpmnprocess;

import com.domnerka.camundautillity.BPMNModificationUtility;
import com.domnerka.dto.common.PagedResponseDto;
import com.domnerka.dto.prpcess.BPMNProcessDto;
import com.domnerka.dto.prpcess.ExtendedProcessDefinitionDto;
import com.domnerka.dto.prpcess.ProcessDefinitionBpmnDto;
import com.domnerka.exception.BPMNProcessDeploymentException;
import com.domnerka.exception.ProcessXMLReadException;
import com.domnerka.featuredprocessdefinition.FeaturedProcessDefinitionEntity;
import com.domnerka.repository.camunda.DeploymentRepository;
import com.domnerka.repository.camunda.FeaturedProcessDefinitionRepository;
import com.domnerka.util.SecurityContextUtility;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.domnerka.featuredprocessdefinition.FeaturedProcessDefinitionEntity.fromProcessDefinition;
import static com.domnerka.util.GroupsHierarchyUtility.getLowestLevelGroups;


@Service
@RequiredArgsConstructor
public class BPMNProcessService {

    private final RepositoryService repositoryService;

    private final AuthorizationService authorizationService;

    private final IdentityService identityService;

    private final DeploymentRepository deploymentQueryRepository;

    private final FeaturedProcessDefinitionRepository featuredProcessDefinitionRepository;

    public PagedResponseDto<ExtendedProcessDefinitionDto> getAllProcessDefinition(String search, Pageable pageable) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByDeploymentId().desc();

        if (search != null && !search.isEmpty()) {
            query.processDefinitionNameLike("%" + search + "%");
        }
        String currentUser = SecurityContextUtility.getUsername();
        List<String> currentGroups = getLowestLevelGroups(SecurityContextUtility.getGroups());

        identityService.setAuthentication(currentUser, currentGroups);
        List<ProcessDefinition> processDefinitions = query.startablePermissionCheck()
                .listPage(pageable.getPageNumber() * pageable.getPageSize(), pageable.getPageSize());

        long totalProcess = query.count();
        identityService.clearAuthentication();

        // Collect all deployment IDs
        List<String> deploymentIds = processDefinitions.stream()
                .map(ProcessDefinition::getDeploymentId)
                .distinct()
                .collect(Collectors.toList());

        // Use DeploymentQueryRepository to fetch deployment times
        Map<String, Date> deploymentTimes = deploymentQueryRepository.findDeploymentTimesByIds(deploymentIds);

        List<ExtendedProcessDefinitionDto> definitions = processDefinitions.stream()
                .map(definition -> {
                    ExtendedProcessDefinitionDto dto = ExtendedProcessDefinitionDto.fromProcessDefinition(definition);
                    dto.setCreatedAt(deploymentTimes.get(definition.getDeploymentId()));
                    return dto;
                })
                .toList();

        return new PagedResponseDto<>(definitions, totalProcess);
    }

    public ProcessDefinitionBpmnDto getProcessDefinition(String processDefinitionId) {
        ProcessDefinition processDefinition = getProcessDefinitionById(processDefinitionId);
        String bpmnXmlContent = getBpmnXmlContent(processDefinition);
        FeaturedProcessDefinitionEntity featuredProcess = getFeaturedProcessDefinition(processDefinitionId);

        ProcessDefinitionDto processDefinitionDto = ProcessDefinitionDto.fromProcessDefinition(processDefinition);
        return new ProcessDefinitionBpmnDto(processDefinitionDto, bpmnXmlContent,
                featuredProcess.getIsFeatured(),
                featuredProcess.getDescription(),
                featuredProcess.getCategory());
    }

    private ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        try {
            return repositoryService.getProcessDefinition(processDefinitionId);
        } catch (ProcessEngineException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No matching process definition with id " + processDefinitionId, e);
        }
    }

    private String getBpmnXmlContent(ProcessDefinition processDefinition) {
        try (InputStream resourceStream = repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(), processDefinition.getResourceName());
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new ProcessXMLReadException("Failed to read BPMN XML content", e);
        }
    }

    private FeaturedProcessDefinitionEntity getFeaturedProcessDefinition(String processDefinitionId) {
        return featuredProcessDefinitionRepository.findById(processDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No matching featured process description with id " + processDefinitionId));
    }

    @Transactional
    public String deployBpmnProcess(BPMNProcessDto bpmnProcessDto) {
        String currentUser = SecurityContextUtility.getUsername();
        String randomKey = "process_" + UUID.randomUUID();
        String bpmnXml = bpmnProcessDto.getBpmn().replace("%IDPLACEHOLDER%", randomKey);

        bpmnXml = BPMNModificationUtility.addDescriptionToBpmnXml(bpmnXml, bpmnProcessDto.getDescription());

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        try {
            deploymentBuilder.addString(bpmnProcessDto.getProcessName() + ".bpmn", BPMNModificationUtility
                    .insertIntoEndEventSendMsgNotification(bpmnXml));
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            throw new BPMNProcessDeploymentException("Failed to deploy BPMN process", e);
        }
        deploymentBuilder.name(bpmnProcessDto.getProcessName());

        List<ProcessDefinition> deployedDefinitions = deploymentBuilder.deployWithResult()
                .getDeployedProcessDefinitions();

        if (deployedDefinitions.isEmpty()) {
            throw new RuntimeException("No process definitions were deployed");
        }
        ProcessDefinition processDefinition = deployedDefinitions.get(0);

        FeaturedProcessDefinitionEntity featuredProcessDefinition = fromProcessDefinition(processDefinition,
                bpmnProcessDto);
        featuredProcessDefinitionRepository.saveAndFlush(featuredProcessDefinition);

        Authorization newAuthorization = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
        newAuthorization.setUserId(currentUser);
        newAuthorization.setResource(Resources.PROCESS_DEFINITION);
        newAuthorization.setResourceId(processDefinition.getKey());
        newAuthorization.addPermission(Permissions.ALL);
        authorizationService.saveAuthorization(newAuthorization);

        return "Deployed process definition with id: " + processDefinition.getId();
    }

    @Transactional
    public String updateDeployBpmnProcess(BPMNProcessDto bpmnProcessDto, String key) {

        ProcessDefinition processDefinition = getProcessDefinitionByKey(key);

        FeaturedProcessDefinitionEntity existingEntity = featuredProcessDefinitionRepository.findLatestByKey(key);

        if (existingEntity == null) {
            throw new RuntimeException("Existing FeaturedProcessDefinitionEntity with key " + key + " not found");
        }

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        try {
            String updatedBpmn = BPMNModificationUtility.insertIntoEndEventSendMsgNotification(
                    bpmnProcessDto.getBpmn());
            updatedBpmn = updatedBpmn.replaceFirst("name=\"(.*?)\"",
                    "name=\"" + bpmnProcessDto.getProcessName() + "\"");

            updatedBpmn = BPMNModificationUtility.addDescriptionToBpmnXml(updatedBpmn, bpmnProcessDto.getDescription());

            deploymentBuilder.addString(bpmnProcessDto.getProcessName() + ".bpmn", updatedBpmn);
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
            throw new BPMNProcessDeploymentException("Failed to update BPMN process", e);
        }

        deploymentBuilder.name(bpmnProcessDto.getProcessName());

        List<ProcessDefinition> deployedDefinitions = deploymentBuilder.deployWithResult()
                .getDeployedProcessDefinitions();

        if (deployedDefinitions.isEmpty()) {
            throw new RuntimeException("No process definitions were deployed");
        }
        ProcessDefinition firstDeployedDefinition = deployedDefinitions.get(0);

        FeaturedProcessDefinitionEntity updatedProcess = fromProcessDefinition(
                firstDeployedDefinition,
                bpmnProcessDto);
        featuredProcessDefinitionRepository.saveAndFlush(updatedProcess);

        return "Updated process definition with key: " + key;
    }

    private ProcessDefinition getProcessDefinitionByKey(String processDefinitionKey) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();

        if (processDefinition == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Process definition is not found: " + processDefinitionKey);
        }
        return processDefinition;
    }

    @Transactional
    public void deleteBpmnProcess(String key) {
        try {
            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(key).list();

            if (processDefinitions.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No matching definition with key: " + key);
            }

            for (ProcessDefinition definition : processDefinitions) {
                try {
                    repositoryService.deleteDeployment(definition.getDeploymentId());
                    featuredProcessDefinitionRepository.deleteById(definition.getId());
                } catch (ProcessEngineException e) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Cannot delete process definition, there are active instance(s) depend on it.");
                }
            }

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An error occurred while deleting process definitions: " + e.getMessage(), e);
        }
    }
}
