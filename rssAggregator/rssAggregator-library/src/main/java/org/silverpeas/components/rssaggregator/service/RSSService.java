/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.components.rssaggregator.service;

import org.silverpeas.components.rssaggregator.model.RSSItem;
import org.silverpeas.components.rssaggregator.model.RssAgregatorException;
import org.silverpeas.components.rssaggregator.model.SPChannel;

import java.util.List;

public interface RSSService {

  /**
   * Retrieve all the items from all the syndication channels of the specified application.
   * @param applicationId the application identifier
   * @param agregateContent true if sorting all items from date, false else if sorting by channel
   * @return list of RSSItems which are retrieved from application RSS channels
   * @throws RssAgregatorException if an error occurs.
   */
  List<RSSItem> getApplicationItems(String applicationId, boolean agregateContent)  throws RssAgregatorException;

  /**
   * Retrieve all the syndication channels of an application
   * @param applicationId the current application identifier (instance of rssagregator application)
   * @return the list of SPChannel
   * @throws RssAgregatorException if an error occurs.
   */
  List<SPChannel> getAllChannels(String applicationId) throws RssAgregatorException;
}
