/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.rssAgregator.control;

import java.util.List;

import com.silverpeas.rssAgregator.model.RSSItem;
import com.silverpeas.rssAgregator.model.RssAgregatorException;
import com.silverpeas.rssAgregator.model.SPChannel;

public interface RSSService {

  /**
   * 
   * @param applicationId the application identifier
   * @param agregateContent true if sorting all items from date, false else if sorting by channel
   * @return list of RSSItems which are retrieved from application RSS channels
   * @throws RssAgregatorException
   */
  public List<RSSItem> getApplicationItems(String applicationId, boolean agregateContent)  throws RssAgregatorException;
  
  /**
   * Retrieve all the channel of an application
   * @param applicationId the current application identifier (instance of rssagregator application)
   * @return the list of SPChannel
   * @throws RssAgregatorException
   */
  public List<SPChannel> getAllChannels(String applicationId) throws RssAgregatorException;

}
