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

import com.silverpeas.SilverpeasComponentService;
import com.silverpeas.annotation.Service;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentUserNotificationService;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.components.suggestionbox.SuggestionBoxComponentSettings;
import org.silverpeas.components.suggestionbox.notification.SuggestionBoxSubscriptionUserNotification;
import org.silverpeas.components.suggestionbox.notification.SuggestionPendingValidationUserNotification;
import org.silverpeas.components.suggestionbox.notification.SuggestionValidationUserNotification;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.contribution.model.ContributionValidation;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.upload.UploadedFile;
import org.silverpeas.wysiwyg.control.WysiwygController;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The default implementation of the {@link SuggestionBoxService} interface and of the
 * {@link SilverpeasComponentService} interface.
 * @author mmoquillon
 */
@Service
public class DefaultSuggestionBoxService implements SuggestionBoxService,
    SilverpeasComponentService<Suggestion> {

  @Inject
  private SuggestionBoxRepository suggestionBoxRepository;

  @Inject
  private SuggestionRepository suggestionRepository;

  @Inject
  private CommentUserNotificationService commentUserNotificationService;

  @Inject
  private CommentService commentService;

  /**
   * Gets an instance of a SuggestionBoxService.
   * <p/>
   * This method is a convenient one. It uses the {@link SuggestionBoxServiceFactory} to produce an
   * instance that it returns directly.
   * @return a SuggestionBoxService instance.
   */
  public static SuggestionBoxService getInstance() {
    SuggestionBoxServiceFactory factory = SuggestionBoxServiceFactory.getFactory();
    return factory.getSuggestionBoxService();
  }

  /**
   * Initializes the Suggestion Box component by setting some transversal core services for their
   * use by the component instances. One of these services is the user comment notification.
   */
  @PostConstruct
  public void initialize() {
    commentUserNotificationService.register(SuggestionBoxComponentSettings.COMPONENT_NAME, this);
  }

  /**
   * Releases the uses of the transversal core services that were used by the instances of the
   * Suggestion Box component.
   */
  @PreDestroy
  public void release() {
    commentUserNotificationService.unregister(SuggestionBoxComponentSettings.COMPONENT_NAME);
  }

  @Override
  public SuggestionBox getByComponentInstanceId(String componentInstanceId) {
    return suggestionBoxRepository.getByComponentInstanceId(componentInstanceId);
  }

  @Override
  public void indexSuggestionBox(final SuggestionBox suggestionBox) {
    List<Suggestion> suggestions = findSuggestionsByCriteria(
        SuggestionCriteria.from(suggestionBox).statusIsOneOf(ContributionStatus.VALIDATED));
    for (Suggestion suggestion : suggestions) {
      createSuggestionIndex(suggestion);
    }
  }

  @Override
  public List<Suggestion> findSuggestionsByCriteria(final SuggestionCriteria criteria) {
    List<Suggestion> suggestions = suggestionRepository.findByCriteria(criteria);
    if (criteria.mustLoadWysiwygContent()) {
      for (Suggestion suggestion : suggestions) {
        String content = WysiwygController
            .load(suggestion.getSuggestionBox().getComponentInstanceId(), suggestion.getId(), null);
        suggestion.setContent(content);
      }
    }
    return withCommentCount(suggestions);
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
   * @param uploadedFiles a collection of file to attach to the suggestion.
   */
  @Transactional
  @Override
  public void addSuggestion(final SuggestionBox box, final Suggestion suggestion,
      final Collection<UploadedFile> uploadedFiles) {
    final UserDetail author = suggestion.getLastUpdater();
    SuggestionBox actualBox = suggestionBoxRepository.getById(box.getId());
    actualBox.addSuggestion(suggestion);

    suggestionRepository.save(OperationContext.fromUser(author), suggestion);
    suggestionRepository.flush();

    // Description
    WysiwygController.
        save(suggestion.getContent(), box.getComponentInstanceId(), suggestion.getId(), author.
            getId(), null, false);

    // Attach uploaded files
    if (CollectionUtil.isNotEmpty(uploadedFiles)) {
      for (UploadedFile uploadedFile : uploadedFiles) {
        // Register attachment
        uploadedFile
            .registerAttachment(new ForeignPK(suggestion.getId(), box.getComponentInstanceId()),
                null, false);
      }
    }
  }

  /**
   * Deletes the specified suggestion box.
   * @param box the box to delete from Silverpeas.
   */
  @Transactional
  @Override
  public void deleteSuggestionBox(final SuggestionBox box) {

    // TODO Deletion of all data attached to the box and its suggestions :
    // - votes

    // Finally deleting the box and its suggestions from the persistence.
    suggestionBoxRepository.delete(box);
    suggestionBoxRepository.flush();

    // Deletion of comments
    commentService.deleteAllCommentsByComponentInstanceId(box.getComponentInstanceId());

    // Deletion of box edito
    AttachmentService attachmentService = AttachmentServiceFactory.getAttachmentService();
    attachmentService.deleteAllAttachments(box.getComponentInstanceId());

    // Deleting all component subscriptions
    SubscriptionServiceFactory.getFactory().getSubscribeService()
        .unsubscribeByResource(ComponentSubscriptionResource.from(box.getComponentInstanceId()));
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
    List<Suggestion> suggestions = findSuggestionsByCriteria(criteria.withWysiwygContent());
    if (suggestions.size() == 1) {
      suggestion = withCommentCount(suggestions.get(0));
    }
    return suggestion;
  }

  @Override
  @Transactional
  public void removeSuggestion(SuggestionBox box, Suggestion suggestion) {
    Suggestion actual = suggestionRepository.getById(suggestion.getId());
    if (suggestion.getSuggestionBox().equals(box) && (actual.getValidation().isInDraft() || actual.
        getValidation().isRefused())) {
      suggestionRepository.delete(actual);
      suggestionRepository.flush();

      WysiwygController.deleteWysiwygAttachments(box.getComponentInstanceId(), suggestion.getId());
    }
  }

  @Override
  public Suggestion publishSuggestion(final SuggestionBox box, final Suggestion suggestion) {

    // Persisting the publishing.
    Transaction transaction = Transaction.getTransaction();
    Pair<Suggestion, Boolean> result = transaction.
        perform(new Transaction.Process<Pair<Suggestion, Boolean>>() {
          @Override
          public Pair<Suggestion, Boolean> execute() {
            boolean triggerNotif = false;
            Suggestion actual = findSuggestionById(box, suggestion.getId());
            if (suggestion.getSuggestionBox().equals(box) && (actual.getValidation().isInDraft()
            || actual.getValidation().isRefused())) {
              UserDetail updater = suggestion.getLastUpdater();
              SilverpeasRole greaterUserRole = box.getGreaterUserRole(updater);
              if (greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.writer)) {
                ContributionValidation validation = actual.getValidation();
                if (greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.publisher)) {
                  validation.setStatus(ContributionStatus.VALIDATED);
                  validation.setDate(new Date());
                  validation.setValidator(updater);
                } else {
                  validation.setStatus(ContributionStatus.PENDING_VALIDATION);
                }
                suggestionRepository.save(OperationContext.fromUser(updater), actual);
                suggestionRepository.flush();
                triggerNotif = true;
              }
            }
            return Pair.of(actual, triggerNotif);
          }
        });

    // Sending notification after the persistence is successfully committed.
    Suggestion updatedSuggestion = result.getLeft();
    if (result.getRight()) {
      switch (updatedSuggestion.getValidation().getStatus()) {
        case PENDING_VALIDATION:
          UserNotificationHelper
              .buildAndSend(new SuggestionPendingValidationUserNotification(updatedSuggestion));
          break;
        case VALIDATED:
          createSuggestionIndex(updatedSuggestion);
          UserNotificationHelper
              .buildAndSend(new SuggestionBoxSubscriptionUserNotification(updatedSuggestion));
          break;
      }
    }
    return updatedSuggestion;
  }

  @Override

  public Suggestion validateSuggestion(final SuggestionBox box, final Suggestion suggestion,
      final ContributionValidation validation) {

    // Persisting the validation.
    Transaction transaction = Transaction.getTransaction();
    Pair<Suggestion, Boolean> result = transaction.
        perform(new Transaction.Process<Pair<Suggestion, Boolean>>() {
          @Override
          public Pair<Suggestion, Boolean> execute() {
            boolean triggerNotif = false;
            Suggestion actual = findSuggestionById(box, suggestion.getId());
            if (suggestion.getSuggestionBox().equals(box) && actual.getValidation().
            isPendingValidation()) {
              UserDetail updater = suggestion.getLastUpdater();
              SilverpeasRole greaterUserRole = box.getGreaterUserRole(updater);
              if (greaterUserRole.isGreaterThanOrEquals(SilverpeasRole.publisher)) {
                ContributionValidation actualValidation = actual.getValidation();
                actualValidation.setStatus(validation.getStatus());
                actualValidation.setComment(validation.getComment());
                actualValidation.setDate(new Date());
                actualValidation.setValidator(updater);
              }
              suggestionRepository.save(OperationContext.fromUser(updater), actual);
              suggestionRepository.flush();
              triggerNotif = true;
            }
            return Pair.of(actual, triggerNotif);
          }
        });

    // Sending notification(s) after the persistence is successfully committed.
    Suggestion updatedSuggestion = result.getLeft();
    if (result.getRight()) {
      switch (updatedSuggestion.getValidation().getStatus()) {
        case VALIDATED:
          createSuggestionIndex(updatedSuggestion);
          UserNotificationHelper
              .buildAndSend(new SuggestionBoxSubscriptionUserNotification(updatedSuggestion));
        case REFUSED:
          // The below notification is sent on VALIDATED or REFUSED status.
          UserNotificationHelper
              .buildAndSend(new SuggestionValidationUserNotification(updatedSuggestion));
          break;
      }
    }
    return updatedSuggestion;
  }

  /**
   * Creates a suggestion index.
   * The suggestion validation must be at validated status. Otherwise the index creation is
   * ignored.
   * @param suggestion the suggestion for which the indexation must be performed.
   */
  private void createSuggestionIndex(Suggestion suggestion) {
    SilverTrace.info("suggestionBox", "suggestionBoxService.createSuggestionIndex()",
        "root.MSG_GEN_ENTER_METHOD", "suggestion id = " + suggestion.getId());
    if (suggestion != null && suggestion.getValidation().isValidated()) {
      FullIndexEntry indexEntry = new FullIndexEntry(suggestion.getComponentInstanceId(),
          Suggestion.TYPE,
          suggestion.getId());
      indexEntry.setTitle(suggestion.getTitle());
      indexEntry.setCreationDate(suggestion.getValidation().getDate());
      indexEntry.setCreationUser(suggestion.getCreatedBy());
      WysiwygController.addToIndex(indexEntry,
          new ForeignPK(suggestion.getId(), suggestion.getComponentInstanceId()), null);
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }

  @Override
  public Suggestion getContentById(String contentId) {
    Suggestion suggestion = suggestionRepository.getById(contentId);
    if (suggestion != null) {
      suggestion = withCommentCount(suggestion);
    }
    return suggestion;
  }

  @Override
  public ResourceLocator getComponentSettings() {
    return SuggestionBoxComponentSettings.getSettings();
  }

  @Override
  public ResourceLocator getComponentMessages(String language) {
    return SuggestionBoxComponentSettings.getMessagesIn(language);
  }

  private Suggestion withCommentCount(final Suggestion suggestion) {
    int count = commentService.getCommentsCountOnPublication(suggestion.getContributionType(),
        new ForeignPK(suggestion.getId(), suggestion.getComponentInstanceId()));
    suggestion.setCommentCount(count);
    return suggestion;
  }

  private List<Suggestion> withCommentCount(final List<Suggestion> suggestions) {
    for (Suggestion suggestion : suggestions) {
      withCommentCount(suggestion);
    }
    return suggestions;
  }
}
