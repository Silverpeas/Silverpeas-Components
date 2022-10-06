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

import org.silverpeas.components.community.model.CommunityMembership;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.WebResourceUri;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * The memberships to a community of users exposed as a resource through the Web. If the community
 * of users for which memberships are asked doesn't exist, then {@link NotFoundException} is
 * thrown.
 * @author mmoquillon
 */
@WebService
@Path(CommunityWebResource.RESOURCE_NAME + "/{componentInstanceId}/" +
    CommunityMembershipResource.RESOURCE_NAME)
@Authorized
public class CommunityMembershipResource extends CommunityWebResource {

  protected static final String RESOURCE_NAME = "memberships";

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  @Override
  protected String getResourceBasePath() {
    return super.getResourceBasePath() + "/" + getComponentId() + "/memberships";
  }

  @Override
  protected WebResourceUri initWebResourceUri() {
    return createWebResourceUri(getResourceBasePath());
  }

  /**
   * Gets all the memberships to the community of users, whatever the status of them.
   * @param page a pagination page to restrict the memberships to get.
   * @return a web entity representing a paginated collection of memberships.
   */
  @GET
  @Path("all")
  @Produces(MediaType.APPLICATION_JSON)
  public CommunityMembershipEntities getAllMemberships(@QueryParam("page") final String page) {
    return process(community -> {
      PaginationPage paginationPage = getPaginationPage(page);
      SilverpeasList<CommunityMembership> memberships =
          getWebManager().getHistory(community, paginationPage);
      return asWebEntities(memberships);
    });
  }

  /**
   * Gets all the memberships to the community of users pending for validation.
   * @param page a pagination page to restrict the memberships to get.
   * @return a web entity representing a paginated collection of memberships.
   */
  @GET
  @Path("pending")
  @Produces(MediaType.APPLICATION_JSON)
  public CommunityMembershipEntities getPendingMemberships(@QueryParam("page") String page) {
    return process(community -> {
      PaginationPage paginationPage = getPaginationPage(page);
      SilverpeasList<CommunityMembership> memberships =
          getWebManager().getMembersToValidate(community, paginationPage);
      return asWebEntities(memberships);
    });
  }

  /**
   * Gets all the actual memberships to the community of users. These memberships are those they are
   * committed; in other term, the memberships of the members of the community of users.
   * @param page a pagination page to restrict the memberships to get.
   * @return a web entity representing a paginated collection of memberships.
   */
  @GET
  @Path("members")
  @Produces(MediaType.APPLICATION_JSON)
  public CommunityMembershipEntities getActualMemberships(@QueryParam("page") String page) {
    return process(community -> {
      PaginationPage paginationPage = getPaginationPage(page);
      SilverpeasList<CommunityMembership> memberships =
          getWebManager().getMembers(community, paginationPage);
      return asWebEntities(memberships);
    });
  }

  /**
   * Gets the membership of the specified user or throw {@link NotFoundException} if no such
   * membership to the community of users exists.
   * @param userId the unique identifier of the user.
   * @return the web entity representing a membership to the community of users.
   */
  @GET
  @Path("users/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CommunityMembershipEntity getUserMembership(@PathParam("userId") String userId) {
    return process(community -> {
      User user = userId.equals("me") ? getUser() : User.getById(userId);
      CommunityMembership membership = community.getMembershipsProvider().get(user)
          .orElseThrow(() -> new NotFoundException(
              "User " + getUser().getId() + " doesn't have any membership in the community " +
                  community.getComponentInstanceId()));
      return asWebEntity(membership);
    });
  }

  /**
   * Gets a given existing membership to the community of users or throw {@link NotFoundException}
   * if no such membership exists.
   * @param memberId the unique identifier of a membership.
   * @return the web entity representing a membership to the community of users.
   */
  @GET
  @Path("all/{membershipId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CommunityMembershipEntity getMembership(
      @PathParam("membershipId") final String memberId) {
    return process(community -> {
      CommunityMembership membership = community.getMembershipsProvider().get(memberId)
          .orElseThrow(() -> new NotFoundException(
              "No such membership " + memberId + " in the community " +
                  community.getComponentInstanceId()));
      return asWebEntity(membership);
    });
  }

  private CommunityMembershipEntities asWebEntities(
      final SilverpeasList<CommunityMembership> memberships) {
    SilverpeasList<CommunityMembershipEntity> entities = memberships.stream()
        .map(this::asWebEntity)
        .collect(SilverpeasList.collector(memberships));
    return CommunityMembershipEntities.builder()
        .with(entities)
        .with(getCommunityUriBuilder())
        .build();
  }

  private CommunityMembershipEntity asWebEntity(final CommunityMembership membership) {
    return CommunityMembershipEntity.builder()
        .with(membership)
        .with(getCommunityUriBuilder())
        .build();
  }

  private PaginationPage getPaginationPage(String page) {
    return StringUtil.isNotDefined(page) ? NO_PAGINATION : fromPage(page);
  }
}
