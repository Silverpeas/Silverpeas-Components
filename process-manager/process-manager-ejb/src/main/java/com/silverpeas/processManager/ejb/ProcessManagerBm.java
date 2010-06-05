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
package com.silverpeas.processManager.ejb;

import com.silverpeas.processManager.ProcessManagerException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.EJBObject;

public interface ProcessManagerBm extends EJBObject {

  /**
   * Create a process instance for a specific workflow component, by a
   * specific user using one role of thoose defined in a given workflow
   * definition. The contents of a file is passed in as a single parameter. This
   * file is uploaded into the process data and stored in the first field of the
   * file type.
   * 
   * @param componentId
   *            the ID of the component which defines the workflow (must be a
   *            workflow component).
   * @param userId
   *            the current user ID.
   * @param fileName
   *            the name of the file being pushed during process creation.
   * @param fileContent
   *            the full content of the file being pushed during process
   *            creation (as an array of bytes).
   * @return the instance ID of the newly started process
   * @throws ProcessManagerException
   * @throws RemoteException
   */
  String createProcess(String componentId, String userId, String fileName,
      byte[] fileContent) throws ProcessManagerException,
      RemoteException;

  /**
   * Create a process instance for a specific workflow component, by a
   * specific user using one role of thoose defined in a given workflow
   * definition.
   * <p>
   * Some information may be specified that will fill in the creation form of
   * the new process instance. Such data should be placed into a map structure
   * of key-value pairs where keys are the name of the intended fields of the
   * creation form and values are strins (text fields), dates (date fields),
   * colelctions of strings, collections of dates, or a single
   * {@link FileContent} object.
   * </p>
   * <p>
   * {@link FileContent} are used to pass in as an argument a complete file of
   * binary data, loaded into memory.
   * </p>
   *
   * @param componentId
   *            the ID of the component which defines the workflow (must be a
   *            workflow component).
   * @param userId
   *            the current user ID.
   * @param userRole
   *            the role of the user while creating the process instance (this
   *            role must have been defined in the workflow process
   *            definition).
   * @param metadata
   *            a map of all input metadata, coming with the file and
   *            describing it. The key is expected to be the name of a field
   *            in the process form definition (with specification of the type
   *            name of the field), and the value must be the value to put
   *            into this field (it may be a collection of value if the field
   *            is multivaluated, else only the first value is considered).
   * @return the instance ID of the newly started process
   * @throws ProcessManagerException
   * @throws RemoteException
   */
  String createProcess(String componentId, String userId, String userRole,
      Map<String, ? extends Serializable> metadata)
      throws ProcessManagerException, RemoteException;
}
