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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.community;

import org.silverpeas.components.community.model.CommunityMembership;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.components.community.model.MembershipStatus;
import org.silverpeas.components.community.notification.user.MembershipLeaveUserNotificationBuilder;
import org.silverpeas.components.community.notification.user.MembershipRequestUserNotificationBuilder;
import org.silverpeas.components.community.notification.user.MembershipRequestValidationUserNotificationBuilder;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static org.silverpeas.components.community.CommunityComponentSettings.getMessagesIn;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.fromString;
import static org.silverpeas.kernel.util.StringUtil.getBooleanValue;

/**
 * WEB manager which allows to centralize code to be used by REST Web Services and Web Component
 * Controller.
 *
 * @author silveryocha
 */
@Service
public class CommunityWebManager {

  private static final String CACHE_KEY_PREFIX = CommunityWebManager.class.getSimpleName() + ":";
  public static final PaginationPage NO_PAGINATION = new PaginationPage(1, Integer.MAX_VALUE);

  protected CommunityWebManager() {
  }

  /**
   * Gets the singleton instance of the provider.
   */
  public static CommunityWebManager get() {
    return ServiceProvider.getService(CommunityWebManager.class);
  }

  /**
   * Makes the current user joining the given community.
   *
   * @param community {@link CommunityOfUsers} instance representing the community.
   */
  public void join(final CommunityOfUsers community) {
    final SilverpeasRole defaultRole = getDefaultMemberRoleOf(community);
    final String spaceName = getSpaceName(community);
    final User currentRequester = User.getCurrentRequester();
    if (adminMustValidateNewMemberOf(community)) {
      community.addAsAPendingMember(currentRequester);
      MembershipRequestUserNotificationBuilder
          .about(community)
          .newRequestFrom(currentRequester)
          .build()
          .send();
      successMessage("community.join.pendingValidation.success", spaceName);
    } else {
      community.addAsMember(currentRequester, defaultRole);
      successMessage("community.join.success", spaceName);
    }
  }

  /**
   * Validates the membership request of user given in parameters on specified community.
   *
   * @param requester the user behind the request to join the community.
   * @param community the community the user accesses.
   * @param accept true to accept the request, false to refuse.
   * @param message message linked to the acceptation or refuse.
   */
  public void validateRequestOf(final User requester, final CommunityOfUsers community,
      final boolean accept, final String message) {
    if (accept) {
      final SilverpeasRole defaultRole = getDefaultMemberRoleOf(community);
      community.addAsMember(requester, defaultRole);
    } else {
      community.refuseMembership(requester);
    }
    MembershipRequestValidationUserNotificationBuilder
        .about(community)
        .validating(requester, accept)
        .withMessage(message)
        .build()
        .send();
  }

  /**
   * Makes the given user leaving the given community.
   *
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @param member the member to manage.
   */
  public void endMembershipOf(final CommunityOfUsers community, final User member) {
    community.removeMembership(member);
    successMessage("community.endMembership.success", member.getDisplayedName(),
        getSpaceName(community));
  }

  /**
   * Makes the current user leaving the given community.
   *
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @param reason the index of the reason of the leaving.
   * @param message a message to explain more precisely the member leaving.
   * @param contactInFuture boolean, true to indicate that the member accepts to be contacted in the
   * future about its leaving.
   */
  public void leave(final CommunityOfUsers community, final int reason, final String message,
      final boolean contactInFuture) {
    final User leavingMember = User.getCurrentRequester();
    community.removeMembership(leavingMember);
    MembershipLeaveUserNotificationBuilder
        .about(community)
        .memberLeavingIs(leavingMember)
        .withReason(reason)
        .andMessage(message)
        .andContactInFuture(contactInFuture)
        .build()
        .send();
    successMessage("community.leave.success", getSpaceName(community));
  }

  /**
   * Saves into instance parameter of the given community the value of parameter
   * 'displayCharterOnSpaceHomepage'.
   *
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @param value true to display the charter, false otherwise.
   */
  public void setDisplayCharterOnSpaceHomepage(final CommunityOfUsers community,
      final boolean value) {
    final ComponentInst componentInst = OrganizationController.get()
        .getComponentInst(community.getComponentInstanceId());
    componentInst.getParameter("displayCharterOnSpaceHomepage").setValue(value ? "yes" : "no");
    try {
      Administration.get().updateComponentInst(componentInst);
    } catch (AdminException e) {
      throw new WebApplicationException(e);
    }
  }

  /**
   * Gets members pending validation of the given community.
   *
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @param page the pending members to get are paginated. Indicates the page to return. If null,
   * all the pending members are got.
   * @return list of {@link CommunityMembership} instance, representing each one a pending member.
   */
  @SuppressWarnings("unchecked")
  public SilverpeasList<CommunityMembership> getMembersToValidate(
      @Nonnull final CommunityOfUsers community, @Nullable final PaginationPage page) {
    PaginationPage paginationPage = page == null ? NO_PAGINATION : page;
    return requestCache("membersToValidate", community.getId(), SilverpeasList.class,
        () -> community.getMembershipsProvider().getPending(paginationPage));
  }

