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

import com.silverpeas.comment.service.CommentService;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.EntityRepository;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.persistence.repository.jpa.NamedParameters;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * This entity repository provides all necessary methods in order to handle the persistence of
 * suggestion associated to suggestion boxes.
 * @author Yohann Chastagnier
 */
@Named
public class SuggestionRepository implements EntityRepository<Suggestion, UuidIdentifier> {

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
    if (criteria.mustLoadWysiwygContent()) {
      suggestions = withContent(suggestions);
    }
    return withCommentCount(suggestions);
  }

  @Override
  public List<Suggestion> getAll() {
    return withContent(withCommentCount(suggestionManager.getAll()));
  }

  @Override
  public Suggestion getById(final String id) {
    return withContent(withCommentCount(suggestionManager.getById(id)));
  }

  @Override
  public List<Suggestion> getById(final String... ids) {
    return withContent(withCommentCount(suggestionManager.getById(ids)));
  }

  @Override
  public List<Suggestion> getById(final Collection<String> ids) {
    return withContent(withCommentCount(suggestionManager.getById(ids)));
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

  private Suggestion withContent(final Suggestion suggestion) {
    if (suggestion != null) {
      String content = WysiwygController
          .load(suggestion.getSuggestionBox().getComponentInstanceId(), suggestion.getId(), null);
      suggestion.setContent(content);
    }
    return suggestion;
  }

  private List<Suggestion> withContent(final List<Suggestion> suggestions) {
    for (Suggestion suggestion : suggestions) {
      withContent(suggestion);
    }
    return suggestions;
  }

  private Suggestion withCommentCount(final Suggestion suggestion) {
    if (suggestion != null) {
      int count = commentService.getCommentsCountOnPublication(suggestion.getContributionType(),
          new ForeignPK(suggestion.getId(), suggestion.getComponentInstanceId()));
      suggestion.setCommentCount(count);
    }
    return suggestion;
  }

  private List<Suggestion> withCommentCount(final List<Suggestion> suggestions) {
    for (Suggestion suggestion : suggestions) {
      withCommentCount(suggestion);
    }
    return suggestions;
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
