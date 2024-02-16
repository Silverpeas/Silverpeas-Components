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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.gallery.servlets;

import org.silverpeas.components.gallery.control.GallerySessionController;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AjaxServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String action = getAction(req);
    String result = null;

    if ("Sort".equals(action)) {
      result = sort(req);
    }

    Writer writer = resp.getWriter();
    writer.write(result);
  }

  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  private String sort(HttpServletRequest req) {
    HttpSession session = req.getSession();
    String orderedList = req.getParameter("orderedList");
    String componentId = (String) session.getAttribute("Silverpeas_Album_ComponentId");

    StringTokenizer tokenizer = new StringTokenizer(orderedList, ",");
    List<NodePK> albumPKs = new ArrayList<NodePK>();
    while (tokenizer.hasMoreTokens()) {
      albumPKs.add(new NodePK(tokenizer.nextToken(), componentId));
    }

    // Save album order
    try {
      GallerySessionController.sortAlbums(albumPKs);
      return "ok";
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }

    return "error";

  }

}
