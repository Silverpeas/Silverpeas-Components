/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created on 13 avr. 2005
 *
 */
package com.silverpeas.projectManager;

import com.silverpeas.projectManager.control.ProjectManagerSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;

/**
 * @author neysseri
 */
public class ProjectManagerIndexer implements ComponentIndexerInterface {

  private ProjectManagerSessionController pm = null;

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface
   * #index(com.stratelia.silverpeas.peasCore.MainSessionController,
   * com.stratelia.silverpeas.peasCore.ComponentContext)
   */
  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {
    pm = new ProjectManagerSessionController(mainSessionCtrl, context);

    pm.index();
  }
}
