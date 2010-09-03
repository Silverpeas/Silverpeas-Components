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
<%@ include file="check.jsp" %>

<% 
String 		profile			= (String) request.getAttribute("Profile");
Collection	categories		= (Collection) request.getAttribute("Categories");
String 		nbTotal			= (String) request.getAttribute("NbTotal");
Form 		formSearch 		= (Form) request.getAttribute("Form");
DataRecord	data 			= (DataRecord) request.getAttribute("Data"); 
String		instanceId		= (String) request.getAttribute("InstanceId");
boolean		validation		= ((Boolean) request.getAttribute("Validation")).booleanValue();

// déclaration des boutons
Button validateButton = (Button) gef.getFormButton(resource.getString("GML.search")+" dans <b>"+nbTotal+"</b> "+resource.getString("classifieds.classifieds"), "javascript:onClick=sendData();", false);

%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script language="javascript">
	var subscriptionWindow = window;

	function openSPWindow(fonction, windowName){
		pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
	}

	function sendData() {
		document.classifiedForm.submit();
	}
	
	function addSubscription() {
		url = "NewSubscription";
	    windowName = "subscriptionWindow";
		larg = "550";
		haut = "350";
	    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
	    if (!subscriptionWindow.closed && subscriptionWindow.name== "subscriptionWindow")
	        subscriptionWindow.close();
	    subscriptionWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
	}
</script>

</head>

<body id="classifieds">
<center>
<div id="<%=instanceId%>">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	
	// affichage des options
	operationPane.addOperation(resource.getIcon("classifieds.addClassified"),resource.getString("classifieds.addClassified"), "NewClassified");
	operationPane.addOperation(resource.getIcon("classifieds.myClassifieds"), resource.getString("classifieds.myClassifieds"), "ViewMyClassifieds");
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("classifieds.subscriptionsAdd"),resource.getString("classifieds.addSubscription"), "javaScript:addSubscription()");
	operationPane.addOperation(resource.getIcon("classifieds.mySubscriptions"), resource.getString("classifieds.mySubscriptions"), "ViewMySubscriptions");
	if ("admin".equals(profile) && validation) {
	  	operationPane.addLine();
		operationPane.addOperation(resource.getIcon("classifieds.viewClassifiedToValidate"),resource.getString("classifieds.viewClassifiedToValidate"), "ViewClassifiedToValidate");
	}

	out.println(window.printBefore());
    out.println(frame.printBefore());
    
    Board	board		 = gef.getBoard();
    
	// afficher les critères de tri
	%>
	<FORM Name="classifiedForm" action="SearchClassifieds" Method="POST" ENCTYPE="multipart/form-data">
		<% if (formSearch != null) { %>
			<center>
			<div id="search">
				<!-- AFFICHAGE du formulaire -->
					<%=board.printBefore()%>
					<%
						PagesContext xmlContext = new PagesContext("myForm", "0", resource.getLanguage(), false, instanceId, null);
						xmlContext.setBorderPrinted(false);
						xmlContext.setIgnoreDefaultValues(true);
						
						formSearch.display(out, xmlContext, data);
				    
				    // bouton valider
					ButtonPane buttonPane = gef.getButtonPane();
					buttonPane.addButton(validateButton);
					out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
			
					out.println(board.printAfter());
					%>
			</div>
			</center>
		<% } %>	
	</FORM>
           
	<%
	// affichage des pavés pour les petites annonces par catégorie
	int nbAffiche = 0;
	%>
	<div id="categories">
		<% if (categories != null) {
			Category category;
			Iterator itC = categories.iterator();
			String leftOrRight = "left";
			while (itC.hasNext()) {
				// pour une catégorie
				category = (Category) itC.next();
				String categoryName = category.getValue();
				out.println("<div id=\"category"+leftOrRight+"\" class=\"category"+category.getKey()+"\">");
				//out.println(board.printBefore());				
				nbAffiche = nbAffiche + 1;
							
				// affichage des annonces de cette catégorie
				Collection classifieds = category.getClassifieds();
				int nbClassifieds = 0;
				%>
				<div class="categoryTitle"><a href="ViewAllClassifiedsByCategory?CategoryName=<%=categoryName%>&FieldKey=<%=category.getKey()%>"><%=categoryName%></a></div>
				<div class="categoryContent">
				<%
				if (classifieds == null || classifieds.size() == 0) 
				{
					%>
						<span class="emptyCategory"><%=resource.getString("classifieds.CategoryEmpty")%></span>
					<%	
				}
				else
				{
					ClassifiedDetail classified;
					Iterator it = classifieds.iterator();
					// on ne veut que 5 annonces par catégories
					int max = 5;
					out.println("<ul>");
					while (it.hasNext() && nbClassifieds < max) {
						classified = (ClassifiedDetail) it.next();
						nbClassifieds = nbClassifieds + 1;
						%>
							<li><a href="ViewClassified?ClassifiedId=<%=classified.getClassifiedId()%>"><%=classified.getTitle()%></a> <span class="date"><%=resource.getOutputDateAndHour(classified.getUpdateDate(), classified.getCreationDate())%></span></li>
						<%
					}
					out.println("</ul>");
				}
				out.print("</div>");
				
			  // lien pour la visualisation de toutes les annonces de la catégorie
        %>
          <div id="ViewAllClassifiedsByCategory"><a href="ViewAllClassifiedsByCategory?CategoryName=<%=categoryName%>&FieldKey=<%=category.getKey()%>"><%=resource.getString("classifieds.viewAllClassifiedsByCategory")%></a></div>
        <%
				// lien pour la saisie d'une nouvelle annonce
				%>
					<div id="newClassified"><a href="NewClassified?FieldKey=<%=category.getKey()%>"><%=resource.getString("classifieds.newClassified")%></a></div>
				<%
				
				out.print("</div>");
				if ("left".equals(leftOrRight))
				{
				  leftOrRight = "right";
				}
				else
				{
				  leftOrRight = "left";
				}
			}
		}%>
		<!-- affichage des informations legales -->
    <div id="infos" class="tableBoard">
      <%=resource.getString("classifieds.infos")%>
    </div>
	</div>
	
	<%
  	out.println(frame.printAfter());
	 out.println(window.printAfter());
%>
</div>
</center>
</body>
</html>