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

package com.silverpeas.whitePages.service;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.silverpeas.whitePages.dao.SearchFieldDao;
import com.silverpeas.whitePages.model.SearchField;

public class WhitePagesServiceImpl implements WhitePagesService {
  public static final String COMPONENT_NAME = "whitePages";

  private SearchFieldDao searchFieldDao;
  
  public SearchFieldDao getSearchFieldDao() {
    return searchFieldDao;
  }

  public void setSearchFieldDao(SearchFieldDao searchFieldDao) {
    this.searchFieldDao = searchFieldDao;
  }

  public void createSearchFields(String[] fields, String instanceId) {
    
    // delete all old fields
    deleteFields(instanceId); 
    
    // insert new fields
    for (int i = 0; i < fields.length; i++) {
      SearchField searchField = new SearchField();
      searchField.setFieldId(fields[i]);
      searchField.setInstanceId(instanceId);
      searchFieldDao.createSearchField(searchField); 
    }
    
  }
  
  public SortedSet<SearchField> getSearchFields(String instanceId) {
    SortedSet<SearchField> fields = new TreeSet<SearchField>(new SearchFieldComparator());
    Set<SearchField> searchFields = searchFieldDao.getSearchFields(instanceId);
    if(searchFields!=null && searchFields.size() > 0){
      fields.addAll(searchFields);
    }
    return fields;
  }
  
  public void deleteFields(String instanceId){
    searchFieldDao.deleteSearchFieldsFor(instanceId); 
  }
  
}
