/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.webSites.control.ejb;

/**
 * This is the webSite manager EJB-tier controller of the MVC. It is implemented as a session EJB.
 * It controls all the activities that happen in a client session. It also provides mechanisms to
 * access other session EJBs.
 *
 * @author Cecile BONIN
 * @version 1.0
 */
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import com.stratelia.webactiv.webSites.siteManage.model.FolderDetail;
import com.stratelia.webactiv.webSites.siteManage.model.IconDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;

import javax.ejb.Local;
import java.util.Collection;
import java.util.List;

@Local
public interface WebSiteBm {

  /* gestion des themes */
  public FolderDetail goTo(NodePK pk);

  public NodePK addFolder(NodeDetail subFolder, NodePK fatherId, UserDetail currentUser);

  public NodePK updateFolder(NodeDetail topic, NodePK fatherPK);

  public NodeDetail getFolderDetail(NodePK pk);

  public void deleteFolder(NodePK pkToDelete);

  public void changeTopicsOrder(String way, NodePK nodePK, NodePK fatherPK);

  /* gestion des publi */
  public PublicationDetail getPublicationDetail(PublicationPK pk);

  public String createPublication(String componentId, PublicationDetail pubDetail);

  public void addPublicationToTopic(PublicationPK pubPK, NodePK fatherPK);

  public void removePublicationFromTopic(PublicationPK pubPK, NodePK fatherPK);

  public void updatePublication(PublicationDetail pubDetail, String componentId);

  public void deletePublication(PublicationPK pubPK);

  public Collection<NodePK> getAllFatherPK(PublicationPK pubPK);

  public String getIdPublication(String componentId, String idSite);

  public void updateClassification(PublicationPK pubPK, List<String> arrayTopic);

  public void changePubsOrder(PublicationPK pubPK, NodePK nodePK, int direction);

  public Collection<SiteDetail> getAllWebSite(String componentId);

  public SiteDetail getWebSite(String componentId, String id);

  public List<SiteDetail> getWebSites(String componentId, List<String> ids);

  public Collection<IconDetail> getIcons(String componentId, String id);

  public String getNextId(String componentId);

  public Collection<IconDetail> getAllIcons(String componentId);

  public void associateIcons(String componentId, String id, Collection<String> liste);

  public void publish(String componentId, Collection<String> liste);

  public void dePublish(String componentId, Collection<String> liste);

  public void deleteWebSites(String componentId, Collection<String> liste);

  public int getSilverObjectId(String componentId, String id);

  public void index(String componentId);

  public void updateWebSite(String componentId, SiteDetail description);

  public String createWebSite(String componentId, SiteDetail description, UserDetail currentUser);
}