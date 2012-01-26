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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.yellowpages.servlets;

import com.silverpeas.util.MimeTypes;
import com.silverpeas.yellowpages.model.Company;
import com.silverpeas.yellowpages.service.CompanyService;
import com.silverpeas.yellowpages.service.ServicesFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class CompanyAutocompleteServlet extends HttpServlet {

    public final String SERVLET_JSON_CONTENT_TYPE = "application/json";
    private static final String SEPARATOR = ":";

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(MimeTypes.SERVLET_HTML_CONTENT_TYPE);
        //response.setContentType(SERVLET_JSON_CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        // TODO ne pas utiliser le service directement mais le scc
        CompanyService service = ServicesFactory.getCompanyService();
        String searchParameter = request.getParameter("q");
        try {
            List<Company> companyList = service.findCompaniesByPattern(searchParameter);
            for (Iterator<Company> iterator = companyList.iterator(); iterator.hasNext(); ) {
                StringBuffer buffer = new StringBuffer();
                Company company = iterator.next();
                out.println(buffer.append(company.getCompanyId()).append(SEPARATOR).append(company.getName()));
            }
        } finally {
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
