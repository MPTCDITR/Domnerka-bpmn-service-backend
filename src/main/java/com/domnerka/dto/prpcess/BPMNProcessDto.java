package com.domnerka.dto.prpcess;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BPMNProcessDto {

    @NotNull(message = "Process name cannot be null")
    @Size(min = 1, max = 255, message = "Process name must be between 1 and 255 characters")
    private String processName;

    @NotNull(message = "BPMN content cannot be null")
    @Size(min = 1, message = "BPMN content must not be empty")
    private String bpmn;

    public void setProcessName(String processName) {
        this.processName = processName == null ? null : processName.trim();
    }

    public void setBpmn(String bpmn) {
        this.bpmn = bpmn == null ? null : bpmn.trim();
    }

    @NotNull(message = "description cannot be null")
    @Size(min = 1, max = 255, message = "description must be between 1 and 255 characters")
    private String description;

    @NotNull(message = "is featured is required!")
    private Boolean isFeatured;

    private String categoryId;
}
