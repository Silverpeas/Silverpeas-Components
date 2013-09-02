/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.kmelia.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.converter.DocumentFormat;
import com.silverpeas.util.MimeTypes;

import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.util.ClientBrowserUtil;

import org.apache.commons.io.IOUtils;

import static com.silverpeas.converter.DocumentFormat.inFormat;
import static com.silverpeas.util.StringUtil.isDefined;

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
      Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
      request.setAttribute("javax.servlet.jsp.jspException", ex);
      getServletConfig().getServletContext().getRequestDispatcher(
          "/admin/jsp/errorpageMain.jsp").forward(request, response);
    }
  }

  private void exportInArchive(final HttpServletRequest request,
      final HttpServletResponse response) throws IOException {
    response.setContentType(MimeTypes.ARCHIVE_MIME_TYPE);
    String componentId = request.getParameter("ComponentId");
    KmeliaSessionController kmelia = (KmeliaSessionController) request.getSession().getAttribute(
        "Silverpeas_kmelia_" + componentId);
    File exportFile = kmelia.exportPublication();
    String fileName = ClientBrowserUtil.rfc2047EncodeFilename(request, exportFile.getName());
    InputStream in = new FileInputStream(exportFile);
    OutputStream out = response.getOutputStream();
    try {
      response.setHeader("Content-Length", String.valueOf(exportFile.length()));
      response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
      IOUtils.copy(in, out);
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
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
    InputStream in = new FileInputStream(generatedDocument);
    OutputStream out = response.getOutputStream();
    try {
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
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }
}
