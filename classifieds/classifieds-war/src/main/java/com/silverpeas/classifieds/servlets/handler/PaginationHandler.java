/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.classifieds.servlets.handler;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.classifieds.control.ClassifiedsSessionController;
import com.silverpeas.classifieds.servlets.FunctionHandler;
import com.silverpeas.util.StringUtil;

/**
 * Use Case : for all users, show all adds of given category
 * @author Nicolas Eysseric
 */
public class PaginationHandler extends FunctionHandler {

  @Override
  public String getDestination(ClassifiedsSessionController classifiedsSC,
      HttpServletRequest request) throws Exception {

    String index = request.getParameter("Index");
    if (!StringUtil.isInteger(index)) {
      index = (String) request.getAttribute("Index");
    }
    
    // Stores objects in request
    request.setAttribute("SearchContext", classifiedsSC.getSearchContext());
    request.setAttribute("NbTotal", classifiedsSC.getNbTotalClassifieds());
    request.setAttribute("Classifieds", classifiedsSC.getPage(Integer.valueOf(index)));
    request.setAttribute("Pagination", classifiedsSC.getPagination());
    
    return "classifiedsResult.jsp";
  }

}