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
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.wysiwyg.control.WysiwygController;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * A business service to provide a high level interface in the management of the suggestion boxes
 * and of the suggestions.
 * <p>
 * This service isn't intended to be used as such but within the business objects SuggestionBox and
 * Suggestion.
 * <p>
 * @author mmoquillon
 */
@Service
public class SuggestionBoxService {

  @Inject
  private SuggestionBoxRepository repository;

  /**
   * @see org.silverpeas.components.suggestionbox.model.SuggestionBox#getByComponentInstanceId
   * (String)
   */
  SuggestionBox getByComponentInstanceId(String componentInstanceId) {
    return repository.getByComponentInstanceId(componentInstanceId);
  }

  /**
   * Saves the specified suggestion box.
   * @param box the box to save in Silverpeas.
   */
  @Transactional
  public void saveSuggestionBox(final SuggestionBox box) {
    repository.save(OperationContext.fromUser(box.getCreatedBy()), box);
  }

  /**
   * Deletes the specified suggestion box.
   * @param box the box to delete from Silverpeas.
   */
  @Transactional
  public void deleteSuggestionBox(final SuggestionBox box) {

    // TODO Deletion of all data attached to the box and its suggestions :
    // - comments
    // - votes
    // - suggestion attachments

    // Deletion of box edito
    WysiwygController.deleteWysiwygAttachments(box.getComponentInstanceId(), box.getId());

    // Finally deleting the box and its suggestions from the persistence.
    repository.delete(box);
  }

  /**
   * Gets the instance of attachement services.
   * @return
   */
  private AttachmentService getAttachmentService() {
    return AttachmentServiceFactory.getAttachmentService();
  }
}
