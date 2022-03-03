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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.whitepages.service;

import org.silverpeas.components.whitepages.dao.SearchFieldDao;
import org.silverpeas.components.whitepages.model.SearchField;
import org.silverpeas.core.annotation.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
@Transactional
public class DefaultWhitePagesService implements WhitePagesService {

  public static final String COMPONENT_NAME = "whitePages";
  @Inject
  private SearchFieldDao searchFieldDao;

  @Override
  public void createSearchFields(String[] fields, String instanceId) {

    // delete all old fields
    deleteFields(instanceId);

    // insert new fields
    for (final String field : fields) {
      SearchField searchField = new SearchField();
      searchField.setFieldId(field);
      searchField.setInstanceId(instanceId);
      searchFieldDao.createSearchField(searchField);
    }

  }

  @Override
  public SortedSet<SearchField> getSearchFields(String instanceId) {
    SortedSet<SearchField> fields = new TreeSet<>(new SearchFieldComparator());
    Set<SearchField> searchFields = searchFieldDao.getSearchFields(instanceId);
    if (searchFields != null && searchFields.size() > 0) {
      fields.addAll(searchFields);
    }
    return fields;
  }

  @Override
  public void deleteFields(String instanceId) {
    searchFieldDao.deleteSearchFieldsFor(instanceId);
  }
}
