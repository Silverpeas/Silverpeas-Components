/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.model;

import com.silverpeas.annotation.Service;
import org.silverpeas.persistence.repository.OperationContext;

import javax.inject.Inject;

import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * A simple persister for testing purpose and with which the transactions are managed
 * <p>
 * @author mmoquillon
 */
@Service
public class PersistenceService {

  @Inject
  private SuggestionBoxRepository suggestionBoxRepository;
  @Inject
  private SuggestionRepository suggestionRepository;

  @Transactional
  public void save(final OperationContext ctx, final SuggestionBox box) {
    if (box.getAddedSuggestions().isEmpty()) {
      suggestionBoxRepository.save(ctx, box);
    } else {
      suggestionRepository.save(ctx, box.getAddedSuggestions());
    }
  }

  @Transactional
  public void delete(final SuggestionBox box) {
    suggestionBoxRepository.delete(box);
  }

  public SuggestionBox getById(String id) {
    return suggestionBoxRepository.getById(id);
  }

  public SuggestionBox getByComponentInstanceId(String componentInstanceId) {
    return suggestionBoxRepository.getByComponentInstanceId(componentInstanceId);
  }

  protected PersistenceService() {
  }
}
