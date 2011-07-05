/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.classifieds.control;

import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class ClassifiedsInitialize implements IInitialize {
  public ClassifiedsInitialize() {
  }

  @Override
  public boolean Initialize() {
    initializeJobsScheduling();
    initializeCallbacks();
    return true;
  }

  /**
   * Initializes the scheduling of the classifieds module's jobs
   */
  protected void initializeJobsScheduling() {
    SilverTrace.info("classifieds", getClass().getSimpleName(), "root.EX_NO_MESSAGE",
        "Initialize the scheduling");
    ScheduledUnpublishExpiredClassifieds sc = new ScheduledUnpublishExpiredClassifieds();
    sc.initialize();
  }

  /**
   * Initializes the callbacks subscription within the CallBackManager.
   */
  protected void initializeCallbacks() {
    SilverTrace.info("classifieds", getClass().getSimpleName(), "root.EX_NO_MESSAGE",
        "Initialize the callbacks on events");
    CallBack callback = new ClassifiedCommentCallback();
    callback.subscribe();
  }
}