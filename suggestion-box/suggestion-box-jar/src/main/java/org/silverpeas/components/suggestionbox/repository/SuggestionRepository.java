/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.repository;

import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.jpa.AbstractJpaEntityRepository;
import org.silverpeas.persistence.repository.jpa.NamedParameters;

import javax.inject.Named;

import java.util.List;

/**
 * This entity repository provides all necessary methods in order to handle the persistence of
 * suggestion associated to suggestion boxes.
 * @author Yohann Chastagnier
 */
@Named
public class SuggestionRepository extends AbstractJpaEntityRepository<Suggestion, UuidIdentifier> {

  /**
   * Finds suggestions according to the given suggestion criteria.
   * @param criteria the suggestion criteria.
   * @return the suggestion list corresponding to the given suggestion criteria.
   */
  public List<Suggestion> findByCriteria(final SuggestionCriteria criteria) {
    NamedParameters params = initializeNamedParameters();
    JPQLQueryBuilder queryBuilder = new JPQLQueryBuilder(params);
    criteria.processWith(queryBuilder);

    // Playing th query and returning the requested result
    return listFromJpqlString(queryBuilder.result(), params);
  }
}
