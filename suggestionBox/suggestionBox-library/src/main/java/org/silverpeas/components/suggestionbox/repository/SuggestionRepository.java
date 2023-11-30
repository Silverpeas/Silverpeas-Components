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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.repository;

import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.rating.model.ContributionRating;
import org.silverpeas.core.contribution.rating.service.RatingService;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.persistence.datasource.repository.QueryCriteria;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This entity repository provides all necessary methods in order to handle the persistence of
 * suggestion associated to suggestion boxes.
 * @author Yohann Chastagnier
 */
@Repository
public class SuggestionRepository implements EntityRepository<Suggestion> {

  public static SuggestionRepository get() {
    return ServiceProvider.getService(SuggestionRepository.class);
  }

  @Inject
  private CommentService commentService;

  @Inject
  SuggestionJPARepository suggestionManager;

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
  public SilverpeasList<Suggestion> getAll() {
    return decorate(suggestionManager.getAll(), SuggestionCriteria.from(null).withWysiwygContent());
  }

  @Override
  public Suggestion getById(final String id) {
    return decorate(suggestionManager.getById(id),
        SuggestionCriteria.from(null).withWysiwygContent());
  }

  @Override
  public SilverpeasList<Suggestion> getById(final Collection<String> ids) {
    return decorate(suggestionManager.getById(ids),
        SuggestionCriteria.from(null).withWysiwygContent());
  }

  @Override
  public SilverpeasList<Suggestion> findByCriteria(final QueryCriteria criteria) {
    return decorate(suggestionManager.findByCriteria(criteria),
        SuggestionCriteria.from(null).withWysiwygContent());
  }

  @Override
  public Suggestion save(final Suggestion suggestion) {
    suggestionManager.save(suggestion);
    suggestionManager.flush();

    if (suggestion.isContentModified()) {
      WysiwygController.save(suggestion.getContent(), suggestion.getSuggestionBox().
          getComponentInstanceId(), suggestion.getId(), suggestion.getLastUpdaterId(), null, false);
    }

    return suggestion;
  }

  @Override
  public SilverpeasList<Suggestion> save(final Suggestion... suggestions) {
    return save(Arrays.asList(suggestions));
  }

  @Override
  public SilverpeasList<Suggestion> save(final List<Suggestion> suggestions) {
    for (Suggestion suggestion : suggestions) {
      save(suggestion);
    }
    return SilverpeasList.wrap(suggestions);
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
      commentService.deleteAllCommentsOnResource(suggestion.getContributionType(),
          new ResourceReference(suggestion.getId(), suggestion.getComponentInstanceId()));
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

  @Override
  public void flush() {
    suggestionManager.flush();
  }

  @Override
  public boolean contains(final Suggestion entity) {
    return suggestionManager.contains(entity);
  }

  private Suggestion decorate(final Suggestion suggestion, final SuggestionCriteria criteria) {
    if (criteria.mustLoadWysiwygContent()) {
      withContent(suggestion);
    }
    withCommentCount(suggestion);
    suggestion.setRating(RatingService.get().getRating(suggestion));
    return suggestion;
  }

  private SilverpeasList<Suggestion> decorate(final List<Suggestion> suggestions,
      final SuggestionCriteria criteria) {
    Map<String, ContributionRating> suggestionRatings = RatingService.get()
        .getRatings(suggestions.toArray(new SilverpeasContent[0]));
    for (Suggestion suggestion : suggestions) {
      if (criteria.mustLoadWysiwygContent()) {
        withContent(suggestion);
      }
      withCommentCount(suggestion);
      suggestion.setRating(suggestionRatings.get(suggestion.getId()));
    }
    return SilverpeasList.wrap(suggestions);
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
      int count = commentService.getCommentsCountOnResource(suggestion.getContributionType(),
          new ResourceReference(suggestion.getId(), suggestion.getComponentInstanceId()));
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

    if (suggestion != null && suggestion.getValidation().isValidated()) {
      FullIndexEntry indexEntry =
          new FullIndexEntry(new IndexEntryKey(suggestion.getComponentInstanceId(), Suggestion.TYPE,
              suggestion.getId()));
      indexEntry.setTitle(suggestion.getTitle());
      indexEntry.setCreationDate(suggestion.getValidation().getDate());
      indexEntry.setCreationUser(suggestion.getCreatorId());
      WysiwygController.addToIndex(indexEntry,
          new ResourceReference(suggestion.getId(), suggestion.getComponentInstanceId()), null);
      IndexEngineProxy.addIndexEntry(indexEntry);
    }
  }
}
