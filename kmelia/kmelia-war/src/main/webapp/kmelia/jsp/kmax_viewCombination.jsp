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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ page import="com.stratelia.webactiv.util.coordinates.model.Coordinate"%>
<%@ page import="com.stratelia.webactiv.util.coordinates.model.CoordinatePoint"%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="kmax_axisReport.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%!
CoordinatePoint getPoint(NodeDetail nodeDetail, Collection points, String translation, KmeliaSessionController kmeliaScc)
{
	Iterator 		pointsIt 	= points.iterator();
	CoordinatePoint point 		= null;
	while (pointsIt.hasNext()) {
		point = (CoordinatePoint) pointsIt.next();
		if (point.getPath().indexOf("/"+nodeDetail.getNodePK().getId()+"/") != -1)
		{
			try
			{
				NodeDetail pointDetail = kmeliaScc.getNodeHeader(new Integer(point.getNodeId()).toString());
				point.setName(pointDetail.getName(translation));
			}  catch (Exception e)
			{
				SilverTrace.error( "kmax", "kmax_viewCombination.jsp", "kmelia.EX_IMPOSSIBLE_DACCEDER_AU_THEME", e );
			}
			return point;
		}
	}
	return null;
}
%>

<%
	String deleteSrc			= m_context + "/util/icons/delete.gif";
	String alertSrc			= m_context + "/util/icons/alert.gif";
	String deletePubliSrc		= m_context + "/util/icons/publicationDelete.gif";
	String hLineSrc			= m_context + "/util/icons/colorPix/1px.gif";

	ResourceLocator publicationSettings = new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", kmeliaScc.getLanguage());
	
	UserCompletePublication userPubComplete = null;
	UserDetail ownerDetail = null;
	boolean isOwner = false;
	
	CompletePublication pubComplete = null;
	PublicationDetail pubDetail = null;
	InfoDetail infos = null;
	ModelDetail model = null;
	String vignette_url = null;
	
	String 	profile 	= (String) request.getParameter("Profile");
	String	currentLang = (String) request.getAttribute("Language");
	
	String	wizardLast		= (String) request.getAttribute("WizardLast");
	String 	wizard			= (String) request.getAttribute("Wizard");
	String 	wizardRow		= (String) request.getAttribute("WizardRow");

	if (!StringUtil.isDefined(wizardLast))
		wizardLast = "4";

	if (wizardRow == null)
		wizardRow = "4";

	boolean isEnd = true;
	
	String action = "KmaxViewCombination";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title></title>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
	function search() {
	    z = "";
	    nbSelectedAxis = 0;
	    for (var i=0; i<document.axisForm.length; i++) {
	        if (document.axisForm.elements[i].value.length != 0) {
	            if (nbSelectedAxis != 0)
	                z += ",";
	            nbSelectedAxis = 1;
	            truc = document.axisForm.elements[i].value.split("|");
	            z += truc[0];
	        }
	    }
	    if (nbSelectedAxis != 1) {
	            window.alert("Vous devez s�lectionnez au moins un axe !");
	    } else {
	            document.managerForm.action = "KmaxAddCoordinate";
	            document.managerForm.SearchCombination.value = z;
	            document.managerForm.submit();
	    }
	}

	function deleteCoordinate(coordinateId) {
	    if(window.confirm("<%=kmeliaScc.getString("ConfirmDeleteCoordinate")%> ?")){
	        document.managerForm.CoordinateId.value = coordinateId;
	        document.managerForm.action = "KmaxDeleteCoordinate";
	        document.managerForm.submit();
	    }
	}
