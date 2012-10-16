<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkDataWarning.jsp" %>
<%
    DataWarningQueryResult resultQuery = (DataWarningQueryResult)request.getAttribute("resultQuery");
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("operationPaneReqVisu")%></TITLE>
<%
	out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());

        if(resultQuery != null)
        {
            ArrayPane arrayPane = gef.getArrayPane("ViewRequete","",request,session);
            arrayPane.setSortable(false);
            arrayPane.setVisibleLineNumber(-1);
            
            Iterator itCols = resultQuery.getColumns().iterator();
            while (itCols.hasNext())
            {
            	String theCol = (String)itCols.next();
            	if ((theCol == null) || (theCol.length() <= 0))
            		theCol = resource.getString("noName");
                arrayPane.addArrayColumn(theCol);	                         
            }

            Iterator itRows = resultQuery.getValues().iterator();
            while (itRows.hasNext())
            {
                ArrayList theRow = (ArrayList)itRows.next();
                ArrayLine arrayLine = arrayPane.addArrayLine();
                Iterator itVals = theRow.iterator();
                while (itVals.hasNext())
                    arrayLine.addArrayCellText((String)itVals.next());
            }
            out.println(arrayPane.print());
        }
%>
<CENTER>
<%
    buttonPane.addButton((Button) gef.getFormButton("OK", "javascript:onClick=window.close();", false));
    out.println(buttonPane.print());
%>
</CENTER>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>