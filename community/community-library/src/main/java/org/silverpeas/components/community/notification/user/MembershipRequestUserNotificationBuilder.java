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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.community.notification.user;

import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.service.SpaceProfile;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Collection;

import static org.silverpeas.core.util.URLUtil.Permalink.COMPONENT;
import static org.silverpeas.core.util.URLUtil.getPermalink;

/**
 * User notification dedicated to notify validators about a new member request to join a
 * community.
 * @author silveryocha
 */
public class MembershipRequestUserNotificationBuilder
    extends AbstractCommunityUserNotificationBuilder {

  private final SpaceProfile spaceProfile;
  private User requester;

  protected MembershipRequestUserNotificationBuilder(final CommunityOfUsers resource) {
    super(resource);
    this.spaceProfile = getSpaceManagerProfile();
  }

  /**
   * Initializing the user notification builder with given community.
   * @param community a {@link CommunityOfUsers} instance.
   * @return the builder instance itself.
   */
  public static MembershipRequestUserNotificationBuilder about(final CommunityOfUsers community) {
    return new MembershipRequestUserNotificationBuilder(community);
  }

  /**
   * Indicating the requester for which the join request validation has been performed and if the
   * request has been accepted or refused.
   * @param requester a {@link User} instance if the join requester.
   * @return the builder instance itself.
   */
  public MembershipRequestUserNotificationBuilder newRequestFrom(final User requester) {
    this.requester = requester;
    return this;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "community.join.request.notif.subject";
  }

  @Override
  protected String getTemplateFileName() {
    return "communityMembershipRequest";
  }

  @Override
  protected String getTitle(final String language) {
    return getBundle(language).getStringWithParams(getBundleSubjectKey(),
        getSpace().getName(language));
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return spaceProfile.getAllUserIds();
  }

  @Override
  protected Collection<String> getGroupIdsToNotify() {
    return spaceProfile.getAllGroupIds();
  }

  @Override
  protected String getSender() {
    return requester.getId();
  }

  @Override
  protected String getResourceURL(final CommunityOfUsers resource) {
    return getPermalink(COMPONENT, getResource().getComponentInstanceId());
  }

  @Override
  protected void performTemplateData(final String language, final CommunityOfUsers resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("requesterFullName", requester.getDisplayedName());
  }
}
