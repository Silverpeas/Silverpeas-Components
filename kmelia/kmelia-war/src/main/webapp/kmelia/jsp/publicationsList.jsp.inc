<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.text.NumberFormat"%>
<%@ page import="java.text.ParsePosition"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.net.URLEncoder"%>

<%@ page import="org.silverpeas.search.searchEngine.model.* "%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail"%>
<%@ page import="com.stratelia.webactiv.kmelia.model.KmeliaPublication"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail "%>
<%@ page import="com.stratelia.webactiv.kmelia.control.KmeliaSessionController"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%!

String displayImportance(int importance, int maxImportance, ResourcesWrapper resources, JspWriter out) throws IOException {
  String stars = "";

  //display full Stars
  for (int i=0; i<importance; i++) {
    stars += "<img src=\""+resources.getIcon("kmelia.fullStar")+"\" valign=\"absmiddle\">";
  }
  //display empty stars
  for (int i=importance+1; i<=5; i++) {
    stars += "<img src=\""+resources.getIcon("kmelia.emptyStar")+"\" valign=\"absmiddle\">";
  }
  return stars;
}

String getUserName(KmeliaPublication kmeliaPub, KmeliaSessionController kmeliaScc)
{
	UserDetail			user		= kmeliaPub.getCreator(); //contains creator
	PublicationDetail	pub			= kmeliaPub.getDetail();
	String 				updaterId	= pub.getUpdaterId();
	UserDetail			updater		= null;
	if (updaterId != null && updaterId.length()>0)
		updater = kmeliaScc.getUserDetail(updaterId);
	if (updater == null)
		updater = user;

	String userName = "";
	if (updater != null && (updater.getFirstName().length() > 0 || updater.getLastName().length() > 0))
		userName = updater.getFirstName() + " " + updater.getLastName();
	else
		userName = kmeliaScc.getString("kmelia.UnknownUser");

	return userName;
}

void displaySameSubjectPublications(Collection pubs, String publicationLabel, KmeliaSessionController kmeliaScc, String currentPubId, boolean checkboxAllowed, ResourcesWrapper resources, JspWriter out) throws IOException {

    PublicationDetail 	pub;
    KmeliaPublication 	kmeliaPub;
    UserDetail 			user;
   	boolean 			showImportance 	= !"no".equals(resources.getSetting("showImportance"));
   	String				language = kmeliaScc.getCurrentLanguage();

    Iterator iterator = pubs.iterator();

	out.println("<table border=\"0\" width=\"98%\" align=center>");
	out.println("<form name=\"seeAlsoForm\" Action=\"DeleteSeeAlso\" method=\"Post\"/>");
	out.println("<input type=\"hidden\" name=\"PubId\" value=\"\"/>");
    out.println("<tr>");
          out.println("<td>");
          out.println("<!-- Publications Header -->");
          out.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            out.println("<tr>");
                out.println("<td width=\"40\"><img src=\""+resources.getIcon("kmelia.publication")+"\" border=0></td>");
                out.println("<td align=\"left\" width=\"100%\"><b>"+publicationLabel+"</b></td>");
            out.println("</tr>");
          if (iterator.hasNext()) {
           	out.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");
            out.println("<!-- Publications Header End -->");
            while (iterator.hasNext()) {
                kmeliaPub = (KmeliaPublication) iterator.next();
                pub = kmeliaPub.getDetail();
                user = kmeliaPub.getCreator();
                out.println("<!-- Publication Body -->");
                  if ( pub.getStatus() != null && pub.getStatus().equals("Valid") && !pub.getPK().getId().equals(currentPubId)) {
                      out.println("<tr>");
                        out.print("<td valign=\"top\" colspan=\"2\">");
                      	out.print("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr valign=\"middle\">");
                      	if (checkboxAllowed)
                      		out.print("<td align=\"center\"><input type=\"checkbox\" name=\"PubIds\" value=\""+pub.getPK().getId()+"-"+pub.getPK().getInstanceId()+"\"></td>");
						out.print("<td width=\"1\">&#149;&nbsp;</td><td nowrap>");
						out.print("<a href=\""+URLManager.getSimpleURL(URLManager.URL_PUBLI, pub.getPK().getId(), pub.getPK().getInstanceId())+"\"><b>"+pub.getName(language)+"</b></a>");
						out.print("&nbsp;</td><td width=\"100%\">");
						if (showImportance)
							out.print(displayImportance(new Integer(pub.getImportance()).intValue(), 5, resources, out));
						else
							out.print("&nbsp;");
						out.println("</td>");
						out.println("</tr>");
						out.println("<tr>");
						if (checkboxAllowed)
							out.println("<td width=\"1\">&nbsp;</td>");
						out.println("<td width=\"1\">&nbsp;</td>");
						out.println("<td colspan=\"3\">"+getUserName(kmeliaPub, kmeliaScc)+" - "+resources.getOutputDate(pub.getUpdateDate())+"<br/>");
						out.println(pub.getDescription(language)+"<br/><br/></td>");
						out.println("</td></tr></table>");
						out.println("</td>");
                      out.println("</tr>");
                  }
                out.println("<!-- Publication Body End -->");
            } // End while
            out.println("</table>");
          } // End if
          else
		  {
              out.println("<tr>");
              out.println("<td>&nbsp;</td>");
              out.println("<td>"+kmeliaScc.getString("PubAucune")+"</td>");
              out.println("</tr>");
              out.println("</table>");
          }
          out.println("</td>");
          out.println("<td valign=\"top\" colspan=\"2\" width=\"80\">");
					out.println("&nbsp;");
          out.println("</td>");
          out.println("</tr>");
          out.println("</form>");
out.println("</table>");
}

