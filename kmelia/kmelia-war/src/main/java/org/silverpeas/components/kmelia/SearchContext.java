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
package org.silverpeas.components.kmelia;

import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.node.model.NodeDetail;

import java.io.Serializable;
import java.util.List;

public class SearchContext implements Serializable {

  public static final int NONE = 0;
  public static final int GLOBAL = 1;
  public static final int LOCAL = 2;

  private QueryDescription queryDescription;
  private NodeDetail node;
  private List<KmeliaPublication> results;
  private int paginationIndex;
  private PagesContext formContext;

  public SearchContext(QueryDescription queryDescription, PagesContext formContext) {
    this.queryDescription = queryDescription;
    this.formContext = formContext;
  }

  public String getQuery() {
    return queryDescription.getQuery();
  }

  public NodeDetail getNode() {
    return node;
  }

  public void setNode(final NodeDetail node) {
    this.node = node;
  }

  public QueryDescription getQueryDescription() {
    return queryDescription;
  }

  public List<KmeliaPublication> getResults() {
    return results;
  }

  public void setResults(final List<KmeliaPublication> results) {
    this.results = results;
  }

  public int getPaginationIndex() {
    return paginationIndex;
  }

  public void setPaginationIndex(final int paginationIndex) {
    this.paginationIndex = paginationIndex;
  }

  public PagesContext getFormContext() {
    return formContext;
  }
}
