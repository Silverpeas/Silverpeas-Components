/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.silverpeas.rssAgregator;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.rssAgregator.control.RssAgregatorBm;
import com.silverpeas.rssAgregator.control.RssAgregatorBmImpl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.exception.SilverpeasException;

import java.sql.Connection;

/**
 * @author nesseric
 */
public class RssAgregatorInstanciator implements ComponentsInstanciatorIntf {

  public RssAgregatorInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("RssAgregator", "RssAgregatorInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "componentId = " + componentId);
    SilverTrace.info("RssAgregator", "RssAgregatorInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("RssAgregator", "RssAgregatorInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "componentId = " + componentId);
    RssAgregatorBm rss = new RssAgregatorBmImpl();
    try {
      rss.deleteChannels(componentId);
    } catch (Exception e) {
      throw new InstanciationException("RssAgregatorInstanciator", SilverpeasException.ERROR, "", e);
    }
    SilverTrace.info("RssAgregator", "RssAgregatorInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }
}