</script>
</head>
<body>
<%
	Window window = gef.getWindow();
	Frame frame = gef.getFrame();
	Board board = gef.getBoard();
	
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(kmeliaScc.getSpaceLabel());
	browseBar.setComponentName(kmeliaScc.getComponentLabel(), "KmaxMain");
	String pubName = kmeliaScc.getSessionPublication().getPublication().getPublicationDetail().getName(currentLang);
	browseBar.setExtraInformation(Encode.encodeSpecialChar(pubName));
	String id = kmeliaScc.getSessionPublication().getPublication().getPublicationDetail().getId();
	browseBar.setI18N(action, currentLang);
	
    out.println(window.printBefore());

	if ("progress".equals(wizard))
		displayWizardOperations(wizardRow, id, kmeliaScc, gef, action, resources, out, kmaxMode);
	else
		displayAllOperations(id, kmeliaScc, gef, action, resources, out, true);
    	
    out.println(frame.printBefore());
    if ("finish".equals(wizard) || "progress".equals(wizard))
	{
		//cadre d'aide
    	Board boardHelp = gef.getBoard();
	    out.println(boardHelp.printBefore());
		out.println("<table border=\"0\"><tr>");
		out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resources.getIcon("kmelia.info")+"\"></td>");
		out.println("<td>"+Encode.javaStringToHtmlParagraphe(resources.getString("kmelia.HelpKmaxClassification"))+"</td>");
		out.println("</tr></table>");
	    out.println(boardHelp.printAfter());
	    out.println("<br />");
	}
    
	out.println(displayAxisToPublish(kmeliaScc, gef, currentLang));
    
	Collection coordinates = kmeliaScc.getPublicationCoordinates(id);
	out.println("<br>");
	out.println(board.printBefore());

	out.println("<table align=\"center\" cellpading=\"0\" cellspacing=\"3\" border=\"0 \" width=\"100%\">");
    if (coordinates.size() <= 0) {
        out.println("<tr><td align=\"center\">"+kmeliaScc.getString("NoPositions")+"</td></tr>");
    } else {
      //display the axis names
      List axisHeaders = kmeliaScc.getAxisHeaders();
      Iterator headersIt = axisHeaders.iterator();
      NodeDetail nodeDetail = null;
	  out.println("<tr><td colspan=\"15\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+"\" width=\"100%\" height=\"1\"></td></tr>");
      out.println("<tr>");
      while (headersIt.hasNext()) {
          nodeDetail = (NodeDetail) headersIt.next();
		  //Do not get hidden nodes (Basket and unclassified)
		  if (!NodeDetail.STATUS_INVISIBLE.equals(nodeDetail.getStatus()))
			  out.println("<td align=\"center\"><b>"+Encode.javaStringToHtmlString(nodeDetail.getName(currentLang))+"</b></td>");
      }
     out.println("<td align=\"center\"><b>"+kmeliaScc.getString("Del")+"</b></td></tr>");
	 out.println("<tr><td colspan=\"15\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+"\" width=\"100%\" height=\"1\"></td></tr>");

      //display coordinates
      Iterator it = coordinates.iterator();
      Coordinate coordinate = null;
      Collection points = null;
      Iterator pointsIt = null;
      CoordinatePoint point = null;
      String pointName = "";
      int pointLevel = 0;
      while (it.hasNext()) {
          coordinate = (Coordinate) it.next();
          points = coordinate.getCoordinatePoints();
          out.println("<tr>");
          headersIt = axisHeaders.iterator();
          while (headersIt.hasNext()) {
          	nodeDetail 	= (NodeDetail) headersIt.next();
          	if (!NodeDetail.STATUS_INVISIBLE.equals(nodeDetail.getStatus()))
          	{
	          	point		= getPoint(nodeDetail, points, currentLang, kmeliaScc);
	          	if (point != null)
	          	{
	          		pointName = point.getName();
	              	pointLevel = point.getLevel();
	              	if (pointLevel == 2)
	                	out.println("<td align=\"center\">"+kmeliaScc.getString("All")+"</td>");
	              	else
	                	out.println("<td align=\"center\">"+Encode.javaStringToHtmlString(pointName)+"</td>");
	          	}
	          	else
	          		out.println("<td align=\"center\">"+kmeliaScc.getString("All")+"</td>");
          	}
       	}
          out.println("<td  align=\"center\"><A href=\"javaScript:deleteCoordinate('"+coordinate.getCoordinateId()+"')\"><img src=\""+deleteSrc+"\" title=\""+kmeliaScc.getString("Delete")+"\" border=0></A></td>");
          out.println("</tr>");
      }
	  out.println("<tr><td colspan=\"15\" align=\"center\" class=\"intfdcolor\" height=\"1\"><img src=\""+hLineSrc+"\" width=\"100%\" height=\"1\"></td></tr>");
    }
    out.println("</table>");
    out.println("</center>");
    out.println(board.printAfter());
    
    if ("progress".equals(wizard))
	{
    	Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "DeletePublication?PubId="+id, false);
    	Button nextButton;
    	if (isEnd)
    		nextButton = (Button) gef.getFormButton(resources.getString("kmelia.End"), "WizardNext?Position=KmaxClassification", false);
    	else
    		nextButton = (Button) gef.getFormButton(resources.getString("GML.next"), "WizardNext?Position=KmaxClassification", false);
    	
		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(nextButton);
		buttonPane.addButton(cancelButton);
		buttonPane.setHorizontalPosition();
		out.println("<br /><center>"+buttonPane.print()+"</center><br />");
	}
    
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<form name="managerForm" method="post">
	<input type="hidden" name="AxisId"/>
	<input type="hidden" name="AxisName"/>
	<input type="hidden" name="AxisDescription"/>
	<input type="hidden" name="ComponentId"/>
	<input type="hidden" name="ComponentName"/>
	<input type="hidden" name="ComponentDescription"/>
	<input type="hidden" name="SearchCombination"/>
	<input type="hidden" name="PubId" value="<%=id%>"/>
	<input type="hidden" name="CoordinateId"/>
</form>
</body>
</html>