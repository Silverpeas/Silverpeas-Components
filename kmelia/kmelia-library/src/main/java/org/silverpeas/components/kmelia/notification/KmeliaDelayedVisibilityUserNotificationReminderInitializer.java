/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.kmelia.notification;

import org.apache.commons.io.FileUtils;
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
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
public class KmeliaDelayedVisibilityUserNotificationReminderInitializer
    implements Initialization {

  private File dataFile = new File(FileRepositoryManager.getInitDataDirPath(),
      "kmeliaDelayedVisibilityReminderInitialization");

  @Inject
  private KmeliaDelayedVisibilityUserNotificationReminder delayedVisibilityUserNotificationReminder;

  @Override
  public void init() throws Exception {
    if (!dataFile.exists()) {
      final List<PublicationPK> potentialPubIds = JdbcSqlQuery.select("pubId, instanceId")
          .from("sb_publication_publi")
          .where("instanceId like 'kmelia%'")
          .and("pubbegindate >= ?", DateUtil.today2SQLDate())
          .execute(r -> new PublicationPK(String.valueOf(r.getInt(1)), r.getString(2)));
      final StringBuilder report = new StringBuilder((potentialPubIds.size() + 1) * 180);
      try {
        if (potentialPubIds.isEmpty()) {
          report.append("No reminder has been set about delayed visibility.");
        } else {
          potentialPubIds.forEach(i -> {
            final PublicationDetail potentialPub = KmeliaService.get().getPublicationDetail(i);
            if (delayedVisibilityUserNotificationReminder.setAbout(potentialPub)) {
              report.append(MessageFormat.format(
                  "Reminder set for publication with id {0}, located into instance {1}, with " +
                      "visibility set to {2} at {3}\n", potentialPub.getId(),
                  potentialPub.getComponentInstanceId(), LocalDateTime
                      .ofInstant(potentialPub.getBeginDate().toInstant(), ZoneId.systemDefault())
                      .toLocalDate(), potentialPub.getBeginHour()));
            }
          });
        }
      } finally {
        FileUtils.write(dataFile, report.toString(), Charsets.UTF_8);
      }
    }
  }
}
