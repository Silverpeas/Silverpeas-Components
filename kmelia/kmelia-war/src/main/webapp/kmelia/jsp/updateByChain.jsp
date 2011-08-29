<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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

<%@ page import="com.stratelia.webactiv.kmelia.model.updatechain.*" %>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkKmelia.jsp" %>

<%
String creatorName		= "";
String creationDate		= "";
String name				= "";

KmeliaPublication kmeliaPublication = null;
UserDetail ownerDetail = null;

CompletePublication pubComplete = null;
PublicationDetail pubDetail = null;

//R�cup�ration des param�tres
String 				profile 			= (String) request.getAttribute("Profile");
String 				action 				= (String) request.getAttribute("Action");
String 				id 					= (String) request.getAttribute("PubId");
String 				currentLang 		= (String) request.getAttribute("Language");
Integer				rang				= (Integer) request.getAttribute("Rang");
Integer				nbPublis			= (Integer) request.getAttribute("NbPublis");
String 				linkedPathString 	= (String) request.getAttribute("LinkedPathString");
List	 			topics				= (List) request.getAttribute("Topics");
String 				fileUrl				= (String) request.getAttribute("FileUrl");
Fields				fields				= (Fields) request.getAttribute("SaveFields");

String				pubName				= "";

String 				title 				= fields.getTitle();
String 				libelle 			= fields.getLibelle();

if (fields.getName().getLastValue())
{
	pubName = fields.getName().getValue();
}

String[]			topicChoice			= fields.getTopics();

Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendPublicationDataToRouter('javascript:onClick=next();');", false);
Button updateButton = (Button) gef.getFormButton(resources.getString("Update"), "javascript:onClick=sendPublicationDataToRouter('javascript:onClick=last();');", false);
Button skipButton = (Button) gef.getFormButton(resources.getString("kmelia.skip"), "javascript:onClick=returnToRouter('UpdateChainSkipUpdate');", false);
Button endButton = (Button) gef.getFormButton(resources.getString("kmelia.end"), "javascript:onClick=returnToRouter('UpdateChainEndUpdate');", false);
Button updateAllButton = (Button) gef.getFormButton(resources.getString("kmelia.updateAll"), "javascript:onClick=sendPublicationDataToRouter('UpdateChainUpdateAll');", false);

      //Recuperation des parametres de la publication
	  kmeliaPublication = kmeliaScc.getPublication(id);

 	  kmeliaScc.setSessionPublication(kmeliaPublication);
      pubComplete 	= kmeliaPublication.getCompleteDetail();
      pubDetail 	= pubComplete.getPublicationDetail();
      ownerDetail 	= kmeliaPublication.getCreator();

      creationDate 	= resources.getOutputDate(pubDetail.getCreationDate());
      if (ownerDetail != null)
          creatorName = ownerDetail.getDisplayedName();
      else
          creatorName = resources.getString("UnknownAuthor");

	boolean 	fin   = rang.intValue() == nbPublis.intValue()-1;


%>

<HTML>
<HEAD>
<TITLE></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<style>
.content {
	padding-left: 0px;
	overflow: auto;
	height: 250px;
}

.basic  {
	width: 90%;
}

.basic a {
	cursor:pointer;
	display:block;
	text-decoration: none;
	color: #000000;
	width: 100%;
	margin-top: 2px;
	padding: 5px;
	background-image: url(<%=m_context%>/admin/jsp/icons/silverpeasV4/fondOff.gif);
	background-repeat: repeat-x;
}

</style>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

<script language="javascript">

function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}

function sendPublicationDataToRouter(func) {
	if (isCorrectForm()) {
    	document.pubForm.action = func;
        document.pubForm.submit();
    }
}

