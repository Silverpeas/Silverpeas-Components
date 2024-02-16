/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.components.kmelia.servlets;

import org.apache.commons.io.IOUtils;
import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.core.contribution.converter.DocumentFormat;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.util.ClientBrowserUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.silverpeas.core.contribution.converter.DocumentFormat.inFormat;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * An HTTP servlet dedicated to the export of Kmelia publications.
 */
public class KmeliaPublicationExportServlet extends HttpServlet {

  private static final long serialVersionUID = 5790958079143913041L;
  private static final String PUBLICATION_ID_PARAMETER = "PubId";
  private static final String COMPONENT_INSTANCE_ID_PARAMETER = "ComponentId";
  private static final String DOCUMENT_FORMAT_PARAMETER = "Format";
  private static final String ZIP_EXPORT = "zip";

  /**
   * Listens for HTTP requests querying the export of a publication into a given format. Sends back
   * the content of the export result.
   *
   * @param request the HTTP requests with the export parameters.
   * @param response the HTTP responses with the export content.
   * @throws ServletException if an error occurs while processing the export.
   * @throws IOException if an error occurs while generating export files or when communicating with
   * the requester.
   */
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String format = request.getParameter(DOCUMENT_FORMAT_PARAMETER);
    if (!isDefined(format)) {
      format = ZIP_EXPORT;
    }
    try {
      if (ZIP_EXPORT.equals(format)) {
        exportInArchive(request, response);
      } else {
        exportInDocument(request, response);
      }
    } catch (Exception ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      request.setAttribute("javax.servlet.jsp.jspException", ex);
      getServletConfig().getServletContext().getRequestDispatcher(
          "/admin/jsp/errorpageMain.jsp").forward(request, response);
    }
  }

  private void exportInArchive(final HttpServletRequest request,
      final HttpServletResponse response) throws IOException {
    response.setContentType(MimeTypes.ARCHIVE_MIME_TYPE);
    String componentId = request.getParameter(COMPONENT_INSTANCE_ID_PARAMETER);
    KmeliaSessionController kmelia = (KmeliaSessionController) request.getSession().getAttribute(
        "Silverpeas_kmelia_" + componentId);
    File exportFile = kmelia.exportPublication();
    String fileName = ClientBrowserUtil.rfc2047EncodeFilename(request, exportFile.getName());
    try (InputStream in = new FileInputStream(exportFile);
         OutputStream out = response.getOutputStream()) {
      response.setHeader("Content-Length", String.valueOf(exportFile.length()));
      response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
      IOUtils.copy(in, out);
    }
  }

  private void exportInDocument(final HttpServletRequest request,
      final HttpServletResponse response) throws IOException {
    String fromPublicationId = request.getParameter(PUBLICATION_ID_PARAMETER);
    DocumentFormat format = inFormat(request.getParameter(DOCUMENT_FORMAT_PARAMETER));
    String componentId = request.getParameter(COMPONENT_INSTANCE_ID_PARAMETER);
    KmeliaSessionController kmelia = (KmeliaSessionController) request.getSession().getAttribute(
        "Silverpeas_kmelia_" + componentId);
    File generatedDocument = kmelia.generateDocument(inFormat(format), fromPublicationId);
    try (InputStream in = new FileInputStream(generatedDocument);
         OutputStream out = response.getOutputStream()) {
      String documentName = ClientBrowserUtil.rfc2047EncodeFilename(request, generatedDocument.
          getName());
      response.setHeader("Content-Disposition", "inline; filename=\"" + documentName + "\"");
      response.setContentType(format.getMimeType());
      response.setHeader("Content-Length", String.valueOf(generatedDocument.length()));
      IOUtils.copy(in, out);
    } finally {
      if (generatedDocument != null) {
        generatedDocument.delete();
      }
    }
  }
}
