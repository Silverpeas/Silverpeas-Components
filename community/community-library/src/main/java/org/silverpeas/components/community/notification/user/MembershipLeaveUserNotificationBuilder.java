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

import org.silverpeas.components.community.CommunityComponentSettings;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.service.SpaceProfile;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.util.LocalizationBundle;

import java.util.Collection;

import static org.silverpeas.components.community.CommunityComponentSettings.getLeaveReasons;

/**
 * User notification dedicated to the membership leave.
 * @author silveryocha
 */
public class MembershipLeaveUserNotificationBuilder
    extends AbstractCommunityUserNotificationBuilder {

  private final SpaceProfile spaceProfile;
  private User leavingMember;
  private int reason;
  private String message;
  private boolean contactInFuture;

  protected MembershipLeaveUserNotificationBuilder(final CommunityOfUsers resource) {
    super(resource);
    this.spaceProfile = getSpaceManagerProfile();
  }

  /**
   * Initializing the user notification builder with given community.
   * @param community a {@link CommunityOfUsers} instance.
   * @return the builder instance itself.
   */
  public static MembershipLeaveUserNotificationBuilder about(
      final CommunityOfUsers community) {
    return new MembershipLeaveUserNotificationBuilder(community);
  }

  /**
   * Indicating the member which is leaving the community.
   * @param leavingMember a {@link User} instance representing the member leaving the community.
   * @return the builder instance itself.
   */
  public MembershipLeaveUserNotificationBuilder memberLeavingIs(final User leavingMember) {
    this.leavingMember = leavingMember;
    return this;
  }

  /**
   * Sets the leaving reason that the member has indicated.
   * @param reason an integer representing the index to retrieve the reason label into
   * {@link CommunityComponentSettings#getLeaveReasons(String)} list.
   * @return the builder instance itself.
   */
  public MembershipLeaveUserNotificationBuilder withReason(final int reason) {
    this.reason = reason;
    return this;
  }

  /**
   * Sets an additional message that allows the member to explain more precisely the reason of
   * its leaving.
   * @param message a string.
   * @return the builder instance itself.
   */
  public MembershipLeaveUserNotificationBuilder andMessage(final String message) {
    this.message = message;
    return this;
  }

  /**
   * Indicates that the member accepts or not to be contacted in the future about its leaving.
   * @param contactInFuture a boolean.
   * @return the builder instance itself.
   */
  public MembershipLeaveUserNotificationBuilder andContactInFuture(final boolean contactInFuture) {
    this.contactInFuture = contactInFuture;
    return this;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "community.membership.leaving.notif.subject";
  }

  @Override
  protected String getTemplateFileName() {
    return "communityMembershipLeaving";
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
    return leavingMember.getId();
  }

  @Override
  protected void perform(final CommunityOfUsers resource) {
    super.perform(resource);
    getNotificationMetaData().setOriginalExtraMessage(message);
  }

  @Override
  protected void performTemplateData(final String language, final CommunityOfUsers resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("reason", getLeaveReasons(language).get(reason));
    template.setAttribute("memberFullName", leavingMember.getDisplayedName());
    final LocalizationBundle bundle = getBundle(language);
    template.setAttribute("contactInFuture", contactInFuture
        ? bundle.getString("GML.yes")
        : bundle.getString("GML.no"));
  }
}