void displaySearchResults(List<MatchingIndexEntry> pubs, String publicationLabel, KmeliaSessionController kmeliaScc, String currentPubId, ResourcesWrapper resources, JspWriter out) throws IOException, java.text.ParseException {

    NumberFormat				percent		= NumberFormat.getPercentInstance();
	UserDetail					user		= null;

      out.println("<!-- Publications Header -->");
          out.println("<table width=\"98%\" align=center border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
            out.println("<tr>");
                out.println("<td width=\"80\"><img src=\""+resources.getIcon("kmelia.publication")+"\" border=0></td>");
                out.println("<td align=\"left\"><b>"+publicationLabel+"</b></td>");
          if (pubs!=null && !pubs.isEmpty()) {
                out.println("<td align=\"right\" valign=\"middle\">");
                out.println("&nbsp;");
                out.println("</td>");
            out.println("</tr>");
            out.println("<tr><td colspan=\"2\" >&nbsp;</td></tr>");
            out.println("<!-- Publications Header End -->");
            for (MatchingIndexEntry pub : pubs) {
                  out.println("<!-- Publication Body -->");
                  if (!pub.getObjectId().equals(currentPubId)) {
                      if (pub.getObjectType().equals("Publication")) {
							String userName = "";
							user = kmeliaScc.getOrganisationController().getUserDetail(pub.getCreationUser());
							if ((user != null) && ((user.getFirstName().length() > 0) || (user.getLastName().length() > 0)))
								userName = user.getFirstName() + " " + user.getLastName();
							else
								userName = kmeliaScc.getString("UnknownAuthor");
                          out.println("<tr>");
                            out.println("<td valign=\"top\" width=\"80\" align=\"center\">"+percent.format(new Double(pub.getScore()))+" </td>");
                            out.println("<td valign=\"top\" colspan=\"2\">");
                             out.println("<p>&#149; <a href=\"javascript:onClick=publicationGoTo('"+pub.getObjectId()+"')\"><b>"+pub.getTitle()+"</b></a><br> "+userName+" - "+resources.getOutputDate(pub.getCreationDate())+"<br>");
                             out.println(""+Encode.javaStringToHtmlString(pub.getPreView())+"<BR><BR></p>");
                            out.println("</td>");
                          out.println("</tr>");
                      }
                   }
                  out.println("<!-- Publication Body End -->");
            } // End while
            out.println("</table>");
          } // End if
          else {
              out.println("<td align=\"right\" valign=\"middle\">&nbsp;</td>");
              out.println("<tr>");
              out.println("<td>&nbsp;</td>");
              out.println("<td>"+kmeliaScc.getString("PubAucune")+"</td>");
              out.println("</tr>");
              out.println("</table>");
          }
}
%>