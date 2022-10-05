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
 *
 */

package org.silverpeas.components.community.web;

import org.silverpeas.components.community.CommunityWebManager;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.Collection;

/**
 * Base class of all the resources in the community application exposed through the Web. It is
 * expected the web resource to be spawn for each incoming request; the life-cycle of each web
 * resource instance is within the one of the request.
 * @author mmoquillon
 */
@Authorized
public abstract class CommunityWebResource extends RESTWebService {

  protected static final String RESOURCE_NAME = "community";

  protected static final PaginationPage NO_PAGINATION = CommunityWebManager.NO_PAGINATION;

  private CommunityWebResourceURIBuilder uriBuilder;

  private CommunityOfUsers community;

  @Inject
  private CommunityWebManager webManager;

  @PostConstruct
  protected void initURIBuilder() {
    uriBuilder = new CommunityWebResourceURIBuilder(getUri());
  }

  /**
   * Gets the relative base path at which are all exposed the resources in the community
   * application.
   * @return the base path of the resource relative to the URI at which are all exposed the
   * REST-based web services in Silverpeas.
   */
  @Override
  protected String getResourceBasePath() {
    return CommunityWebResource.RESOURCE_NAME;
  }

  /**
   * Gets the roles the user requesting currently this web resource plays in the community of
   * users.
   * @return a collection of roles the current user plays.
   */
  @Override
  protected Collection<SilverpeasRole> getUserRoles() {
    return webManager.getUserRoleOn(getCommunity());
  }

  /**
   * Gets the Web Manager to use to fulfill the incoming request. The web manager centralizes all
   * the behaviour provided in the website, both by the Web GUI and the REST web resources.
   * @return a community web manager.
   */
  protected CommunityWebManager getWebManager() {
    return webManager;
  }

  /**
   * Processes the specified web function behalf the community of users concerned by the incoming
   * request.
   * @param function the web function to apply.
   * @param <T> the concrete type of the answer.
   * @return the answer of the web function.
   */
  protected <T> T process(final WebFunction<T> function) {
    return process(() -> function.applyWith(getCommunity())).execute();
  }

  private CommunityOfUsers getCommunity() {
    if (community == null) {
      community = CommunityOfUsers.getByComponentInstanceId(getComponentId())
          .orElseThrow(
              () -> new NotFoundException("No such component instance: " + getComponentId()));
    }
    return community;
  }

  /**
   * Gets the builder of URIs of the web resources managed by the community application.
   * @return a builder of URIS to identify the web resources handled in a community application
   * instance.
   */
  protected CommunityWebResourceURIBuilder getCommunityUriBuilder() {
    return uriBuilder;
  }

  @FunctionalInterface
  protected interface WebFunction<R> {
    R applyWith(final CommunityOfUsers community);
  }
}
