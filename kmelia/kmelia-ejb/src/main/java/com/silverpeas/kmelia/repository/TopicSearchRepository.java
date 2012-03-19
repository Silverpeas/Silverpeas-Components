package com.silverpeas.kmelia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.silverpeas.kmelia.domain.TopicSearch;

/**
 * Repository that handles TopicSearch beans.
 */
public interface TopicSearchRepository extends JpaRepository<TopicSearch, Long> {

  /**
   * Returns all TopicSearch from Repository with the given instanceId. This method will be
   * translated into a query using the one declared in the {@link Query} annotation declared one.
   * @param instanceId
   * @return
   */
  @Query("from TopicSearch where instanceId = :instanceId")
  List<TopicSearch> findByInstanceId(@Param("instanceId") String instanceId);

}
