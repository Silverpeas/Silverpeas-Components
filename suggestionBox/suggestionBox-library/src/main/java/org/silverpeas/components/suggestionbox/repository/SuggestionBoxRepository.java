/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;

/**
 * This entity suggestionRepository provides all necessary methods in order to handle the
 * persistence of suggestion boxes, whatever the data sources used underlying.
 * @author Yohann Chastagnier
 */
@Repository
public class SuggestionBoxRepository extends SilverpeasJpaEntityRepository<SuggestionBox> {

  public static SuggestionBoxRepository get() {
    return ServiceProvider.getService(SuggestionBoxRepository.class);
  }

  @Inject
  private SuggestionRepository suggestionRepository;

  /**
   * Gets the suggestion box represented by the specified component instance identifier.
   * @param componentInstanceId the component instance id associated to the required suggestion
   * box.
   * @return the suggestion box instance if exists, null otherwise.
   */
  public SuggestionBox getByComponentInstanceId(String componentInstanceId) {
    NamedParameters parameters = newNamedParameters();
    return getFromNamedQuery("suggestionBoxFromComponentInstance",
        parameters.add("componentInstanceId", componentInstanceId));
  }
}
