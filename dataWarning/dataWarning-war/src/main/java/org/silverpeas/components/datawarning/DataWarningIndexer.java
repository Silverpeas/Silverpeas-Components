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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package org.silverpeas.components.datawarning;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.web.index.components.ComponentIndexation;
import org.silverpeas.kernel.annotation.Technical;

import javax.inject.Named;
import javax.inject.Singleton;

@Technical
@Service
@Singleton
@Named("dataWarning" + ComponentIndexation.QUALIFIER_SUFFIX)
public class DataWarningIndexer implements ComponentIndexation {

  @Override
  public void index(SilverpeasComponentInstance componentInst) {
    // nothing to index
  }


  @Override
  public void index(final SilverpeasComponentInstance componentInst, final boolean deleteAllBefore) {
    // PLEASE REMOVE THIS METHOD OVERRIDING IF INDEX METHOD IS IMPLEMENTED
    index(componentInst);
  }
}
