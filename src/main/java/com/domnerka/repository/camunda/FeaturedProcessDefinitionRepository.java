package com.domnerka.repository.camunda;

import com.domnerka.featuredprocessdefinition.FeaturedProcessDefinitionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturedProcessDefinitionRepository extends JpaRepository<FeaturedProcessDefinitionEntity, String> {

    @Query("SELECT f FROM FeaturedProcessDefinitionEntity f WHERE f.key = :key ORDER BY f.version DESC LIMIT 1")
    FeaturedProcessDefinitionEntity findLatestByKey(@Param("key") String key);

    @Query(nativeQuery = true, value = "SELECT f.* FROM featured_process_definitions f " +
            "INNER JOIN (SELECT `key`, MAX(version) as LargestVersion " +
            "FROM featured_process_definitions GROUP BY `key`) sub ON f.`key` = sub.`key` " +
            "AND f.version = sub.LargestVersion", countQuery = "SELECT COUNT(*) FROM (SELECT f.* FROM featured_process_definitions f "
            +
            "INNER JOIN (SELECT `key`, MAX(version) as LargestVersion " +
            "FROM featured_process_definitions GROUP BY `key`) sub ON f.`key` = sub.`key` "
            +
            "AND f.version = sub.LargestVersion) AS count_query")
    Page<FeaturedProcessDefinitionEntity> findAllWithLatestVersionKey(Pageable pageable);

    @Query(nativeQuery = true, value = """
                SELECT fpd.*
                FROM featured_process_definitions fpd
                INNER JOIN (
                    SELECT `key`, MAX(version) as max_version
                    FROM featured_process_definitions
                    GROUP BY `key`
                ) latest ON fpd.`key` = latest.`key` AND fpd.version = latest.max_version
                WHERE fpd.is_featured = true
                ORDER BY fpd.id DESC
                LIMIT 5
            """)
    List<FeaturedProcessDefinitionEntity> findLatestFeaturedProcessDefinitions();

}
