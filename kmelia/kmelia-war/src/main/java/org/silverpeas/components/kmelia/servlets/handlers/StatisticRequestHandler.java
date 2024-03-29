/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.servlets.handlers;

import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.model.StatsFilterVO;
import org.silverpeas.components.kmelia.search.KmeliaSearchServiceProvider;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.components.kmelia.stats.StatisticService;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * This class aims to manage Kmelia statistic request.
 */
public class StatisticRequestHandler {

  /**
   * @param request the HttpServletRequest
   * @param function the specific destination function
   * @param controller the kmeliaSessionController
   */
  public String handleRequest(HttpServletRequest request, String function,
      KmeliaSessionController controller) {
    if ("statistics".equals(function)) {
      return processStatisticRequestHandler(request, controller);
    } else if ("statSelectionGroup".equals(function)) {
      return processStatisticGroupSelectionRequestHandler(request, controller);
    }
    return function;
  }

  /**
   * @param request the HttpServletRequest
   * @param kmelia the kmelia session controller
   */
  private String processStatisticRequestHandler(HttpServletRequest request,
      KmeliaSessionController kmelia) {
    String curTopicId = kmelia.getCurrentFolderId();
    // Check if we are on the root node in order to display most interested query
    if (NodePK.ROOT_NODE_ID.equals(curTopicId)) {
      request.setAttribute("mostInterestedSearch", KmeliaSearchServiceProvider
            .getTopicSearchService().getMostInterestedSearch(kmelia.getComponentId()));
    }
    // Retrieve profile name and list
    List<String> groupIds = getPertinentGroups(kmelia);
    List<Group> groups = kmelia.groupIds2Groups(groupIds);
    request.setAttribute("filterGroups", groups);

    // Build the statsFilter
    String instanceId = kmelia.getComponentId();
    Integer topicId = Integer.parseInt(curTopicId);
    // Retrieve time interval from request form
    String beginDateStr = request.getParameter("beginDate");
    String endDateStr = request.getParameter("endDate");
    Date startDate = new Date();
    Date endDate = startDate;
    if (StringUtil.isDefined(beginDateStr) && StringUtil.isDefined(endDateStr)) {
      try {
        beginDateStr = DateUtil.date2SQLDate(beginDateStr, kmelia.getLanguage());
        endDateStr = DateUtil.date2SQLDate(endDateStr, kmelia.getLanguage());
        startDate = DateUtil.parse(beginDateStr);
        endDate = DateUtil.parse(endDateStr);
      } catch (ParseException e) {
        SilverLogger.getLogger(this)
            .error("Error when parsing date from request startDate=" + beginDateStr + ", endDate=" +
                endDateStr, e);
      }
    }
    endDate = DateUtil.getEndOfDay(endDate);
    startDate = DateUtil.getBeginOfDay(startDate);
    StatsFilterVO statFilter = new StatsFilterVO(instanceId, topicId, startDate, endDate);

    request.setAttribute("startDate", startDate);
    request.setAttribute("endDate", endDate);

    // Retrieve the group filter
    String groupId = request.getParameter("filterIdGroup");
    if (StringUtil.isDefined(groupId)) {
      // Filter statistics for each user inside current group
      statFilter.setGroupId(Integer.parseInt(groupId));
      request.setAttribute("filterIdGroup", groupId);
      // Retrieve group label
      for (Group group : groups) {
        if (group.getId().equals(groupId)) {
          request.setAttribute("filterLibGroup", group.getName());
        }
      }
    }

    StatisticService statService = KmeliaSearchServiceProvider.getStatisticService();
    request.setAttribute("detailActivity", statService.getStatisticActivity(statFilter));
    request.setAttribute("distinctPublications", statService
        .getNumberOfDifferentConsultedPublications(statFilter));
    return "statisticsTopic.jsp";
  }

  /**
   * This method is specific to Statistic Request Handler
   * @param kmelia the kmelia session controller
   * @return the list of pertinent group for statistics (it means ROLE_WRITER and ROLE_READER)
   */
  private List<String> getPertinentGroups(KmeliaSessionController kmelia) {
    Set<String> groupsSet = new HashSet<>();
    ProfileInst writerProfile = kmelia.getProfile(KmeliaHelper.ROLE_WRITER);
    ProfileInst readerProfile = kmelia.getProfile(KmeliaHelper.ROLE_READER);
    groupsSet.addAll(writerProfile.getAllGroups());
    groupsSet.addAll(readerProfile.getAllGroups());
    List<String> groups = new ArrayList<>(groupsSet);
    return groups;
  }

  /**
   * UNUSED CODE, waiting to make only a group selection inside UserPanel available
   * @param request the HttpServletRequest
   * @param kmelia the KmeliaSessionController
   * @return the current path destination of the RequestRouter
   */
  private String processStatisticGroupSelectionRequestHandler(HttpServletRequest request,
      KmeliaSessionController kmelia) {

    String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    Pair<String, String>[] hostPath = new Pair[1];
    hostPath[0] = new Pair<>(kmelia.getString("kmelia.SelectValidator"), "");

    Selection sel = kmelia.getSelection();
    sel.resetAll();
    sel.setHostSpaceName(kmelia.getSpaceLabel());
    sel.setHostComponentName(new Pair<>(kmelia.getComponentLabel(), ""));
    sel.setHostPath(hostPath);

    sel.setMultiSelect(false);
    sel.setSetSelectable(true);
    sel.setElementSelectable(false);

    String hostUrl =
        m_context + URLUtil.getURL("useless", kmelia.getComponentId())
            + "StatisticSetGroup?Role=";// + role
    String cancelUrl =
        m_context + URLUtil.getURL("useless", kmelia.getComponentId()) + "CloseWindow";

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setHtmlFormName("statForm");
    sel.setHtmlFormElementName("filterLibGroup");
    sel.setHtmlFormElementId("filterIdGroup");

    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(kmelia.getComponentId());
    // We set only profile name for user and writer roles
    ProfileInst writerProfile = kmelia.getProfile(KmeliaHelper.ROLE_WRITER);
    ProfileInst readerProfile = kmelia.getProfile(KmeliaHelper.ROLE_READER);

    List<String> profileNames = new ArrayList<>();
    profileNames.add(readerProfile.getName());
    profileNames.add(writerProfile.getName());
    sug.setProfileNames(profileNames);

    sel.setExtraParams(sug);

    return Selection.getSelectionURL();
  }

}