function returnToRouter(func) {
    	document.pubForm.action = func;
        document.pubForm.submit();
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.pubForm.Name.value);

     if (isWhitespace(title)) {
           errorMsg+="  - '<%=resources.getString("PubTitre")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }

     <% if ("writer".equals(profile) && (kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable())) { %>
  		var validatorId = stripInitialWhitespace(document.pubForm.ValideurId.value);
  		if (isWhitespace(validatorId)) {
     		errorMsg+="  - '<%=resources.getString("kmelia.Valideur")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
        	errorNb++;
	    }
  	 <% } %>

     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

function init() {
	document.pubForm.Name.focus();
}

function getSelectedOjects()
{
	return getObjects(true);
}

function getObjects(selected)
{
	var  items = "";
	var boxItems = document.pubForm.topicChoice;
	if (boxItems != null){
		// au moins une checkbox exist
		var nbBox = boxItems.length;
		if ( (nbBox == null) && (boxItems.checked == selected) ){
			// il n'y a qu'une checkbox non selectionn�e
			items += boxItems.value+",";
		} else{
			// search not checked boxes
			for (i=0;i<boxItems.length ;i++ ){
				if (boxItems[i].checked == selected){
					items += boxItems[i].value+",";
				}
			}
		}
	}
	return items;
}

function next()
{
	<% if(topics != null && !topics.isEmpty()) { %>
		var selectedPaths = getSelectedOjects();
		document.pubForm.action = "UpdateChainNextUpdate";
		if (selectedPaths.indexOf(',<%=componentId%>,') == -1)
		{
			alert('<%=Encode.javaStringToHtmlString(Encode.javaStringToJsString(resources.getString("kmelia.LocalPathMandatory")))%>');
		}
		else
		{
			document.pubForm.submit();
		}
	<% } else {%>
		document.pubForm.action = "UpdateChainNextUpdate";
		document.pubForm.submit();
	<%}%>
}

function last()
{
	<% if(topics != null && !topics.isEmpty()) { %>
		var selectedPaths = getSelectedOjects();
		document.pubForm.action = "UpdateChainLastUpdate";
		if (selectedPaths.indexOf(',<%=componentId%>,') == -1)
		{
			alert('<%=Encode.javaStringToHtmlString(Encode.javaStringToJsString(resources.getString("kmelia.LocalPathMandatory")))%>');
		}
		else
		{
			document.pubForm.submit();
		}
	<% } else {%>
		document.pubForm.action = "UpdateChainLastUpdate";
		document.pubForm.submit();
	<%}%>
}

</script>
</HEAD>
<BODY class="yui-skin-sam" onLoad="init()">
<%
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();
        Board board = gef.getBoard();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(kmeliaScc.getSpaceLabel());
        browseBar.setComponentName(kmeliaScc.getComponentLabel(), "javascript:onClick=topicGoTo('0')");
        browseBar.setPath(linkedPathString);
		browseBar.setExtraInformation(Encode.javaStringToHtmlString(pubName));

        out.println(window.printBefore());
        out.println(frame.printBefore());

%>
<TABLE>
<TR>
	<TD valign="top" WIDTH="50%">
		<%// affichage de la pi�ce jointe %>
		<%// ---------------------------- %>

			<%
			if (StringUtil.isDefined(fileUrl))
			{
				%>
				<iframe src="<%=fileUrl%>" height="600" width="450" scrolling="auto"></iframe>
				<%
			} %>
	</TD>

	<TD WIDTH="50%" valign="top">

	<%// affichage du titre du fichier %>
	<%// ----------------------------- %>
	<% out.println(board.printBefore()); %>
	<TABLE>
		<TR><TD class="txtlibform"><%=title%></TD></TR>
		<TR><TD class="txtlibform"><%=libelle%></TD></TR>
	</TABLE>
	<% out.println(board.printAfter()); %>
	<BR/>

	<%// affichage des donn�es de l'ent�te %>
	<%// --------------------------------- %>

	<% out.println(board.printBefore()); %>

	<FORM Name="pubForm" Action="" Method="POST" ENCTYPE="POST">
		<TABLE CELLPADDING="5" WIDTH="80%">

			<%
	  		FieldsContext fieldsContext = new FieldsContext(resources.getLanguage(), componentId, null);
			%>

			<TR><TD class="txtlibform"><%=resources.getString("Publication")%></TD>
      			<TD><%=rang.intValue()+1%> / <%=nbPublis.intValue()%><input type="hidden" name="PubId" value="<%=id%>"></TD>
      		</TR>
  			<TR><TD class="txtlibform"><%=resources.getString("PubTitre")%></TD>
 				<TD>
				<%if (fields.getName() != null)
				{
					fields.getName().display(out, fieldsContext, true);
				} %>
				</TD>
  			<TR><TD class="txtlibform"><%=resources.getString("PubDescription")%></TD>
				<TD>
				<%if (fields.getDescription() != null)
				{
					fields.getDescription().display(out, fieldsContext, false);
				} %>
				</TD>
  			</TR>
	      	<TR><TD class="txtlibform"><%=resources.getString("PubMotsCles")%></TD>
	      		<TD>
		     	<%if (fields.getKeywords() != null)
				{
						fields.getKeywords().display(out, fieldsContext, false);
				} %>
				</TD>
      		</TR>

  			<TR><TD class="txtlibform"><%=resources.getString("PubDateCreation")%></TD>
      			<TD><%=creationDate%>&nbsp;<span class="txtlibform"><%=resources.getString("kmelia.By")%></span>&nbsp;<%=creatorName%></TD>
      		</TR>

			<% if ("writer".equals(profile) && (kmeliaScc.isTargetValidationEnable() || kmeliaScc.isTargetMultiValidationEnable())) {
  				String selectUserLab = resources.getString("kmelia.SelectValidator");
  				String link = "&nbsp;<a href=\"#\" onclick=\"javascript:SP_openWindow('SelectValidator','selectUser',800,600,'');\">";
         		link += "<img src=\""
              			+ resources.getIcon("kmelia.user")
              			+ "\" width=\"15\" height=\"15\" border=\"0\" alt=\""
              			+ selectUserLab + "\" align=\"absmiddle\" title=\""
              			+ selectUserLab + "\"></a>";
  			%>
	  			<TR><TD class="txtlibform"><%=resources.getString("kmelia.Valideur")%></TD>
	      		<TD>
			      	<% if (kmeliaScc.isTargetValidationEnable()) { %>
			      		<input type="text" name="Valideur" value="" size="60" readonly="readonly"/>
			      	<% } else { %>
			      		<textarea name="Valideur" rows="4" cols="40" readonly></textarea>
			      	<% } %>
			      	<input type="hidden" name="ValideurId" value=""><%=link%>&nbsp;<img src="<%=resources.getIcon("kmelia.mandatory")%>" align="absmiddle" width="5" height="5" border="0"></TD></TR>
  			<% } %>

  			<TR><TD><input type="hidden" name="Position" value="View"><input type="hidden" name="Action" value="<%=action%>"><input type="hidden" name="PubId" value="<%=id%>"><input type="hidden" name="Importance" value="1"><input type="hidden" name="Status" value=""></TD></TR>
  			<TR><TD colspan="2">( <img border="0" src="<%=resources.getIcon("kmelia.mandatory")%>" width="5" height="5"> : <%=resources.getString("GML.requiredField")%> )</TD></TR>

		</TABLE>

	<% out.println(board.printAfter()); %>

	<BR/>

	<%// affichage des emplacements
	  // --------------------------
 		if(topics != null && !topics.isEmpty())
	    {
			out.println(board.printBefore());

	    	out.println("<table>");
	    	Iterator iter = topics.iterator();
	    	while(iter.hasNext())
	    	{
	    		NodeDetail topic = (NodeDetail) iter.next();
	   			if (topic.getId() != 1 && topic.getId() != 2)
	    		{
		    		name = topic.getName(currentLang);

		    		String ind = "";
		    		if(topic.getLevel() > 2)
		    		{
		    			int sizeOfInd = topic.getLevel() - 2;
		    			if(sizeOfInd > 0)
		    			{
		    				for(int i=0;i<sizeOfInd;i++)
		    				{
		    					ind += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		    				}
		    			}
		    		}
		    		name = ind + name;

					String usedCheck = "";
		    		for (int i = 0; topicChoice != null && i < topicChoice.length; i++)
					{
						String topicId = topicChoice[i];
						StringTokenizer tokenizer = new StringTokenizer(topicId,",");
						String nodeId = tokenizer.nextToken();
			    		if (Integer.toString(topic.getId()).equals(nodeId))
			    			usedCheck = " checked";
			    	}

          			out.println("<tr><td width=\"10px\">");
       				out.println("<input type=\"checkbox\" valign=\"absmiddle\" name=\"topicChoice\" value=\""+topic.getId()+","+topic.getNodePK().getInstanceId()+"\""+usedCheck+">");

       				out.println("</td><td>"+name+"</td></tr>");
	    		}

	    	}
	    	out.println("</table>");
	        out.println(board.printAfter());
	    }
 		else
 		{
			out.println("<input type=\"hidden\" valign=\"absmiddle\" name=\"topicChoice\" value=\"\">");
 		}
	%>
	</form>

	<br><br>
	<%
	ButtonPane buttonPane = gef.getButtonPane();
	ButtonPane buttonPane2 = gef.getButtonPane();

	if (fin)
	{
		buttonPane.addButton(updateButton);
		 buttonPane.addButton(endButton);
	}
	else
	{
		buttonPane.addButton(validateButton);
	    buttonPane.addButton(skipButton);
	    buttonPane.addButton(endButton);
			buttonPane2.addButton(updateAllButton);
	}
	buttonPane.setHorizontalPosition();
	%>
	<table>
		<tr>
			<td align="center">
			<% out.println("<center>"+buttonPane.print()+"</center>");	%>
			</td>
			<td align="center">
				<% out.println(buttonPane2.print()); %>
			</td>
		</tr>
	</table>
	<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
   %>
	</TD>
</TR>
</TABLE>

</BODY>
</HTML>