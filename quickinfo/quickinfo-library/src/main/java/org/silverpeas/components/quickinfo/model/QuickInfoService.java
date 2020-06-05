/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

import org.silverpeas.components.quickinfo.NewsByStatus;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

public interface QuickInfoService extends ApplicationService<News> {

  static QuickInfoService get() {
    return ServiceProvider.getService(QuickInfoService.class);
  }

  News create(News news);

  void publish(String id, String userId);

  void update(final News news, List<PdcPosition> positions, Collection<UploadedFile> uploadedFiles,
      final boolean forcePublishing);

  void removeNews(String id);

  List<News> getAllNews(String componentId);

  NewsByStatus getAllNewsByStatus(String componentId, String userId);

  List<News> getVisibleNews(String componentId);

  List<News> getPlatformNews(String userId);

  List<News> getNewsForTicker(String userId);

  List<News> getUnreadBlockingNews(String userId);

  void acknowledgeNews(String id, String userId);

  News getNews(String id);

  News getNewsByForeignId(String id);

  void submitNewsOnHomepage(String id, String userId);

  /**
   * Performs processes about news linked to given reminder.<br/>
   * If news is not concerned, nothing is performed.
   * @param reminder a {@link Reminder} instance.
   */
  void performReminder(final Reminder reminder);
}
