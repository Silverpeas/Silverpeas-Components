/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.jstl;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

import org.silverpeas.core.util.StringUtil;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane;

/**
 * Helper class to be able to remove old JSP methods.
 */
public class KmeliaDisplayHelper {

  /**
   * Default constructor
   */
  private KmeliaDisplayHelper() {
  }

  private static void displayJavascriptAndFormToOperations(KmeliaSessionController kmeliaScc,
      JspWriter out) throws IOException {
    out.println("<form Name=\"operationsForm\" ACTION=\"null\" Method=\"POST\">");
    out.println("<input type=\"hidden\" name=\"PubId\">");
    out.println("<input type=\"hidden\" name=\"Action\">");
    out.println("</form>");

    out.println("<script language=\"javascript\">");
    out.println("function goToOperationByGet(target, pubId, operation) {");
    out.println("goToOperationWithMethod(target, pubId, operation, 'GET');");
    out.println("}");

    out.println("function goToOperationWithMethod(target, pubId, operation, formMethod) {");
    out.println("alertMsg = \"" + kmeliaScc.getString("PubRemplirFormulaire") + "\";");
    out.println("if (pubId == \"\") {");
    out.println("window.alert(alertMsg);");
    out.println("} else { ");
    out.println("document.operationsForm.PubId.value = pubId;");
    out.println("document.operationsForm.Action.value = operation;");
    out.println("document.operationsForm.action = target;");
    out.println("document.operationsForm.method = formMethod;");
    out.println("document.operationsForm.submit();");
    out.println("}");
    out.println("}");

    out.println("function goToOperation(target, pubId, operation) {");
    out.println("goToOperationWithMethod(target, pubId, operation, 'POST');");
    out.println("}");

    out.println("function goToOperationInAnotherWindow(target, pubId, operation) {");
    out.println("alertMsg = \"" + kmeliaScc.getString("PubRemplirFormulaire") + "\";");
    out.println("if (pubId == \"\") {");
    out.println("window.alert(alertMsg);");
    out.println("} else { ");
    out.println("url = target+\"?PubId=\"+pubId+\"&Action=\"+operation;");
    out.println("windowName = \"publicationWindow\";");
    out.println("windowParams = \"directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars\";");
    out.println("larg = \"900\";");
    out.println("haut = \"600\";");
    out.println(
        "publicationWindow = SP_openWindow(url, windowName, larg, haut, windowParams, false);");
    out.println("}");
    out.println("}");

    out.println("</script>");
  }

  public static void displayAllOperations(String id, KmeliaSessionController kmeliaScc,
      GraphicElementFactory gef, String action, MultiSilverpeasBundle resources, JspWriter out) throws
      IOException {
    boolean kmaxMode = false;
    displayAllOperations(id, kmeliaScc, gef, action, resources, out, kmaxMode);
  }

  public static void displayAllOperations(String id, KmeliaSessionController kmeliaScc,
      GraphicElementFactory gef, String action, MultiSilverpeasBundle resources, JspWriter out,
      boolean kmaxMode) throws IOException {
    String routerUrl = URLUtil.getApplicationURL() + URLUtil.getURL(kmeliaScc
        .getComponentRootName(), kmeliaScc.getSpaceId(), kmeliaScc.getComponentId());
    displayJavascriptAndFormToOperations(kmeliaScc, out);
    boolean enabled = StringUtil.isDefined(id);
    List<String> invisibleTabs = kmeliaScc.getInvisibleTabs();

    TabbedPane tabbedPane = gef.getTabbedPane();
    PublicationDetail pubDetail = kmeliaScc.getSessionPublication().getDetail();
    PublicationDetail cloneDetail = null;
    if (kmeliaScc.getSessionClone() != null) {
      cloneDetail = kmeliaScc.getSessionClone().getDetail();
    }

    String decoration = "";
    String pubId = pubDetail.getPK().getId();
    String previewTabLabel = resources.getString("PublicationPreview");
    String sureId = pubId;
    if (cloneDetail != null) {
      decoration = " *";
      sureId = cloneDetail.getId();
      previewTabLabel = resources.getString("kmelia.PublicPreview");
    }

    if (invisibleTabs.indexOf(KmeliaSessionController.TAB_PREVIEW) == -1) {
      tabbedPane.addTab(previewTabLabel, routerUrl + "ViewPublication?PubId=" + pubId, "View".equals(
          action) || "ViewPublication".equals(action), enabled);
    }
    if (cloneDetail != null) {
      tabbedPane.addTab(resources.getString("kmelia.ClonePreview") + decoration, routerUrl
          + "ViewClone", "ViewClone".equals(action), enabled);
    }
    if (invisibleTabs.indexOf(KmeliaSessionController.TAB_HEADER) == -1) {
      tabbedPane.addTab(kmeliaScc.getString("Header") + decoration, routerUrl
          + "ToUpdatePublicationHeader", "UpdateView".equals(action)
          || "New".equals(action) || "KmaxModifyPublication".equals(action), enabled);
    }
    if (invisibleTabs.indexOf(KmeliaSessionController.TAB_CONTENT) == -1) {
      tabbedPane.addTab(resources.getString("Model") + decoration,
          "javaScript:onClick=goToOperation('" + routerUrl + "ToPubliContent', '" + sureId
          + "', 'ModelUpdateView')", "ModelUpdateView".equals(action) || "NewModel".equals(action)
          || "ModelChoice".equals(action), enabled);
    }
    if (invisibleTabs.indexOf(KmeliaSessionController.TAB_ATTACHMENTS) == -1) {
      if (kmeliaScc.getComponentId().startsWith("toolbox")) {
        decoration = "";
        tabbedPane.addTab(resources.getString("GML.attachments") + decoration,
            "javaScript:onClick=goToOperationByGet('" + routerUrl + "ViewAttachments', '" + pubId
            + "', 'ViewAttachments')", "ViewAttachments".equals(action), enabled);
      }
    }

    if (invisibleTabs.indexOf(KmeliaSessionController.TAB_ACCESS_PATHS) == -1 && !kmaxMode && !pubDetail.isAlias()) {
      tabbedPane.addTab(resources.getString("PubGererChemins")+ " ("+kmeliaScc.getPublicationLocations().size()+")", routerUrl
          + "PublicationPaths?PubId=" + pubId, "ViewPath".equals(action), enabled);
    }

    if (kmaxMode) {
      tabbedPane.addTab(kmeliaScc.getString("PubPositions"), "KmaxViewCombination?PubId=" + pubId,
          action.equals("KmaxViewCombination"), enabled);
    }

    if (invisibleTabs.indexOf(kmeliaScc.TAB_READER_LIST) == -1) {
      tabbedPane.addTab(resources.getString("PubGererControlesLecture"), routerUrl
          + "ReadingControl", action.equals("ViewReadingControl"), enabled);
    }

    if (kmeliaScc.isValidationTabVisible()) {
      tabbedPane.addTab(resources.getString("kmelia.validation"), routerUrl + "ViewValidationSteps",
          "ViewValidationSteps".equals(action), enabled);
    }

    out.println(tabbedPane.print());
  }

