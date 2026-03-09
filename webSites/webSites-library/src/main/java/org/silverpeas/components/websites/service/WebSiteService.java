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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites.service;

import org.silverpeas.components.websites.model.FolderDetail;
import org.silverpeas.components.websites.model.IconDetail;
import org.silverpeas.components.websites.model.SiteDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

/**
 * This is the WebSite manager service controller of the MVC. It is implemented as a CDI Bean. It
 * controls all the activities that happen in a client session. It also provides mechanisms to
 * access other service layer.
 *
 * @author Cecile BONIN
 * @version 1.0
 */
public interface WebSiteService {

  static WebSiteService get() {
    return ServiceProvider.getService(WebSiteService.class);
  }

  /* gestion des themes */
  FolderDetail goTo(NodePK pk);

  void addFolder(NodeDetail subFolder, NodePK fatherId, UserDetail currentUser);

  void updateFolder(NodeDetail topic, NodePK fatherPK);

  NodeDetail getFolderDetail(NodePK pk);

  void deleteFolder(NodePK pkToDelete);

  void changeTopicsOrder(String way, NodePK nodePK, NodePK fatherPK);

  /* gestion des publi */
  PublicationDetail getPublicationDetail(PublicationPK pk);

  String createPublication(String componentId, PublicationDetail pubDetail);

  void addPublicationToTopic(PublicationPK pubPK, NodePK fatherPK);

  void removePublicationFromTopic(PublicationPK pubPK, NodePK fatherPK);

  void updatePublication(PublicationDetail pubDetail, String componentId);

  void deletePublication(PublicationPK pubPK);

  Collection<NodePK> getAllFatherPK(PublicationPK pubPK);

  String getIdPublication(String componentId, String idSite);

  void updateClassification(PublicationPK pubPK, List<String> arrayTopic);

  void changePubsOrder(PublicationPK pubPK, NodePK nodePK, int direction);

  Collection<SiteDetail> getAllWebSite(String componentId);

  SiteDetail getWebSite(String componentId, String id);

  List<SiteDetail> getWebSites(String componentId, List<String> ids);

  Collection<IconDetail> getIcons(String componentId, String id);

  String getNextId(String componentId);

  Collection<IconDetail> getAllIcons(String componentId);

  void associateIcons(String componentId, String id, Collection<String> liste);

  void publish(String componentId, Collection<String> liste);

  void dePublish(String componentId, Collection<String> liste);

  void deleteWebSites(String componentId, Collection<String> liste);

  int getSilverObjectId(String componentId, String id);

  void index(String componentId);

  void updateWebSite(String componentId, SiteDetail description);

  String createWebSite(String componentId, SiteDetail description, UserDetail currentUser);
}