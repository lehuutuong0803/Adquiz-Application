package com.adquiz.content.repository;


import com.adquiz.content.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findByParentIsNull();

    List<Topic> findByParentId(UUID parentId);

    @Query(value = """
            SELECT * FROM topics
            WHERE parent_id = :parentId
            AND (name ILIKE '%' || :query || '%' OR similarity(name, :query) > 0.2)
            ORDER BY similarity(name, :query) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Topic> findSimilarSubtopics(
            @Param("query") String query,
            @Param("parentId") UUID parentId
    );

    Optional<Topic> findByNameIgnoreCase(String name);

    @Query(value = """
            SELECT * FROM topics
            WHERE parent_id IS NULL
            AND (name ILIKE '%' || :query || '%' OR similarity(name, :query) > 0.2)
            ORDER BY similarity(name, :query) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Topic> findSimilarParentTopics(@Param("query") String query);

    @Query(value = """
            SELECT t FROM Topic t
            LEFT JOIN FETCH t.parent WHERE t.id = :id
            """)
    Optional<Topic> findByIdWithParent(@Param("id") UUID id);
}
