<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ page import="org.silverpeas.core.node.coordinates.model.Coordinate"%>
<%@ page import="org.silverpeas.core.node.coordinates.model.CoordinatePoint"%>
<%@ page import="org.silverpeas.components.kmelia.jstl.KmeliaDisplayHelper"%>
<%@ page import="org.silverpeas.kernel.logging.SilverLogger" %>


<%@ include file="checkKmelia.jsp" %>
<%@ include file="kmax_axisReport.jsp" %>

<%!
CoordinatePoint getPoint(NodeDetail nodeDetail, Collection points, String translation, KmeliaSessionController kmeliaScc) {
	Iterator pointsIt 	= points.iterator();
	while (pointsIt.hasNext()) {
		CoordinatePoint point = (CoordinatePoint) pointsIt.next();
		if (point.getPath().contains("/" + nodeDetail.getNodePK().getId() + "/")) {
			try {
				NodeDetail pointDetail = kmeliaScc.getNodeHeader(Integer.toString(point.getNodeId()));
				point.setName(pointDetail.getName(translation));
			}  catch (Exception e) {
				SilverLogger.getLogger(this).error(e);
			}
			return point;
		}
	}
	return null;
}
%>

<%
	String deleteSrc = m_context + "/util/icons/delete.gif";
	String hLineSrc	 = m_context + "/util/icons/colorPix/1px.gif";
	
	String	currentLang = (String) request.getAttribute("Language");

	String action = "KmaxViewCombination";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title></title>
<view:looknfeel/>
<script type="text/javascript">
	function search() {
    var criterias = "";
    $(".axis").each(function() {
      var val = $(this).val();
      if (val.length != 0) {
        if (criterias.length != 0) {
          criterias += ",";
        }
        truc = val.split("|");
        criterias += truc[0];
      }
    });

    if (criterias.length == 0) {
      jQuery.popup.error("Vous devez sélectionnez au moins un axe !");
    } else {
      document.managerForm.action = "KmaxAddCoordinate";
      document.managerForm.SearchCombination.value = criterias;
      document.managerForm.submit();
    }
	}

	function deleteCoordinate(coordinateId) {
    var label = "<%=kmeliaScc.getString("ConfirmDeleteCoordinate")%> ?";
    jQuery.popup.confirm(label, function() {
      document.managerForm.CoordinateId.value = coordinateId;
      document.managerForm.action = "KmaxDeleteCoordinate";
      document.managerForm.submit();
    });
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
	String pubName = kmeliaScc.getSessionPublication().getDetail().getName(currentLang);
	browseBar.setExtraInformation(pubName);
	String id = kmeliaScc.getSessionPublication().getDetail().getId();
	browseBar.setI18N(action, currentLang);
	
  out.println(window.printBefore());

  KmeliaDisplayHelper.displayAllOperations(id, kmeliaScc, gef, action, resources, out, true);

  out.println(frame.printBefore());

	out.println(displayAxisToPublish(kmeliaScc, gef, currentLang));
    
	Collection coordinates = kmeliaScc.getPublicationCoordinates(id);
	out.println("<br/>");
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
			  out.println("<td align=\"center\"><b>" + WebEncodeHelper.javaStringToHtmlString(nodeDetail.getName(currentLang)) + "</b></td>");
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
          	if (!NodeDetail.STATUS_INVISIBLE.equals(nodeDetail.getStatus())) {
	          	point		= getPoint(nodeDetail, points, currentLang, kmeliaScc);
	          	if (point != null) {
	          		pointName = point.getName();
	              pointLevel = point.getLevel();
	              if (pointLevel == 2) {
                  out.println("<td align=\"center\">" + kmeliaScc.getString("All") + "</td>");
                } else {
                  out.println("<td align=\"center\">" + WebEncodeHelper.javaStringToHtmlString(pointName)+"</td>");
                }
	          	} else {
	          		out.println("<td align=\"center\">"+kmeliaScc.getString("All")+"</td>");
              }
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