<%@ page import="org.silverpeas.core.admin.user.model.User" %><%--
  ~ Copyright (C) 2000 - 2023 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.blog.multilang.blogBundle"/>

<fmt:message key="blog.customize" var="customizeLabel"/>
<fmt:message key="blog.wallPaper" var="wallpaperLabel"/>
<fmt:message key="blog.styleSheet" var="stylesheetLabel"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="blog-management">
  <silverpeas-blog-personalization-popin
      v-on:api="personalizationPopin = $event"
      v-bind:wallpaper="wallpaper"
      v-bind:stylesheet="stylesheet"></silverpeas-blog-personalization-popin>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="blog-personalization-popin">
  <silverpeas-popin v-on:api="setPopinApi"
                    class="blog-personalization-popin"
                    v-bind:title="'${silfn:escapeJs(customizeLabel)}'"
                    v-bind:dialog-class="'my-photo-popin'"
                    type="validation">
    <silverpeas-form-pane v-on:api="setFormPaneApi"
                          v-bind:mandatoryLegend="false"
                          v-bind:manualActions="true">
      <silverpeas-blog-personalization-form
          v-on:api="setFormApi"
          v-bind:wallpaper="wallpaper"
          v-bind:stylesheet="stylesheet"></silverpeas-blog-personalization-form>
    </silverpeas-form-pane>
  </silverpeas-popin>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="blog-personalization-form">
  <div class="blog-personalization-form form-container">
    <div>
      <silverpeas-label for="wallpaper-input" class="label-ui-dialog">${wallpaperLabel}</silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-image-file-input
            id="wallpaper-input"
            name="wallPaper"
            v-on:api="wallpaperInput = $event"
            v-bind:display-file-data="true"
            v-bind:original-name="wallpaper.name"
            v-bind:original-size="wallpaper.size"
            v-bind:full-image-url="wallpaperModel.deleteOriginal ? undefined : wallpaper.url"
            v-model="wallpaperModel"></silverpeas-image-file-input>
      </div>
    </div>
    <div>
      <silverpeas-label for="stylesheet-input" class="label-ui-dialog">${stylesheetLabel}</silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-file-input
            id="stylesheet-input"
            name="styleSheet"
            v-on:api="stylesheetInput = $event"
            v-bind:display-file-data="true"
            v-bind:original-name="stylesheet.name"
            v-bind:original-size="stylesheet.size"
            v-bind:handled-types="['text/css']"
            v-model="stylesheetModel"></silverpeas-file-input>
      </div>
    </div>
  </div>
</silverpeas-component-template>