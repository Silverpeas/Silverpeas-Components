/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.mailinglist.service.model;

import com.silverpeas.annotation.Service;
import com.silverpeas.mailinglist.model.MailingListRuntimeException;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.InternalGroupSubscriber;
import com.silverpeas.mailinglist.service.model.beans.InternalSubscriber;
import com.silverpeas.mailinglist.service.model.beans.InternalUser;
import com.silverpeas.mailinglist.service.model.beans.InternalUserSubscriber;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.dao.MailingListDao;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.silverpeas.core.admin.OrganisationController;
import org.springframework.transaction.annotation.Transactional;

@Service("mailingListService")
@Transactional
public class MailingListServiceImpl implements MailingListService {

  public static final String COMPONENT_NAME = "mailinglist";
  @Inject
  private MailingListDao mailingListDao;
  @Inject
  private OrganisationController organisationController;

  public MailingListDao getMailingListDao() {
    return mailingListDao;
  }

  public OrganisationController getOrganisationController() {
    return organisationController;
  }

  @Override
  public String createMailingList(MailingList mailingList) {
    String subscribedAddress = organisationController.getComponentParameterValue(
        mailingList.getComponentId(), PARAM_ADDRESS);
    String componentId = mailingList.getComponentId();
    String[] ids = this.organisationController.getCompoId("mailinglist");
    if (ids != null) {
      for (int i = 0; i < ids.length; i++) {
        String currentId = ids[i];
        if (currentId != null && !currentId.startsWith(COMPONENT_NAME)) {
          currentId = COMPONENT_NAME + currentId;
        }
        if (!componentId.equalsIgnoreCase(currentId)) {
          String param = organisationController.getComponentParameterValue(ids[i],
              PARAM_ADDRESS);
          if (param != null && param.equalsIgnoreCase(subscribedAddress)) {
            SilverTrace.error("mailingList",
                "MailingListServiceImpl.createMailingList",
                "mailinglist.creation.existingAddress", subscribedAddress);
            throw new MailingListRuntimeException(
                "com.silverpeas.mailinglist.service.model.MailingListServiceImpl",
                SilverpeasRuntimeException.ERROR, "Address already subscribed");
          }
        }
      }
    }
    return this.mailingListDao.createMailingList(mailingList);
  }

