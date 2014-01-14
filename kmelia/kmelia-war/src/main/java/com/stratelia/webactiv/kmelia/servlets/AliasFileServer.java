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
package com.stratelia.webactiv.kmelia.servlets;

import java.io.*;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.Charsets;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.KmeliaSecurity;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.Alias;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Class declaration
 *
 * @author
 */
public class AliasFileServer extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse response)
      throws ServletException, IOException {
    SilverTrace.info("kmelia", "AliasFileServer.doPost", "root.MSG_GEN_ENTER_METHOD");

    String userId = "undefined";
    HttpSession session = req.getSession(true);
    MainSessionController mainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    if (mainSessionCtrl != null) {
      userId = mainSessionCtrl.getUserId();
    }

    WAPrimaryKey foreignKey = null;

    String attachmentId = req.getParameter("AttachmentId");
    if (!StringUtil.isDefined(attachmentId)) {
      attachmentId = req.getParameter("VersionId");
    }
    String language = req.getParameter("lang");
    SimpleDocument attachment = null;
    if (StringUtil.isDefined(attachmentId)) {
      // Check first if attachment exists
      attachment = AttachmentServiceFactory.getAttachmentService().
          searchDocumentById(new SimpleDocumentPK(attachmentId), language);
      if (attachment != null) {
        foreignKey = new ForeignPK(attachment.getForeignId(), attachment.getInstanceId());
      }
    }

    if (foreignKey != null) {
      PublicationPK pubPK = new PublicationPK(foreignKey.getId(), foreignKey.getInstanceId());
      List<Alias> aliases = (List<Alias>) getPublicationBm().getAlias(pubPK);

      // check if user have rights to see alias files
      boolean rightsOK = false;
      KmeliaSecurity security = new KmeliaSecurity();
      for (int a = 0; !rightsOK && a < aliases.size(); a++) {
        Alias alias = aliases.get(a);
        if (!foreignKey.getInstanceId().equals(alias.getInstanceId())) {
          // it's an alias
          // Check if user is allowed to see topic's content
          rightsOK = security.isAccessAuthorized(alias.getInstanceId(), userId, alias.getId(),
              KmeliaSecurity.NODE_TYPE);
        }
      }

      if (rightsOK) {
        response.setContentType(attachment.getContentType());
        response.setHeader("Content-Disposition", "inline; filename=\"" + attachment.getFilename() +
            '"');
        response.setHeader("Content-Length", String.valueOf(attachment.getSize()));
        display(response, attachment.getAttachmentPath());
      }
    }
  }

  /**
   * This method writes the result of the preview action.
   *
   * @param res - The HttpServletResponse where the html code is write
   * @param htmlFilePath - the canonical path of the html document generated by the parser tools. if
   * this String is null that an exception had been catched the html document generated is empty !!
   * also, we display a warning html page
   */
  private void display(HttpServletResponse res, String filePath) throws IOException {
    File file = new File(filePath);
    SilverTrace.info("kmelia", "AliasFileServer.display()",
        "root.MSG_GEN_ENTER_METHOD", "filePath = " + filePath);
    try {
      if (!file.exists()) {
        displayWarningHtmlCode(res);
      } else {
        FileUtils.copyFile(file, res.getOutputStream());
        res.getOutputStream().flush();
      }
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "AliasFileServer.doPost",
          "root.EX_CANT_READ_FILE", "filePath = " + filePath);
      displayWarningHtmlCode(res);
    }
  }

  // Add By Mohammed Hguig
  private void displayWarningHtmlCode(HttpServletResponse res) throws IOException {
    OutputStream out = res.getOutputStream();
    ResourceLocator resourceLocator = new ResourceLocator(
        "org.silverpeas.util.peasUtil.multiLang.fileServerBundle", "");

    InputStream in = new ByteArrayInputStream(resourceLocator.getString("warning").
        getBytes(Charsets.UTF_8));
    try {
      IOUtils.copy(in, out);
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "AliasFileServer.displayWarningHtmlCode",
          "root.EX_CANT_READ_FILE", "warning properties");
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  private PublicationBm getPublicationBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
    } catch (Exception e) {
      SilverTrace.warn("kmelia", "AliasFileServer.getPublicationBm", "root.EX_CANT_GET_EJB");
      return null;
    }
  }
}