  /**
   * Gets members of the given community.
   *
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @param page the members to get are paginated. Indicates the page to return. If null, all the
   * members are got.
   * @return list of {@link CommunityMembership} instance, representing each one a committed member.
   */
  @SuppressWarnings("unchecked")
  public SilverpeasList<CommunityMembership> getMembers(@Nonnull final CommunityOfUsers community,
      @Nullable final PaginationPage page) {
    PaginationPage paginationPage = page == null ? NO_PAGINATION : page;
    return requestCache("members", community.getId(), SilverpeasList.class,
        () -> community.getMembershipsProvider().getInRange(paginationPage));
  }

  /**
   * Gets history of the given community.
   *
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @param page the members to get are paginated. Indicates the page to return. If null, all the
   * members are got.
   * @return list of {@link CommunityMembership} instance, representing each one a membership
   * whatever its status.
   */
  @SuppressWarnings("unchecked")
  public SilverpeasList<CommunityMembership> getHistory(@Nonnull final CommunityOfUsers community,
      @Nullable final PaginationPage page) {
    PaginationPage paginationPage = page == null ? NO_PAGINATION : page;
    return requestCache("history", community.getId(), SilverpeasList.class,
        () -> community.getMembershipsProvider().getHistory(paginationPage));
  }

  /**
   * Indicates if the current requester is a member.
   * <p>
   * A member MUST be directly specified into ADMIN, PUBLISHER, WRITER or READER role of direct
   * parent space.
   * </p>
   *
   * @param community {@link CommunityOfUsers} instance.
   * @return true if member, false otherwise.
   */
  public boolean isMemberOf(final CommunityOfUsers community) {
    return Objects.requireNonNull(requestCache("isMemberOf", community.getId(), Boolean.class,
        () -> community.isMember(User.getCurrentRequester())));
  }

  /**
   * Indicates if the current requester has membership pending validation.
   * <p>
   * A member MUST be directly specified into ADMIN, PUBLISHER, WRITER or READER role of direct
   * parent space.
   * </p>
   *
   * @param community {@link CommunityOfUsers} instance.
   * @return true if member, false otherwise.
   */
  public boolean isMembershipPendingFor(final CommunityOfUsers community) {
    return Objects.requireNonNull(requestCache("isMembershipPending",
        community.getId(),
        Boolean.class,
        () -> community.getMembershipsProvider()
            .get(User.getCurrentRequester())
            .map(CommunityMembership::getStatus)
            .map(MembershipStatus::isPending)
            .orElse(false)));
  }

  /**
   * Gets the roles the current requester has on the given community.
   *
   * @param community {@link CommunityOfUsers} instance.
   * @return a set of {@link SilverpeasRole}.
   */
  @SuppressWarnings("unchecked")
  public Set<SilverpeasRole> getUserRoleOn(final CommunityOfUsers community) {
    return requestCache("userRoleOf", community.getId(), Set.class,
        () -> community.getUserRoles(User.getCurrentRequester()));
  }

  private boolean adminMustValidateNewMemberOf(final CommunityOfUsers community) {
    return getBooleanValue(getCommunityInstanceParameter(community, "validateNewMember"));
  }

  private SilverpeasRole getDefaultMemberRoleOf(final CommunityOfUsers community) {
    return fromString(getCommunityInstanceParameter(community, "defaultMemberRole"));
  }

  private <T> T requestCache(final String type, final String id, Class<T> classType,
      Supplier<T> supplier) {
    return CacheAccessorProvider.getThreadCacheAccessor()
        .getCache()
        .computeIfAbsent(CACHE_KEY_PREFIX + type + ":" + id, classType, supplier);
  }

  private String getCommunityInstanceParameter(final CommunityOfUsers community,
      final String parameterName) {
    return OrganizationController.get()
        .getComponentParameterValue(community.getComponentInstanceId(), parameterName);
  }

  private String getSpaceName(final CommunityOfUsers community) {
    final String userLanguage = getUserLanguage();
    return OrganizationController.get()
        .getSpaceInstById(community.getSpaceId())
        .getName(userLanguage);
  }

  /**
   * Push a success message to the current user.
   *
   * @param messageKey the key of the message.
   * @param params the message parameters.
   */
  private void successMessage(String messageKey, Object... params) {
    final String userLanguage = getUserLanguage();
    getMessager().addSuccess(getMessagesIn(userLanguage).getString(messageKey), params);
  }

  private String getUserLanguage() {
    return requestCache("language", "user", String.class,
        () -> User.getCurrentRequester().getUserPreferences().getLanguage());
  }

  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }
}
