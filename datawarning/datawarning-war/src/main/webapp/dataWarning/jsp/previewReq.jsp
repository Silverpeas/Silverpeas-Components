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