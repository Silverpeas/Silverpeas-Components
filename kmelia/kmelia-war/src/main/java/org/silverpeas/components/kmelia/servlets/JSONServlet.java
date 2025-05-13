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
import org.silverpeas.core.clipboard.ClipboardException;
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

      if (KmeliaHelper.isNonVisiblePubsFolder(id)) {
        String profile = kmeliaSC.getUserTopicProfile(id);
        if (kmeliaSC.isSuppressionAllowed(profile)) {
          operations.put(OP_DELETE_PUBLICATIONS, true);
        }
        operations.put(OP_EXPORT_PUBLICATIONS, true);
      } else {
        // getting profile
        String profile = kmeliaSC.getUserTopicProfile(id);

        // getting operations of topic according to profile and current
        Role role = new Role(profile);
        boolean isRoot = NodePK.ROOT_NODE_ID.equals(id);
        boolean isBasket = NodePK.BIN_NODE_ID.equals(id);

        if (isBasket) {
          addBasketOperations(kmeliaSC, operations, role);
        } else if (StringUtil.isDefined(profile)) {
          NodeDetail node = kmeliaSC.getNodeHeader(id);
          UserDetail user = kmeliaSC.getUserDetail();
          // general operations
          addGeneralOperations(kmeliaSC, operations, role, profile, isRoot, node);

          // topic operations
          addTopicOperations(kmeliaSC, operations, role, isRoot, node, user);

          // publication operations
          try {
            addPublicationOperations(kmeliaSC, operations, role, isRoot, user);
          } catch (ClipboardException e) {
            SilverLogger.getLogger(this).error(e);
          }
        }
      }
      return operations;
    });

  }

  private void addPublicationOperations(final KmeliaSessionController kmeliaSC,
      final JSONCodec.JSONObject operations, final Role role,
      final boolean isRoot, final UserDetail user) throws ClipboardException {
    boolean publicationsInTopicAllowed = isPublicationsInTopicAllowed(isRoot, kmeliaSC);
    boolean addPublicationAllowed = addPublicationAllowed(isRoot, kmeliaSC, role);
    boolean operationsOnSelectionAllowed = isOperationsOnSelectionAllowed(isRoot, kmeliaSC, role);
    boolean somePublicationsExist = ofNullable(kmeliaSC.getSessionPublicationsList())
        .filter(not(Collection::isEmpty))
        .isPresent();
    boolean oneTemplateUsed = kmeliaSC.getXmlFormForPublications() != null;
    boolean copyCutAllowed = operationsOnSelectionAllowed && somePublicationsExist;
    boolean notRootNotAnonymousNotGuest = !isRoot && !user.isAnonymous() && !user.isAccessGuest();

    operations.put("addPubli", addPublicationAllowed);
    operations.put("addFiles", addPublicationAllowed && kmeliaSC.isAttachmentsEnabled() &&
        !kmeliaSC.isImportFileAllowed());
    operations.put("importFile", isImportFilesAllowed(isRoot, kmeliaSC, role));
    operations.put("importFiles", isImportFilesAllowed(isRoot, kmeliaSC, role));
    operations.put("copyPublications", copyCutAllowed);
    operations.put("cutPublications", copyCutAllowed);

    boolean pasteNodeAllowed = kmeliaSC.isPasteNodeAllowed();
    boolean pastePublicationAllowed = kmeliaSC.isPastePublicationAllowed(isRoot);
    operations.put("paste", !role.isUser() && (pasteNodeAllowed || pastePublicationAllowed));

    operations.put("putPublicationsInBasket", publicationsInTopicAllowed && displayPutIntoBasketSelectionShortcut());

    operations.put("sortPublications", role.isAdmin() && publicationsInTopicAllowed && somePublicationsExist);

    operations.put("updatePublications",operationsOnSelectionAllowed && oneTemplateUsed);
    operations.put(OP_DELETE_PUBLICATIONS, operationsOnSelectionAllowed);

    boolean exportOnSelectionAllowed = kmeliaSC.isExportPublicationAllowed(kmeliaSC.getHighestSilverpeasUserRole());
    operations.put(OP_EXPORT_PUBLICATIONS, exportOnSelectionAllowed && somePublicationsExist);
    operations.put("manageSubscriptions", role.isAdmin());
    operations.put("subscriptions", isSubscriptionsAllowed(isRoot, user));
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

  private void addBasketOperations(final KmeliaSessionController kmeliaSC,
      final JSONCodec.JSONObject operations, final Role role) {
    boolean binOperationsAllowed = role.isAdmin() || role.isPublisher() || role.isWriter();
    boolean suppressionAllowed = kmeliaSC.isSuppressionAllowed(role.toString());
    operations.put("emptyTrash", binOperationsAllowed && suppressionAllowed);
    operations.put(OP_EXPORT_PUBLICATIONS, binOperationsAllowed);
    operations.put("copyPublications", binOperationsAllowed);
    operations.put("cutPublications", binOperationsAllowed);
    operations.put(OP_DELETE_PUBLICATIONS, binOperationsAllowed && suppressionAllowed);
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


  /** Return if publications creation is allowed
   *
   * @param isRoot
   * @return true or false
   */
  private boolean isPublicationsInTopicAllowed(boolean isRoot, KmeliaSessionController kmeliaSC) {
    return !isRoot || (kmeliaSC.getNbPublicationsOnRoot() == 0 || !kmeliaSC.isTreeStructure());
  }

  /** Return if publications creation is allowed meanwhile the user role
   *
   * @param isRoot
   * @param kmeliaSC
   * @param role
   * @return true or false
   */
  private boolean addPublicationAllowed(boolean isRoot, KmeliaSessionController kmeliaSC, Role role) {
    return !role.isUser() && isPublicationsInTopicAllowed(isRoot, kmeliaSC);
  }

  /** Return if subscriptions is allowed
   *
   * @param isRoot
   * @param user
   * @return true or false
   */
  private boolean isSubscriptionsAllowed(boolean isRoot, UserDetail user) {
    return isRoot && !user.isAnonymous() && !user.isAccessGuest();
  }

  /** Return if import file(s) is allowed
   *
   * @param isRoot
   * @param kmeliaSC
   * @param role
   * @return true or false
   */
  private boolean isImportFilesAllowed(boolean isRoot,KmeliaSessionController kmeliaSC, Role role) {
    return addPublicationAllowed(isRoot, kmeliaSC, role) && kmeliaSC.isImportFileAllowed();
  }


  /** Return if operations on selection are allowed
   *
   * @param isRoot
   * @param kmeliaSC
   * @param role
   * @return true or false
   */
  private boolean isOperationsOnSelectionAllowed(boolean isRoot,KmeliaSessionController kmeliaSC, Role role) {
    return (role.isAdmin() || role.isPublisher()) && isPublicationsInTopicAllowed(isRoot, kmeliaSC) && kmeliaSC.isSuppressionAllowed(role.toString());
  }


}
