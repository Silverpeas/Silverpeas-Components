/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.kmelia.servlets;

import com.stratelia.webactiv.util.ClientBrowserUtil;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.util.MimeTypes;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import java.io.File;
import static org.apache.commons.io.FileUtils.*;

/**
 *
 * @author ehugonnet
 */
public class KmeliaPdfGeneratorServlet extends HttpServlet {

  private static final long serialVersionUID = -293231440555171364L;

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String componentId = request.getParameter("ComponentId");
    KmeliaSessionController kmelia = (KmeliaSessionController) request.getSession().getAttribute(
        "Silverpeas_kmelia_" + componentId);
    String pubId = request.getParameter("PubId");
    OutputStream out = response.getOutputStream();
    File pdfExport = kmelia.generatePdf(pubId);
    try {
      byte[] data = readFileToByteArray(pdfExport);
      String pdfName = ClientBrowserUtil.rfc2047EncodeFilename(request, pdfExport.getName());
      response.setHeader("Content-Disposition", "inline; filename=\"" + pdfName + "\"");
      response.setContentType(MimeTypes.PDF_MIME_TYPE);
      response.setContentLength(data.length);
      out.write(data);
      out.flush();
      out.close();
    } finally {
      if (pdfExport != null) {
        pdfExport.delete();
      }
    }
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);




  }

  /**
   * Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);




  }

  /**
   * Returns a short description of the servlet.
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";


  }// </editor-fold>
}
