/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community.model;

import org.silverpeas.components.community.repository.CommunityOfUsersRepository;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessControl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Optional;
import java.util.Set;

import static java.util.function.Predicate.not;

/**
 * The community of users for a given Silverpeas resource, for instance a collaborative space. Users
 * in the community are said to be members of this community, and hence of the resource for which
 * the community has been created. A community is always managed by a Community application
 * instance; it is like the resource for which the community has been created delegates its
 * management to that application instance.
 */
@Entity
@Table(name = "SC_Community")
@NamedQuery(
    name = "CommunityByComponentInstanceId",
    query = "select c from CommunityOfUsers c where c.componentInstanceId = :componentInstanceId " +
        "order by c.componentInstanceId, c.id")
public class CommunityOfUsers
    extends SilverpeasJpaEntity<CommunityOfUsers, UuidIdentifier> {
  private static final long serialVersionUID = -4908726669864467915L;

  @Column(name = "resourceId", nullable = false)
  private String resourceId;

  @Column(name = "instanceId", nullable = false)
  private String componentInstanceId;

  /**
   * Constructs a new empty Community instance.
   */
  protected CommunityOfUsers() {
    // this constructor is for the persistence engine.
  }

  /**
   * Constructs a new Community instance for the specified resource.
   * @param componentInstanceId the unique identifier of a component instance managing the
   * community.
   * @param resourceId the unique identifier of a resource in Silverpeas for which the community is
   * constructed.
   */
  public CommunityOfUsers(final String componentInstanceId, final String resourceId) {
    this.componentInstanceId = componentInstanceId;
    this.resourceId = resourceId;
  }

  /**
   * Gets the community of users managed by the specified component instance. If the component
   * instance doesn't exist then nothing is returned.
   * @param instanceId the unique identifier of a component instance.
   * @return maybe a community instance or nothing if the component instance doesn't exist.
   */
  public static Optional<CommunityOfUsers> getByComponentInstanceId(final String instanceId) {
    CommunityOfUsersRepository repository = CommunityOfUsersRepository.get();
    return repository.getByComponentInstanceId(instanceId);
  }

  /**
   * Gets the space facade content of the Community.
   * @return a {@link WysiwygContent} instance.
   */
  public WysiwygContent getSpaceFacadeContent() {
    return WysiwygController.get(getComponentInstanceId(), "SpaceFacade", null);
  }

  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Gets the unique identifier of a resource for which this community of users is about.
   * @return the unique identifier of a resource in Silverpeas. For instance the unique identifier
   * of a space.
   */
  public String getResourceId() {
    return resourceId;
  }

  /**
   * Saves this community state into the persistence context. If the community doesn't exist yet its
   * state is then persisted, otherwise its state is updated in the persistence context.
   * @return the saved Community.
   */
  public CommunityOfUsers save() {
    return Transaction.performInOne(() -> {
      CommunityOfUsersRepository repository = CommunityOfUsersRepository.get();
      return repository.save(this);
    });
  }

  /**
   * Indicates if the given user is a member.
   * <p>
   *   A member MUST be directly specified into ADMIN, PUBLISHER, WRITER or READER role of direct
   *   parent space.
   * </p>
   * @param user {@link User} instance.
   * @return true if member, false otherwise.
   */
  public boolean isMember(final User user) {
    final OrganizationController controller = OrganizationController.get();
    final String currentRequesterId = User.getCurrentRequester().getId();
    return Optional.of(getResourceId())
        .map(controller::getSpaceInstById)
        .stream()
        .flatMap(s -> s.getAllSpaceProfilesInst().stream())
        .filter(not(SpaceProfileInst::isManager).and(not(SpaceProfileInst::isInherited)))
        .flatMap(p -> p.getAllUsers().stream())
        .anyMatch(i -> i.equals(currentRequesterId));
  }

  /**
   * Gets the roles the given user has on the given community.
   * @param user {@link User} instance.
   * @return an unmodifiable set of {@link SilverpeasRole}.
   */
  public Set<SilverpeasRole> getUserRoleOn(final User user) {
    return Set.copyOf(ComponentAccessControl.get()
        .getUserRoles(user.getId(), getComponentInstanceId(), AccessControlContext.init()));
  }
}