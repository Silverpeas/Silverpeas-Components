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
<%@ include file="check.jsp" %>

<% 
String 		profile			= (String) request.getAttribute("Profile");
Collection	categories		= (Collection) request.getAttribute("Categories");
String 		nbTotal			= (String) request.getAttribute("NbTotal");
Form 		formSearch 		= (Form) request.getAttribute("Form");
DataRecord	data 			= (DataRecord) request.getAttribute("Data"); 
String		instanceId		= (String) request.getAttribute("InstanceId");

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
<style>
#classifieds {
	text-align: center;
}

#classifieds #search {
	width: 50%;
	margin: auto;
}

#classifieds #categories {
  padding-top: 20px;
  float: left;
}

#classifieds #categories #categoryleft {
	width: 47%;
	float: left;
	margin-left: 10px;
	margin-bottom: 10px;
	position: relative;
	
  background-color:#FFFFFF;
  background-image:url(/silverpeas/admin/jsp/icons/silverpeasV5/fondBoard.jpg);
  background-position:right top;
  background-repeat:no-repeat;
  border:1px solid #CCCCCC;
}

#classifieds #categories #categoryright {
	width: 47%;
	float: right;
	margin-right: 10px;
	margin-bottom: 10px;
	position: relative;
	
	background-color:#FFFFFF;
  background-image:url(/silverpeas/admin/jsp/icons/silverpeasV5/fondBoard.jpg);
  background-position:right top;
  background-repeat:no-repeat;
  border:1px solid #CCCCCC;
}

#classifieds #categories #categoryTitle {
  background-image:url(/silverpeas/admin/jsp/icons/silverpeasV5/milieuBouton.gif);
  background-repeat:repeat-x;
  color:#FFFFFF;
  height:20px;
  text-align: center;
  font-weight: bold;
  text-transform: uppercase;
  padding-top: 3px;
  position: relative;
}

#classifieds #categories #categoryContent {
  height: 70px;
  position: relative;
}

#classifieds #categories #newClassified {
  float: right;
  padding: 5px;
}

#classifieds #categories #categoryContent ul {
  height: 70px;
  /*list-style-type: disc;*/
  margin-left: 10px;
  list-style-type: inherit;
  margin-top: 5px;
}

#classifieds #categories #categoryContent .emptyCategory {
  float:left;
  padding:5px;
}

#classifieds #infos {
	width: 96%;
	margin-left: 10px;
	text-align: center;
	float: left;
	padding: 10px;
}

</style>
</head>

<body id="classifieds">
<center>
<div id="<%=instanceId%>">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	
	// affichage des options
	operationPane.addOperation(resource.getIcon("classifieds.myClassifieds"), resource.getString("classifieds.myClassifieds"), "ViewMyClassifieds");
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("classifieds.addClassified"),resource.getString("classifieds.addClassified"), "NewClassified");
	if ("admin".equals(profile)) {
		operationPane.addOperation(resource.getIcon("classifieds.viewClassifiedToValidate"),resource.getString("classifieds.viewClassifiedToValidate"), "ViewClassifiedToValidate");
	}
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("classifieds.subscriptionsAdd"),resource.getString("classifieds.addSubscription"), "javaScript:addSubscription()");
	operationPane.addOperation(resource.getIcon("classifieds.mySubscriptions"), resource.getString("classifieds.mySubscriptions"), "ViewMySubscriptions");

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
				out.println("<div id=\"categoryTitle\">"+categoryName+"</div>");
				out.println("<div id=\"categoryContent\">");
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
							<li><a href="ViewClassified?ClassifiedId=<%=classified.getClassifiedId()%>"><%=classified.getTitle()%></a></li>
						<%
					}
					out.println("</ul>");
				}
				out.print("</div>");
				// lien pour la saisie d'une nouvelle annonce
				%>
					<div id="newClassified"><a href="NewClassified?FieldKey=<%=category.getKey()%>"><%=resource.getString("classifieds.newClassified")%></a></div>
				<%
				//out.println(board.printAfter());
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