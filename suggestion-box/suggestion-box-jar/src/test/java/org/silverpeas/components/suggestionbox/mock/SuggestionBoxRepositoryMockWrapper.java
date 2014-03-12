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

import org.silverpeas.components.suggestionbox.model.SuggestionBox;
import org.silverpeas.components.suggestionbox.repository.SuggestionBoxRepository;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.OperationContext;

import static org.mockito.Mockito.mock;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

/**
 * A wrapper of the SuggestionBoxRepository mock for testing purpose and to be managed by the IoC
 * container.
 * @author mmoquillon
 */
@Named("suggestionBoxRepository")
public class SuggestionBoxRepositoryMockWrapper extends SuggestionBoxRepository {

  private SuggestionBoxRepository mock = mock(SuggestionBoxRepository.class);

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
  public SuggestionBox getByIdentifier(UuidIdentifier id) {
    return mock.getByIdentifier(id);
  }

  @Override
  public List<SuggestionBox> getByIdentifier(UuidIdentifier... ids) {
    return mock.getByIdentifier(ids);
  }

  @Override
  public List<SuggestionBox> getByIdentifier(Collection<UuidIdentifier> ids) {
    return mock.getByIdentifier(ids);
  }

  @Override
  public SuggestionBox save(OperationContext context, SuggestionBox entity) {
    return mock.save(context, entity);
  }

  @Override
  public List<SuggestionBox> save(OperationContext context, SuggestionBox... entities) {
    return mock.save(context, entities);
  }

  @Override
  public List<SuggestionBox> save(OperationContext context, List<SuggestionBox> entities) {
    return mock.save(context, entities);
  }

  @Override
  public void delete(SuggestionBox... entity) {
    mock.delete(entity);
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
  public long deleteByIdentifier(UuidIdentifier... ids) {
    return mock.deleteByIdentifier(ids);
  }

  @Override
  public long deleteByIdentifier(Collection<UuidIdentifier> ids) {
    return mock.deleteByIdentifier(ids);
  }

  @Override
  public SuggestionBox getByComponentInstanceId(final String componentInstanceId) {
    return mock.getByComponentInstanceId(componentInstanceId);
  }
}
