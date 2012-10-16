/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.whitePages.dao;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.silverpeas.whitePages.model.SearchField;

public class SearchFieldDaoImpl extends HibernateDaoSupport implements SearchFieldDao {

  public String createSearchField(SearchField searchField){
    String id = (String)getSession().save(searchField);
    searchField.setId(id);
    return id;
  }

  public void deleteSearchFieldsFor(String instanceId){
    Set<SearchField> returnSet = getSearchFields(instanceId);
    if (returnSet != null && returnSet.size() > 0) {
      Iterator<SearchField> iterRes = returnSet.iterator();
      while (iterRes.hasNext()) {
        getSession().delete(iterRes.next());
      }
    }
  }
  
  public Set<SearchField> getSearchFields(String instanceId){
    Criteria criteria = getSession().createCriteria(SearchField.class);
    criteria.add(Restrictions.eq("instanceId", instanceId));
    Set<SearchField> returnSet = new HashSet<SearchField>();
    returnSet.addAll(criteria.list());
    return returnSet;
  }
  
}
