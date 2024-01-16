/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quickinfo.notification;

import org.apache.commons.io.FileUtils;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoService;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.inject.Inject;
import java.io.File;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * In charge of initializing the reminder about news with a publish visibility set into the future.
 * @author silveryocha
 */
@Service
public class QuickInfoDelayedVisibilityUserNotificationReminderInitializer
    implements Initialization {

  private File dataFile = new File(FileRepositoryManager.getInitDataDirPath(),
      "quickInfoDelayedVisibilityReminderInitialization");

  @Inject
  private QuickInfoDelayedVisibilityUserNotificationReminder delayedVisibilityUserNotificationReminder;

  @Override
  public void init() throws Exception {
    if (!dataFile.exists()) {
      final List<String> potentialNewsIds = JdbcSqlQuery.createSelect("n.id")
          .from("sc_quickinfo_news n, sb_publication_publi p")
          .where("CAST(n.foreignid AS INT) = p.pubid")
          .and("p.pubbegindate >= ?", DateUtil.today2SQLDate())
          .execute(r -> r.getString(1));
      final StringBuilder report = new StringBuilder((potentialNewsIds.size() + 1) * 180);
      try {
        if (potentialNewsIds.isEmpty()) {
          report.append("No reminder has been set about delayed visibility.");
        } else {
          potentialNewsIds.forEach(i -> {
            final News potentialNews = QuickInfoService.get().getNews(i);
            if (delayedVisibilityUserNotificationReminder.setAbout(potentialNews)) {
              report.append(MessageFormat.format(
                  "Reminder set for news with id {0}, located into instance {1}, linked to " +
                      "publication {2}, with visibility set to {3} at {4}\n", potentialNews.getId(),
                  potentialNews.getComponentInstanceId(), potentialNews.getPublicationId(),
                  LocalDateTime.ofInstant(potentialNews.getPublication().getBeginDate().toInstant(),
                      ZoneId.systemDefault()).toLocalDate(),
                  potentialNews.getPublication().getBeginHour()));
            }
          });
        }
      } finally {
        FileUtils.write(dataFile, report.toString(), Charsets.UTF_8);
      }
    }
  }
}
