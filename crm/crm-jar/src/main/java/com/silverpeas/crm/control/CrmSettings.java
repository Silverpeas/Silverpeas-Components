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

package com.silverpeas.crm.control;

import com.stratelia.webactiv.util.ResourceLocator;

public class CrmSettings {
  static private String[] m_Function = null;
  static private String[] m_Event_State = null;
  static private String[] m_Media = null;

  static {
    ResourceLocator rs = new ResourceLocator("com.silverpeas.crm.multilang.crmBundle", "");
    m_Function = rs.getStringArray("crm.", ".function", -1);
    m_Event_State = rs.getStringArray("crm.", ".event_state", -1);
    m_Media = rs.getStringArray("crm.", ".media", -1);
  }

  static public String[] getFunction() {
    return m_Function;
  }

  static public String[] getEventState() {
    return m_Event_State;
  }

  static public String[] getMedia() {
    return m_Media;
  }
}
