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
<%@ page isELIgnored ="false" %> 
<%@ page import="java.util.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.rpc.json.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.admin.*" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<script>
function constructdate(date)
{
  var d = new Date();
  d.setTime(date.time);
  return d;
}

function refreshUserInfo()
{
   var userid = $('userid').getValue();

   if( userid == '--New--' ) return;

   Wiki.jsonrpc("users.getUserInfo", [userid], function(userprofile){
      $('loginname').value = userprofile.loginName;
      $('loginid').value = userprofile.loginName;
      $('fullname').value = userprofile.fullname;
      $('email').value = userprofile.email;
      $('lastmodified').setHTML(constructdate(userprofile.lastModified));
      $('creationdate').setHTML(constructdate(userprofile.created));
   });
}

function addNew()
{
  $('loginid').value = "--New--";
  $('loginname').value = "--New--";
  $('fullname').value = "Undefined";
  $('email').value = "";
  $('lastmodified').innerHTML = "";
  $('creationdate').innerHTML = "";
 
  var idlist=$('userid');
  var len = idlist.options.length;
  idlist.options[len] = new Option('--New--','--New--');
  idlist.selectedIndex = len;
}
</script>
<div>
   <p>
   This is a list of user accounts that exist in this system.
   </p>
   <p><wiki:Messages/></p>
   <div id="userlist">
      <select name="userid" id="userid" size="16" onchange="javascript:refreshUserInfo()">
         <c:forEach var="user" items="${engine.userManager.userDatabase.wikiNames}">
            <option><c:out value="${user.name}" escapeXml="true"/></option>
         </c:forEach>
      </select>
   </div>
   <div id="useredit">
   <form action="<wiki:Link jsp='admin/Admin.jsp' format='url'><wiki:Param name='tab-admin' value='users'/></wiki:Link>" 
       class="wikiform"
          id="adminuserform" 
    onsubmit="return Wiki.submitOnce(this);"
      method="post" accept-charset="<wiki:ContentEncoding/>"
     enctype="application/x-www-form-urlencoded" >
     <input type="hidden" name='bean' value='com.ecyrd.jspwiki.ui.admin.beans.UserBean'/>
     <input type="hidden" id="loginid" name="loginid" value="" />
     <table>
     <tr>
       <td><label for="loginname">Login name</label></td>
       <td>
           <input type="text" name="loginname" id="loginname"
                  size="20" value="" />
       </td>
     </tr>
     <tr>
       <td><label for="password">Password </label></td>
       <td>
          <input type="password" name="password" id="password" size="20" value="" />
       </td>
     </tr>
     <tr>
       <td><label for="password2">Confirm password</label></td>
       <td>
         <input type="password" name="password2" id="password2" size="20" value="" />
       </td>
     </tr>
     <tr>
       <td><label for="fullname">Full name</label></td>
       <td>
         <input type="text" name="fullname" id="fullname"
                size="20" value="" />
       </td>
     </tr>
     <tr>
       <td><label for="email">Email</label></td>
       <td>
         <input type="text" name="email" id="email"
                size="20" value="" />
       </td>
     </tr>

     <tr class="additinfo">
       <td><label>Creation date</label></td>
       <td class="formvalue" id="creationdate">
       </td>
     </tr>
     <tr class="additinfo">
       <td><label>Last modified</label></td>
       <td class="formvalue" id="lastmodified">
       </td>

     <tr>
        <td><input type="submit" name="action" value="Save"/></td>
     </tr>

     </table>
   </div>
   <div id="useractions">
     <input type="submit" name="action" value="Remove" onclick="return( confirm('Are you sure you wish to remove this user?') && Wiki.submitOnce(this) );"/>      <input type="button" value="Add" onclick="javascript:addNew()"/>
   </form>
   </div>
</div>