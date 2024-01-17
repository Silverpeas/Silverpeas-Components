/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.SearchEngineException;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;
import java.util.Map;

public interface MixedSearchService {

  static MixedSearchService get() {
    return ServiceProvider.getService(MixedSearchService.class);
  }

  /**
   * Applies the specified query.
   * @param query the search query.
   * @return a List of SearchResult
   * @throws SearchEngineException if an error occurs during the search.
   */
  List<SearchResult> search(SearchQuery query) throws SearchEngineException;


  class SearchQuery {
    private String componentId;
    private String userId;
    private String query;
    private String taxonomyPosition;
    private Map<String, String> xmlFields;
    private String xmlTemplate;
    private List<FieldDescription> fieldsQuery;
    private String language;

    public String getComponentId() {
      return componentId;
    }

    public String getUserId() {
      return userId;
    }

    public String getQuery() {
      return query;
    }

    public String getTaxonomyPosition() {
      return taxonomyPosition;
    }

    public Map<String, String> getXmlFields() {
      return xmlFields;
    }

    public String getXmlTemplate() {
      return xmlTemplate;
    }

    public List<FieldDescription> getFieldsQuery() {
      return fieldsQuery;
    }

    public String getLanguage() {
      return language;
    }

    public SearchQuery setComponentId(String componentId) {
      this.componentId = componentId;
      return this;
    }

    public SearchQuery setUserId(String userId) {
      this.userId = userId;
      return this;
    }

    public SearchQuery setQuery(String query) {
      this.query = query;
      return this;
    }

    public SearchQuery setTaxonomyPosition(String taxonomyPosition) {
      this.taxonomyPosition = taxonomyPosition;
      return this;
    }

    public SearchQuery setXmlFields(Map<String, String> xmlFields) {
      this.xmlFields = xmlFields;
      return this;
    }

    public SearchQuery setXmlTemplate(String xmlTemplate) {
      this.xmlTemplate = xmlTemplate;
      return this;
    }

    public SearchQuery setFieldsQuery(List<FieldDescription> fieldsQuery) {
      this.fieldsQuery = fieldsQuery;
      return this;
    }

    public SearchQuery setLanguage(String language) {
      this.language = language;
      return this;
    }
  }
}
