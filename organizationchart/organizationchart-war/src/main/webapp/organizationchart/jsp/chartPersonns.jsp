<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page isELIgnored="false"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.Iterator"%>

<%@ page import="com.silverpeas.components.organizationchart.model.OrganizationalChart"%>
<%@ page import="com.silverpeas.components.organizationchart.model.OrganizationalUnit"%>
<%@ include file="check.jsp"%>

<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%
// ------------------------------------------------------------------------------
// ORGANIGRAMME DE TYPE PERSONNES
// ------------------------------------------------------------------------------	 			
%>
var jCells = new Array('jCells');
var jLinks = new Array('jLinks');
<c:set var="parentUnit" value="${organigramme.parent}"/>
<%
// gestion du label central
ArrayList center = new ArrayList();
ArrayList centerNames = new ArrayList();
%>
<c:forEach var="person" items="${organigramme.personns}">
	<c:set var="personName" value="${person.name}" />
	<c:set var="personDetails" value="${person.detail}" />
	<%
	String labelToAdd = (String)pageContext.getAttribute("personName");
	Map detailsMap = (Map)pageContext.getAttribute("personDetails");
    if(detailsMap != null && detailsMap.size() > 0){
    	Set keys = detailsMap.keySet();
    	Iterator iter = keys.iterator();
    	int nbkey = 0;
    	while(iter.hasNext()){
      		String key = (String)iter.next();
      		String val = (String)detailsMap.get(key);
      		labelToAdd += " / " + key + ": " + val;
    	}
 	}
	%>
	<c:choose>
	 	<c:when test="${person.visibleOnCenter}">
			<%center.add(labelToAdd);%>
			<c:set var="centerName" value="${person.visibleCenterLabel}" />
			<%
			if(!centerNames.contains((String)pageContext.getAttribute("centerName"))){
				centerNames.add((String)pageContext.getAttribute("centerName"));
			}
			%>
	 	</c:when>
	</c:choose>
</c:forEach>
<%
String centerTitle = "";
if(center.size() > 0 && centerNames.size() > 0){
	for ( int i=0; i<centerNames.size(); i++ )
    {
    	if(centerTitle.length()>0){
        	centerTitle += " - ";
     	}
     	centerTitle += centerNames.get(i);
   	}
   	if(centerTitle.length()>0){
    	centerTitle += ": ";
   	}
}
OrganizationalUnit parentUnit = (OrganizationalUnit)pageContext.getAttribute("parentUnit");
String url = m_context + organizationChartScc.getComponentUrl() + "?baseOu=" + URLEncoder.encode(parentUnit.getCompleteName());
String urlUpper = "";
if(parentUnit.getParentOu() != null){ 
	urlUpper = m_context + organizationChartScc.getComponentUrl() + "?baseOu=" + URLEncoder.encode(parentUnit.getParentOu());
}
%>
var infoSup = new Array();
<%
for (int l=0; l<center.size(); l++ )
{
  if(l==0){
%>
    infoSup[<%=l%>] = "<%=centerTitle + center.get(l)%>";

<%
  }else{
%>
    infoSup[<%=l%>] = "<%=center.get(l)%>";
<%
  }
}
%>
jCells[0] = new JCell(0, "<c:out value="${organigramme.parent.name}"/>", infoSup, "<%=url%>", 0, 0, 0,<c:out value="${organigramme.parent.underOrganizationalUnitExists}"/>,false, "<%=urlUpper%>");
<%
int j = 1;
int other = -1;
Map categMap = new HashMap();
%>
<c:forEach var="category" items="${organigramme.categories}">
	<c:choose>
		<c:when test="${category.otherCategory}">
      		<c:set var="otherName" value="${category.name}"/>
      	</c:when>
		<c:otherwise>
			jCells[<%=j%>] = new JCell(<%=j%>, "<c:out value="${category.name}"/>", "", "", 1, 1, 1,false,false,"");
			jLinks[<%=j-1%>] = new JLink(0, <%=j%>, 0, 0);
			<c:set var="categKey" value="${category.key}"/>
			<%
			categMap.put((String)pageContext.getAttribute("categKey"),String.valueOf(j));
			%>
			<%j++;%>
		</c:otherwise>
	</c:choose>
</c:forEach>
<%if(pageContext.getAttribute("otherName") != null && !"".equals((String)pageContext.getAttribute("otherName"))){%>
	jCells[<%=j%>] = new JCell(<%=j%>, "<c:out value="${otherName}"/>", "", "", 1, 1, 1,false,false,"");
	jLinks[<%=j-1%>] = new JLink(0, <%=j%>, 0, 0);
	<%
    other = j;
    j++;
}
%>
var detailForChild;
<c:forEach var="child" items="${organigramme.personns}">
	<c:choose>
	 	<c:when test="${child.visibleOnCenter}">
		</c:when>
		<c:otherwise>
			<c:set var="details" value="${child.detail}"/>
			detailForChild = new Array();
			<%
			Map detailsMap = (Map)pageContext.getAttribute("details");
			if(detailsMap != null && detailsMap.size() > 0){
				Set keys = detailsMap.keySet();
				Iterator iter = keys.iterator();
				int nbkey = 0;
				while(iter.hasNext()){
			   		String key = (String)iter.next();
			   		String val = (String)detailsMap.get(key);
			   		%>
			   		detailForChild[<%=nbkey%>] = "<%=key%>" + ":" + "<%=val%>" ;
			   		<%
			    	nbkey++;
			 	}	
			}
			%>
			jCells[<%=j%>] = new JCell(<%=j%>, "<c:out value="${child.name}"/>", detailForChild, "", 2, 2, 1,false,false,"");
			<c:set var="childCateg" value="${child.visibleCategory}"/>
			<%
			String childCategory = (String)pageContext.getAttribute("childCateg");
			int start = other;
			if(childCategory != null && categMap.get(childCategory) != null ){
				start = Integer.valueOf((String)categMap.get(childCategory)).intValue();
			}
			%>
			jLinks[<%=j-1%>] = new JLink(<%=start%>, <%=j%>, 0, 1);
			<%j++;%>
		</c:otherwise>
	</c:choose>
</c:forEach>