  @Override
  public void addExternalUser(String componentId, ExternalUser user) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList != null) {
      mailingList.getExternalSubscribers().add(user);
      this.mailingListDao.updateMailingList(mailingList);
    }
  }

  @Override
  public void removeExternalUser(String componentId, ExternalUser user) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList != null) {
      mailingList.getExternalSubscribers().remove(user);
      this.mailingListDao.updateMailingList(mailingList);
    }
  }

  @Override
  public void deleteMailingList(String componentId) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList != null) {
      this.mailingListDao.deleteMailingList(mailingList);
    }
  }

  @Override
  public MailingList findMailingList(String componentId) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList == null) {
      return null;
    }
    fillMailingList(mailingList);
    return mailingList;
  }

  protected void fillMailingList(MailingList mailingList) {
    String subscribedAddress = organisationController.getComponentParameterValue(mailingList.
        getComponentId(),
        PARAM_ADDRESS);
    mailingList.setSubscribedAddress(subscribedAddress);
    ComponentInst component = organisationController.getComponentInst(mailingList.getComponentId());
    mailingList.setName(component.getLabel());
    mailingList.setDescription(component.getDescription());
    String moderated = organisationController.getComponentParameterValue(mailingList.
        getComponentId(),
        PARAM_MODERATE);
    mailingList.setModerated(getParamBooleanValue(moderated));
    String notify = organisationController.
        getComponentParameterValue(mailingList.getComponentId(), PARAM_NOTIFY);
    mailingList.setNotify(getParamBooleanValue(notify));
    String open = organisationController.getComponentParameterValue(mailingList.getComponentId(),
        PARAM_OPEN);
    mailingList.setOpen(getParamBooleanValue(open));
    String rss = organisationController.getComponentParameterValue(mailingList.getComponentId(),
        PARAM_RSS);
    mailingList.setSupportRSS(getParamBooleanValue(rss));
    UserDetail[] details = organisationController.getAllUsers(mailingList.getComponentId());
    for (int i = 0; i < details.length; i++) {
      String[] roles = organisationController.getUserProfiles(details[i].getId(),
          mailingList.getComponentId());
      for (int j = 0; j < roles.length; j++) {
        if (ROLE_READER.equals(roles[j])) {
          InternalUser user = new InternalUser(details[i].getId(), details[i].geteMail());
          user.setDomain(organisationController.getDomain(details[i].getDomainId()).
              getSilverpeasServerURL());
          user.setName(details[i].getDisplayedName());
          user.setEmail(details[i].geteMail());
          mailingList.getReaders().add(user);
        } else if (ROLE_MODERATOR.equalsIgnoreCase(roles[j]) || ROLE_ADMINISTRATOR.equalsIgnoreCase(
            roles[j])) {
          InternalUser user = new InternalUser(details[i].getId(), details[i].geteMail());
          user.setDomain(organisationController.getDomain(details[i].getDomainId()).
              getSilverpeasServerURL());
          user.setName(details[i].getDisplayedName());
          user.setEmail(details[i].geteMail());
          mailingList.getModerators().add(user);
        }
      }
    }
  }

  @Override
  public void addExternalUsers(String componentId,
      Collection<ExternalUser> users) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList != null) {
      for (ExternalUser user : users) {
        user.setComponentId(componentId);
        mailingList.getExternalSubscribers().add(user);
      }
      this.mailingListDao.updateMailingList(mailingList);
    }
  }

  @Override
  public void removeExternalUsers(String componentId,
      Collection<ExternalUser> users) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList != null) {
      for (ExternalUser user : users) {
        user.setComponentId(componentId);
        mailingList.getExternalSubscribers().remove(user);
      }
      this.mailingListDao.updateMailingList(mailingList);
    }
  }

  protected boolean getParamBooleanValue(String param) {
    return param != null && (Boolean.valueOf(param).booleanValue() || "Y".equalsIgnoreCase(param)
        || "YES".equalsIgnoreCase(param));
  }

  @Override
  public List<MailingList> listAllMailingLists() {
    List<MailingList> lists = mailingListDao.listMailingLists();
    for (MailingList mailingList : lists) {
      fillMailingList(mailingList);
    }
    return lists;
  }

  @Override
  public void setInternalSubscribers(String componentId,
      Collection<String> userIds) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList != null) {
      Map<String, InternalSubscriber> subscribers = prepareMap(mailingList.getInternalSubscribers());
      mailingList.getInternalSubscribers().clear();
      if (userIds != null && !userIds.isEmpty()) {
        Set<InternalUserSubscriber> newUsers = new HashSet<InternalUserSubscriber>(
            userIds.size());
        for (String userId : userIds) {
          InternalUserSubscriber user = (InternalUserSubscriber) subscribers.get(userId);
          if (user == null) {
            user = new InternalUserSubscriber();
            user.setExternalId(userId);
          }
          newUsers.add(user);
        }
        mailingList.getInternalSubscribers().addAll(newUsers);
      } else {
        mailingList.getInternalSubscribers().clear();
      }
      this.mailingListDao.updateMailingList(mailingList);
    }
  }

  @Override
  public void setGroupSubscribers(String componentId,
      Collection<String> groupIds) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList != null) {
      Map<String, InternalSubscriber> groups = prepareMap(mailingList.getGroupSubscribers());
      mailingList.getGroupSubscribers().clear();
      if (groupIds != null && !groupIds.isEmpty()) {
        Set<InternalGroupSubscriber> newGroups = new HashSet<InternalGroupSubscriber>(
            groupIds.size());
        for (String groupId : groupIds) {
          InternalGroupSubscriber group = (InternalGroupSubscriber) groups.get(groupId);
          if (group == null) {
            group = new InternalGroupSubscriber();
            group.setExternalId(groupId);
          }
          newGroups.add(group);
        }
        mailingList.getGroupSubscribers().addAll(newGroups);
      } else {
        mailingList.getGroupSubscribers().clear();
      }
      this.mailingListDao.updateMailingList(mailingList);
    }
  }

  @Override
  public void subscribe(String componentId, String userId) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList != null) {
      InternalUserSubscriber user = new InternalUserSubscriber();
      user.setExternalId(userId);
      mailingList.getInternalSubscribers().add(user);
      this.mailingListDao.updateMailingList(mailingList);
    }
  }

  @Override
  public void unsubscribe(String componentId, String userId) {
    MailingList mailingList = this.mailingListDao.findByComponentId(componentId);
    if (mailingList != null && userId != null) {
      Iterator<InternalUserSubscriber> iter = mailingList.getInternalSubscribers().iterator();
      while (iter.hasNext()) {
        InternalUserSubscriber user = iter.next();
        if (userId.equalsIgnoreCase(user.getExternalId())) {
          iter.remove();
        }
      }
      this.mailingListDao.updateMailingList(mailingList);
    }
  }

  private Map<String, InternalSubscriber> prepareMap(
      Set<? extends InternalSubscriber> subscribers) {
    Map<String, InternalSubscriber> result = new HashMap<String, InternalSubscriber>(
        subscribers.size());
    for (InternalSubscriber subscriber : subscribers) {
      result.put(subscriber.getExternalId(), subscriber);
    }
    return result;
  }
}
