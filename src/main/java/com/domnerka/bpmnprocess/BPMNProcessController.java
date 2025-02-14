package com.domnerka.bpmnprocess;

import com.domnerka.dto.common.PagedResponseDto;
import com.domnerka.dto.prpcess.BPMNProcessDto;
import com.domnerka.dto.prpcess.ExtendedProcessDefinitionDto;
import com.domnerka.dto.prpcess.ProcessDefinitionBpmnDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bpmn-process")
@RequiredArgsConstructor
public class BPMNProcessController {

    private final BPMNProcessService bpmnProcessService;

    @GetMapping("")
    public PagedResponseDto<ExtendedProcessDefinitionDto> getAllProcessDefinition(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return bpmnProcessService.getAllProcessDefinition(search, pageable);
    }

    @GetMapping("/{processDefinitionId}")
    public ProcessDefinitionBpmnDto getProcessDefinition(@PathVariable String processDefinitionId) {
        return bpmnProcessService.getProcessDefinition(processDefinitionId);
    }

    @PostMapping("")
    public String deployBpmnProcess(@Valid @RequestBody BPMNProcessDto bpmnProcessDto) {
        return bpmnProcessService.deployBpmnProcess(bpmnProcessDto);
    }

    @PutMapping("/process/{key}")
    public ResponseEntity<String> updateBpmnProcess(@PathVariable String key,
                                                    @RequestBody BPMNProcessDto bpmnProcessDto) {
        String result = bpmnProcessService.updateDeployBpmnProcess(bpmnProcessDto, key);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<String> deleteBpmnProcess(@PathVariable String key) {
        bpmnProcessService.deleteBpmnProcess(key);
        return ResponseEntity.ok("Deleted all process definitions with key: " + key);
    }
}
