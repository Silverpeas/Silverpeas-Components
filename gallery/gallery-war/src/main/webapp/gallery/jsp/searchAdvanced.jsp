<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<% 	
	List 				metaDataKeys	= (List) request.getAttribute("MetaDataKeys");
	Form				form	 		= (Form) request.getAttribute("Form");
	DataRecord			data			= (DataRecord) request.getAttribute("Data");
	
	String 				keyWord			= (String) request.getAttribute("KeyWord");
%>

<%@page import="org.silverpeas.search.indexEngine.DateFormatter"%>
<html>
<head>
  <view:looknfeel/>
  <view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function sendData() 
{
    //var query = stripInitialWhitespace(document.searchForm.SearchKeyWord.value);
	//if (!isWhitespace(query) && query != "*") {
    	//displayStaticMessage();
  		setTimeout("document.searchForm.submit();", 500);
    //}
}
</script>
</head>
<body class="yui-skin-sam">

<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(resource.getString("gallery.searchAdvanced"));
	
	Board board = gef.getBoard();
	Button validateButton 	= (Button) gef.getFormButton(resource.getString("GML.search"), "javascript:onClick=sendData();", false);
	Button razButton		= (Button) gef.getFormButton(resource.getString("gallery.raz"), "ClearSearch", false);
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
    
	%>
	<form name="searchForm" action="Search" method="POST" onSubmit="javascript:sendData();" ENCTYPE="multipart/form-data" accept-charset="UTF-8">
	
		<%
 		// affichage de la zone de recherche
		// ---------------------------------
		out.println("<br/>");
		out.println(board.printBefore());
		%>
		<table>
			<tr>
				<td class="txtlibform" nowrap width="200px"><%=resource.getString("GML.search")%> :</td>
				<td><input type="text" name="SearchKeyWord" value="<%=keyWord%>" size="36"></td>
			</tr>
		</table>
		<%
		out.println(board.printAfter());
		 
		// affichage des données IPTC
		// --------------------------
			 
		if (metaDataKeys != null && metaDataKeys.size() > 0) 
		{
			out.println("<br/>");
			out.println(board.printBefore());
			out.println("<table cellspacing=\"3\" cellpadding=\"0\">");
			Iterator it = (Iterator) metaDataKeys.iterator();
			while (it.hasNext())
			{
				MetaData metaData = (MetaData) it.next();
	
				// extraire les données
				String property = metaData.getProperty();
				String metaDataLabel = metaData.getLabel();
				String metaDataValue = metaData.getValue();
				if (!StringUtil.isDefined(metaDataValue))
					metaDataValue = "";
				// affichage
				%>
				<tr>
					<td class="txtlibform" nowrap width="200px"><%=metaDataLabel%> :</td>
					
					<% if (metaData.isDate()) {
							String beginDate = "";
							String endDate = "";
							//metaDataValue looks like [20080101 TO 20081231]
							if (StringUtil.isDefined(metaDataValue))
							{
								beginDate = metaDataValue.substring(1, 9);
								if (!DateFormatter.nullBeginDate.equals(beginDate))
									beginDate = resource.getOutputDate(DateFormatter.string2Date(beginDate));
								else
									beginDate = "";
								
								endDate = metaDataValue.substring(13, metaDataValue.length()-1);
								if (!DateFormatter.nullEndDate.equals(endDate))
									endDate = resource.getOutputDate(DateFormatter.string2Date(endDate));
								else
									endDate = "";
							}
					%>
						<td>
							<input type="text" class="dateToPick" id="<%=property%>_Begin" name="<%=property%>_Begin" size="12" value="<%= beginDate %>"/>
							<input type="text" class="dateToPick" id="<%=property%>_End" name="<%=property%>_End" size="12" value="<%= endDate %>"/>
						</td>
					<% } else { %>
						<td><input type="text" name="<%=property%>" value="<%=metaDataValue%>" size="36"/></td>
					<% } %>
				</tr>
				<%
			}
			out.println("</table>");
			out.println(board.printAfter());
		}
			 
		// affichage du formulaire XML
		// ---------------------------
		
		out.println("<br/>");
		
		if (form != null) 
		{
			out.println(board.printBefore());
			out.println("<table><tr><td>");
				PagesContext xmlContext = new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, null);
				xmlContext.setBorderPrinted(false);
				xmlContext.setUseMandatory(false);
				xmlContext.setUseBlankFields(true);
		    	form.display(out, xmlContext, data);
	    	out.println("</table></td></tr>");
			out.println(board.printAfter());
			out.println("<br/>");
		}
			 
		// affichage du PDC
		// ----------------
		out.println(board.printBefore());
		out.flush();
		getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/pdcInComponent.jsp?ComponentId="+componentId).include(request, response);
		out.println(board.printAfter());
		
		// bouton de validation
		// --------------------
		
		ButtonPane buttonPane = gef.getButtonPane();
	    buttonPane.addButton(validateButton);
	    buttonPane.addButton(razButton);
		out.println("<BR/><center>"+buttonPane.print()+"</center><BR/>");
		
		

	%>
	</form>

	<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
	%>

</body>
</html>