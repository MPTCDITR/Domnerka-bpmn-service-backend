package com.domnerka.featuredprocessdefinition;

import com.domnerka.dto.common.PagedResponseDto;
import com.domnerka.repository.camunda.FeaturedProcessDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeaturedProcessDefinitionService {

    private final FeaturedProcessDefinitionRepository processDefinitionRepository;

    public PagedResponseDto<FeaturedProcessDefinitionEntity> getAllProcessDefinitions(Pageable pageable) {
        Page<FeaturedProcessDefinitionEntity> pageResult = processDefinitionRepository
                .findAllWithLatestVersionKey(pageable);

        List<FeaturedProcessDefinitionEntity> definitionEntities = pageResult.getContent();
        return new PagedResponseDto<>(definitionEntities, pageResult.getTotalElements());
    }

    public List<FeaturedProcessDefinitionEntity> getFeaturedProcessDefinitions() {
        return processDefinitionRepository.findLatestFeaturedProcessDefinitions();
    }

    public FeaturedProcessDefinitionEntity getProcessDefinitionById(String processDefinitionId) {
        return processDefinitionRepository.findById(processDefinitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Featured process definition not found with id: " + processDefinitionId));
    }
}
