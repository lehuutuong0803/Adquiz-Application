package com.adquiz.content.repository;

import com.adquiz.content.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByTopicIdAndBloomLevel(UUID topicId, Short bloomLevel);

    Long countByTopicIdAndBloomLevel(UUID topicId, Short bloomLevel);

}
