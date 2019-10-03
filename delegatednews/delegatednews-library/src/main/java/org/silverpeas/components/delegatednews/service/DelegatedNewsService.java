/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.delegatednews.service;

import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface DelegatedNewsService {

  class Constants {
    public static final String DELEGATED_COMPONENT_PARAM = "delegatedNews";

    private Constants() {
    }
  }

  static DelegatedNewsService get() {
    return ServiceProvider.getService(DelegatedNewsService.class);
  }

  void submitNews(String id, SilverpeasContent news, String lastUpdaterId, Period visibilityPeriod,
      String userId);

  DelegatedNews getDelegatedNews(int pubId);

  List<DelegatedNews> getDelegatedNews(Collection<String> pubIds);

  List<DelegatedNews> getAllDelegatedNews();

  List<DelegatedNews> getAllValidDelegatedNews();

  void validateDelegatedNews(int pubId, String validatorId);

  void refuseDelegatedNews(int pubId, String validatorId, String refusalMotive);

  void updateDateDelegatedNews(int pubId, Date dateHourBegin, Date dateHourEnd);

  void updateDelegatedNews(PublicationPK publicationPK, String updaterId, Period visibilityPeriod);

  void deleteDelegatedNews(int pubId);

  DelegatedNews updateOrderDelegatedNews(int pubId, int newsOrder);
}
