package com.silverpeas.mailinglist.service.model;

import java.util.Collection;
import java.util.List;

import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;

public interface MailingListService {

  public static final String PARAM_RSS = "rss";
  public static final String PARAM_OPEN = "open";
  public static final String PARAM_NOTIFY = "notify";
  public static final String PARAM_MODERATE = "moderated";
  public static final String PARAM_ADDRESS = "subscribedAddress";
  public static final String ROLE_MODERATOR = "moderator";
  public static final String ROLE_ADMINISTRATOR = "admin";
  public static final String ROLE_READER = "reader";
  public static final String ROLE_SUBSCRIBER = "subscriber";

  public String createMailingList(MailingList mailingList);

  public void addExternalUser(String componentId, ExternalUser user);

  public void addExternalUsers(String componentId, Collection<ExternalUser> users);

  public void removeExternalUser(String componentId, ExternalUser user);

  public void removeExternalUsers(String componentId, Collection<ExternalUser> users);

  public void setInternalSubscribers(String componentId, Collection<String> userIds);

  public void setGroupSubscribers(String componentId, Collection<String> groups);

  public void deleteMailingList(String componentId);

  public MailingList findMailingList(String componentId);

  public List<MailingList> listAllMailingLists();

  public void subscribe(String componentId, String userId);

  public void unsubscribe(String componentId, String userId);
  
}