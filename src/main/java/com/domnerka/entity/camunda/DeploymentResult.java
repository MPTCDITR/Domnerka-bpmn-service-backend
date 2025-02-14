package com.domnerka.entity.camunda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "ACT_RE_DEPLOYMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentResult {
    @Id
    @Column(name = "ID_")
    private String id;

    @Column(name = "DEPLOY_TIME_")
    private Date deployTime;
}
