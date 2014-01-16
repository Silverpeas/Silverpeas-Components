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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<style type="text/css">
	ul.componentLists {list-style-type: none; margin-left: -30px;}
	li.componentLists {margin-bottom: 10px;}
	ul.componentList {list-style-type: none; margin-left: -10px;}
	ul.componentList img {border: 0px; width: 16px; height: 16px;}
	.groupLabel {line-height: 30px; font-weight: bold;}
</style>
<ul class="componentLists">
	<c:forEach items="${componentLists}" var="componentList">
		<li class="componentLists suite_${componentList.suiteIndex}">
			<span class="groupLabel">${componentList.label}</span>
			<ul class="componentList">
				<c:forEach items="${componentList.components}" var="component">
					<li class="txtlibform component_${component.name}">
						<img src="/silverpeas/util/icons/component/${component.name}Small.gif" class="component-icon" alt=""/>
						<input type="checkbox" name="services" value="${component.name}"/>${component.label}
					</li>
				</c:forEach>
			</ul>
		</li>
	</c:forEach>
</ul>