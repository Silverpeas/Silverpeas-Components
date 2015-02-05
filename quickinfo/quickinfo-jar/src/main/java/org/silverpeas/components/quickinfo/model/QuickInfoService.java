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

package org.silverpeas.components.quickinfo.model;

import java.util.List;

import com.silverpeas.ApplicationService;
import org.silverpeas.components.quickinfo.NewsByStatus;

import com.silverpeas.pdc.model.PdcPosition;
import org.silverpeas.util.ServiceProvider;

public interface QuickInfoService extends ApplicationService<News> {

  static QuickInfoService get() {
    return ServiceProvider.getService(QuickInfoService.class);
  }

  public News create(News news);

  public void publish(String id, String userId);

  public void update(final News news, List<PdcPosition> positions, final boolean forcePublishing);

  public void removeNews(String id);

  public List<News> getAllNews(String componentId);

  public NewsByStatus getAllNewsByStatus(String componentId, String userId);

  public List<News> getVisibleNews(String componentId);

  public List<News> getPlatformNews(String userId);

  public List<News> getNewsForTicker(String userId);

  public List<News> getUnreadBlockingNews(String userId);

  public void acknowledgeNews(String id, String userId);

  public News getNews(String id);

  public News getNewsByForeignId(String id);

  public void submitNewsOnHomepage(String id, String userId);

}
