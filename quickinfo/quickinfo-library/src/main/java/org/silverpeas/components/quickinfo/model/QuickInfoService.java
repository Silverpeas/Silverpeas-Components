/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

package org.silverpeas.components.quickinfo.model;

import org.silverpeas.components.quickinfo.NewsByStatus;
import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.model.PasteDetail;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuickInfoService extends ApplicationService {

  static QuickInfoService get() {
    return ServiceProvider.getService(QuickInfoService.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  Optional<News> getContributionById(ContributionIdentifier contributionId);

  News create(News news);

  /**
   * Creating new news from an existing one.
   *
   * @param newsToCopy news to copy.
   * @param pasteDetail the details about the copy (user identifier and component destination).
   */
  void copyNews(News newsToCopy, PasteDetail pasteDetail);

  /**
   * Publishes the specified news of a QuickInfo instance.
   * @param newsRef the reference of the news (its identifier and its QuickInfo instance).
   * @param userId the unique identifier of the user publishing the news.
   * @throws org.silverpeas.kernel.exception.NotFoundException if the instance has no such news.
   */
  void publish(ResourceReference newsRef, String userId);

  void update(final News news, List<PdcPosition> positions, Collection<UploadedFile> uploadedFiles,
      final boolean forcePublishing);

  /**
   * Removes the specified news from the QuickInfo instance it belongs to.
   * @param newsRef the reference of the news (its identifier and its QuickInfo instance).
   * @throws org.silverpeas.kernel.exception.NotFoundException if the instance has no such news.
   */
  void removeNews(ResourceReference newsRef);

  List<News> getAllNews(String componentId);

  NewsByStatus getAllNewsByStatus(String componentId, String userId);

  List<News> getVisibleNews(String componentId);

  List<News> getPlatformNews(String userId);

  List<News> getNewsForTicker(String userId);

  List<News> getUnreadBlockingNews(String userId);

  void acknowledgeNews(ResourceReference newsRef, String userId);

  /**
   * Gets the specified news in the QuickInfo instance it belongs to.
   * @param newsRef the reference of the news (its identifier and its QuickInfo instance).
   * @return the news or null if the instance has no such news.
   */
  News getNews(ResourceReference newsRef);

  News getNewsByForeignId(String id);

  /**
   * Submits the specified news of a QuickInfo instance to the news of the main page.
   * @param newsRef the reference of the news (its identifier and its QuickInfo instance).
   * @param userId the unique identifier of the user submitting the news.
   * @throws org.silverpeas.kernel.exception.NotFoundException if the instance has no such news.
   */
  void submitNewsOnHomepage(ResourceReference newsRef, String userId);

  /**
   * Performs processes about news linked to given reminder.<br/>
   * If news is not concerned, nothing is performed.
   * @param reminder a {@link Reminder} instance.
   */
  void performReminder(final Reminder reminder);
}
