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
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.wysiwyg.control.WysiwygController;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * The default implementation of the {@link SuggestionBoxService} interface.
 * @author mmoquillon
 */
@Service
public class DefaultSuggestionBoxService implements SuggestionBoxService {

  @Inject
  private SuggestionBoxRepository suggestionBoxRepository;

  @Inject
  private SuggestionRepository suggestionRepository;

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Gets an instance of a SuggestionBoxService.
   * <p>
   * This method is a convenient one. It uses the {@link SuggestionBoxServiceFactory} to produce an
   * instance that it returns directly.
   * @return a SuggestionBoxService instance.
   */
  public static SuggestionBoxService getInstance() {
    SuggestionBoxServiceFactory factory = SuggestionBoxServiceFactory.getFactory();
    return factory.getSuggestionBoxService();
  }

  @Override
  public SuggestionBox getByComponentInstanceId(String componentInstanceId) {
    return suggestionBoxRepository.getByComponentInstanceId(componentInstanceId);
  }

  /**
   * Saves the specified suggestion box.
   * @param box the box to save in Silverpeas.
   */
  @Transactional
  @Override
  public void saveSuggestionBox(final SuggestionBox box) {
    String author = box.getLastUpdatedBy();
    suggestionBoxRepository.save(OperationContext.fromUser(author), box);
  }

  /**
   * Adds into the specified suggestion box the new specified suggestion.
   * @param box a suggestion box
   * @param suggestion a new suggestions to add into the suggestion box.
   */
  @Transactional
  @Override
  public void addSuggestion(final SuggestionBox box, final Suggestion suggestion) {
    final UserDetail author = suggestion.getLastUpdater();
    SuggestionBox actualBox = suggestionBoxRepository.getById(box.getId());
    actualBox.addSuggestion(suggestion);

    suggestionRepository.save(OperationContext.fromUser(author), suggestion);
    suggestionRepository.flush();

    WysiwygController.
        save(suggestion.getContent(), box.getComponentInstanceId(), suggestion.getId(), author.
            getId(), null, false);
  }

  /**
   * Deletes the specified suggestion box.
   * @param box the box to delete from Silverpeas.
   */
  @Transactional
  @Override
  public void deleteSuggestionBox(final SuggestionBox box) {

    // TODO Deletion of all data attached to the box and its suggestions :
    // - comments
    // - votes
    // - suggestion attachments

    // Finally deleting the box and its suggestions from the persistence.
    suggestionBoxRepository.delete(box);
    suggestionBoxRepository.flush();

    // Deletion of box edito
    WysiwygController.deleteWysiwygAttachments(box.getComponentInstanceId(), box.getId());
  }

  @Override
  @Transactional
  public void updateSuggestion(final Suggestion suggestion) {
    suggestionRepository.
        save(OperationContext.fromUser(suggestion.getLastUpdater()), suggestion);
    suggestionRepository.flush();

    if (suggestion.isContentModified()) {
      WysiwygController.save(suggestion.getContent(), suggestion.getSuggestionBox().getId(),
          suggestion.getId(), suggestion.getLastUpdatedBy(), null, false);
    }
  }

  @Override
  public Suggestion getSuggestionById(SuggestionBox box, String suggestionId) {
    Suggestion suggestion = suggestionRepository.getById(suggestionId);
    if (suggestion == null || !suggestion.getSuggestionBox().equals(box)) {
      suggestion = Suggestion.NONE;
    }
    return suggestion;
  }
}
