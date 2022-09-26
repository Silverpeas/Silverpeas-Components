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
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Collection;

import static java.util.Collections.singletonList;

/**
 * User notification dedicated to the management of the validation of member request to join a
 * community.
 * @author silveryocha
 */
public class MembershipRequestValidationUserNotificationBuilder
    extends AbstractCommunityUserNotificationBuilder {

  private User requester;
  private boolean accepted;
  private String message;

  protected MembershipRequestValidationUserNotificationBuilder(final CommunityOfUsers resource) {
    super(resource);
  }

  /**
   * Initializing the user notification builder with given community.
   * @param community a {@link CommunityOfUsers} instance.
   * @return the builder instance itself.
   */
  public static MembershipRequestValidationUserNotificationBuilder about(
      final CommunityOfUsers community) {
    return new MembershipRequestValidationUserNotificationBuilder(community);
  }

  /**
   * Indicating the requester for which the join request validation has been performed and if the
   * request has been accepted or refused.
   * @param requester a {@link User} instance if the join requester.
   * @param accept the result of the validation. true if accepted, false otherwise.
   * @return the builder instance itself.
   */
  public MembershipRequestValidationUserNotificationBuilder validating(final User requester,
      final boolean accept) {
    this.requester = requester;
    this.accepted = accept;
    return this;
  }

  /**
   * Sets an additional message if any.
   * @param message a string.
   * @return the builder instance itself.
   */
  public MembershipRequestValidationUserNotificationBuilder withMessage(final String message) {
    this.message = message;
    return this;
  }

  @Override
  protected String getBundleSubjectKey() {
    return accepted ?
        "community.join.request.validate.accept.notif.subject" :
        "community.join.request.validate.refuse.notif.subject";
  }

  @Override
  protected String getTemplateFileName() {
    return "communityMembershipRequestValidation";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return singletonList(requester.getId());
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
    template.setAttribute("accepted", accepted);
  }
}
