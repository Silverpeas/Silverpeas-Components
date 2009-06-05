package com.silverpeas.mailinglist.service.job;

import java.util.Collection;
import java.util.List;

import com.silverpeas.mailinglist.service.model.MailingListService;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;

public class StubMailingListService implements MailingListService {

  public void addExternalUser(String componentId, ExternalUser user) {
    // TODO Auto-generated method stub

  }

  public void addExternalUsers(String componentId, Collection users) {
    // TODO Auto-generated method stub

  }

  public String createMailingList(MailingList mailingList) {
    // TODO Auto-generated method stub
    return null;
  }

  public void deleteMailingList(String componentId) {
    // TODO Auto-generated method stub

  }

  public MailingList findMailingList(String componentId) {
    MailingList list = new MailingList();
    list.setComponentId(componentId);
    list.setSubscribedAddress(componentId);
    return list;
  }

  public List listAllMailingLists() {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeExternalUser(String componentId, ExternalUser user) {
    // TODO Auto-generated method stub

  }

  public void removeExternalUsers(String componentId, Collection users) {
    // TODO Auto-generated method stub

  }

  public void setGroupSubscribers(String componentId, Collection groups) {
    // TODO Auto-generated method stub

  }

  public void setInternalSubscribers(String componentId, Collection user) {
    // TODO Auto-generated method stub

  }

  public void subscribe(String componentId, String userId) {
    // TODO Auto-generated method stub

  }

  public void unsubscribe(String componentId, String userId) {
    // TODO Auto-generated method stub

  }

}
