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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.model;

import org.silverpeas.components.suggestionbox.SuggestionBoxComponentSettings;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.rating.service.RatingService;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.SettingBundle;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * The default implementation of the {@link SuggestionBoxService} interface and of the
 * {@link ApplicationService} interface.
 * @author mmoquillon
 */
@Service
public class DefaultSuggestionBoxService implements SuggestionBoxService {

  @Inject
  private SuggestionBoxRepository suggestionBoxRepository;

  @Inject
  private CommentService commentService;

  @Override
  public SuggestionBox getByComponentInstanceId(String componentInstanceId) {
    return suggestionBoxRepository.getByComponentInstanceId(componentInstanceId);
  }

  @Override
  public void indexSuggestionBox(final SuggestionBox suggestionBox) {
    suggestionBox.getSuggestions().index();
  }

  /**
   * Saves the specified suggestion box.
   * @param box the box to save in Silverpeas.
   */
  @Transactional
  @Override
  public void saveSuggestionBox(final SuggestionBox box) {
    suggestionBoxRepository.save(box);
  }

  /**
   * Deletes the specified suggestion box.
   * @param box the box to delete from Silverpeas.
   */
  @Transactional
  @Override
  public void deleteSuggestionBox(final SuggestionBox box) {

    // Finally deleting the box and its suggestions from the persistence.
    suggestionBoxRepository.delete(box);
    suggestionBoxRepository.flush();

    // Deletion of all attachments, WYSIWYG comprised.
    AttachmentService attachmentService = AttachmentServiceProvider.getAttachmentService();
    attachmentService.deleteAllAttachments(box.getComponentInstanceId());

    // Deleting all user ratings
    RatingService.get().deleteComponentRatings(box.getComponentInstanceId());
  }

  @Override
  public Suggestion getContentById(String contentId) {
    return Suggestion.getById(contentId);
  }

  @Override
  public SettingBundle getComponentSettings() {
    return SuggestionBoxComponentSettings.getSettings();
  }

  @Override
  public LocalizationBundle getComponentMessages(String language) {
    return SuggestionBoxComponentSettings.getMessagesIn(language);
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("suggestionBox");
  }
}
