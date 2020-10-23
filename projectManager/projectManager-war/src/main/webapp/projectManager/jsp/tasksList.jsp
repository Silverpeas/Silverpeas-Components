<%--

    Copyright (C) 2000 - 2020 Silverpeas

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
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ include file="check.jsp" %>

<view:sp-page>
  <view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
  <view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
  <c:set var="role" value="${requestScope['Role']}"/>
  <c:set var="defaultPaginationPageSize" value="${requestScope.resources.getSetting('tasksList.pagination.page.size.default', 30)}"/>

  <%
    List 	tasks 	= (List) request.getAttribute("Tasks");

    String 	role 		= (String) request.getAttribute("Role");
    Boolean filtreActif = (Boolean) request.getAttribute("FiltreActif");
    Filtre	filtre		= (Filtre) request.getAttribute("Filtre");
    String	userId		= (String) request.getAttribute("UserId");

    String actionFrom 		= "";
    String actionTo			= "";
    String actionNom		= "";
    String statut			= "-1";
    String dateDebutFrom	= "";
    String dateDebutTo		= "";
    String dateFinFrom		= "";
    String dateFinTo		= "";
    String retard			= "-1";
    String avancement		= "-1";
    String responsableId	= "";
    if (filtre != null)
    {
      actionFrom 		= filtre.getActionFrom();
      if (actionFrom == null || "null".equals(actionFrom))
        actionFrom = "";
      actionTo		= filtre.getActionTo();
      if (actionTo == null || "null".equals(actionTo))
        actionTo = "";
      actionNom		= filtre.getActionNom();
      if (actionNom == null || "null".equals(actionNom))
        actionNom = "";
      statut			= filtre.getStatut();
      dateDebutFrom	= filtre.getDateDebutFromUI();
      dateDebutTo		= filtre.getDateDebutToUI();
      dateFinFrom		= filtre.getDateFinFromUI();
      dateFinTo		= filtre.getDateFinToUI();
      retard			= filtre.getRetard();
      avancement		= filtre.getAvancement();
      responsableId	= filtre.getResponsableId();
    }

    List<String> filterRoles = new ArrayList<>();
    filterRoles.add("admin");
    filterRoles.add("responsable");
  %>

  <view:sp-head-part withCheckFormScript="true">
    <view:includePlugin name="toggle"/>
    <view:includePlugin name="datepicker"/>
    <script type="text/javascript">
      function exportTasks(){
        sp.preparedDownloadRequest('Export').download();
      }
      function deleteTask(id) {
        var label = "<%=resource.getString("projectManager.SupprimerTacheConfirmation")%>";
        jQuery.popup.confirm(label, function() {
          document.listForm.Id.value = id;
          document.listForm.submit();
        });
      }
      function isCorrectDate(input)
      {
        var date = input.value;
        if (!isWhitespace(date)) {
          if (!isDateOK(date, '<%=resource.getLanguage()%>')) {
            return false;
          }
        }
        return true;
      }
      function sendFilterData() {
        var errorMsg 			= "";
        var errorNb 			= 0;

        if (!isCorrectDate(document.actionForm.DateDebutFrom))
        {
          errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.Du")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
          errorNb++;
        }
        if (!isCorrectDate(document.actionForm.DateDebutTo))
        {
          errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.Au")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
          errorNb++;
        }
        if (!isCorrectDate(document.actionForm.DateFinFrom))
        {
          errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.Du")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
          errorNb++;
        }
        if (!isCorrectDate(document.actionForm.DateFinTo))
        {
          errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.Au")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
          errorNb++;
        }

        switch(errorNb) {
          case 0 :
            document.actionForm.submit();
            break;
          case 1 :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            jQuery.popup.error(errorMsg);
            break;
          default :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            jQuery.popup.error(errorMsg);
        }
      }

      function resetFilter() {
        $("#filterFields input[type=text]").val("");
        $("#filterFields input[type=hidden]").val("");
        $("#filterFields select[name=Statut]").val("-1");
        $("#filterFields .defaultCheckbox").prop("checked", true);
        window.userSelectionAPI.removeAll();
      }
    </script>
  </view:sp-head-part>

  <view:sp-body-part>
    <fmt:message var="export" key="projectManager.Export"/>
    <fmt:message var="createTask" key="projectManager.CreerTache"/>
    <fmt:message var="project" key="projectManager.Projet"/>
    <fmt:message var="tasks" key="projectManager.Taches"/>
    <fmt:message var="gannt" key="projectManager.Gantt"/>
    <fmt:message var="calendar" key="projectManager.Calendrier"/>

    <view:operationPane>
      <view:operation action="javaScript:exportTasks()" altText="${export}"/>
      <c:if test="${role eq 'admin'}">
        <view:operation action="ToAddTask" altText="${createTask}"/>
      </c:if>
    </view:operationPane>
    <view:window>

      <view:componentInstanceIntro componentId="<%=componentId%>" language="<%=resource.getLanguage()%>"/>
      <view:tabs>
        <view:tab action="ToProject" label="${project}" selected="false"/>
        <view:tab action="Main" label="${tasks}" selected="true"/>
        <view:tab action="ToGantt" label="${gannt}" selected="false"/>
        <c:if test="${role eq 'admin'}">
          <view:tab action="ToCalendar" label="${calendar}" selected="false"/>
        </c:if>
      </view:tabs>

      <view:frame>
        <!---------------------------------------------------------------------------------->
        <!--------------------------- FILTRE ----------------------------------------------->
        <!---------------------------------------------------------------------------------->
        <view:areaOfOperationOfCreation/>
        <br/>
        <form name="actionForm" method="post" action="ToFilterTasks">
          <div class="bgDegradeGris">
            <table cellpadding="0" cellspacing="2" border="0" width="98%">
              <tr>
                <td nowrap="nowrap">
                  <div id="filterLabel">
                    <p>
                      <%if (!filtreActif) {
                        out.println("<a href=\"FilterShow\"><img border=\"0\" src=\""+resource.getIcon("projectManager.boxDown")+"\"/></a>");
                      } else{
                        out.println("<a href=\"FilterHide\"><img border=\"0\" src=\""+resource.getIcon("projectManager.boxUp")+"\"/></a>");
                      }
                      %>
                      <%=resource.getString("projectManager.Filtre") %>
                    </p>
                  </div>
                  <% if (filtreActif.booleanValue()) { %>
                  <table cellpadding="5" cellspacing="0" border="0" width="100%" id="filterFields">
                    <tr>
                      <td class="txtlibform"><%=resource.getString("projectManager.TacheNumero")%> <%=resource.getString("projectManager.Tache")%> :</td>
                      <td><%=resource.getString("projectManager.De")%> <input type="text" name="TaskFrom" size="6" value="<%=actionFrom%>"/> <%=resource.getString("projectManager.A")%> <input type="text" name="TaskTo" size="6" value="<%=actionTo%>"/></td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resource.getString("projectManager.TacheNom")%> <%=resource.getString("projectManager.Tache")%> :</td>
                      <td><input type="text" name="TaskNom" size="60" value="<%=actionNom%>"/></td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resource.getString("projectManager.TacheStatut")%> : </td>
                      <td>
                        <select name="Statut" size="1">
                          <%
                            String 	label 	= "";
                            String 	statusSelected;
                            int 	iStatut = new Integer(statut).intValue();
                            for (int s=-1; s<6; s++)
                            {
                              switch (s)
                              {
                                case -1 : label = "&nbsp;";
                                  break;
                                case 0 : label = resource.getString("projectManager.TacheStatutEnCours");
                                  break;
                                case 1 : label = resource.getString("projectManager.TacheStatutGelee");
                                  break;
                                case 2 : label = resource.getString("projectManager.TacheStatutAbandonnee");
                                  break;
                                case 3 : label = resource.getString("projectManager.TacheStatutRealisee");
                                  break;
                                case 4 : label = resource.getString("projectManager.TacheStatutEnAlerte");
                                  break;
                                case 5 : label = resource.getString("projectManager.TacheAvancementND");
                                  break;
                              }
                              statusSelected = "";
                              if (s==iStatut)
                                statusSelected = "selected";

                              out.println("<option value=\""+s+"\" "+statusSelected+">"+label+"</option>");
                            }
                          %>
                        </select>
                      </td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resource.getString("projectManager.TacheDateDebut")%> :</td>
                      <td><%=resource.getString("projectManager.Du")%> <input type="text" name="DateDebutFrom" size="12" maxlength="10" value="<%=dateDebutFrom%>" class="dateToPick"/>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span>&nbsp;&nbsp;&nbsp;&nbsp;<%=resource.getString("projectManager.Au")%> <input type="text" name="DateDebutTo" size="12" maxlength="10" value="<%=dateDebutTo%>" class="dateToPick"/>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span></td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resource.getString("projectManager.TacheDateFin")%> :</td>
                      <td><%=resource.getString("projectManager.Du")%> <input type="text" name="DateFinFrom" size="12" maxlength="10" value="<%=dateFinFrom%>" class="dateToPick"/>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span>&nbsp;&nbsp;&nbsp;&nbsp;<%=resource.getString("projectManager.Au")%> <input type="text" name="DateFinTo" size="12" maxlength="10" value="<%=dateFinTo%>" class="dateToPick"/>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span></td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resource.getString("projectManager.Retard")%> :</td>
                      <td>
                        <%
                          String retardOui 	= "";
                          String retardNon 	= "";
                          String retardToutes	= "";
                          if (retard.equals("1"))
                            retardOui = "checked";
                          else if (retard.equals("0"))
                            retardNon = "checked";
                          else
                            retardToutes = "checked";
                        %>
                        <input type="radio" name="Retard" value="1" <%=retardOui%>/> <%=resource.getString("GML.yes")%> <input type="radio" name="Retard" value="0" <%=retardNon%>/> <%=resource.getString("GML.no")%> <input type="radio" name="Retard" value="-1" class="defaultCheckbox" <%=retardToutes%>/> <%=resource.getString("GML.allFP")%>
                      </td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resource.getString("projectManager.TacheAvancement")%> :</td>
                      <td>
                        <%
                          String av100 	= "";
                          String av50 	= "";
                          String avToutes	= "";
                          if (avancement.equals("1"))
                            av100 = "checked";
                          else if (avancement.equals("0"))
                            av50 = "checked";
                          else
                            avToutes = "checked";
                        %>
                        <input type="radio" name="Avancement" value="1" <%=av100%>/> 100% <input type="radio" name="Avancement" value="0" <%=av50%>/> <100% <input type="radio" name="Avancement" value="-1" class="defaultCheckbox" <%=avToutes%>/> <%=resource.getString("GML.allFP")%>
                      </td>
                    </tr>
                    <tr>
                      <td class="txtlibform"><%=resource.getString("projectManager.TacheResponsable")%> :</td>
                      <td class="field">
                        <viewTags:selectUsersAndGroups selectionType="USER"
                                                       userIds="<%=Collections.singletonList(responsableId)%>"
                                                       userInputName="ResponsableId"
                                                       jsApiVar="window.userSelectionAPI"
                                                       roleFilter="<%=filterRoles%>"
                                                       componentIdFilter="<%=componentId%>"/>
                      </td>
                    </tr>
                    <tr>
                      <td colspan="2" align="center">
                        <%
                          ButtonPane buttonPane = gef.getButtonPane();
                          buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:sendFilterData()", false));
                          buttonPane.addButton(gef.getFormButton(resource.getString("GML.reset"), "javascript:resetFilter()", false));
                          out.println(buttonPane.print());
                        %>
                      </td>
                    </tr>
                  </table>
                  <% } else { %>
                  <table border="0" cellpadding="0" cellspacing="0"><tr><td class="intfdcolor4"><img border="0" src="<%=resource.getIcon("projectManager.px") %>"/></td></tr></table>
                  <% } %>
                </td>
              </tr>
            </table>
          </div>
        </form>
        <br/>

        <fmt:message var="colStatus" key="projectManager.TacheStatut"/>
        <fmt:message var="colChrono" key="projectManager.TacheNumero"/>
        <fmt:message var="colName" key="projectManager.TacheNom"/>
        <fmt:message var="colManager" key="projectManager.TacheResponsable"/>
        <fmt:message var="colStartDate" key="projectManager.TacheDebut"/>
        <fmt:message var="colEndDate" key="projectManager.TacheFin"/>
        <fmt:message var="colCharge" key="projectManager.TacheCharge"/>
        <fmt:message var="colDone" key="projectManager.TacheConso"/>
        <fmt:message var="colTodo" key="projectManager.TacheReste"/>
        <fmt:message var="colOperations" key="projectManager.Operations"/>

        <fmt:message var="inWorking" key="projectManager.TacheStatutEnCours"/>
        <fmt:message var="inWorkingIcon" key="projectManager.enCours" bundle="${icons}"/>

        <fmt:message var="freezed" key="projectManager.TacheStatutGelee"/>
        <fmt:message var="freezedIcon" key="projectManager.gelee" bundle="${icons}"/>

        <fmt:message var="cancelled" key="projectManager.TacheStatutAbandonnee"/>
        <fmt:message var="cancelledIcon" key="projectManager.abandonnee" bundle="${icons}"/>

        <fmt:message var="completed" key="projectManager.TacheStatutRealisee"/>
        <fmt:message var="completedIcon" key="projectManager.realisee" bundle="${icons}"/>

        <fmt:message var="warning" key="projectManager.TacheStatutEnAlerte"/>
        <fmt:message var="warningIcon" key="projectManager.alerte" bundle="${icons}"/>

        <fmt:message var="toStart" key="projectManager.TacheAvancementND"/>
        <fmt:message var="toStartIcon" key="projectManager.nondemarree" bundle="${icons}"/>

        <fmt:message var="updateIcon" key="projectManager.update" bundle="${icons}"/>
        <fmt:message var="deleteIcon" key="projectManager.delete" bundle="${icons}"/>
        <c:url var="updateIconPath" value="${updateIcon}" />
        <fmt:message var="deleteTitle" key="projectManager.SupprimerTache"/>
        <c:url var="deleteIconPath" value="${deleteIcon}" />
        <fmt:message var="updateTitle" key="projectManager.ModifierTache"/>

        <fmt:message var="treePlusIcon" key="projectManager.treePlus" bundle="${icons}"/>
        <c:url var="treePlusIconPath" value="${treePlusIcon}" />
        <fmt:message var="treeMinusIcon" key="projectManager.treeMinus" bundle="${icons}"/>
        <c:url var="treeMinusIconPath" value="${treeMinusIcon}" />
        <fmt:message var="treeTicon" key="projectManager.treeT" bundle="${icons}"/>
        <fmt:message var="treeLicon" key="projectManager.treeL" bundle="${icons}"/>

        <c:set var="isAttachment" value="true"/>
        <fmt:message var="attachmentIcon" key="projectManager.attachedFile" bundle="${icons}"/>

        <c:set var="previousTaskId" value="-1"/>
        <c:set var="previousTaskLevel" value="-1"/>
        <c:set var="previousTaskDecomposee" value="-1"/>

        <view:arrayPane var="actionsList" routingAddress="Main" numberLinesPerPage="${defaultPaginationPageSize}">
          <view:arrayColumn title="${colStatus}" compareOn="${a -> fn:toLowerCase(a.statut)}"/>
          <view:arrayColumn title="${colChrono}" compareOn="${a -> a.chrono}"/>
          <view:arrayColumn title="${colName}" compareOn="${a -> fn:toLowerCase(a.nom)}"/>
          <view:arrayColumn title="${colManager}" compareOn="${a -> fn:toLowerCase(a.responsableFullName)}"/>
          <view:arrayColumn title="${colStartDate}" compareOn="${a -> a.dateDebut}"/>
          <view:arrayColumn title="${colEndDate}" compareOn="${a -> a.dateFin}"/>
          <view:arrayColumn title="${colCharge}" compareOn="${a -> a.charge}"/>
          <view:arrayColumn title="${colDone}" compareOn="${a -> a.consomme}"/>
          <view:arrayColumn title="${colTodo}" compareOn="${a -> a.raf}"/>
          <c:if test="${role eq 'admin' || role eq 'responsable'}">
            <view:arrayColumn title="${colOperations}" sortable="false"/>
          </c:if>

          <view:arrayLines var="task" items='<%=request.getAttribute("Tasks")%>'>
            <c:set var="isChild" value="false"/>
            <c:set var="isSameLevel" value="false"/>
            <c:set var="taskId" value="${task.id}"/>
            <c:set var="isAttachment" value="false"/>
            <c:if test="${fn:length(task.attachments) > 0}">
              <c:set var="isAttachment" value="true"/>
            </c:if>

            <view:arrayLine id="line">
              <c:choose>
                <c:when test="${task.statut eq 0}">
                  <fmt:message var="statusIcon" key="projectManager.enCours" bundle="${icons}"/>
                  <fmt:message var="altStatusIcon" key="projectManager.TacheStatutEnCours"/>
                </c:when>
                <c:when test="${task.statut eq 1}">
                  <fmt:message var="statusIcon" key="projectManager.gelee" bundle="${icons}"/>
                  <fmt:message var="altStatusIcon" key="projectManager.TacheStatutGelee"/>
                </c:when>
                <c:when test="${task.statut eq 2}">
                  <fmt:message var="statusIcon" key="projectManager.abandonnee" bundle="${icons}"/>
                  <fmt:message var="altStatusIcon" key="projectManager.TacheStatutAbandonnee"/>
                </c:when>
                <c:when test="${task.statut eq 3}">
                  <fmt:message var="statusIcon" key="projectManager.realisee" bundle="${icons}"/>
                  <fmt:message var="altStatusIcon" key="projectManager.TacheStatutRealisee"/>
                </c:when>
                <c:when test="${task.statut eq 4}">
                  <fmt:message var="statusIcon" key="projectManager.alerte" bundle="${icons}"/>
                  <fmt:message var="altStatusIcon" key="projectManager.TacheStatutEnAlerte"/>
                </c:when>
                <c:when test="${task.statut eq 5}">
                  <fmt:message var="statusIcon" key="projectManager.nondemarree" bundle="${icons}"/>
                  <fmt:message var="altStatusIcon" key="projectManager.TacheAvancementND"/>
                </c:when>
              </c:choose>
              <view:arrayCellText text="">
                <view:image src="${statusIcon}" alt="${altStatusIcon}"/>
              </view:arrayCellText>

              <view:arrayCellText text="${task.chrono}"/>

              <c:set var="taskFatherId" value="${task.mereId}"/>

              <c:if test="${taskFatherId eq previousTaskId && previousTaskDecomposee eq '1'}">
                <c:set var="isChild" value="true"/>
              </c:if>

              <c:set var="taskLevel" value="${task.level}"/>
              <c:if test="${taskLevel eq previousTaskLevel}">
                <c:set var="isSameLevel" value="true"/>
              </c:if>

              <c:set var="espaceImage" value='<img src="/silverpeas/util/icons/colorPix/15px.gif">'/>
              <c:set var="spaces" value=""/>
              <c:forEach var="i" begin="0" end="${task.level}">
                <c:set var="spaces" value="${spaces += espaceImage}" />
              </c:forEach>

              <c:if test="${task.estDecomposee eq '0'}">
                <view:arrayCellText text="">
                  <c:if test="${isChild eq true || (isSameLevel && task.level >= 1)}">
                    ${spaces}
                    <view:image src="${treeLicon}"/>
                  </c:if>

                  <view:a href="ViewTask?Id=${taskId}">${task.nom}</view:a>
                  <c:if test="${isAttachment}">
                    <view:image src="${attachmentIcon}"/>
                  </c:if>
                </view:arrayCellText>
              </c:if>

              <c:if test="${task.estDecomposee eq '1'}">
                <view:arrayCellText text="">
                  <c:if test="${isChild eq true || (isSameLevel && task.level >= 1)}">
                    ${spaces}
                    <view:image src="${treeLicon}"/>
                  </c:if>
                  <c:if test="${task.unfold eq false}">
                    <view:icon iconName="${treePlusIconPath}" action="UnfoldTask?Id=${taskId}"/>
                  </c:if>
                  <c:if test="${task.unfold eq true}">
                    <view:icon iconName="${treeMinusIconPath}" action="CollapseTask?Id=${taskId}"/>
                  </c:if>
                  <view:a href="ViewTask?Id=${task.id}">${task.nom}</view:a>
                  <c:if test="${isAttachment}">
                    <view:image src="${attachmentIcon}"/>
                  </c:if>
                </view:arrayCellText>
              </c:if>

              <view:arrayCellText text="${task.responsableFullName}"/>
              <view:arrayCellText text="${task.uiDateDebut}"/>
              <view:arrayCellText text="${task.uiDateFin}"/>
              <view:arrayCellText text="${task.charge}"/>
              <view:arrayCellText text="${task.consomme}"/>
              <view:arrayCellText text="${task.raf}"/>
              <c:if test="${task.updateAvailable eq true || task.deletionAvailable eq true && role ne 'responsable'}">
                <view:arrayCellText text="">
                  <c:if test="${task.updateAvailable eq true}">
                    <view:icon iconName="${updateIconPath}" altText="${updateTitle}" action="ToUpdateTask?Id=${taskId}"/>
                  </c:if>
                  <c:if test="${task.deletionAvailable eq true}">
                    <view:icon iconName="${deleteIconPath}" altText="${deleteTitle}" action="javascript:deleteTask(${taskId})"/>
                  </c:if>
                </view:arrayCellText>
              </c:if>
              <c:set var="previousTaskId" value="${taskId}"/>
              <c:set var="previousTaskLevel" value="${task.level}"/>
              <c:set var="previousTaskDecomposee" value="${task.estDecomposee}"/>
            </view:arrayLine>
          </view:arrayLines>
        </view:arrayPane>

        <table>
          <tr>
            <td>
              <view:image src="${toStartIcon}"/><span class="txtNav">${toStart}</span>
              <view:image src="${inWorkingIcon}"/><span class="txtNav">${inWorking}</span>
              <view:image src="${freezedIcon}"/><span class="txtNav">${freezed}</span>
              <view:image src="${completedIcon}"/><span class="txtNav">${completed}</span>
              <view:image src="${cancelledIcon}"/><span class="txtNav">${cancelled}</span>
              <view:image src="${warningIcon}"/><span class="txtNav">${warning}</span>
            </td>
          </tr>
        </table>
      </view:frame>
    </view:window>
    <form name="listForm" action="RemoveTask" method="post">
      <input type="hidden" name="Id"/>
    </form>
  </view:sp-body-part>
</view:sp-page>