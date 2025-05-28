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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.servlets;

import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.silverpeas.core.web.selection.BasketSelectionUI.displayPutIntoBasketSelectionShortcut;

public class JSONServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final String OP_DELETE_PUBLICATIONS = "deletePublications";
  private static final String OP_EXPORT_PUBLICATIONS = "exportSelection";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    res.setContentType("application/json");
    String id = req.getParameter("Id");
    String componentId = req.getParameter("ComponentId");
    String action = req.getParameter("Action");
    KmeliaSessionController kmeliaSC =
        (KmeliaSessionController) req.getSession().getAttribute("Silverpeas_kmelia_" + componentId);
    if (kmeliaSC == null) {
      return;
    }
    try {
      Writer writer = res.getWriter();
      if ("GetOperations".equals(action)) {
        writer.write(getOperations(id, kmeliaSC));
      }
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String getOperations(String id, KmeliaSessionController kmeliaSC) {
    return JSONCodec.encodeObject(operations -> {
      String profile = kmeliaSC.getUserTopicProfile(id);
      if (KmeliaHelper.isNonVisiblePubsFolder(id)) {
        operations.put(OP_DELETE_PUBLICATIONS, kmeliaSC.isSuppressionAllowed(profile));
        operations.put(OP_EXPORT_PUBLICATIONS, true);
      } else {
        // getting profile
        NodeDetail folder = kmeliaSC.getNodeHeader(id);
        Role role = new Role(profile);
        boolean isRoot = NodePK.ROOT_NODE_ID.equals(id);
        boolean isInBasket = folder.getFullPath().contains("/" + NodePK.BIN_NODE_ID + "/");
        if (isInBasket) {
          addBasketOperations(kmeliaSC, operations, role, folder);
        } else if (StringUtil.isDefined(profile)) {
          UserDetail user = kmeliaSC.getUserDetail();
          // general operations
          addGeneralOperations(kmeliaSC, operations, role, profile, isRoot, folder);

          // topic operations
          addTopicOperations(kmeliaSC, operations, role, isRoot, folder, user);

          // publication operations
          addPublicationOperations(kmeliaSC, operations, role, isRoot, user);
        }
      }
      return operations;
    });

  }

  private void addPublicationOperations(final KmeliaSessionController kmeliaSC,
      final JSONCodec.JSONObject operations, final Role role,
      final boolean isRoot, final UserDetail user) {
    boolean publicationsInTopicAllowed = kmeliaSC.isPublicationAllowed(isRoot);
    boolean publicationAddingAllowed = !role.isUser() && publicationsInTopicAllowed;
    boolean filesImportAllowed = publicationAddingAllowed && kmeliaSC.isImportFileAllowed();
    boolean operationsOnSelectionAllowed =
        (role.isAdmin() || role.isPublisher()) && publicationsInTopicAllowed &&
            kmeliaSC.isSuppressionAllowed(role.toString());
    boolean somePublicationsExist = ofNullable(kmeliaSC.getSessionPublicationsList())
        .filter(not(Collection::isEmpty))
        .isPresent();
    boolean oneTemplateUsed = kmeliaSC.getXmlFormForPublications() != null;
    boolean copyCutAllowed = operationsOnSelectionAllowed && somePublicationsExist;
    boolean notRootNotAnonymousNotGuest = !isRoot && !user.isAnonymous() && !user.isAccessGuest();
    boolean pasteNodeAllowed = kmeliaSC.isPasteNodeAllowed();
    boolean pastePublicationAllowed = kmeliaSC.isPastePublicationAllowed(isRoot);
    boolean subscriptionAllowed =  isRoot && !user.isAnonymous() && !user.isAccessGuest();

    operations.put("addPubli", publicationAddingAllowed);
    operations.put("addFiles", publicationAddingAllowed && kmeliaSC.isAttachmentsEnabled() &&
        !kmeliaSC.isImportFileAllowed());
    operations.put("importFile", filesImportAllowed);
    operations.put("importFiles", filesImportAllowed);
    operations.put("copyPublications", copyCutAllowed);
    operations.put("cutPublications", copyCutAllowed);
    operations.put("paste", !role.isUser() && (pasteNodeAllowed || pastePublicationAllowed));

    operations.put("putPublicationsInBasket", publicationsInTopicAllowed
        && displayPutIntoBasketSelectionShortcut());

    operations.put("sortPublications", role.isAdmin() && publicationsInTopicAllowed
        && somePublicationsExist);

    operations.put("updatePublications",operationsOnSelectionAllowed && oneTemplateUsed);
    operations.put(OP_DELETE_PUBLICATIONS, operationsOnSelectionAllowed);

    boolean exportOnSelectionAllowed = kmeliaSC.isExportPublicationAllowed(kmeliaSC.getHighestSilverpeasUserRole());
    operations.put(OP_EXPORT_PUBLICATIONS, exportOnSelectionAllowed && somePublicationsExist);
    operations.put("manageSubscriptions", role.isAdmin());
    operations.put("subscriptions", subscriptionAllowed);
    operations.put("topicSubscriptions", notRootNotAnonymousNotGuest);
    operations.put("favorites", notRootNotAnonymousNotGuest);

    addPublicationSelectionOperation(kmeliaSC, operations, operationsOnSelectionAllowed);
  }

  private static void addPublicationSelectionOperation(KmeliaSessionController kmeliaSC,
      JSONCodec.JSONObject operations, boolean operationsOnSelectionAllowed) {
    if (kmeliaSC.isAllPublicationsListSelected()) {
      operations.put("unselectAllPublications", operationsOnSelectionAllowed);
    } else {
      operations.put("selectAllPublications", operationsOnSelectionAllowed);
    }
  }

  private void addTopicOperations(final KmeliaSessionController kmeliaSC,
      final JSONCodec.JSONObject operations, final Role role, final boolean isRoot,
      final NodeDetail node, final UserDetail user) {
    boolean isAdmin = role.isAdmin();
    operations.put("addTopic", isAdmin);
    operations.put("updateTopic", !isRoot && isAdmin);
    operations.put("deleteTopic", !isRoot && isAdmin);
    operations.put("sortSubTopics", isAdmin);
    operations.put("copyTopic", !isRoot && isAdmin);
    operations.put("cutTopic", !isRoot && isAdmin);
    operations.put("wysiwygTopic", isAdmin && kmeliaSC.
        isWysiwygOnTopicsEnabled());
    operations.put("shareTopic", node.canBeSharedBy(user));
    boolean exportOnTopicAllowed = kmeliaSC.isExportTopicAllowed(kmeliaSC.getHighestSilverpeasUserRole());
    operations.put("exportTopic", exportOnTopicAllowed);
    if (isRoot) {
      boolean exportOnApplicationAllowed = kmeliaSC.isExportApplicationAllowed(kmeliaSC.getHighestSilverpeasUserRole());
      operations.put("exportPDFApplication", exportOnApplicationAllowed);
    }
    else {
      operations.put("exportPDFTopic", exportOnTopicAllowed);
    }
  }

  private void addGeneralOperations(final KmeliaSessionController kmeliaSC,
      final JSONCodec.JSONObject operations, final Role role, final String profile,
      final boolean isRoot, final NodeDetail node) {

    User user = kmeliaSC.getUserDetail();

    operations.putJSONObject("context",
        c -> c.put("componentId", kmeliaSC.getComponentId()).put("nodeId", node.getId()));
    operations.put("admin", kmeliaSC.isComponentManageable());
    operations.put("pdc", isRoot && kmeliaSC.isPdcUsed() && role.isAdmin());
    operations.put("predefinedPdcPositions", kmeliaSC.isPdcUsed() && role.isAdmin());
    operations.put("templates",
        kmeliaSC.isTemplatesSelectionEnabledForRole(SilverpeasRole.fromString(profile)));
    boolean exportOnApplicationAllowed = kmeliaSC.isExportApplicationAllowed(kmeliaSC.getHighestSilverpeasUserRole());
    operations.put("exportApplication", exportOnApplicationAllowed);

    if (isRoot && kmeliaSC.isStatisticAllowed()) {
      operations.put("statistics", true);
    }
    operations.put("mylinks", !user.isAnonymous() && !user.isAccessGuest());
    operations.put("notify", !user.isAnonymous() && kmeliaSC.isNotificationAllowed() && !user.isAccessGuest());
    operations.put("responsibles", !user.isAnonymous());
  }

  private void addBasketOperations(final KmeliaSessionController kmeliaSc,
      final JSONCodec.JSONObject operations, final Role role,
      NodeDetail node) {
    boolean binOperationsAllowed = role.isAdmin() || role.isPublisher() || role.isWriter();
    boolean isAdmin = role.isAdmin();
    boolean suppressionAllowed = kmeliaSc.isSuppressionAllowed(role.toString());
    operations.put("emptyTrash", binOperationsAllowed && suppressionAllowed);
    operations.put(OP_EXPORT_PUBLICATIONS, binOperationsAllowed);
    operations.put("copyPublications", binOperationsAllowed);
    operations.put("cutPublications", binOperationsAllowed);
    operations.put(OP_DELETE_PUBLICATIONS, binOperationsAllowed && suppressionAllowed);
    if (!node.isBin()) {
      operations.put("deleteTopic", isAdmin);
      operations.put("copyTopic", isAdmin);
      operations.put("cutTopic", isAdmin);
    }
  }

  private static class Role {

    private final SilverpeasRole silverRole;

    public Role(final String profile) {
      this.silverRole = SilverpeasRole.fromString(profile);
    }

    public boolean isAdmin() {
      return silverRole == SilverpeasRole.ADMIN;
    }

    public boolean isPublisher() {
      return this.silverRole == SilverpeasRole.PUBLISHER;
    }


    public boolean isWriter() {
      return this.silverRole == SilverpeasRole.WRITER;
    }

    public boolean isUser() {
      return this.silverRole == SilverpeasRole.USER;
    }

    @Override
    public String toString() {
      return silverRole.getName();
    }
  }

}
