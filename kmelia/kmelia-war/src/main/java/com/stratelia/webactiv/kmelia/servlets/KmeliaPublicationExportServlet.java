/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.kmelia.servlets;

import com.silverpeas.converter.DocumentFormat;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.silverpeas.util.MimeTypes;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.util.ClientBrowserUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static com.silverpeas.util.StringUtil.*;
import static com.silverpeas.converter.DocumentFormat.*;

/**
 *
 * @author ehugonnet
 */
public class KmeliaPublicationExportServlet extends HttpServlet {

  private static final long serialVersionUID = 5790958079143913041L;
  private static final String PUBLICATION_ID_PARAMETER = "PubId";
  private static final String COMPONENT_INSTANCE_ID_PARAMETER = "ComponentId";
  private static final String DOCUMENT_FORMAT_PARAMETER = "Format";
  private static final String ZIP_EXPORT = "zip";

  /**
   * Listens for HTTP requests querying the export of a publication into a given format.
   * Sends back the content of the export result.
   * @param request the HTTP requests with the export parameters.
   * @param response the HTTP responses with the export content.
   * @throws ServletException if an error occurs while processing the export.
   * @throws IOException if an error occurs while generating export files or when communicating
   * with the requester.
   */
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException,
      IOException {
    String format = request.getParameter(DOCUMENT_FORMAT_PARAMETER);
    if (!isDefined(format) || ZIP_EXPORT.equals(format)) {
      exportInArchive(request, response);
    } else {
      exportInDocument(request, response);
    }
  }

  private void exportInArchive(final HttpServletRequest request,
      final HttpServletResponse response) throws IOException {
    response.setContentType(MimeTypes.ARCHIVE_MIME_TYPE);
    String componentId = request.getParameter("ComponentId");
    KmeliaSessionController kmelia = (KmeliaSessionController) request.getSession().getAttribute(
        "Silverpeas_kmelia_" + componentId);
    OutputStream out = response.getOutputStream();
    File exportFile = kmelia.exportPublication();
    String fileName = ClientBrowserUtil.rfc2047EncodeFilename(request, exportFile.getName());
    InputStream in = new FileInputStream(exportFile);
    try {
      response.setContentLength(Long.valueOf(exportFile.length()).intValue());
      response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
      ByteStreams.copy(in, out);
    } finally {
      Closeables.closeQuietly(in);
      Closeables.closeQuietly(out);
    }
  }

  private void exportInDocument(final HttpServletRequest request,
      final HttpServletResponse response) throws IOException {
    String fromPublicationId = request.getParameter(PUBLICATION_ID_PARAMETER);
    DocumentFormat format = inFormat(request.getParameter(DOCUMENT_FORMAT_PARAMETER));
    String componentId = request.getParameter(COMPONENT_INSTANCE_ID_PARAMETER);
    KmeliaSessionController kmelia = (KmeliaSessionController) request.getSession().getAttribute(
        "Silverpeas_kmelia_" + componentId);
    OutputStream out = response.getOutputStream();
    File generatedDocument = kmelia.generateDocument(inFormat(format), fromPublicationId);
    try {
      FileInputStream in = new FileInputStream(generatedDocument);
      String documentName = ClientBrowserUtil.rfc2047EncodeFilename(request, generatedDocument.
          getName());
      response.setHeader("Content-Disposition", "inline; filename=\"" + documentName + "\"");
      response.setContentType(format.getMimeType());
      response.setContentLength(Long.valueOf(generatedDocument.length()).intValue());
      ByteStreams.copy(in, out);
    } finally {
      if (generatedDocument != null) {
        generatedDocument.delete();
      }
    }
  }
}
