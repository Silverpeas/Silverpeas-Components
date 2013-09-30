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
<%@page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="checkYellowpages.jsp" %>

<%
List<PublicationTemplate> forms = (List<PublicationTemplate>) request.getAttribute("XMLForms");
NodeDetail node = (NodeDetail) request.getAttribute("Node");

String id = "";
String name = "";
String desc = "";
String modelId = "";
String action = "AddFolder";
if (node != null) {
  id = node.getNodePK().getId();
  name = node.getName();
  desc = node.getDescription();
  modelId = node.getModelId();
  action = "UpdateFolder";
}
%>
<form name="AddAndUpdateFolderForm" action="<%=action %>" method="post">
            <table cellpadding="5" cellspacing="0">
                <tr>            
                    <td class="txtlibform"><%=resources.getString("TopicTitle")%> : </td>
                    <td><input type="text" name="Name" value="<%=name %>" size="60" maxlength="60">&nbsp;<img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5" />
                    	<input type="hidden" name="TopicId" value="<%=id %>"/>
                    </td>
                </tr>
                <tr>            
                    <td class="txtlibform"><%=resources.getString("GML.description")%> :</td>
                    <td><input type="text" name="Description" value="<%=desc %>" size="60" maxlength="200"></td>
                </tr>
                <% if (forms != null && !forms.isEmpty()) { %>
                	<tr>            
                    	<td class="txtlibform"><%=resources.getString("Model")%> :</td>
                    	<td><select name="FormId">
                    		<option value=""><%=resources.getString("Nomodel")%></option>
                    		<%  String selected = "";
                    			for (PublicationTemplate form : forms) {
                    			    selected = "";
                    				if (form.getFileName().equals(modelId)) {
                    				  selected = "selected=\"selected\"";
                    				}
                    			%>
                    			<option value="<%=form.getFileName()%>" <%=selected %>><%=form.getName() %></option>
                    		<% } %>
                    	</select>
                    	</td>
                	</tr>
                <% } %>
                <tr> 
                    <td colspan="2"><img border="0" src="<%=resources.getIcon("yellowpages.mandatory")%>" width="5" height="5"/> : <%=resources.getString("GML.requiredField")%></td>
                </tr>
            </table>
</form>