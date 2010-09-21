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
// ORGANIGRAMME DE TYPE UNIT
// ------------------------------------------------------------------------------	 			
%>
var jCells = new Array('jCells');
var jLinks = new Array('jLinks');
<c:set var="parentUnit" value="${organigramme.parent}" />
<%
ArrayList center = new ArrayList();
ArrayList right = new ArrayList();
ArrayList left = new ArrayList();
ArrayList centerNames = new ArrayList();
ArrayList rightNames = new ArrayList();
ArrayList leftNames = new ArrayList();
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
 		<c:when test="${person.visibleOnRight}">
			<%right.add(labelToAdd);%>
			<c:set var="rightName" value="${person.visibleRightLabel}" />
			<%
			if(!rightNames.contains((String)pageContext.getAttribute("rightName"))){
				rightNames.add((String)pageContext.getAttribute("rightName"));
			}
			%>
			</c:when>
		<c:when test="${person.visibleOnLeft}">
			<%left.add(labelToAdd);%>
			<c:set var="leftName" value="${person.visibleLeftLabel}" />
			<%
			if(!leftNames.contains((String)pageContext.getAttribute("leftName"))){
				leftNames.add((String)pageContext.getAttribute("leftName"));
			}
			%>
   		</c:when>
  	</c:choose>
</c:forEach>
<%
String centerTitle = "";
if(center.size() > 0 && centerNames.size() > 0){
	for(int i=0; i<centerNames.size(); i++ )
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
jCells[0] = new JCell(0, "<c:out value="${organigramme.parent.name}"/>", infoSup, "<%=url%>", 0, 0, 0, false,<c:out value="${organigramme.parent.underPersonnsExists}"/>,"<%=urlUpper%>");
<%
int i = 1;
int level = 1;
boolean rightOrLeftExist = false;
   
if(right != null && right.size()>0){
	String rightTitle = "";
  	if(rightNames.size() > 0){
 		for ( int k=0; k<rightNames.size(); k++ )
    	{
      		if(rightTitle.length()>0){
          		rightTitle += " - ";
      	}
      	rightTitle += rightNames.get(k);
    }
    if(rightTitle.length()>0){
      	rightTitle += ": ";
    }
}
%>
infoSup = new Array();
<%
for (int l=0; l<right.size(); l++ )
{
%>
	infoSup[<%=l%>] = "<%=right.get(l)%>";
<%
}
%>
jCells[<%=i%>] = new JCell(<%=i%>, "<%=rightTitle%>", infoSup, "", <%=level%>, 1, 1, false, false,"");
jLinks[<%=i-1%>] = new JLink(0, <%=i%>, 0, 2);
<%
i++;
rightOrLeftExist = true;
}
if(left != null && left.size()>0){
  	String leftTitle = "";
  	if(leftNames.size() > 0){
 		for ( int k=0; k<leftNames.size(); k++ )
    	{
      		if(leftTitle.length()>0){
          		leftTitle += " - ";
      		}
      		leftTitle += leftNames.get(k);
    	}
	    if(leftTitle.length()>0){
	      leftTitle += ": ";
	    }
	}
%>
     infoSup = new Array();
     <%
     for (int l=0; l<left.size(); l++ )
     {
     %>
       infoSup[<%=l%>] = "<%=left.get(l)%>";
     <%
     }
     %>
     jCells[<%=i%>] = new JCell(<%=i%>, "<%=leftTitle%>", infoSup, "", <%=level%>, 1, 1, false, false,"");
     jLinks[<%=i-1%>] = new JLink(0, <%=i%>, 0, 3);
<%    
     i++;
  	 rightOrLeftExist = true;
}

if(rightOrLeftExist) {
  level = 2;
}
%>     
<c:forEach var="child" items="${organigramme.units}">
<%
	url = m_context + organizationChartScc.getComponentUrl() + "?baseOu=" + URLEncoder.encode(((OrganizationalUnit)pageContext.getAttribute("child")).getCompleteName());
	%>
	jCells[<%=i%>] = new JCell(<%=i%>, "<c:out value="${child.name}"/>", "", "<%=url%>", <%=level%>, 1, 0,<c:out value="${child.underOrganizationalUnitExists}"/>,<c:out value="${child.underPersonnsExists}"/>,"");
	jLinks[<%=i-1%>] = new JLink(0, <%=i%>, 0, 0);
	<%i++;%>
</c:forEach>