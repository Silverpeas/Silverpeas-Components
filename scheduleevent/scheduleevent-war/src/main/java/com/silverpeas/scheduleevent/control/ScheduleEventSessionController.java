/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.scheduleevent.control;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.scheduleevent.service.ScheduleEventService;
import com.silverpeas.scheduleevent.service.ServicesFactory;
import com.silverpeas.scheduleevent.service.model.ScheduleEventBean;
import com.silverpeas.scheduleevent.service.model.ScheduleEventStatus;
import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEventComparator;
import com.silverpeas.scheduleevent.view.OptionDateVO;
import com.silverpeas.scheduleevent.view.ScheduleEventVO;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

public class ScheduleEventSessionController extends AbstractComponentSessionController {
  private Selection sel = null;
  private ScheduleEvent currentScheduleEvent = null;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public ScheduleEventSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.components.scheduleevent.multilang.ScheduleEventBundle",
        "com.silverpeas.components.scheduleevent.settings.ScheduleEventIcons");
    sel = getSelection();
  }

  public void setCurrentScheduleEvent(ScheduleEvent currentScheduleEvent) {
    this.currentScheduleEvent = currentScheduleEvent;
  }

  public ScheduleEvent getCurrentScheduleEvent() {
    return currentScheduleEvent;
  }

  public ScheduleEventBean getCurrentScheduleEventVO() {
    return new ScheduleEventVO(getCurrentScheduleEvent());
  }

  public void resetScheduleEventCreationBuffer() {
    setCurrentScheduleEvent(null);
  }

  private Set<Contributor> getCurrentContributors() {
    Set<Contributor> contributors = currentScheduleEvent.getContributors();
    if (contributors == null) {
      contributors = new HashSet<Contributor>(); 
    }
    return contributors;
  }
  
  private void addContributor(Set<Contributor> contributors, String userId) {
    Contributor contributor = new Contributor();
    contributor.setScheduleEvent(currentScheduleEvent);
    contributor.setUserId(Integer.parseInt(userId));
    contributor.setUserName(getUserDetail(userId).getDisplayedName());
    contributors.add(contributor);
  }
  
  public void createCurrentScheduleEvent() {
    setCurrentScheduleEvent(new ScheduleEvent());
    currentScheduleEvent.setAuthor(Integer.parseInt(getUserId()));
    Set<Contributor> contributors = getCurrentContributors(); 
    addContributor(contributors, getUserId());
    currentScheduleEvent.setContributors(contributors);
  }

  public boolean isCurrentScheduleEventDefined() {
    return getCurrentScheduleEvent() != null;
  }

  public String initSelectUsersPanel() {
    SilverTrace.debug("ScheduleEvent",
        "ScheduleEventSessionController.initSelectUsersPanel()",
        "root.MSG_GEN_PARAM_VALUE", "ENTER METHOD");

    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    PairObject hostComponentName = new PairObject(getComponentName(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("scheduleevent.form.selectContributors"), "");

    sel.resetAll();
    sel.setHostSpaceName(this.getString("domainName"));
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    String[] idUsers = getContributorsUserIds(currentScheduleEvent.getContributors());
    sel.setSelectedElements(idUsers);
    sel.setSelectedSets(new String[0]);
    
    // Contraintes
    String hostDirection, cancelDirection;
    if (currentScheduleEvent.id == null) {
    	hostDirection = "ConfirmUsers?popupMode=Yes";
    	cancelDirection = "ConfirmScreen?popupMode=Yes";
    } else {
    	hostDirection = "ConfirmModifyUsers?scheduleEventId=" + currentScheduleEvent.id;
    	cancelDirection = "Detail?scheduleEventId=" + currentScheduleEvent.id;
    }
    
    String hostUrl = m_context
    	+ URLManager.getURL(URLManager.CMP_SCHEDULE_EVENT) + hostDirection;
    String cancelUrl = m_context
    	+ URLManager.getURL(URLManager.CMP_SCHEDULE_EVENT) + cancelDirection;
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setMultiSelect(true);
    sel.setPopupMode(true);
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  private static String[] getContributorsUserIds(Set<Contributor> contributors) {
    Set<String> result = new HashSet<String>(contributors.size());
    for (Contributor subscriber : contributors) {
      if (subscriber.getUserId() != -1) {
        result.add(String.valueOf(subscriber.getUserId()));
      }
    }
    return (String[]) result.toArray(new String[result.size()]);
  }

  public void setIdUsersAndGroups() {
    String[] usersId =
      SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel.getSelectedSets());

    if (usersId.length < 1) {
      return;
    }
    Set<Contributor> recordedContributors = getCurrentContributors();
    deleteRecordedContributors(usersId, recordedContributors);
    addContributors(usersId, recordedContributors);
    currentScheduleEvent.setContributors(recordedContributors);
  }

  public void addContributors(String[] usersId, Set<Contributor> recordedContributors) {
    if (usersId.length < 1) return;
    UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(usersId);
    boolean foundCreator = false;
    for (int u = 0; u < userDetails.length; u++) {
      UserDetail detail = userDetails[u];
      if (detail.getId().equals(String.valueOf(currentScheduleEvent.author))) {
        foundCreator = true;
      }
      boolean foundAlreadyCreated = false;
      for (Contributor contributor : recordedContributors) {
        if ( userDetails[u].getId().equals(String.valueOf(contributor.getUserId())) ) {
          foundAlreadyCreated = true;
        }
      }
      if (!foundAlreadyCreated) {
        addContributor(recordedContributors, detail.getId());
        SilverTrace.debug("scheduleevent", "ScheduleEventSessionController.setIdUsersAndGroups()", 
            "Contributor '" + getUserDetail(detail.getId()).getDisplayedName() + 
            "' added to event '" + currentScheduleEvent.getTitle() + "'");
      }
    }
    if (!foundCreator) {
      addContributor(recordedContributors, String.valueOf(currentScheduleEvent.author));
    }
  }

  private void deleteRecordedContributors(String[] usersId, Set<Contributor> recordedContributors) {
//    if (usersId.length < 1 || recordedContributors.isEmpty()) { 
    if (recordedContributors.isEmpty()) { 
    	return;
    }
    
  	UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(usersId);
  	Contributor[] contrib = (Contributor[])recordedContributors.toArray(new Contributor[recordedContributors.size()]);
    boolean found = false;
  	for (int c = contrib.length-1; c >= 0; c--) {
  	  if (getUserId().equals(String.valueOf(contrib[c].getUserId()))) {
  	    continue;
  	  }
  		for (int i=0; i<userDetails.length; i++) {
  			if ( userDetails[i].getId().equals(String.valueOf(contrib[c].getUserId())) ) {
  				found = true;
  			}
  		}
  		if (!found) {
//        if (currentScheduleEvent.id == null) {
//          getScheduleEventService().deleteContributor(contrib[c].getId());
//        } else {
          currentScheduleEvent.getContributors().remove(contrib[c]);
//        }
        SilverTrace.debug("scheduleevent", "ScheduleEventSessionController.setIdUsersAndGroups()", 
            "Contributor '" + contrib[c].getUserName() + "' deleted from event '" + currentScheduleEvent.getTitle() + "'");
  		}
  	}
  }
    
  public void updateIdUsersAndGroups() {
    String[] usersId =
        SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel.getSelectedSets());

    Set<Contributor> recordedContributors = currentScheduleEvent.contributors;
    if (recordedContributors == null)
    	recordedContributors = new HashSet<Contributor>();

		deleteRecordedContributors(usersId, recordedContributors);
		addContributors(usersId, recordedContributors);
    currentScheduleEvent.setContributors(recordedContributors);
    getScheduleEventService().updateScheduleEvent(currentScheduleEvent);
	  }

  public void save() {

    // add last info for a complete save
    currentScheduleEvent.setAuthor(Integer.parseInt(getUserId()));
    currentScheduleEvent.setStatus(ScheduleEventStatus.OPEN);
    currentScheduleEvent.setCreationDate(new Date());

    // create all dateoption for database
    // preTreatementForDateOption();
    
    getScheduleEventService().createScheduleEvent(currentScheduleEvent);
    
    // notify contributors
    //initAlertUser();
    sendSubscriptionsNotification("create");
    
    // delete session object after saving it
    currentScheduleEvent = null;

  }
 
  public void sendSubscriptionsNotification(String type) {
    // send email alerts
    try {
      Set<Contributor> contributors = currentScheduleEvent.getContributors();
      List<String> newSubscribers = new ArrayList<String>(contributors.size());
      for (Contributor contributor : contributors) {
            newSubscribers.add(Integer.toString(contributor.getUserId()));
        }
      
        if (!newSubscribers.isEmpty()) {
    
          ResourceLocator message = new ResourceLocator(
              "com.silverpeas.components.scheduleevent.multilang.ScheduleEventBundle", DisplayI18NHelper.getDefaultLanguage());
          String subject = message.getString("scheduleEvent.notifSubject");
    
          Map<String, SilverpeasTemplate> templates = new HashMap<String, SilverpeasTemplate>();
          String fileName = "";
          if ("create".equals(type)) {
            fileName = "scheduleEventNotificationCreate";
          } 
          NotificationMetaData notifMetaData = new NotificationMetaData(
                  NotificationParameters.NORMAL, subject, templates, fileName);

          //String url = "/ScheduleEvent/" + currentScheduleEvent.getId();
          String url = "/Rscheduleevent/jsp/Detail?scheduleEventId=" + currentScheduleEvent.getId();
          for (String lang : DisplayI18NHelper.getLanguages()) {
            SilverpeasTemplate template = getNewTemplate();
            templates.put(lang, template);
            template.setAttribute("scheduleEventName", currentScheduleEvent.getTitle());
            template.setAttribute("scheduleEventDate", DateUtil.getOutputDate(currentScheduleEvent.getCreationDate(),lang));
            template.setAttribute("senderName", getUserDetail().getDisplayedName());        
            template.setAttribute("silverpeasURL", url);
    
            ResourceLocator localizedMessage = new ResourceLocator(
                "com.silverpeas.components.scheduleevent.multilang.ScheduleEventBundle", lang);
            notifMetaData.addLanguage(lang, localizedMessage.getString("scheduleEvent.notifSubject", subject), "");
            
            
          }
          for (String subscriberId : newSubscribers) {
            notifMetaData.addUserRecipient(new UserRecipient(subscriberId));
          }
          notifMetaData.setLink(url);
          notifMetaData.setComponentId(getComponentId());
          notifMetaData.setSender(getUserId());
          notifyUsers(notifMetaData, getUserId());
        }
    } catch (Exception e) {
      SilverTrace.warn("scheduleEvent", "ScheduleEventSessionController.sendSubscriptionsNotification()",
              "scheduleEvent.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "", e);
    }
  }

  private void notifyUsers(NotificationMetaData notifMetaData, String senderId) {
    Connection con = null;
    try {
      con = initCon();
      notifMetaData.setConnection(con);
      if (notifMetaData.getSender() == null || notifMetaData.getSender().length() == 0) {
        notifMetaData.setSender(senderId);
      }
      NotificationSender notifSender = new NotificationSender(notifMetaData.getComponentId());
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      SilverTrace.warn("scheduleEvent", "ScheduleEventSessionController.notifyUsers()",
              "scheduleEvent.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", e);
    } finally {
      fermerCon(con);
    }
  }
  
  private Connection initCon() {
    Connection con = null;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      //throw new BlogRuntimeException("blogBmEJB.initCon()", SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }
  private void fermerCon(Connection con) {
    try {
      con.close();
    } catch (SQLException e) {
      // traitement des exceptions
      //throw new BlogRuntimeException("GalleryBmEJB.fermerCon()", SilverpeasException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }
  
  protected SilverpeasTemplate getNewTemplate() {
    ResourceLocator rs =
          new ResourceLocator("com.silverpeas.components.scheduleevent.settings.ScheduleEventSettings", "");
      Properties templateConfiguration = new Properties();
      templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, rs
          .getString("templatePath"));
      templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, rs
          .getString("customersTemplatePath"));
    
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfiguration);
    }

  private ScheduleEventService getScheduleEventService() {
    return ServicesFactory.getScheduleEventService();
  }

  public List<ScheduleEvent> getScheduleEventsByUserId() {
    Set<ScheduleEvent> allEvents = getScheduleEventService().listAllScheduleEventsByUserId(getUserId());
    List<ScheduleEvent> results = new ArrayList<ScheduleEvent>(allEvents);
    Collections.sort(results, new ScheduleEventComparator());

    return results;
  }

  public ScheduleEvent getDetail(String id) {
    // update last visited date
    getScheduleEventService().setLastVisited(id, Integer.valueOf(getUserId()));
    // return detail for page
    return getScheduleEventService().findScheduleEvent(id);
  }

  public void switchState(String id) {
    ScheduleEvent event = getScheduleEventService().findScheduleEvent(id);
    int actualStatus = event.getStatus();
    int newStatus = ScheduleEventStatus.OPEN;
    if (ScheduleEventStatus.OPEN == actualStatus) {
      newStatus = ScheduleEventStatus.CLOSED;
    }
    getScheduleEventService().updateScheduleEventStatus(id, newStatus);
  }

  public void delete(String scheduleEventId) {
    getScheduleEventService().deleteScheduleEvent(scheduleEventId);
  }

  public void updateUserAvailabilities(ScheduleEvent scheduleEvent) {
    updateValidationDate(scheduleEvent, getUserId());
    getScheduleEventService().updateScheduleEvent(scheduleEvent);
  }

  private void updateValidationDate(ScheduleEvent scheduleEvent, String userId) {
    Contributor contributor = getContributor(scheduleEvent, userId);
    if (contributor != null) {
      contributor.setLastValidation(new Date());
    }
  }

  private Contributor getContributor(ScheduleEvent scheduleEvent, String id) {
    try {
      int userId = Integer.parseInt(id);
      for (Contributor contributor : scheduleEvent.contributors) {
        if (contributor.getUserId() == userId) {
          return contributor;
        }
      }
    } catch (Exception e) {
    }
    return null;
  }

  public ScheduleEvent purgeOldResponseForUserId(ScheduleEvent scheduleEvent) {
    return getScheduleEventService().purgeOldResponseForUserId(scheduleEvent,
        Integer.parseInt(getUserId()));
  }

  public Double getSubscribersRateAnswerFor(ScheduleEvent event) {
    return 0.0;
  }

  public Set<OptionDateVO> getCurrentOptionalDateIndexes() throws Exception {
    return ((ScheduleEventVO) getCurrentScheduleEventVO()).getOptionalDateIndexes();
  }

  public void setCurrentScheduleEventWith(Set<OptionDateVO> optionalDays) {
    ((ScheduleEventVO) getCurrentScheduleEventVO()).setScheduleEventWith(optionalDays);
  }

  public Response makeReponseFor(ScheduleEvent scheduleEvent, String dateId) {
    // TODO: Can add checks for dateId, scheduleEvent integrity
    Response result = new Response();
    result.setScheduleEvent(scheduleEvent);
    result.setUserId(Integer.parseInt(getUserId()));
    result.setOptionId(dateId);
    return result;
  }

}
