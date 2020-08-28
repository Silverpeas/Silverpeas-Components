/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.formsonline.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.components.formsonline.model.DefaultFormsOnlineService.HierarchicalValidatorCacheManager;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

public class FormDetail {
  public static final int STATE_NOT_YET_PUBLISHED = 0;
  public static final int STATE_PUBLISHED = 1;
  public static final int STATE_UNPUBLISHED = 2;

  public static final int VALIDATOR_OK = 0;
  public static final int VALIDATOR_UNDEFINED = 1;
  public static final int VALIDATOR_NOT_ALLOWED = 2;

  public static final String RECEIVERS_TYPE_INTERMEDIATE = "I";
  public static final String RECEIVERS_TYPE_FINAL = "R";

  private int id = -1;
  private String xmlFormName = null;
  private String name = "";
  private String description = "";
  private String title = "";
  private String creatorId = null;
  private Date creationDate = new Date();
  private String instanceId = null;
  private int state = STATE_NOT_YET_PUBLISHED;
  private boolean hierarchicalValidation = false;
  private String requestExchangeReceiver = null;
  private boolean deleteAfterRequestExchange = false;

  private boolean sendable = true;
  private int nbRequests = 0;

  private MemoizedSupplier<String> hierarchicalValidatorOfCurrentUser;

  private List<User> sendersAsUsers;
  private List<Group> sendersAsGroups;

  private List<User> intermediateReceiversAsUsers;
  private List<Group> intermediateReceiversAsGroups;
  private List<User> receiversAsUsers;
  private List<Group> receiversAsGroups;

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the state
   */
  public int getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(int state) {
    this.state = state;
  }

