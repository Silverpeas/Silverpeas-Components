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
package org.silverpeas.components.suggestionbox.mock;

import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionCriteria;
import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.OperationContext;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * A wrapper of the SuggestionRepository mock for testing purpose and to be managed by the IoC
 * container.
 * @author mmoquillon
 */
@Named("suggestionRepository")
public class SuggestionRepositoryMockWrapper extends SuggestionRepository {

  private final SuggestionRepository mock = mock(SuggestionRepository.class);

  public SuggestionRepository getMock() {
    return mock;
  }

  @Override
  public List<Suggestion> getAll() {
    return mock.getAll();
  }

  @Override
  public Suggestion getById(String id) {
    return mock.getById(id);
  }

  @Override
  public List<Suggestion> getById(String... ids) {
    return mock.getById(ids);
  }

  @Override
  public List<Suggestion> getById(Collection<String> ids) {
    return mock.getById(ids);
  }

  @Override
  public Suggestion save(OperationContext context, Suggestion entity) {
    return mock.save(context, entity);
  }

  @Override
  public List<Suggestion> save(OperationContext context, Suggestion... entities) {
    return mock.save(context, entities);
  }

  @Override
  public List<Suggestion> save(OperationContext context, List<Suggestion> entities) {
    return mock.save(context, entities);
  }

  @Override
  public void delete(Suggestion... entity) {
    mock.delete(entity);
  }

  @Override
  public void delete(List<Suggestion> entities) {
    mock.delete(entities);
  }

  @Override
  public long deleteById(String... ids) {
    return mock.deleteById(ids);
  }

  @Override
  public List<Suggestion> findByCriteria(final SuggestionCriteria criteria) {
    return mock.findByCriteria(criteria);
  }

  @Override
  public void index(final Suggestion suggestion) {
    mock.index(suggestion);
  }
}
