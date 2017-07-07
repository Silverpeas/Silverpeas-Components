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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.mock;

import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * A wrapper of the SuggestionBoxRepository mock for testing purpose and to be managed by the IoC
 * container.
 * @author mmoquillon
 */
@Named("suggestionBoxRepository")
public class SuggestionBoxRepositoryMockWrapper extends SuggestionBoxRepository {

  private final SuggestionBoxRepository mock = mock(SuggestionBoxRepository.class);

  public SuggestionBoxRepository getMock() {
    return mock;
  }

  @Override
  public List<SuggestionBox> getAll() {
    return mock.getAll();
  }

  @Override
  public SuggestionBox getById(String id) {
    return mock.getById(id);
  }

  @Override
  public List<SuggestionBox> getById(String... ids) {
    return mock.getById(ids);
  }

  @Override
  public List<SuggestionBox> getById(Collection<String> ids) {
    return mock.getById(ids);
  }

  @Override
  public SuggestionBox save(SuggestionBox entity) {
    return mock.save(entity);
  }

  @Override
  public List<SuggestionBox> save(SuggestionBox... entities) {
    return mock.save(entities);
  }

  @Override
  public List<SuggestionBox> save(List<SuggestionBox> entities) {
    return mock.save(context, entities);
  }

  @Override
  public void delete(SuggestionBox... suggestionBoxes) {
    mock.delete(suggestionBoxes);
  }

  @Override
  public void delete(List<SuggestionBox> entities) {
    mock.delete(entities);
  }

  @Override
  public long deleteById(String... ids) {
    return mock.deleteById(ids);
  }

  @Override
  public long deleteById(Collection<String> ids) {
    return mock.deleteById(ids);
  }

  @Override
  public SuggestionBox getByComponentInstanceId(final String componentInstanceId) {
    return mock.getByComponentInstanceId(componentInstanceId);
  }

  @Override
  public void flush() {
    mock.flush();
  }
}
