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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="checkKmelia.jsp" %>

<%
    String translation = (String) request.getAttribute("Language");
%>

<view:sp-page>
    <view:sp-head-part>
        <title><%=resources.getString("kmelia.folder.nonvisiblepubs")%></title>
        <view:script src="javaScript/navigation.js"/>
        <view:script src="javaScript/publications.js"/>
        <script type="text/javascript">
          function getWebContext() {
            return "<%=m_context%>";
          }

          function getComponentId() {
            return "<%=componentId%>";
          }

          function getNonVisiblePubsFolderId() {
            return "<%=KmeliaHelper.SPECIALFOLDER_NONVISIBLEPUBS%>";
          }

          function getPubIdToHighlight() {
            return "";
          }

          $(document).ready(function () {
            setCurrentNodeId(getNonVisiblePubsFolderId());
            displayPublications(getNonVisiblePubsFolderId());
          });
        </script>
    </view:sp-head-part>
    <view:sp-body-part id="kmelia" cssClass="yui-skin-sam">
        <%
            Window window = gef.getWindow();
            BrowseBar browseBar = window.getBrowseBar();
            browseBar.setI18N("GoToCurrentTopic", translation);
            browseBar.setExtraInformation(resources.getString("kmelia.folder.nonvisiblepubs"));

            Frame frame = gef.getFrame();

            out.println(window.printBefore());
            out.println(frame.printBefore());
        %>

        <div id="pubList">
            <view:board>
                <%=resources.getString("kmelia.inProgressPublications")%><br/><br/>
                <img alt="progression" src="<%=resources.getIcon("kmelia.progress") %>"/>
            </view:board>
        </div>
        <%
            out.println(frame.printAfter());
            out.println(window.printAfter());
        %>

        <form name="pubForm" action="ViewPublication" method="post">
            <input type="hidden" name="PubId"/>
            <input type="hidden" name="CheckPath" value="1"/>
        </form>
        <view:progressMessage/>
    </view:sp-body-part>
</view:sp-page>
