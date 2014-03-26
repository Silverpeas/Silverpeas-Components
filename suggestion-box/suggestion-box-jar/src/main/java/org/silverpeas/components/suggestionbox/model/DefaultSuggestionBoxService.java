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
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.wysiwyg.control.WysiwygController;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

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

  @Override
  public List<Suggestion> findSuggestionsByCriteria(final SuggestionCriteria criteria) {
    List<Suggestion> suggestions = suggestionRepository.findByCriteria(criteria);
    for (Suggestion suggestion : suggestions) {
      String content = WysiwygController
          .load(suggestion.getSuggestionBox().getComponentInstanceId(), suggestion.getId(), null);
      suggestion.initializeContent(content);
    }
    return suggestions;
  }

  /**
   * Saves the specified suggestion box.
   * @param box the box to save in Silverpeas.
   */
  @Transactional
  @Override
  public void saveSuggestionBox(final SuggestionBox box) {
    final UserDetail author = box.getLastUpdater();
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
    AttachmentService attachmentService = AttachmentServiceFactory.getAttachmentService();
    attachmentService.deleteAllAttachments(box.getComponentInstanceId());
  }

  @Override
  @Transactional
  public void updateSuggestion(final Suggestion suggestion) {
    suggestionRepository.
        save(OperationContext.fromUser(suggestion.getLastUpdater()), suggestion);
    suggestionRepository.flush();

    if (suggestion.isContentModified()) {
      WysiwygController.save(suggestion.getContent(), suggestion.getSuggestionBox().
          getComponentInstanceId(), suggestion.getId(), suggestion.getLastUpdatedBy(), null, false);
    }
  }

  @Override
  public Suggestion findSuggestionById(SuggestionBox box, String suggestionId) {
    Suggestion suggestion = Suggestion.NONE;
    SuggestionCriteria criteria = SuggestionCriteria.from(box).identifierIsOneOf(suggestionId);
    List<Suggestion> suggestions = findSuggestionsByCriteria(criteria);
    if (suggestions.size() == 1) {
      suggestion = suggestions.get(0);
    }
    return suggestion;
  }

  @Override
  @Transactional
  public void removeSuggestion(SuggestionBox box, Suggestion suggestion) {
    Suggestion actual = suggestionRepository.getById(suggestion.getId());
    if (suggestion.getSuggestionBox().equals(box)) {
      suggestionRepository.delete(actual);
      suggestionRepository.flush();

      WysiwygController.deleteWysiwygAttachments(box.getComponentInstanceId(), suggestion.getId());
    }
  }

  @Override
  @Transactional
  public Suggestion publishSuggestion(final SuggestionBox box, final Suggestion suggestion) {
    Suggestion actual = findSuggestionById(box, suggestion.getId());
    if (suggestion.getSuggestionBox().equals(box)) {
      UserDetail updater = suggestion.getLastUpdater();
      SilverpeasRole greaterUserRole = box.getGreaterUserRole(updater);
      if (greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.publisher)) {
        actual.setStatus(ContributionStatus.VALIDATED);
        actual.setApprobationDate(new Date());
      } else if (greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.writer)) {
        actual.setStatus(ContributionStatus.PENDING_VALIDATION);
      }
      suggestionRepository.save(OperationContext.fromUser(updater), actual);
      suggestionRepository.flush();
    }

    return actual;
  }
}
