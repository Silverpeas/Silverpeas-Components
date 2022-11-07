/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

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

      if (KmeliaHelper.isNonVisiblePubsFolder(id)) {
        operations.put(OP_DELETE_PUBLICATIONS, true);
        operations.put(OP_EXPORT_PUBLICATIONS, true);
      } else {
        // getting profile
        String profile = kmeliaSC.getUserTopicProfile(id);

        // getting operations of topic according to profile and current
        boolean isAdmin = SilverpeasRole.ADMIN.isInRole(profile);
        boolean isPublisher = SilverpeasRole.PUBLISHER.isInRole(profile);
        boolean isWriter = SilverpeasRole.WRITER.isInRole(profile);
        boolean isUser = SilverpeasRole.USER.isInRole(profile);
        boolean isRoot = NodePK.ROOT_NODE_ID.equals(id);
        boolean isBasket = NodePK.BIN_NODE_ID.equals(id);
        Role role = new Role().setAdmin(isAdmin).setPublisher(isPublisher).setWriter(isWriter)
            .setUser(isUser);

        if (isBasket) {
          addBasketOperations(operations, role);
        } else if (StringUtil.isDefined(profile)) {
          NodeDetail node = kmeliaSC.getNodeHeader(id);
          UserDetail user = kmeliaSC.getUserDetail();
          // general operations
          addGeneralOperations(kmeliaSC, operations, role, profile, isRoot, node);

          // topic operations
          addTopicOperations(kmeliaSC, operations, role, isRoot, node, user);

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
    boolean publicationsInTopic =
        !isRoot || (kmeliaSC.getNbPublicationsOnRoot() == 0 || !kmeliaSC.isTreeStructure());
    boolean addPublicationAllowed = !role.isUser() && publicationsInTopic;
    boolean operationsOnSelectionAllowed =
        (role.isAdmin() || role.isPublisher()) && publicationsInTopic;
    boolean somePublicationsExist = ofNullable(kmeliaSC.getSessionPublicationsList())
        .filter(not(Collection::isEmpty))
        .isPresent();
    boolean oneTemplateUsed = kmeliaSC.getXmlFormForPublications() != null;
    boolean copyCutAllowed = operationsOnSelectionAllowed && somePublicationsExist;
    boolean notRootNotAnonymous = !isRoot && !user.isAnonymous();

    operations.put("addPubli", addPublicationAllowed);
    operations.put("importFile", addPublicationAllowed && kmeliaSC.isImportFileAllowed());
    operations.put("importFiles", addPublicationAllowed && kmeliaSC.isImportFilesAllowed());
    operations.put("copyPublications", copyCutAllowed);
    operations.put("cutPublications", copyCutAllowed);
    operations.put("paste", addPublicationAllowed);

    operations.put("putPublicationsInBasket", publicationsInTopic && displayPutIntoBasketSelectionShortcut());

    operations.put("sortPublications", role.isAdmin() && publicationsInTopic && somePublicationsExist);

    operations.put("updatePublications",operationsOnSelectionAllowed && oneTemplateUsed);
    operations.put(OP_DELETE_PUBLICATIONS, operationsOnSelectionAllowed);

    operations.put(OP_EXPORT_PUBLICATIONS, !user.isAnonymous() && somePublicationsExist);
    operations.put("manageSubscriptions", role.isAdmin());
    operations.put("subscriptions", isRoot && !user.isAnonymous());
    operations.put("topicSubscriptions", notRootNotAnonymous);
    operations.put("favorites", notRootNotAnonymous);

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
    if (!isRoot && isAdmin && kmeliaSC.isOrientedWebContent()) {
      operations.put("showTopic", NodeDetail.STATUS_INVISIBLE.equalsIgnoreCase(node.getStatus()));
      operations.put("hideTopic", NodeDetail.STATUS_VISIBLE.equalsIgnoreCase(node.getStatus()));
    }
    operations.put("wysiwygTopic", isAdmin && (kmeliaSC.isOrientedWebContent() || kmeliaSC.
        isWysiwygOnTopicsEnabled()));
    operations.put("shareTopic", node.canBeSharedBy(user));
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
    operations.put("exporting",
        kmeliaSC.isExportComponentAllowed() && kmeliaSC.isExportZipAllowed() &&
            (role.isAdmin() || kmeliaSC.isExportAllowedToUsers()));
    operations.put("exportPDF",
        kmeliaSC.isExportComponentAllowed() && kmeliaSC.isExportPdfAllowed() &&
            (role.isAdmin() || role.isPublisher()));

    if (isRoot && kmeliaSC.isStatisticAllowed()) {
      operations.put("statistics", true);
    }
    operations.put("mylinks", !user.isAnonymous());
    operations.put("notify", !user.isAnonymous() && kmeliaSC.isNotificationAllowed());
    operations.put("responsibles", !user.isAnonymous());
  }

  private void addBasketOperations(final JSONCodec.JSONObject operations, final Role role) {
    boolean binOperationsAllowed = role.isAdmin() || role.isPublisher() || role.isWriter();
    operations.put("emptyTrash", binOperationsAllowed);
    operations.put(OP_EXPORT_PUBLICATIONS, binOperationsAllowed);
    operations.put("copyPublications", binOperationsAllowed);
    operations.put("cutPublications", binOperationsAllowed);
    operations.put(OP_DELETE_PUBLICATIONS, binOperationsAllowed);
  }

  private static class Role {

    private boolean admin;
    private boolean publisher;
    private boolean writer;
    private boolean user;

    public boolean isAdmin() {
      return admin;
    }

    public Role setAdmin(final boolean admin) {
      this.admin = admin;
      return this;
    }

    public boolean isPublisher() {
      return publisher;
    }

    public Role setPublisher(final boolean publisher) {
      this.publisher = publisher;
      return this;
    }

    public boolean isWriter() {
      return writer;
    }

    public Role setWriter(final boolean writer) {
      this.writer = writer;
      return this;
    }

    public boolean isUser() {
      return user;
    }

    public Role setUser(final boolean user) {
      this.user = user;
      return this;
    }
  }
}
