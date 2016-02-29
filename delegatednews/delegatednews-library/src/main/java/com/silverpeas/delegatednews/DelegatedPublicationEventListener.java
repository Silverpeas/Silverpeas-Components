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
package com.silverpeas.delegatednews;

import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.delegatednews.service.DelegatedNewsService;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import org.silverpeas.publication.notification.PublicationEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author mmoquillon
 */
public class DelegatedPublicationEventListener {

  @Inject
  private DelegatedNewsService delegatedNewsService;

  public void onPublicationUpdate(@Observes PublicationEvent event) throws Exception {
    PublicationDetail pubDetail = event.getTransition().getBefore();
    int pubId = Integer.parseInt(pubDetail.getId());
    if (event.isOnUpdate()) {
      DelegatedNews delegatedNews = delegatedNewsService.getDelegatedNews(pubId);
      if (delegatedNews != null) {
        // remove the news whether the publication isn't more accessible (the access dates have been
        // modified).
        if (!pubDetail.isVisible()) {
          delegatedNewsService.deleteDelegatedNews(pubId);
        } else {
          // update the news.
          delegatedNewsService
              .updateDelegatedNews(pubDetail.getId(), pubDetail, pubDetail.getUpdaterId(),
                  pubDetail.getVisibilityPeriod());
        }
      }
    } else if (event.isOnDeletion()) {
      delegatedNewsService.deleteDelegatedNews(pubId);
    }
  }
}
