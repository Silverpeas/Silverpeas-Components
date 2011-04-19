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
 * webSiteBm.java
 *
 * Created on 9 avril 2001, 11:25
 */

package com.stratelia.webactiv.webSites.control.ejb;

/** 
 * This is the webSite manager EJB-tier controller of the MVC.
 * It is implemented as a session EJB. It controls all the activities 
 * that happen in a client session.
 * It also provides mechanisms to access other session EJBs.
 * @author Cecile BONIN
 * @version 1.0
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBObject;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.webSites.siteManage.model.FolderDetail;
import com.stratelia.webactiv.webSites.siteManage.model.IconDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;

public interface WebSiteBm extends EJBObject {

  public void setComponentId(String componentId) throws RemoteException;

  public void setPrefixTableName(String prefixTableName) throws RemoteException;

  public void setSpaceName(String space) throws RemoteException;

  public void setActor(UserDetail user) throws RemoteException;

  /* gestion des themes */
  public FolderDetail goTo(String id) throws RemoteException;

  public NodePK addFolder(NodeDetail subtopic, String alertType)
      throws RemoteException;

  public NodePK updateFolder(NodeDetail topic, String alertType)
      throws RemoteException;

  public NodeDetail getFolderDetail(String subTopicId) throws RemoteException;

  public void deleteFolder(String topicId) throws RemoteException;

  public void changeTopicsOrder(String way, NodePK nodePK, NodePK fatherPK)
      throws RemoteException;

  /* gestion des publi */
  public PublicationDetail getPublicationDetail(String pubId)
      throws RemoteException;

  public String createPublication(PublicationDetail pubDetail)
      throws RemoteException;

  public void addPublicationToTopic(String pubId, String fatherId)
      throws RemoteException;

  public void removePublicationToTopic(String pubId, String fatherId)
      throws RemoteException;

  public void updatePublication(PublicationDetail detail)
      throws RemoteException;

  public void deletePublication(String pubId) throws RemoteException;

  public Collection<NodePK> getAllFatherPK(String pubId) throws RemoteException;

  public String getIdPublication(String idSite) throws RemoteException;

  public void updateClassification(String pubId, ArrayList<String> arrayTopic)
      throws RemoteException;

  public void changePubsOrder(String pubId, NodePK nodePK, int direction)
      throws RemoteException;

  /* gestion des sites */
  public Collection<SiteDetail> getAllWebSite() throws RemoteException;

  public SiteDetail getWebSite(String id) throws RemoteException;

  public List<SiteDetail> getWebSites(List<String> ids) throws RemoteException;

  public Collection<IconDetail> getIcons(String id) throws RemoteException;

  public String getNextId() throws RemoteException;

  public Collection<IconDetail> getAllIcons() throws RemoteException;

  public String createWebSite(SiteDetail description) throws RemoteException;

  public void associateIcons(String id, Collection<String> liste)
      throws RemoteException;

  public void publish(Collection<String> liste) throws RemoteException;

  public void dePublish(Collection<String> liste) throws RemoteException;

  public void deleteWebSites(Collection<String> liste) throws RemoteException;

  public int getSilverObjectId(String id) throws RemoteException;

  public void index() throws RemoteException;

  public void updateWebSite(SiteDetail description) throws RemoteException;
}