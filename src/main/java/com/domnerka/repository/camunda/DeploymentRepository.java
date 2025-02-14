package com.domnerka.repository.camunda;

import com.domnerka.entity.camunda.DeploymentResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class DeploymentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find deployment times for given deployment IDs.
     *
     * @param deploymentIds list of deployment IDs
     * @return map of deployment ID to deployment time
     */
    @Transactional(readOnly = true)
    public Map<String, Date> findDeploymentTimesByIds(List<String> deploymentIds) {
        if (deploymentIds == null || deploymentIds.isEmpty()) {
            return Map.of();
        }

        String jpql = "SELECT d FROM DeploymentResult d WHERE d.id IN :deploymentIds";

        TypedQuery<DeploymentResult> query = entityManager.createQuery(jpql, DeploymentResult.class);
        query.setParameter("deploymentIds", deploymentIds);

        List<DeploymentResult> results = query.getResultList();

        return results.stream().collect(Collectors.toMap(
                DeploymentResult::getId,
                DeploymentResult::getDeployTime));
    }
}
