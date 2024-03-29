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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.components.infoletter;

import org.silverpeas.components.infoletter.model.InfoLetter;
import org.silverpeas.components.infoletter.model.InfoLetterPublication;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.components.infoletter.service.InfoLetterServiceProvider;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.silverstatistics.volume.service.ComponentStatisticsProvider;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Provider
@Named("infoLetter" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class InfoLetterStatistics implements ComponentStatisticsProvider {

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId) {
    List<InfoLetterPublication> publications = getInfoLetters(componentId);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<>(publications.size());

    for (int i = 0; i < publications.size(); i++) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId("-2");
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  private List<InfoLetterPublication> getInfoLetters(String componentId) {
    List<InfoLetterPublication> publications = new ArrayList<>();
    InfoLetterService dataInterface = InfoLetterServiceProvider.getInfoLetterData();
    List<InfoLetter> listLettres = dataInterface.getInfoLetters(componentId);
    if (listLettres != null) {
      for (InfoLetter defaultLetter : listLettres) {
        List<InfoLetterPublication> listParutions =
            dataInterface.getInfoLetterPublications(defaultLetter.getPK());
        publications.addAll(listParutions);
      }
    }
    return publications;
  }
}
