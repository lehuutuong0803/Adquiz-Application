package com.adquiz.content.repository;


import com.adquiz.content.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findByParentIsNull();

    List<Topic> findByParentId(UUID parentId);
}
