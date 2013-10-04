/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.connecteurJDBC.service;

import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import org.silverpeas.connecteurJDBC.control.ConnecteurJDBCService;

import javax.inject.Named;
import java.util.Collection;


@Named()
public class SimpleConnecteurJDBCService implements ConnecteurJDBCService {

  @Override
  public Collection<ConnecteurJDBCConnectionInfoDetail> getConnectionList(
      ConnecteurJDBCConnectionInfoPK pk) throws PersistenceException {
    return getDao().findByWhereClause(pk, "instanceId = '" + pk.getComponentName() + '\'');
  }

  @Override
  public ConnecteurJDBCConnectionInfoDetail getConnection(ConnecteurJDBCConnectionInfoPK pk) throws PersistenceException {
    return getDao().findByPrimaryKey(pk);
  }

  @Override
  public ConnecteurJDBCConnectionInfoPK addConnection(ConnecteurJDBCConnectionInfoDetail detail)
      throws PersistenceException {
    return (ConnecteurJDBCConnectionInfoPK) getDao().add(detail);
  }

  @Override
  public void updateConnection(ConnecteurJDBCConnectionInfoDetail detail)
      throws PersistenceException {
    getDao().update(detail);
  }

  @Override
  public void removeConnection(ConnecteurJDBCConnectionInfoPK pk) throws PersistenceException {
    getDao().remove(pk);
  }

  protected SilverpeasBeanDAO<ConnecteurJDBCConnectionInfoDetail> getDao()
      throws PersistenceException {
    SilverpeasBeanDAO<ConnecteurJDBCConnectionInfoDetail> dao =
        SilverpeasBeanDAOFactory.getDAO(ConnecteurJDBCConnectionInfoDetail.class.getName());
    return dao;
  }

}