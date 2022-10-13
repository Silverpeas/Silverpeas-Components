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

package org.silverpeas.components.quickinfo.service;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.silverstatistics.volume.service.ComponentStatisticsProvider;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Provider
@Named("quickinfo" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class QuickinfoStatistics implements ComponentStatisticsProvider {

  private PublicationService publicationService = null;

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId) {
    Collection<PublicationDetail> infos = getQuickInfos(componentId);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<>(infos.size());
    for (PublicationDetail detail : infos) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(detail.getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }
    return myArrayList;
  }

  private PublicationService getPublicationService() {
    if (publicationService == null) {
      publicationService = PublicationService.get();
    }
    return publicationService;
  }

  private Collection<PublicationDetail> getQuickInfos(String componentId) {
    return getPublicationService().getOrphanPublications(componentId);
  }
}
