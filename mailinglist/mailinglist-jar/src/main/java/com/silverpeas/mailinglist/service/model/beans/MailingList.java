package com.silverpeas.mailinglist.service.model.beans;

import java.util.HashSet;
import java.util.Set;

public class MailingList extends IdentifiedObject {

  public String componentId;
  public String name;
  public String subscribedAddress;
  public String description;
  public boolean open;
  public boolean moderated;
  public boolean notify;
  public boolean supportRSS;

  public Set<InternalUser> moderators = new HashSet<InternalUser>();
  public Set<InternalUser> readers = new HashSet<InternalUser>();
  public Set<ExternalUser> externalSubscribers = new HashSet<ExternalUser>();
  public Set<InternalGroupSubscriber> groupSubscribers = new HashSet<InternalGroupSubscriber>();
  public Set<InternalUserSubscriber> internalSubscribers = new HashSet<InternalUserSubscriber>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSubscribedAddress() {
    return subscribedAddress;
  }

  public void setSubscribedAddress(String subscribedAddress) {
    this.subscribedAddress = subscribedAddress;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean publiclyVisible) {
    this.open = publiclyVisible;
  }

  public boolean isModerated() {
    return moderated;
  }

  public void setModerated(boolean moderated) {
    this.moderated = moderated;
  }

  public Set<InternalUser> getModerators() {
    return moderators;
  }

  public void setModerators(Set<InternalUser> moderators) {
    this.moderators = moderators;
  }

  public Set<InternalUser> getReaders() {
    return readers;
  }

  public void setReaders(Set<InternalUser> readers) {
    this.readers = readers;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public Set<ExternalUser> getExternalSubscribers() {
    return externalSubscribers;
  }

  public void setExternalSubscribers(Set<ExternalUser> externalSubscribers) {
    this.externalSubscribers = externalSubscribers;
  }

  public boolean isNotify() {
    return notify;
  }

  public void setNotify(boolean notify) {
    this.notify = notify;
  }

  public void removeExternalSubscriber(ExternalUser user) {
    externalSubscribers.remove(user);
  }

  public void addExternalSubscriber(ExternalUser user) {
    externalSubscribers.add(user);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isSupportRSS() {
    return supportRSS;
  }

  public void setSupportRSS(boolean supportRSS) {
    this.supportRSS = supportRSS;
  }

  public boolean isEmailAuthorized(String email) {
    for (ExternalUser user : externalSubscribers) {
      if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
        return true;
      }
    }
    for (InternalUser user : readers) {
      if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
        return true;
      }
    }
    for (InternalUser user : moderators) {
      if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
        return true;
      }
    }
    return false;
  }

  public Set<InternalGroupSubscriber> getGroupSubscribers() {
    return groupSubscribers;
  }

  public void setGroupSubscribers(Set<InternalGroupSubscriber> groupSubscribers) {
    this.groupSubscribers = groupSubscribers;
  }

  public Set<InternalUserSubscriber> getInternalSubscribers() {
    return internalSubscribers;
  }

  public void setInternalSubscribers(Set<InternalUserSubscriber> internalSubscribers) {
    this.internalSubscribers = internalSubscribers;
  }

}
