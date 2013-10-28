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
package com.silverpeas.mailinglist.service.model.beans;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "sc_mailinglist_list")
@NamedQueries({
  @NamedQuery(name = "findByComponentId", query =
      "from MailingList where componentId = :componentId"),
  @NamedQuery(name = "findAll", query = "from MailingList")})
public class MailingList extends IdentifiableObject {

  private String componentId;
  @Transient
  private String name;
  @Transient
  private String subscribedAddress;
  @Transient
  private String description;
  @Transient
  private boolean open;
  @Transient
  private boolean moderated;
  @Transient
  private boolean notify;
  @Transient
  private boolean supportRSS;
  @Transient
  private Set<InternalUser> moderators = new HashSet<InternalUser>();
  @Transient
  private Set<InternalUser> readers = new HashSet<InternalUser>();
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "listId", nullable = false)
  private Set<ExternalUser> externalSubscribers = new HashSet<ExternalUser>();
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "mailingListId", nullable = false)
  private Set<InternalGroupSubscriber> groupSubscribers = new HashSet<InternalGroupSubscriber>();
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "mailingListId", nullable = false)
  private Set<InternalUserSubscriber> internalSubscribers = new HashSet<InternalUserSubscriber>();

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

  public Set<InternalUserSubscriber> getInternalSubscribers() {
    return internalSubscribers;
  }
}
