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
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.persistence.repository.OperationContext;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * A simple persister for testing purpose and with which the transactions are managed.
 * <p/>
 * @author mmoquillon
 */
@Transactional
@Service
public class PersistenceService {

  @Inject
  private SuggestionBoxRepository suggestionBoxRepository;
  @Inject
  private SuggestionRepository suggestionRepository;

  public void saveSuggestionBox(final OperationContext ctx, final SuggestionBox box) {
    suggestionBoxRepository.save(ctx, box);
  }

  public void saveSuggestion(final OperationContext ctx, final SuggestionBox box,
      final Suggestion suggestion) {
    SuggestionBox actualBox = suggestionBoxRepository.getById(box.getId());
    actualBox.addSuggestion(suggestion);
    suggestionRepository.save(ctx, suggestion);
  }

  public void deleteSuggestionBox(final SuggestionBox box) {
    suggestionBoxRepository.delete(box);
  }

  public SuggestionBox getSuggestionBoxById(String id) {
    return suggestionBoxRepository.getById(id);
  }

  public SuggestionBox getSuggestionBoxByComponentInstanceId(String componentInstanceId) {
    return suggestionBoxRepository.getByComponentInstanceId(componentInstanceId);
  }

  public Suggestion getSuggestionById(String suggestionId) {
    return suggestionRepository.getById(suggestionId);
  }

  public List<Suggestion> findByCriteria(SuggestionCriteria criteria) {
    return suggestionRepository.findByCriteria(criteria);
  }

  protected PersistenceService() {
  }
}
