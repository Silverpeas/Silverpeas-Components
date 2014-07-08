<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@page import="org.silverpeas.search.indexEngine.DateFormatter"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%-- Set resource bundle --%>
<c:set var="_language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${_language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<%
	List 				metaDataKeys	= (List) request.getAttribute("MetaDataKeys");
	Form				form	 		= (Form) request.getAttribute("Form");
	DataRecord			data			= (DataRecord) request.getAttribute("Data");

	String 				keyWord			= (String) request.getAttribute("KeyWord");
%>

<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>
<c:set var="keyword" value="${requestScope.KeyWord}" />

<html>
<head>
  <view:looknfeel/>
  <view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<c:url value="/util/javaScript/lucene/luceneQueryValidator.js"/>"></script>
<script type="text/javascript" src="<c:url value="/util/javaScript/animation.js"/>"></script>
<script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js"/>"></script>
<script type="text/javascript">
function sendData() {
	if (checkLuceneQuery()) {
     setTimeout("document.searchForm.submit();", 500);
	}
}

// this method requires luceneQueryValidator.js
function checkLuceneQuery() {
  var query = $("#searchQuery").val();
  if(query != null && query.length > 0) {
    query = removeEscapes(query);
    // check question marks are used properly
    if(!checkQuestionMark(query)) {
      return false;
    }
    // check * is used properly
    if(!checkAsterisk(query)) {
      return false;
    }
    return true;
  }
  return true;
}

</script>
</head>
<body id="${instanceId}" class="gallery gallery-search yui-skin-sam">

<fmt:message key="gallery.searchAdvanced" var="searchLabel" />
<view:browseBar path="${searchLabel}">
</view:browseBar>

<%
	Board board = gef.getBoard();
%>
<view:window>
<view:frame>

<form name="searchForm" action="Search" method="POST" onSubmit="javascript:sendData();" enctype="multipart/form-data" accept-charset="UTF-8">
  <br/>
  <view:board>
		<table>
			<tr>
				<td class="txtlibform" nowrap width="200px"><fmt:message key="GML.search"/> :</td>
				<td><input type="text" name="SearchKeyWord" value="<%=keyWord%>" size="36" id="searchQuery"></td>
			</tr>
		</table>
  </view:board>
		<%
		// affichage des données IPTC
		// --------------------------
		if (metaDataKeys != null && metaDataKeys.size() > 0) {
%>
  <br/>
  <view:board>
    <table cellspacing="3" cellpadding="0">

<%
			Iterator it = (Iterator) metaDataKeys.iterator();
			while (it.hasNext())
			{
				MetaData metaData = (MetaData) it.next();
				String property = metaData.getProperty();
				String metaDataLabel = metaData.getLabel();
				String metaDataValue = metaData.getValue();
				if (!StringUtil.isDefined(metaDataValue)) {
					metaDataValue = "";
				}
				%>
				<tr>
					<td class="txtlibform" nowrap width="200px"><%=metaDataLabel%> :</td>
					<% if (metaData.isDate()) {
							String beginDate = "";
							String endDate = "";
							//metaDataValue looks like [20080101 TO 20081231]
							if (StringUtil.isDefined(metaDataValue)) {
								beginDate = metaDataValue.substring(1, 9);
								if (!DateFormatter.nullBeginDate.equals(beginDate)) {
									beginDate = resource.getOutputDate(DateFormatter.string2Date(beginDate));
								} else {
                  beginDate = "";
                }

								endDate = metaDataValue.substring(13, metaDataValue.length()-1);
								if (!DateFormatter.nullEndDate.equals(endDate)) {
									endDate = resource.getOutputDate(DateFormatter.string2Date(endDate));
								} else {
									endDate = "";
								}
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
      %>
    </table>
  </view:board>
<%
		}
%>
	<br/>
<%
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

		// Display PDC
		out.println(board.printBefore());
		out.flush();
		getServletConfig().getServletContext().getRequestDispatcher("/pdcPeas/jsp/pdcInComponent.jsp?ComponentId="+componentId).include(request, response);
		out.println(board.printAfter());

	%>
  <view:buttonPane>
    <fmt:message key="GML.search" var="searchButtonLabel" />
    <view:button label="${searchButtonLabel}" action="javascript:onClick=sendData();"></view:button>
    <fmt:message key="gallery.search.reset" var="resetButtonLabel" />
    <view:button label="${resetButtonLabel}" action="ClearSearch"></view:button>
  </view:buttonPane>
</form>

</view:frame>
</view:window>
</body>
</html>