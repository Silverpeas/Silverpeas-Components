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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<% 
Form 		formSearch 		= (Form) request.getAttribute("Form");
DataRecord	data 			= (DataRecord) request.getAttribute("Data"); 
String		instanceId		= (String) request.getAttribute("InstanceId");

// d�claration des boutons
//Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendData();", false);
//Button cancelButton   = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javaScript:window.close()", false);

%>

<script type="text/javascript">
  $(function() {
		$( "#subscription-adding" ).dialog({
			autoOpen: false,
			height: 200,
			width: 400,
			modal: true,
			buttons: {
				'<%= resource.getString("GML.validate") %>': function() {
                    sendSubscriptionData();
                },
				'<%= resource.getString("GML.cancel") %>' : function() {
					$( this ).dialog( "close" );
				}
			}
		});
	});

	function sendSubscriptionData() {
      $( "#subscription-adding" ).dialog( "close" );
      document.SubscriptionForm.submit();
	}
</script>

<div id="subscription-adding" style="display: none">

<%
	//browseBar.setDomainName(spaceLabel);
	//browseBar.setComponentName(componentLabel);
	//browseBar.setPath(resource.getString("classifieds.subscriptionsAdd"));
	
	// affichage des options
	//out.println(window.printBefore());
    //out.println(frame.printBefore());
    
    //Board	board		 = gef.getBoard();
    
	// afficher les crit�res de tri
	%>
	<br/>
	<FORM Name="SubscriptionForm" action="AddSubscription" Method="POST" ENCTYPE="multipart/form-data">
		<% if (formSearch != null) { %>
			
			<table border="0" width="100%" align="center">
				<!-- AFFICHAGE du formulaire -->
				<tr>
					<td colspan="2">
					<%
						PagesContext xmlContext = new PagesContext("myForm", "0", resource.getLanguage(), false, instanceId, null);
						xmlContext.setBorderPrinted(false);
						xmlContext.setIgnoreDefaultValues(true);
						
						formSearch.display(out, xmlContext, data);
				    %>
					</td>	
				</tr>
			</table>
			<br/>
		<% } %>	
	</FORM>
		
	<%
	// bouton valider
	//ButtonPane buttonPane = gef.getButtonPane();
	//buttonPane.addButton(validateButton);
	//buttonPane.addButton(cancelButton);
	//out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	
	
	
	
  	//out.println(frame.printAfter());
	//out.println(window.printAfter());
%>
</div>