package org.silverpeas.components.suggestionbox.repository;

import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.jpa.SilverpeasJpaEntityManager;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * The JPA manager dedicated to manage Suggestion instances. This class is to be used only
 * by the {@link org.silverpeas.components.suggestionbox.repository.SuggestionRepository} repository
 * to perform the persistence tasks related to JPA. The Suggestion objects have indeed their part
 * persisted into several data sources.
 * @author mmoquillon
 */
@Singleton
public class SuggestionJPAManager extends SilverpeasJpaEntityManager<Suggestion, UuidIdentifier> {
}
