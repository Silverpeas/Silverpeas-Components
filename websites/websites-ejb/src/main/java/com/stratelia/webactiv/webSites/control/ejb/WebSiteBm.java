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
 * @author Cécile BONIN
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

  // CBO : ADD
  public void addPublicationToTopic(String pubId, String fatherId)
      throws RemoteException;

  // CBO : ADD
  public void removePublicationToTopic(String pubId, String fatherId)
      throws RemoteException;

  public void updatePublication(PublicationDetail detail)
      throws RemoteException;

  public void deletePublication(String pubId) throws RemoteException;

  // CBO : REMOVE public void updateInfoDetail(String pubId, InfoDetail infos)
  // throws RemoteException;

  // CBO : UPDATE
  // public FolderDetail getPublicationFather(String pubId) throws
  // RemoteException;
  public Collection getAllFatherPK(String pubId) throws RemoteException;

  // CBO : UPDATE
  // public Collection getAllPublication(String idSite) throws RemoteException;
  public String getIdPublication(String idSite) throws RemoteException;

  // CBO : ADD
  public void updateClassification(String pubId, ArrayList arrayTopic)
      throws RemoteException;

  public void changePubsOrder(String pubId, NodePK nodePK, int direction)
      throws RemoteException;

  // CbO : FIN ADD

  /* gestion des sites */
  public Collection getAllWebSite() throws RemoteException;

  public SiteDetail getWebSite(String id) throws RemoteException;

  public List getWebSites(List ids) throws RemoteException;

  public Collection getIcons(String id) throws RemoteException;

  public String getNextId() throws RemoteException;

  public Collection getAllIcons() throws RemoteException;

  // CBO : UPDATE
  // public void createWebSite(SiteDetail description) throws RemoteException;
  public String createWebSite(SiteDetail description) throws RemoteException;

  public void associateIcons(String id, Collection liste)
      throws RemoteException;

  public void publish(Collection liste) throws RemoteException;

  public void dePublish(Collection liste) throws RemoteException;

  public void deleteWebSites(Collection liste) throws RemoteException;

  // CBO : REMOVE public void deleteWebSitesFromUpdate(Collection liste) throws
  // RemoteException;

  public int getSilverObjectId(String id) throws RemoteException;

  public void index() throws RemoteException;

  // CBO : ADD
  public void updateWebSite(SiteDetail description) throws RemoteException;
}