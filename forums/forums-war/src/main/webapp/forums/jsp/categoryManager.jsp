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
<%
    response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="checkForums.jsp" %>
<% 
    Category category = (Category) request.getAttribute("Category");
    String name;
    String description;
    String categoryId;
    String creationDate;
    String action;
    if (category != null)
    {
        name = category.getName();
        description = category.getDescription();
        categoryId = category.getNodePK().getId();
        creationDate = resources.getOutputDate(category.getCreationDate());
        action = "UpdateCategory";
    }
    else
    {
        name = "";
        description = "";
        categoryId = "";
        creationDate = resources.getOutputDate(new Date());
        action = "CreateCategory";
    }
    
    Window window = graphicFactory.getWindow();
    Frame frame = graphicFactory.getFrame();
    Board board = graphicFactory.getBoard();
%>
<html>
    <head><%

        out.println(graphicFactory.getLookStyleSheet());
%>
    <script type="text/javascript" src="<%=context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript" src="<%=context%>/forums/jsp/javaScript/forums.js"></script>
    <script type="text/javascript">
        // fonctions de contrôle des zones du formulaire avant validation
        function sendData(creation) 
        {
            if (isCorrectForm()) 
            {
                document.forms["categoryForm"].action = (creation ? "CreateCategory" : "UpdateCategory");
                document.forms["categoryForm"].submit();
            }
        }
            
        function isCorrectForm() 
        {
            var errorMsg = "";
            var errorNb = 0;
            var name = stripInitialWhitespace(document.categoryForm.Name.value);
            if (name == "") 
            { 
                errorMsg += "  - '<%=resources.getString("GML.title")%>'  "
                    + "<%=resources.getString("GML.MustBeFilled")%>\n";
                errorNb++;
            }

            if (errorNb > 0)
            {
                window.alert("<%=resources.getString("GML.ThisFormContains")%> " + errorNb
                    + " " + (errorNb == 1 ? "<%=resources.getString("GML.error")%>" : "<%=resources.getString("GML.errors")%>")
                    + " : \n" + errorMsg);
                return false;
            }
            else
            {
                return true;
            }
        }
    </script>
</head>

<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" <%addBodyOnload(out, fsc, "document.categoryForm.Name.focus();");%>>
<%
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(fsc.getSpaceLabel()); 
    browseBar.setComponentName(fsc.getComponentLabel(), "Main");
    browseBar.setPath(resources.getString(action.equals("CreateCategory")
        ? "forums.addCategory" : "forums.editCategory"));
    
    out.println(window.printBefore());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
    <table cellpadding="5" width="100%">
    <form name="categoryForm" action="<%=action%>" method="post">
        <tr>
            <td class="txtlibform"><%=resources.getString("GML.title")%> :</td>
            <td><input type="text" name="Name" size="60" maxlength="150" value="<%=name%>">
                <img src="<%=resources.getIcon("forums.obligatoire")%>" width="5" height="5" border="0"></td>
                <input type="hidden" name="CategoryId" value="<%=categoryId%>"/></td>
                <input type="hidden" name="Langue" value="<%=resources.getLanguage()%>"/></td>
        </tr>
        <tr>
            <td class="txtlibform"><%=resources.getString("GML.description")%> :</td>
            <td><input type="text" name="Description" size="60" maxlength="150" value="<%=description%>" ></td>
        </tr>
        <tr>
            <td class="txtlibform"><%=resources.getString("forums.creationDate")%> :</td>
            <td><%=creationDate%>&nbsp;<span class="txtlibform"></td>
        </tr>
        <tr>
            <td colspan="2">( <img border="0" src="<%=resources.getIcon("forums.obligatoire")%>" width="5" height="5"> : Obligatoire )</td>
        </tr>
    </form>
    </table><%

    out.println(board.printAfter());
%>
    <br>
    <center><%

    ButtonPane buttonPane = graphicFactory.getButtonPane();
    buttonPane.addButton(graphicFactory.getFormButton(resources.getString("GML.validate"),
        "javascript:onclick=sendData(" + action.equals("CreateCategory") + ")", false));
    buttonPane.addButton(graphicFactory.getFormButton(
        resources.getString("GML.cancel"), "Main", false));
    out.println(buttonPane.print());
%>
    </center>
    <br><%

    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</body>
</html>