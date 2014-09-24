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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%!
	/**--------------------------------------------------------------*/
	/**        MODIFY THE FILENAME BELOW TO YOUR jchatbox.xml        */
	/**                                                              */
	/** Samples  :                                                   */
	/** For Win32 OS => c:/apache/jchatbox/conf/jchatbox.xml         */
	/** For Un*x OS  => /home/alice/jchatbox/conf/jchatbox.xml       */
	/** For Max OS   => MAC_HD:webserver:jchatbox:conf:jchatbox.xml  */
	/**--------------------------------------------------------------*/
	private static ResourceLocator settings = new ResourceLocator("com.stratelia.silverpeas.chat.settings.chatSettings", "fr");
	// OLD String XMLjChatBox = "/tmp/chat/conf/jchatbox.xml";
	String XMLjChatBox = settings.getString("xmlConfigLocation");

	// Overides jspInit method. Do not modify.
	public void jspInit()
	{
		jChatBox.Util.XMLConfig.init(XMLjChatBox);
	}
%>
