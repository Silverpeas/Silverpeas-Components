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
import org.silverpeas.components.community.notification.user.MembershipRequestValidationUserNotificationBuilder;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;

import java.util.Set;
import java.util.function.Supplier;

import static org.silverpeas.components.community.CommunityComponentSettings.getMessagesIn;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.fromString;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;


/**
 * WEB manager which allows to centralize code to be used by REST Web Services and Web Component
 * Controller.
 * @author silveryocha
 */
@Service
public class CommunityWebManager {

  private static final String CACHE_KEY_PREFIX = CommunityWebManager.class.getSimpleName() + ":";
  private static final PaginationPage NO_PAGINATION = new PaginationPage(1, Integer.MAX_VALUE);

  protected CommunityWebManager() {
  }

  /**
   * Gets the singleton instance of the provider.
   */
  public static CommunityWebManager get() {
    return ServiceProvider.getSingleton(CommunityWebManager.class);
  }

  /**
   * Makes the current user joining the given community.
   * @param community {@link CommunityOfUsers} instance representing the community.
   */
  public void join(final CommunityOfUsers community) {
    final SilverpeasRole defaultRole = getDefaultMemberRoleOf(community);
    final String userLanguage = getUserLanguage();
    final String spaceName = OrganizationController.get()
        .getSpaceInstById(community.getSpaceId())
        .getName(userLanguage);
    final User currentRequester = User.getCurrentRequester();
    if (adminMustValidateNewMemberOf(community)) {
      community.addAsAPendingMember(currentRequester);
      successMessage("community.join.pendingValidation.success", spaceName);
    } else {
      community.addAsMember(currentRequester, defaultRole);
      successMessage("community.join.success", spaceName);
    }
  }

  /**
   * Validates the membership request of user given in parameters on specified community.
   * @param requester the user behind the request to join the community.
   * @param community the community the user accesses.
   * @param accept true to accept the request, false to refuse.
   * @param message message linked to the accept or refuse.
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
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @param member the member to manage.
   */
  public void endMembershipOf(final CommunityOfUsers community, final User member) {
    community.removeMembership(member);
    final String userLanguage = getUserLanguage();
    successMessage("community.endMembership.success", member.getDisplayedName(),
        OrganizationController.get().getSpaceInstById(community.getSpaceId()).getName(userLanguage));
  }

  /**
   * Makes the current user leaving the given community.
   * @param community {@link CommunityOfUsers} instance representing the community.
   */
  public void leave(final CommunityOfUsers community) {
    community.removeMembership(User.getCurrentRequester());
    final String userLanguage = getUserLanguage();
    successMessage("community.leave.success",
        OrganizationController.get().getSpaceInstById(community.getSpaceId()).getName(userLanguage));
  }

  /**
   * Gets members of the given community.
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @return list of {@link CommunityMembership} instance, representing each one a member.
   */
  @SuppressWarnings("unchecked")
  public SilverpeasList<CommunityMembership> getMembersToValidate(final CommunityOfUsers community) {
    return requestCache("membersToValidate", community.getId(), SilverpeasList.class,
        () -> community.getMembershipsProvider().getPending(NO_PAGINATION));
  }

  /**
   * Gets members of the given community.
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @return list of {@link CommunityMembership} instance, representing each one a member.
   */
  @SuppressWarnings("unchecked")
  public SilverpeasList<CommunityMembership> getMembers(final CommunityOfUsers community) {
    return requestCache("members", community.getId(), SilverpeasList.class,
        () -> community.getMembershipsProvider().getInRange(NO_PAGINATION));
  }

  /**
   * Gets history of the given community.
   * @param community {@link CommunityOfUsers} instance representing the community.
   * @return list of {@link CommunityMembership} instance, representing each one a member
   * registration/unregistration.
   */
  @SuppressWarnings("unchecked")
  public SilverpeasList<CommunityMembership> getHistory(final CommunityOfUsers community) {
    return requestCache("history", community.getId(), SilverpeasList.class,
        () -> community.getMembershipsProvider().getHistory(NO_PAGINATION));
  }

  /**
   * Indicates if the current requester is a member.
   * <p>
   *   A member MUST be directly specified into ADMIN, PUBLISHER, WRITER or READER role of direct
   *   parent space.
   * </p>
   * @param community {@link CommunityOfUsers} instance.
   * @return true if member, false otherwise.
   */
  public boolean isMemberOf(final CommunityOfUsers community) {
    return requestCache("isMemberOf", community.getId(), Boolean.class,
        () -> community.isMember(User.getCurrentRequester()));
  }

  /**
   * Indicates if the current requester has membership pending validation.
   * <p>
   *   A member MUST be directly specified into ADMIN, PUBLISHER, WRITER or READER role of direct
   *   parent space.
   * </p>
   * @param community {@link CommunityOfUsers} instance.
   * @return true if member, false otherwise.
   */
  public boolean isMembershipPendingFor(final CommunityOfUsers community) {
    return requestCache("isMembershipPending", community.getId(), Boolean.class,
        () -> community.getMembershipsProvider()
            .get(User.getCurrentRequester())
            .map(CommunityMembership::getStatus)
            .map(MembershipStatus::isPending)
            .orElse(false));
  }

  /**
   * Gets the roles the current requester has on the given community.
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
    return CacheServiceProvider.getRequestCacheService()
        .getCache()
        .computeIfAbsent(CACHE_KEY_PREFIX + type + ":" + id, classType, supplier);
  }

  private String getCommunityInstanceParameter(final CommunityOfUsers community,
      final String parameterName) {
    return OrganizationController.get()
        .getComponentParameterValue(community.getComponentInstanceId(), parameterName);
  }

  /**
   * Push a success message to the current user.
   * @param messageKey the key of the message.
   * @param params the message parameters.
   */
  private void successMessage(String messageKey, Object... params) {
    final String userLanguage = getUserLanguage();
    getMessager().addSuccess(getMessagesIn(userLanguage).getStringWithParams(messageKey, params));
  }

  private String getUserLanguage() {
    return requestCache("language", "user", String.class,
        () -> User.getCurrentRequester().getUserPreferences().getLanguage());
  }

  private WebMessager getMessager() {
    return WebMessager.getInstance();
  }
}
