/**
 * Copyright (C) 2000 - 2015 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.infoletter;

import com.silverpeas.admin.components.ComponentInstancePostConstruction;
import org.silverpeas.components.infoletter.control.InfoLetterServiceProvider;
import org.silverpeas.components.infoletter.model.InfoLetter;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Creates for each spawned InfoLetter instance a default letter and indexes it.
 * @author mmoquillon
 */
@Named
public class InfoLetterInstancePostConstruction implements ComponentInstancePostConstruction {

  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    InfoLetterService infoLetterService = InfoLetterServiceProvider.getInfoLetterData();
    InfoLetter infoLetter = infoLetterService.createDefaultLetter(componentInstanceId);
    FullIndexEntry indexEntry =
        new FullIndexEntry(componentInstanceId, "Lettre", infoLetter.getPK().getId());
    indexEntry.setTitle(infoLetter.getName());
    IndexEngineProxy.addIndexEntry(indexEntry);
  }
}
