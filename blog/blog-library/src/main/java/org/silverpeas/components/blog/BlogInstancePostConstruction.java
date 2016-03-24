/**
 * Copyright (C) 2000 - 2015 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.blog;

import org.silverpeas.core.admin.component.ComponentInstancePostConstruction;
import org.silverpeas.core.admin.user.model.UserDetail;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Once an instance of the blog application is created, creates for it one entry for categories and
 * another one for archives with the Node service.
 * @author mmoquillon
 */
@Named
public class BlogInstancePostConstruction implements ComponentInstancePostConstruction {

  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    NodeService nodeService = NodeService.get();

    NodeDetail rootCategory = getCategoryRootNodeFor(componentInstanceId);
    NodeDetail rootArchive = getArchiveRootNodeFor(componentInstanceId);

    nodeService.createNode(rootCategory);
    nodeService.createNode(rootArchive);
  }

  private NodeDetail getCategoryRootNodeFor(String componentInstanceId) {
    NodeDetail rootCategory = new NodeDetail();
    rootCategory.setNodePK(new NodePK("0", componentInstanceId));
    rootCategory.setFatherPK(null);
    rootCategory.setUseId(true);
    rootCategory.setName("Accueil Catégories");
    rootCategory.setDescription("Racine Catégories");
    rootCategory.setCreatorId(UserDetail.getCurrentRequester().getId());
    rootCategory.setLevel(1);
    rootCategory.setStatus("Visible");
    return rootCategory;
  }

  private NodeDetail getArchiveRootNodeFor(String componentInstanceId) {
    NodeDetail rootArchive = new NodeDetail();
    rootArchive.setNodePK(new NodePK("1", componentInstanceId));
    rootArchive.setFatherPK(null);
    rootArchive.setUseId(true);
    rootArchive.setName("Accueil Archives");
    rootArchive.setDescription("Racine Archives");
    rootArchive.setCreatorId(UserDetail.getCurrentRequester().getId());
    rootArchive.setLevel(1);
    rootArchive.setStatus("Visible");
    return rootArchive;
  }
}
