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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.whitepages.service;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchEngineException;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.search.SearchService;
import org.silverpeas.core.util.CollectionUtil;

import java.util.List;
import java.util.Map;

@Service
public class MixedSearchServiceImpl implements MixedSearchService {

  @Override
  public List<SearchResult> search(SearchQuery searchQuery) throws SearchEngineException {
    //build the search
    QueryDescription query = new QueryDescription(searchQuery.getQuery());

    //Set the identity of the user who processing the search
    query.setSearchingUser(searchQuery.getUserId());

    //Set the list of all components which are available for the user
    query.addComponent(searchQuery.getComponentId());

    var xmlFields = searchQuery.getXmlFields();
    var xmlTemplate = searchQuery.getXmlTemplate();
    // XML search
    if (xmlFields != null && !xmlFields.isEmpty() && xmlTemplate != null) {
      for (Map.Entry<String, String> entry : xmlFields.entrySet()) {
        String value = entry.getValue();
        value = value.trim().replace("##", " AND ");
        query.addFieldQuery(
            new FieldDescription(xmlTemplate + "$$" + entry.getKey(), value,
                searchQuery.getLanguage()));
      }
    }

    // LDAP and classical Silverpeas fields search
    if (CollectionUtil.isNotEmpty(searchQuery.getFieldsQuery())) {
      query.setFieldQueries(searchQuery.getFieldsQuery());
    }

    query.setTaxonomyPosition(searchQuery.getTaxonomyPosition());

    SearchService searchService = SearchService.get();
    return searchService.search(query);
  }
}