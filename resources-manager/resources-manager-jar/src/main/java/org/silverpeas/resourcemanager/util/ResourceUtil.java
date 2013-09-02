/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.resourcemanager.util;

import com.silverpeas.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.silverpeas.resourcemanager.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * User: Yohann Chastagnier
 * Date: 04/04/13
 */
public class ResourceUtil {

  /**
   * Get a list of resource identifiers from a list of resource.
   * @param resources
   * @return
   */
  public static List<Long> toIdList(List<Resource> resources) {
    List<Long> result = new ArrayList<Long>();
    if (CollectionUtils.isNotEmpty(resources)) {
      for (Resource resource : resources) {
        result.add(resource.getId());
      }
    }
    return result;
  }

  /**
   * Get a list of resource identifiers from a string containing resource identifiers separated by
   * comma.
   * @param stringOfIds
   * @return
   */
  public static List<Long> toIdList(String stringOfIds) {
    List<Long> result = new ArrayList<Long>();
    if (StringUtil.isDefined(stringOfIds)) {
      StringTokenizer tokenizer = new StringTokenizer(stringOfIds, ",");
      while (tokenizer.hasMoreTokens()) {
        result.add(Long.parseLong(tokenizer.nextToken()));
      }
    }
    return result;
  }
}
