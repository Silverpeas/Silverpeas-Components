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

import com.silverpeas.SilverpeasContent;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.notation.ejb.RatingServiceProvider;
import org.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.SilverpeasEntityRepository;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.persistence.repository.jpa.NamedParameters;
import org.silverpeas.rating.ContributionRating;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This entity repository provides all necessary methods in order to handle the persistence of
 * suggestion associated to suggestion boxes.
 * @author Yohann Chastagnier
 */
@Named
public class SuggestionRepository implements
    SilverpeasEntityRepository<Suggestion, UuidIdentifier> {

  @Inject
  private CommentService commentService;

  @Inject
  SuggestionJPAManager suggestionManager;

  /**
   * Finds suggestions according to the given suggestion criteria.
   * @param criteria the suggestion criteria.
   * @return the suggestion list corresponding to the given suggestion criteria.
   */
  public List<Suggestion> findByCriteria(final SuggestionCriteria criteria) {
    NamedParameters params = suggestionManager.newNamedParameters();
    JPQLQueryBuilder queryBuilder = new JPQLQueryBuilder(params);
    criteria.processWith(queryBuilder);

    // Playing the query and returning the requested result
    List<Suggestion> suggestions = suggestionManager.findByCriteria(queryBuilder.result());
    return decorate(suggestions, criteria);
  }

  @Override
  public List<Suggestion> getAll() {
    return decorate(suggestionManager.getAll(), SuggestionCriteria.from(null).withWysiwygContent());
  }

  @Override
  public Suggestion getById(final String id) {
    return decorate(suggestionManager.getById(id),
        SuggestionCriteria.from(null).withWysiwygContent());
  }

  @Override
  public List<Suggestion> getById(final String... ids) {
    return decorate(suggestionManager.getById(ids),
        SuggestionCriteria.from(null).withWysiwygContent());
  }

  @Override
  public List<Suggestion> getById(final Collection<String> ids) {
    return decorate(suggestionManager.getById(ids),
        SuggestionCriteria.from(null).withWysiwygContent());
  }

  @Override
  public Suggestion save(final OperationContext context, final Suggestion suggestion) {
    suggestionManager.save(context, suggestion);
    suggestionManager.flush();

    if (suggestion.isContentModified()) {
      WysiwygController.save(suggestion.getContent(), suggestion.getSuggestionBox().
          getComponentInstanceId(), suggestion.getId(), suggestion.getLastUpdatedBy(), null, false);
    }

    return suggestion;
  }

  @Override
  public List<Suggestion> save(final OperationContext context, final Suggestion... suggestions) {
    return save(context, Arrays.asList(suggestions));
  }

  @Override
  public List<Suggestion> save(final OperationContext context, final List<Suggestion> suggestions) {
    for (Suggestion suggestion : suggestions) {
      save(context, suggestion);
    }
    return suggestions;
  }

  @Override
  public void delete(final Suggestion... suggestions) {
    delete(Arrays.asList(suggestions));
  }

  @Override
  public void delete(final List<Suggestion> suggestions) {
    suggestionManager.delete(suggestions);
    suggestionManager.flush();

    for (Suggestion suggestion : suggestions) {
      WysiwygController
          .deleteWysiwygAttachments(suggestion.getComponentInstanceId(), suggestion.getId());
      commentService.deleteAllCommentsOnPublication(suggestion.getContributionType(),
          new ForeignPK(suggestion.getId(), suggestion.getComponentInstanceId()));
    }
  }

  @Override
  public long deleteById(final String... ids) {
    return deleteById(Arrays.asList(ids));
  }

  @Override
  public long deleteById(final Collection<String> ids) {
    List<Suggestion> suggestions = suggestionManager.getById(ids);
    delete(suggestions);
    return suggestions.size();
  }

  private Suggestion decorate(final Suggestion suggestion, final SuggestionCriteria criteria) {
    if (criteria.mustLoadWysiwygContent()) {
      withContent(suggestion);
    }
    withCommentCount(suggestion);
    suggestion.setRating(RatingServiceProvider.getRatingService().getRating(suggestion));
    return suggestion;
  }

  private List<Suggestion> decorate(final List<Suggestion> suggestions,
      final SuggestionCriteria criteria) {
    Map<String, ContributionRating> suggestionRatings = RatingServiceProvider.getRatingService().getRatings(
        suggestions.toArray(new SilverpeasContent[suggestions.size()]));
    for (Suggestion suggestion : suggestions) {
      if (criteria.mustLoadWysiwygContent()) {
        withContent(suggestion);
      }
      withCommentCount(suggestion);
      suggestion.setRating(suggestionRatings.get(suggestion.getId()));
    }
    return suggestions;
  }

  private void withContent(final Suggestion suggestion) {
    if (suggestion != null) {
      String content = WysiwygController
          .load(suggestion.getSuggestionBox().getComponentInstanceId(), suggestion.getId(), null);
      suggestion.setContent(content);
    }
  }

  private void withCommentCount(final Suggestion suggestion) {
    if (suggestion != null) {
      int count = commentService.getCommentsCountOnPublication(suggestion.getContributionType(),
          new ForeignPK(suggestion.getId(), suggestion.getComponentInstanceId()));
      suggestion.setCommentCount(count);
    }
  }

  /**
   * Indexes the specified suggestion.
   * The suggestion validation must be at validated status. Otherwise the index creation is
   * ignored.
   * @param suggestion the suggestion for which the indexation must be performed.
   */
  public void index(Suggestion suggestion) {
    SilverTrace.info("suggestionBox", "suggestionBoxService.createSuggestionIndex()",
        "root.MSG_GEN_ENTER_METHOD", "suggestion id = " + suggestion.getId());
    if (suggestion != null && suggestion.getValidation().isValidated()) {
      FullIndexEntry indexEntry =
          new FullIndexEntry(suggestion.getComponentInstanceId(), Suggestion.TYPE,
              suggestion.getId());
      indexEntry.setTitle(suggestion.getTitle());
      indexEntry.setCreationDate(suggestion.getValidation().getDate());
      indexEntry.setCreationUser(suggestion.getCreatedBy());
      WysiwygController.addToIndex(indexEntry,
          new ForeignPK(suggestion.getId(), suggestion.getComponentInstanceId()), null);
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }
}
