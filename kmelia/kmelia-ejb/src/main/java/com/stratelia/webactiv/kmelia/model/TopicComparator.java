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

package com.stratelia.webactiv.kmelia.model;

import com.stratelia.webactiv.util.node.model.NodeDetail;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TopicComparator implements Comparator<NodeDetail> {

  private static final String DEFAULT_NAME = "*";

  private boolean useCriteria;
  private Map<String, Integer> namesWeights;

  public TopicComparator() {
    useCriteria = false;
  }

  public TopicComparator(String[] criteria) {
    if (criteria != null) {
      namesWeights = new HashMap<String, Integer>();
      int i = 0;
      for (i = 0; i < criteria.length; i++) {
        namesWeights.put(criteria[i].toLowerCase(), i);
      }
      if (i > 0 && !namesWeights.containsKey(DEFAULT_NAME)) {
        namesWeights.put(DEFAULT_NAME, i);
      }
    }
    useCriteria = (namesWeights != null && !namesWeights.isEmpty());
  }


  private Integer getNameWeight(String name) {
    return (namesWeights.containsKey(name) ? namesWeights.get(name) : namesWeights.get(DEFAULT_NAME));
  }

  @Override
  public int compare(NodeDetail node1, NodeDetail node2) {
    int result = 0;
    if (node1.getId() > 2 && node2.getId() > 2) {
      String name1 = node1.getName().toLowerCase();
      String name2 = node2.getName().toLowerCase();
      if (useCriteria) {
        result = getNameWeight(name1).compareTo(getNameWeight(name2));
      }
      if (result == 0) {
        result = name1.compareTo(name2);
      }
    }
    return result;
  }

}