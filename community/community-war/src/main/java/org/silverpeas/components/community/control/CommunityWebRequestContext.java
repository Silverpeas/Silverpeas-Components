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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community.control;

import org.silverpeas.components.community.CommunityWebManager;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.web.mvc.webcomponent.WebComponentRequestContext;

import javax.ws.rs.WebApplicationException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * The execution context of an incoming HTTP request in regard to the application.
 * Any specific treatment related to the incoming requests and to the outgoing responses should be
 * performed here. For example, if you wish to perform some actions before the processing of the
 * request by the Web Component Controller CommunityWebController.
 * Usually, either this class is empty or the WebComponentRequestContext is directly used with
 * the Web Component Controller CommunityWebController.
 */
public class CommunityWebRequestContext extends
    WebComponentRequestContext<CommunityWebController> {

  private final MemoizedSupplier<CommunityOfUsers> community = new MemoizedSupplier<>(
      () -> CommunityOfUsers.getByComponentInstanceId(getComponentInstanceId())
          .orElseThrow(() -> new WebApplicationException(
              String.format("Community about instance %s does not exist", getComponentInstanceId()),
              NOT_FOUND)));

  public CommunityOfUsers getCommunity() {
    return community.get();
  }

  public boolean isSpaceHomePage() {
    return getRequest().getParameterAsBoolean("FromSpaceHomepage");
  }

  public boolean isMember() {
    return CommunityWebManager.get().isMemberOf(getCommunity());
  }

  @Override
  public Collection<SilverpeasRole> getUserRoles() {
    final Set<SilverpeasRole> roles = CommunityWebManager.get().getUserRoleOn(getCommunity());
    if (isSpaceHomePage()) {
      return roles.stream()
          .filter(not(SilverpeasRole.ADMIN::equals))
          .collect(Collectors.toSet());
    }
    return roles;
  }
}