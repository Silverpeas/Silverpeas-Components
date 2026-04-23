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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery.service;

import jakarta.inject.Inject;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.kernel.SilverpeasException;

/**
 * Initializes the Gallery application by setting up the different schedulers, one to alert the
 * user, the other to delete orders.
 */
@Service
public class GalleryInitialize implements Initialization {

  @Inject
  private ScheduledAlertUser alertUser;

  @Inject
  private ScheduledDeleteOrder deleteOrder;

  @Override
  public void init() {
    alertUser.initialize();
    deleteOrder.initialize();
  }

  @Override
  public void release() throws SilverpeasException {
    alertUser.release();
    deleteOrder.release();
  }
}