  public static void displayUserOperations(KmeliaSessionController kmeliaScc, JspWriter out)
      throws IOException {
    displayJavascriptAndFormToOperations(kmeliaScc, out);
  }

  public static void displayOnNewOperations(KmeliaSessionController kmeliaScc, JspWriter out)
      throws IOException {
    displayJavascriptAndFormToOperations(kmeliaScc, out);
  }

  public static void displayUserAttachmentsView(PublicationDetail pubDetail, String webContext,
      JspWriter out, String lang, boolean showIcon, MultiSilverpeasBundle resources) throws IOException {
    ResourceReference foreignKey = new ResourceReference(pubDetail.getPK());
    List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService()
        .listDocumentsByForeignKey(foreignKey, lang);

    if (!documents.isEmpty()) {
      out.println(
          "<table ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor>");
      out.println("<tr><td>");
      out.println(
          "<table ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4>");
      if (showIcon) {
        out.println("<tr><td align=\"center\"><img src=\"" + webContext
            + "/util/icons/attachedFiles.gif\"></td></tr>");
      }
      for (SimpleDocument document : documents) {
        SimpleDocument document_version = document.getLastPublicVersion();
        if (document_version != null) {
          String title = document_version.getTitle();
          if (!StringUtil.isDefined(title)) {
            title = document_version.getFilename();
          }
          out.println("<tr>");
          out.print("<td><img alt=\"\" src=\"" + document_version.getDisplayIcon()
              + "\" width=20>&nbsp;<A href=\"" + webContext+document_version.getAttachmentURL()
              + "\" target=\"_blank\">" + title + "</a>");
          if (document_version.isVersioned()) {
            out.println("&nbsp;(v" + document_version.getMajorVersion() + "." + document_version
                .getMinorVersion() + ")<br/>");
          } else {
            out.println("&nbsp;<br/>");
          }
          String separator = "";
          if (!"no".equals(resources.getSetting("showFileSize"))) {
            out.println(" " + FileRepositoryManager.formatFileSize(document_version.getSize()));
            separator = " / ";
          }
          if (!"no".equals(resources.getSetting("showDownloadEstimation"))) {
            out.println(separator + FileRepositoryManager.getFileDownloadTime(document_version
                .getSize()));
          }
          if (StringUtil.isDefined(document_version.getDescription())) {
            if (!"no".equals(resources.getSetting("showInfo"))) {
              out.println("<br><i>" + document_version.getDescription() + "</i>");
            }

          }
          if (document_version.isVersioned()
              && document_version.getMajorVersion() > 1) {
            if (showIcon) {
              out.println("<br/> >> <a href=\"javaScript:viewPublicVersions(" + document.getId()
                  + ")\">Toutes les versions...</a>");
            } else {
              out.println(" (<a href=\"javaScript:viewPublicVersions(" + document.getId()
                  + ")\">Toutes les versions...</a>)");
            }
          }
          out.println("</td></tr>");
        }
      }
      out.println("</table>");
      out.println("</td></tr>");
      out.println("</table>");
    }

  }
}
