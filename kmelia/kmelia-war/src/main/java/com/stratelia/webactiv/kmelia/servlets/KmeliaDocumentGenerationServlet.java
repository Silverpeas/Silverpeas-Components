/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.kmelia.servlets;

import com.google.common.io.ByteStreams;
import com.silverpeas.converter.DocumentFormat;
import com.stratelia.webactiv.util.ClientBrowserUtil;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import java.io.File;
import java.io.FileInputStream;
import static com.silverpeas.converter.DocumentFormat.*;

/**
 * This servlet listens for HTTP requests asking the generation of a given Kmelia publication into
 * a document in a given format.
 * The content of the generated document is then served through the HTTP response.
 */
public class KmeliaDocumentGenerationServlet extends HttpServlet {

  private static final long serialVersionUID = -293231440555171364L;
  
  private static final String PUBLICATION_ID_PARAMETER = "PubId";
  private static final String COMPONENT_INSTANCE_ID_PARAMETER = "ComponentId";
  private static final String DOCUMENT_FORMAT_PARAMETER = "Format";

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException,
      IOException {
    String fromPublicationId = request.getParameter(PUBLICATION_ID_PARAMETER);
    DocumentFormat format = inFormat(request.getParameter(DOCUMENT_FORMAT_PARAMETER));
    String componentId = request.getParameter(COMPONENT_INSTANCE_ID_PARAMETER);
    KmeliaSessionController kmelia = (KmeliaSessionController) request.getSession().getAttribute(
        "Silverpeas_kmelia_" + componentId);
    OutputStream out = response.getOutputStream();
    File generatedDocument = kmelia.generateDocument(inFormat(format), fromPublicationId);
    try {
      FileInputStream in = new FileInputStream(generatedDocument);
      String documentName = ClientBrowserUtil.rfc2047EncodeFilename(request, generatedDocument.getName());
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
