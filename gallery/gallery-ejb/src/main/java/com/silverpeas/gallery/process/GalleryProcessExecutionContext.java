/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.process;

import java.sql.Connection;

import org.silverpeas.process.management.ProcessExecutionContext;

import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * @author Yohann Chastagnier
 */
public class GalleryProcessExecutionContext extends ProcessExecutionContext {

  private Connection connection;

  /**
   * @param user
   * @param componentInstanceId
   */
  public GalleryProcessExecutionContext(final UserDetail user, final String componentInstanceId) {
    super(user, componentInstanceId);
  }

  /**
   * @param connection the connection to set
   */
  public void setConnection(final Connection connection) {
    this.connection = connection;
  }

  /**
   * @return the connection
   */
  public Connection getConnection() {
    return connection;
  }
}
