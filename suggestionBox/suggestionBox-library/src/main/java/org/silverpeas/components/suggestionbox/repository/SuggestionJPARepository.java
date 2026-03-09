package org.silverpeas.components.suggestionbox.repository;

import jakarta.inject.Singleton;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

/**
 * The JPA manager dedicated to manage Suggestion instances. This class is to be used only
 * by the {@link org.silverpeas.components.suggestionbox.repository.SuggestionRepository} repository
 * to perform the persistence tasks related to JPA. The Suggestion objects have indeed their part
 * persisted into several data sources.
 * @author mmoquillon
 */
@Singleton
@Repository
public class SuggestionJPARepository extends SilverpeasJpaEntityRepository<Suggestion> {

  @Override
  protected NamedParameters newNamedParameters() {
    // to allow access from SuggestionRepository
    return super.newNamedParameters();
  }
}
