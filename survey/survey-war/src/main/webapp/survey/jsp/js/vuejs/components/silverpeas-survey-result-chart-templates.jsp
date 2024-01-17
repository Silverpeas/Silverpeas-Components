<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
<view:setBundle basename="org.silverpeas.survey.multilang.surveyBundle"/>

<fmt:message var="openImageNewTabMsg" key="survey.answer.image.open.newTab"/>
<fmt:message var="suggestionHelp" key="survey.result.suggestion.help"/>
<fmt:message var="seeVoterHelp" key="survey.result.seeVoters.help"/>
<fmt:message var="seeVoterClickHelp" key="survey.result.seeVoters.clickHelp"/>

<c:url var="infoIconUrl" value="/survey/jsp/icons/info.gif"/>
<c:url var="previewIconUrl" value="/util/icons/preview.png"/>
<c:url var="squareIconUrl" value="/survey/jsp/icons/square.gif"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="question-answer-result-chart">
  <td colspan="2">
    <survey-pie-chart
        v-if="pieView"
        v-bind:answer-percents="answerPercents"
        v-bind:anonymous="anonymous"></survey-pie-chart>
    <survey-horizontal-bar-chart-line
        v-else
        v-for="answerPercent in answerPercents"
        v-bind:answer-percent="answerPercent"
        v-bind:anonymous="anonymous"></survey-horizontal-bar-chart-line>
  </td>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="pie-chart">
  <div class="pie-chart-line" v-bind:class="{'as-bars':isDisplayAsBars}">
    <div v-sp-init>
      {{addMessages({
      seeVoterClickHelp : '${silfn:escapeJs(seeVoterClickHelp)}',
      suggestionHelp : '${silfn:escapeJs(suggestionHelp)}'
    })}}
    </div>
    <div v-if="chartApi">
      <survey-chart-answer-label v-for="(answerPercent, index) in answerPercents"
                                 v-bind:answer="answerPercent.answer"
                                 v-bind:anonymous="anonymous"
                                 v-bind:color="getLegendColor(index)"
                                 v-bind:percent="answerPercent.percent"></survey-chart-answer-label>
    </div>
    <div class="flex-container">
      <div v-bind:id="chartDomIdContainer" class="chart-area">
        <div v-bind:id="chartDomId" class="chart"></div>
      </div>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="horizontal-bar-chart-line">
  <div class="horizontal-bar-chart-line">
    <survey-chart-answer-label class="left"
                               v-bind:answer="answerPercent.answer"
                               v-bind:anonymous="anonymous"></survey-chart-answer-label>
    <div class="right">
      <img src="${squareIconUrl}" alt="" height="5" v-bind:width="percentAsInt"/>
      <span>&nbsp;{{ percent.roundHalfUp(2)}} %</span>
      <a v-if="!anonymous && percent > 0"
         v-on:click="viewUsers" class="iconInfo"
         href="javascript:void(0)" title="${seeVoterHelp}">
        <img src="${infoIconUrl}" alt="${seeVoterHelp}"/>
      </a>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="chart-answer-label">
  <div class="labelAnswer" v-bind:class="{'other':answer.opened}">
    <div class="legend-color" v-if="color">
      <div v-bind:style="{'backgroundColor': color,'borderColor':color}">
        {{ percent.roundHalfUp(0) }} %
      </div>
    </div>
    <div class="contentAnswer">
      <div>
        <span v-html="answer.label"></span>
        <a v-if="answer.opened && answer.nbVoters > 0"
           v-on:click="viewSuggestions" class="iconInfo"
           href="javascript:void(0)" title="${suggestionHelp}">
          ${suggestionHelp}
          <img src="${previewIconUrl}" alt="${suggestionHelp}"/>
        </a>
      </div>
      <div class="thumbs" v-if="answer.imageUrl">
        <a v-bind:href="answer.imageUrl" target="_blank" title="${openImageNewTabMsg}">
          <img v-bind:src="answer.imageUrl" alt=""/>
        </a>
      </div>
    </div>
  </div>
</silverpeas-component-template>