/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.components.whitepages.dao;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.components.whitepages.model.SearchField;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Repository("searchFieldDao")
@Transactional
public class SearchFieldDaoImpl implements SearchFieldDao {

  @PersistenceContext
  private EntityManager theEntityManager;

  private EntityManager getEntityManager() {
    return this.theEntityManager;
  }

  @Override
  public String createSearchField(SearchField searchField) {
    getEntityManager().persist(searchField);
    return searchField.getId();
  }

  @Override
  public void deleteSearchFieldsFor(String instanceId) {
    EntityManager entityManager = getEntityManager();
    Set<SearchField> returnSet = getSearchFields(instanceId);
    if (returnSet != null && !returnSet.isEmpty()) {
      for (SearchField searchField : returnSet) {
        entityManager.remove(searchField);
      }
    }
  }

  @Override
  public Set<SearchField> getSearchFields(String instanceId) {
    TypedQuery<SearchField> query = getEntityManager().createNamedQuery("whitepages.findByInstanceId",
        SearchField.class);
    query.setParameter("instanceId", instanceId);
    return new HashSet<>(query.getResultList());
  }
}
