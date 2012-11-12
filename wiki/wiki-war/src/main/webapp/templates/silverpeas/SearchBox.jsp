<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<%-- Provides a simple searchbox that can be easily included anywhere on the page --%>
<%-- Powered by jswpwiki-common.js//SearchBox --%>

<form action="<wiki:Link jsp='Search.jsp' format='url'/>"
        class="wikiform"
           id="searchForm" accept-charset="<wiki:ContentEncoding />">

  <div style="position:relative">
  <input onblur="if( this.value == '' ) { this.value = this.defaultValue }; return true; "
        onfocus="if( this.value == this.defaultValue ) { this.value = ''}; return true; "
           type="text" value="<fmt:message key='sbox.search.submit'/>"
           name="query" id="query"
           size="20" 
      accesskey="f"/>
  <button type="submit"
  		 name="searchSubmit" id="searchSubmit"
  		value="<fmt:message key='find.submit.go'/>"
  		title="<fmt:message key='find.submit.go'/>"></button>
  </div>
  <div id="searchboxMenu" style='visibility:hidden;'>
    <div id="searchTools">
      <c:set var="viewTitle"><fmt:message key="sbox.view.title"/></c:set>
      <c:set var="editTitle"><fmt:message key="sbox.edit.title"/></c:set>
      <c:set var=cloneTitle"><fmt:message key="sbox.clone.title"/></c:set>
      <c:set var="findTitle"><fmt:message key="sbox.find.title"/></c:set>
      <a href="#" id='quickView' class='action'
      onclick="SearchBox.navigate( '<wiki:Link format="url" page="__PAGEHERE__"/>','${viewTitle}' );"
        title="${viewTitle}"><fmt:message key="sbox.view"/></a>
      <a href="#" id='quickEdit' class='action'
      onclick="SearchBox.navigate( '<wiki:Link format="url" context="edit" page="__PAGEHERE__"/>','${editTitle}' );"
        title="${editTitle}"><fmt:message key="sbox.edit"/></a>
      <a href="#" id='quickClone' class='action'	
      onclick="return SearchBox.navigate( '<wiki:Link format="url" page="__PAGEHERE__" context="edit" />', '${cloneTitle}', true );"
        title="${cloneTitle}"><fmt:message key="sbox.clone"/></a>
      <a href="#" id="advancedSearch" class='action'
      onclick="SearchBox.navigate( '<wiki:BaseURL />Search.jsp?query=__PAGEHERE__','<wiki:Variable var="pagename"/>' )"
        title="${findTitle} [ f ]"><fmt:message key="sbox.find"/></a>
    </div>
    <div id="searchResult" >
	  <fmt:message key='sbox.search.result'/>
      <span id="searchTarget" ><fmt:message key='sbox.search.target'/></span>
      <span id="searchSpin" class="spin" style="position:absolute;display:none;"></span>
	  <div id="searchOutput" ></div>
    </div>
    <div id="recentSearches" style="display:none;">
      <fmt:message key="sbox.recentsearches"/>
      <span><a href="#" id="recentClear"><fmt:message key="sbox.clearrecent"/></a></span>
    </div>
  </div>

</form>