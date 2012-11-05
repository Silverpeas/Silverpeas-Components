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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp"%>

<%
Collection resources = (Collection) request.getAttribute("Resources");
%>

<html>
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
   out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">

function refresh() 
{
	<%
	String resourceName = "";
	String resourceId = "";
	int resourceCharge = 0;
	int occupation = 0;
	
	String test = "";
	int i = 0;
	
	out.println("var tdResources = window.opener.document.getElementById(\"resources\");");
	
	// suppression de la table existente
	out.println("try {");
	out.println("var tableResources = window.opener.document.getElementById(\"tableResources\");");
	out.println("tdResources.removeChild(tableResources);");
	out.println("} catch (e) {");
	out.println("}");
	
	if (resources != null) {
		Iterator it = resources.iterator();
		//test += "<table>";
		out.println("var table0 = window.opener.document.createElement(\"table\");");
		out.println("table0.setAttribute(\"id\", \"tableResources\");");
		out.println("tdResources.appendChild(table0);");
		
		out.println("var tbody = window.opener.document.createElement(\"tbody\");");
		out.println("table0.appendChild(tbody);");
		
		while(it.hasNext())
		{
			TaskResourceDetail resourceDetail = (TaskResourceDetail) it.next();
			resourceName = resourceDetail.getUserName();
			resourceId = resourceDetail.getUserId();
			resourceCharge = resourceDetail.getCharge();
			occupation = resourceDetail.getOccupation();
			
			//test += "<tr>";
			out.println("var tr"+i+" = window.opener.document.createElement(\"tr\");");
			out.println("tbody.appendChild(tr"+i+");");
			
			//test += "<td>";
			out.println("var td"+i+"0 = window.opener.document.createElement(\"td\");");
			out.println("tr"+i+".appendChild(td"+i+"0);");
			
			
			
			//test += "<input type=\"hidden\" name=\"Resource"+i+"\" value=\""+resourceId+"\"/>";
			out.println("var inputId = window.opener.document.createElement(\"input\");");
			out.println("inputId.setAttribute(\"id\", \"Resource"+i+"\");");
			out.println("inputId.setAttribute(\"value\", \""+resourceId+"\");");
			out.println("inputId.setAttribute(\"type\", \"hidden\");");
			out.println("td"+i+"0.appendChild(inputId);");

			out.println("var inputName = window.opener.document.createElement(\"span\");");
			out.println("inputName.innerHTML = \""+resourceName+"\";");
			out.println("td"+i+"0.appendChild(inputName);");
			
			//test += resourceName;
			
			//test += "</td>";
			
			//test += "<td>";
			out.println("var td"+i+"1 = window.opener.document.createElement(\"td\");");
			out.println("tr"+i+".appendChild(td"+i+"1);");
							
			//test += "&nbsp;<input type=\"text\" name=\"Charge"+i+"\" size=\"3\"/> %";
			out.println("var inputCharge"+i+" = window.opener.document.createElement(\"input\");");
			out.println("inputCharge"+i+".setAttribute(\"id\", \"Charge"+i+"\");");
			out.println("inputCharge"+i+".setAttribute(\"size\", \"3\");");
			out.println("inputCharge"+i+".setAttribute(\"type\", \"text\");");
			out.println("inputCharge"+i+".setAttribute(\"value\", \""+resourceCharge+"\");");
			//out.println("inputCharge"+i+".setAttribute(\"onBlur\", \"javascript:reloadOccupation('"+i+"');\");");
			//out.println("inputCharge"+i+".onblur = reloadOccupation('"+i+"');");
				
			out.println("td"+i+"1.appendChild(inputCharge"+i+");");
			
			out.println("var percent"+i+" = window.opener.document.createElement(\"span\");");
			out.println("percent"+i+".innerHTML = \"% \";");
			out.println("td"+i+"1.appendChild(percent"+i+");");
					
			// occupation des ressources
			out.println("var occupation"+i+" = window.opener.document.createElement(\"span\");");
			out.println("occupation"+i+".setAttribute(\"id\", \"Occupation"+i+"\");");
			out.println("occupation"+i+".innerHTML = \""+occupation+"\";");
			out.println("td"+i+"1.appendChild(occupation"+i+");");
			
			/*out.println("var percent2 = window.opener.document.createElement(\"span\");");
			out.println("percent2.innerHTML = \"%\";");
			out.println("td"+i+"1.appendChild(percent2);");*/
			
			//test += "</td></tr>";
			i++;
		}
		
		//test += "</table>";
	}
	%>
	//window.opener.document.getElementById("resources").innerHTML = "<%=Encode.javaStringToJsString(test)%>";
	//window.opener.init();
	window.opener.reloadOccupations();
	window.close();
}
</script>
</head>
<body onLoad=refresh()>
</body>
</html>