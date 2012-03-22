package com.stratelia.webactiv.kmelia.servlets.handlers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.kmelia.model.StatsFilterVO;
import com.silverpeas.kmelia.search.KmeliaSearchServiceFactory;
import com.silverpeas.kmelia.stats.StatisticService;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper;
import com.stratelia.webactiv.kmelia.model.TopicDetail;

public class StatisticRequestHandler {

  /**
   * @param request the HttpServletRequest
   * @param controller the kmeliaSessionController
   */
  public void handleRequest(HttpServletRequest request, KmeliaSessionController controller) {
    processStatisticRequestHandler(request, controller);
  }


  /**
   * 
   * @param request the HttpServletRequest
   * @param kmelia the kmelia session controller
   */
  private void processStatisticRequestHandler(HttpServletRequest request,
      KmeliaSessionController kmelia) {
    TopicDetail curTopic = kmelia.getSessionTopic();
    String curTopicId = "0";
    // Check if we are on he root node in order to display most interested query
    if (curTopic != null) {
      if ("0".equals(curTopic.getNodePK().getId())) {
        request.setAttribute("mostInterestedSearch", KmeliaSearchServiceFactory
            .getTopicSearchService().getMostInterestedSearch(kmelia.getComponentId()));
      } else {
        curTopicId = curTopic.getNodePK().getId();
      }
    }
    // Retrieve profile name and list
    List<String> groups = getPertinentGroups(kmelia);
    request.setAttribute("filterGroups", kmelia.groupIds2Groups(groups));
    
    
    // Build the statsFilter
    StatsFilterVO statFilter = new StatsFilterVO();
    statFilter.setInstanceId(kmelia.getComponentId());
    statFilter.setTopicId(Integer.parseInt(curTopicId));

    // TODO replace with the value from form
    Date endDate = new Date();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -9);
    Date beginDate = cal.getTime();
    cal.add(Calendar.DATE, 11);
    endDate = cal.getTime();
    statFilter.setStartDate(beginDate);
    statFilter.setEndDate(endDate);
    request.setAttribute("startDate", beginDate);
    request.setAttribute("endDate", endDate);
    
    // Retrieve the group filter
    
    String groupId = request.getParameter("groupId");
    if(StringUtil.isDefined(groupId)) {
      // Filter statistics for each user inside current group
      statFilter.setGroupId(Integer.parseInt(groupId));
    }
    StatisticService statService = KmeliaSearchServiceFactory.getStatisticService();
    request.setAttribute("nbConsultedPublication", statService.getNbConsultedPublication(statFilter));
    request.setAttribute("nbActivity", statService.getStatisticActivityByPeriod(statFilter));
  }

  /**
   * 
   * @param kmelia the kmelia session controller
   * @return the list of pertinent group for statistics (it means ROLE_WRITER and ROLE_READER)
   */
  private List<String> getPertinentGroups(KmeliaSessionController kmelia) {
    Set<String> groupsSet = new HashSet<String>();
    ProfileInst writerProfile = kmelia.getProfile(KmeliaHelper.ROLE_WRITER);
    ProfileInst readerProfile = kmelia.getProfile(KmeliaHelper.ROLE_READER);
    groupsSet.addAll(writerProfile.getAllGroups());
    groupsSet.addAll(readerProfile.getAllGroups());
    List<String> groups = new ArrayList<String>(groupsSet);
    return groups;
  }

}