  /**
   * @return the creationDate
   */
  public Date getCreationDate() {
    return creationDate != null ? (Date) creationDate.clone() : null;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = (creationDate != null ? (Date) creationDate.clone() : null);
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return the xmlFormName
   */
  public String getXmlFormName() {
    return xmlFormName;
  }

  /**
   * @param xmlFormName the xmlFormName to set
   */
  public void setXmlFormName(String xmlFormName) {
    this.xmlFormName = xmlFormName;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the creatorId
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * @param creatorId the creatorId to set
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * @return the instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @param instanceId the instanceId to set
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Indicates if the hierarchical validation enabled.
   * @return true if enabled, false othserwise.
   */
  public boolean isHierarchicalValidation() {
    return hierarchicalValidation;
  }

  /**
   * Sets the hierarchical validation flag.
   * @param hierarchicalValidation true to enabled, false otherwise.
   */
  public void setHierarchicalValidation(final boolean hierarchicalValidation) {
    this.hierarchicalValidation = hierarchicalValidation;
  }

  /**
   * Gets the receiver data which permits to exchange the data of a new form request.
   * <p>
   * If filled, just after its creation the new form request is exchanged with the given receiver.
   * </p>
   * <p>
   * For now, the receiver is represented by an e-mail. By this way, the data are sent to the
   * receiver just after a new form request creation.
   * </p>
   * @return an optional receiver data in charge of request exchange processing.
   */
  public Optional<String> getRequestExchangeReceiver() {
    return Optional.ofNullable(requestExchangeReceiver);
  }

  /**
   * Sets the receiver data (an e-mail for now) which permits to perform the exchange of a new
   * form request creation.
   * @param requestExchangeReceiver receiver data (an e-mail for now)
   */
  public void setRequestExchangeReceiver(final String requestExchangeReceiver) {
    this.requestExchangeReceiver = defaultStringIfNotDefined(requestExchangeReceiver, null);
  }

  /**
   * Indicates id the exchanged form request MUST be deleted after the exchange processing.
   * <p>
   * If method {@link #getRequestExchangeReceiver()} returns no receiver data, then no deletion
   * is indicated.
   * </p>
   * @return true if a new form request MUST be deleted after exchange, false otherwise.
   */
  public boolean isDeleteAfterRequestExchange() {
    return getRequestExchangeReceiver().map(r -> deleteAfterRequestExchange).orElse(false);
  }

  /**
   * Sets the behavior about the deletion of a new form request when it has just been exchanged with
   * the receiver procided by {@link #getRequestExchangeReceiver()} method.
   * @param deleteAfterRequestExchange  true if a new form request MUST be deleted after
   * exchange, false otherwise.
   */
  public void setDeleteAfterRequestExchange(final boolean deleteAfterRequestExchange) {
    this.deleteAfterRequestExchange = deleteAfterRequestExchange;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final FormDetail that = (FormDetail) o;

    return new EqualsBuilder().append(id, that.id).append(state, that.state)
        .append(hierarchicalValidation, that.hierarchicalValidation)
        .append(deleteAfterRequestExchange, that.deleteAfterRequestExchange)
        .append(xmlFormName, that.xmlFormName).append(name, that.name)
        .append(description, that.description).append(title, that.title)
        .append(creatorId, that.creatorId).append(creationDate, that.creationDate)
        .append(instanceId, that.instanceId)
        .append(requestExchangeReceiver, that.requestExchangeReceiver).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(id).append(xmlFormName).append(name)
        .append(description).append(title).append(creatorId).append(creationDate).append(instanceId)
        .append(state).append(hierarchicalValidation).append(requestExchangeReceiver)
        .append(deleteAfterRequestExchange).toHashCode();
  }

  public boolean isPublished() {
    return getState() == STATE_PUBLISHED;
  }

  public boolean isUnpublished() {
    return getState() == STATE_UNPUBLISHED;
  }

  public boolean isNotYetPublished() {
    return getState() == STATE_NOT_YET_PUBLISHED;
  }

  public void setSendable(boolean sendable) {
    this.sendable = sendable;
  }

  public boolean isSendable() {
    return sendable;
  }

  public boolean isSender(String userId) {
    return isInList(userId, getAllSenders());
  }

  public boolean isValidator(String userId) {
    return isInList(userId, getAllReceivers());
  }

  public FormPK getPK() {
    return new FormPK(Integer.toString(getId()), getInstanceId());
  }

  private boolean isInList(String userId, List<User> users) {
    for (User user : users) {
      if (user.getId().equals(userId)) {
        return true;
      }
    }
    return false;
  }

  private List<User> getAllSenders() {
    List<User> senders = getSendersAsUsers();
    senders.addAll(getUsers(getSendersAsGroups()));
    return senders;
  }

  private List<User> getAllReceivers() {
    List<User> users = getReceiversAsUsers();
    users.addAll(getUsers(getReceiversAsGroups()));
    users.addAll(getIntermediateReceiversAsUsers());
    users.addAll(getUsers(getIntermediateReceiversAsGroups()));
    return users;
  }

  private List<User> getUsers(List<Group> groups) {
    List<User> users = new ArrayList<>();
    for (Group group : groups) {
      for (User user : group.getAllUsers()) {
        users.add(user);
      }
    }
    return users;
  }

  public List<User> getSendersAsUsers() {
    if (sendersAsUsers == null) {
      return new ArrayList<>();
    }
    return sendersAsUsers;
  }

  public void setSendersAsUsers(final List<User> sendersAsUsers) {
    this.sendersAsUsers = sendersAsUsers;
  }

  public List<Group> getSendersAsGroups() {
    if (sendersAsGroups == null) {
      return new ArrayList<>();
    }
    return sendersAsGroups;
  }

  public void setSendersAsGroups(final List<Group> sendersAsGroups) {
    this.sendersAsGroups = sendersAsGroups;
  }

  public List<User> getReceiversAsUsers() {
    if (receiversAsUsers == null) {
      return new ArrayList<>();
    }
    return receiversAsUsers;
  }

  public void setReceiversAsUsers(final List<User> receiversAsUsers) {
    this.receiversAsUsers = receiversAsUsers;
  }

  public List<Group> getReceiversAsGroups() {
    if (receiversAsGroups == null) {
      return new ArrayList<>();
    }
    return receiversAsGroups;
  }

  public void setReceiversAsGroups(final List<Group> receiversAsGroups) {
    this.receiversAsGroups = receiversAsGroups;
  }

  protected List<User> getAllFinalReceivers() {
    List<User> users = getReceiversAsUsers();
    users.addAll(getUsers(getReceiversAsGroups()));
    return users;
  }

  public List<User> getIntermediateReceiversAsUsers() {
    if (intermediateReceiversAsUsers == null) {
      return new ArrayList<>();
    }
    return intermediateReceiversAsUsers;
  }

  public void setIntermediateReceiversAsUsers(final List<User> receiversAsUsers) {
    this.intermediateReceiversAsUsers = receiversAsUsers;
  }

  public List<Group> getIntermediateReceiversAsGroups() {
    if (intermediateReceiversAsGroups == null) {
      return new ArrayList<>();
    }
    return intermediateReceiversAsGroups;
  }

  public void setIntermediateReceiversAsGroups(final List<Group> receiversAsGroups) {
    this.intermediateReceiversAsGroups = receiversAsGroups;
  }

  protected List<User> getAllIntermediateReceivers() {
    List<User> users = getIntermediateReceiversAsUsers();
    users.addAll(getUsers(getIntermediateReceiversAsGroups()));
    return users;
  }

  public boolean isIntermediateValidator(String userId) {
    return isInList(userId, getAllIntermediateReceivers());
  }

  public boolean isIntermediateValidation() {
    return CollectionUtil.isNotEmpty(getIntermediateReceiversAsUsers()) ||
        CollectionUtil.isNotEmpty(getIntermediateReceiversAsGroups());
  }

  public int getNbRequests() {
    return nbRequests;
  }

  public void setNbRequests(final int nbRequests) {
    this.nbRequests = nbRequests;
  }

  public String getHierarchicalValidatorOfCurrentUser() {
    if (hierarchicalValidatorOfCurrentUser == null && isHierarchicalValidation()) {
      hierarchicalValidatorOfCurrentUser = new MemoizedSupplier<>(() -> {
        if (!this.isHierarchicalValidation()) {
          return StringUtil.EMPTY;
        }
        final HierarchicalValidatorCacheManager hvManager = new HierarchicalValidatorCacheManager();
        return hvManager.getHierarchicalValidatorOf(User.getCurrentRequester().getId());
      });
    }
    return hierarchicalValidatorOfCurrentUser.get();
  }

  public int getHierarchicalValidatorState() {
    if (!isHierarchicalValidation()) {
      return VALIDATOR_OK;
    }
    if (StringUtil.isNotDefined(getHierarchicalValidatorOfCurrentUser())) {
      return VALIDATOR_UNDEFINED;
    } else if (OrganizationController.get()
        .isComponentAvailableToUser(getInstanceId(), getHierarchicalValidatorOfCurrentUser())) {
      return VALIDATOR_OK;
    }
    return VALIDATOR_NOT_ALLOWED;
  }
}