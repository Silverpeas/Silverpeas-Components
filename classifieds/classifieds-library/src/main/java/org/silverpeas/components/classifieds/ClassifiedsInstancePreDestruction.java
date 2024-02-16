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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.classifieds;

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.components.classifieds.service.ClassifiedService;
import org.silverpeas.components.classifieds.service.ClassifiedServiceProvider;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Deletes all the subscriptions and classifieds of the Classifieds instance that is being deleted.
 * @author mmoquillon
 */
@Named
public class ClassifiedsInstancePreDestruction implements ComponentInstancePreDestruction {
  /**
   * Performs pre destruction tasks in the behalf of the specified Classifieds instance.
   * @param componentInstanceId the unique identifier of the Classifieds instance.
   */
  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    try {
      ClassifiedService service = ClassifiedServiceProvider.getClassifiedService();
      service.deleteAllClassifieds(componentInstanceId);
      service.deleteAllSubscribes(componentInstanceId);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }
}
