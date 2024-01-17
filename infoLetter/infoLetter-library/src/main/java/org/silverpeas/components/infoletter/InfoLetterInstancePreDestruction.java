/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.infoletter;

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.components.infoletter.model.InfoLetterService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * A process to delete all the info letters data related to the application instance that is
 * being deleted.
 * @author mmoquillon
 */
@Named
public class InfoLetterInstancePreDestruction implements ComponentInstancePreDestruction {

  @Inject
  private InfoLetterService service;

  /**
   * Performs pre destruction tasks in the behalf of the specified InfoLetter instance.
   * @param componentInstanceId the unique identifier of the InfoLetter instance.
   */
  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    service.deleteAllInfoLetters(componentInstanceId);
  }
}
