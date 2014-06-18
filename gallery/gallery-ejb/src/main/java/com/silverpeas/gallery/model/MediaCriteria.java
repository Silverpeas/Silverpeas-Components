/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.model;

import com.silverpeas.gallery.constant.MediaType;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.silverpeas.gallery.model.MediaCriteria.VISIBILITY.BY_DEFAULT;
import static com.silverpeas.gallery.model.MediaCriteria.VISIBILITY.FORCE_GET_ALL;

/**
 * Class that permits to set media search criteria for media application.
 * @author: Yohann Chastagnier
 */
public class MediaCriteria {

  public enum VISIBILITY {
    BY_DEFAULT, VISIBLE_ONLY, HIDDEN_ONLY, FORCE_GET_ALL
  }

  public enum QUERY_ORDER_BY {

    DIMENSION_ASC(false, "dimension", true), DIMENSION_DESC(false, "dimension", false),
    CREATE_DATE_ASC(true, "M.createDate", true), CREATE_DATE_DESC(true, "M.createDate", false),
    LAST_UPDATE_DATE_ASC(true, "M.lastUpdateDate", true),
    LAST_UPDATE_DATE_DESC(true, "M.lastUpdateDate", false),
    TITLE_ASC(true, "M.title", true), TITLE_DESC(true, "M.title", false),
    AUTHOR_ASC_EMPTY_END(false, "M.author", true), AUTHOR_DESC_EMPTY_END(false, "M.author", false),
    AUTHOR_ASC(true, "M.author", true), AUTHOR_DESC(true, "M.author", false);

    private final boolean applicableOnSQLQuery;
    private final String propertyName;
    private final boolean asc;

    public static QUERY_ORDER_BY fromPropertyName(String property, String sort) {
      QUERY_ORDER_BY orderBy = null;
      for (QUERY_ORDER_BY queryOrderBy : values()) {
        String orderByName = queryOrderBy.name().toLowerCase();
        if (orderByName.startsWith(property.toLowerCase()) &&
            orderByName.endsWith(sort.toLowerCase())) {
          orderBy = queryOrderBy;
        }
      }
      return orderBy;
    }

    private QUERY_ORDER_BY(final boolean applicableOnSQLQuery, final String propertyName,
        final boolean asc) {
      this.applicableOnSQLQuery = applicableOnSQLQuery;
      this.propertyName = propertyName;
      this.asc = asc;
    }

    public boolean isApplicableOnSQLQuery() {
      return applicableOnSQLQuery;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public boolean isAsc() {
      return asc;
    }
  }

  private UserDetail requester;
  private SilverpeasRole requesterComponentLowerRole;
  private String componentInstanceId;
  private UserDetail creator;
  private final List<Integer> albumIds = new ArrayList<Integer>();
  private final List<MediaType> mediaTypes = new ArrayList<MediaType>();
  private final List<QUERY_ORDER_BY> orderByList = new ArrayList<QUERY_ORDER_BY>();
  private final List<String> identifiers = new ArrayList<String>();
  private VISIBILITY visibility = BY_DEFAULT;
  private Date referenceDate = DateUtil.getDate();

  private MediaCriteria() {

  }

  /**
   * Initializes media search criteria axed on the given media component instance id.
   * @param componentInstanceId the identifier of the media instance.
   * @return an instance of media criteria based on the specified identifier of component instance.
   */
  public static MediaCriteria fromComponentInstanceId(String componentInstanceId) {
    MediaCriteria criteria = new MediaCriteria();
    criteria.onComponentInstanceId(componentInstanceId);
    return criteria;
  }

  /**
   * Initializes media search criteria axed on the given media identifier.
   * @param mediaId the identifier of the media instance.
   * @return an instance of media criteria based on the specified identifier of media.
   */
  public static MediaCriteria fromMediaId(String mediaId) {
    MediaCriteria criteria = new MediaCriteria();
    criteria.identifierIsOneOf(mediaId);
    return criteria;
  }

  /**
   * Sets the criterion of the identifier of the component instance the media must be attached.
   * @param componentInstanceId the identifier of the component instance.
   * @return an instance of media criteria with the instance of component instance criterion setl.
   */
  public MediaCriteria onComponentInstanceId(String componentInstanceId) {
    this.componentInstanceId = componentInstanceId;
    return this;
  }

  /**
   * Sets the requester. If no requester is specified to criteria, then
   * {@link UserDetail#getCurrentRequester()} is used.
   * @param requester the requester.
   * @return an instance of media criteria with the requester criterion filled.
   */
  public MediaCriteria setRequester(UserDetail requester) {
    this.requester = requester;
    requesterComponentLowerRole = null;
    return this;
  }

  /**
   * Sets the creator criterion to find media created by the given user.
   * @param user the user that must be the creator of the media(s).
   * @return the media criteria itself with the new criterion on the media creator.
   */
  public MediaCriteria createdBy(UserDetail user) {
    this.creator = user;
    return this;
  }

  /**
   * Sets the list of media album identifiers criterion to find media which are attached to one
   * of the given ones.
   * @param albumIds the media album identifier list that the media must be attached.
   * @return the media criteria itself with the new criterion on the media types.
   */
  public MediaCriteria albumIdentifierIsOneOf(Integer... albumIds) {
    CollectionUtil.addAllIgnoreNull(this.albumIds, albumIds);
    return this;
  }

  /**
   * Sets the list of media type criterion to find media which have their type equals to one
   * of the given ones.
   * @param mediaTypes the media type list that the media type must verify.
   * @return the media criteria itself with the new criterion on the media types.
   */
  public MediaCriteria mediaTypeIsOneOf(MediaType... mediaTypes) {
    CollectionUtil.addAllIgnoreNull(this.mediaTypes, mediaTypes);
    return this;
  }

  /**
   * Configures the order of the media list.
   * @param orderBies the list of order by directives.
   * @return the media criteria itself with the list ordering criterion.
   */
  public MediaCriteria orderedBy(QUERY_ORDER_BY... orderBies) {
    CollectionUtil.addAllIgnoreNull(this.orderByList, orderBies);
    return this;
  }

  /**
   * Sets the identifiers criterion to find the medias with an identifier equals to one of the
   * specified ones.
   * @param identifiers a list of identifiers the medias to find should have.
   * @return the media criteria itself with the new criterion on the media identifiers.
   */
  public MediaCriteria identifierIsOneOf(String... identifiers) {
    CollectionUtil.addAllIgnoreNull(this.identifiers, identifiers);
    return this;
  }

  /**
   * Sets the visibility criterion to find the medias according to their period of visibility.
   * If visibility is {@link VISIBILITY#BY_DEFAULT}, then the requester is verified to get
   * VISIBLE (all user roles) or VISIBLE + HIDDEN (lowest user role must be the publisher one).
   * @param visibility the visibility requested.
   * @return the media criteria itself with the new criterion on the media visibility.
   */
  public MediaCriteria visibility(VISIBILITY visibility) {
    if (visibility == null) {
      throw new IllegalArgumentException("visibility parameter must not be null");
    }
    this.visibility = visibility;
    return this;
  }

  /**
   * Sets the reference date criterion (used by visibility criterion for example).
   * @param referenceDate the reference date specified.
   * @return the media criteria itself with the new criterion on the media reference date.
   */
  public MediaCriteria referenceDateOf(Date referenceDate) {
    if (referenceDate == null) {
      throw new IllegalArgumentException("dateReference parameter must not be null");
    }
    this.referenceDate = referenceDate;
    return this;
  }

  /**
   * Gets the indetifier of media instance.
   * {@link #fromComponentInstanceId(String)}
   * @return the criterion on the media instance to which the medias should belong.
   */
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Gets the requester.
   * @return the criterion of requester, {@link UserDetail#getCurrentRequester()} if none.
   */
  private UserDetail getRequester() {
    if (requester == null) {
      return UserDetail.getCurrentRequester();
    }
    return requester;
  }

  private SilverpeasRole getRequesterComponentLowerRole() {
    if (requesterComponentLowerRole == null) {
      Set<SilverpeasRole> requesterRoles = SilverpeasRole.from(
          OrganisationControllerFactory.getOrganisationController()
              .getUserProfiles(getRequester().getId(), getComponentInstanceId()));
      requesterComponentLowerRole = SilverpeasRole.getGreaterFrom(requesterRoles);
    }
    return requesterComponentLowerRole;
  }

  /**
   * Gets the creator criteria value.
   * {@link #createdBy(UserDetail)}
   * @return the criterion on the creator of the medias.
   */
  private UserDetail getCreator() {
    return creator;
  }

  /**
   * Gets the media album identifier criteria value.
   * {@link #albumIdentifierIsOneOf(Integer...)}
   * @return the criterion on the album identifiers the medias must be attached.
   */
  private List<Integer> getAlbumIds() {
    return albumIds;
  }

  /**
   * Gets the media type criteria value.
   * {@link #mediaTypeIsOneOf(MediaType...)}
   * @return the criterion on the status the medias should match.
   */
  private List<MediaType> getMediaTypes() {
    return mediaTypes;
  }

  /**
   * Gets the identifiers criteria value.
   * {@link #identifierIsOneOf(String...)}
   * @return the criterion on the identifiers the medias should match.
   */
  private List<String> getIdentifiers() {
    return identifiers;
  }

  /**
   * Gets the order by directive list.
   * @return the order by directives.
   */
  private List<QUERY_ORDER_BY> getOrderByList() {
    return orderByList;
  }

  /**
   * Gets the visibility criterion.
   * @return the visibility criterion.
   */
  private VISIBILITY getVisibility() {
    return visibility;
  }

  /**
   * Gets the reference date.
   * @return the reference date.
   */
  private Date getReferenceDate() {
    return referenceDate;
  }

  /**
   * Processes this criteria with the specified processor.
   * It chains in a given order the different criterion to process.
   * @param processor the processor to use for processing each criterion in this criteria.
   */
  public void processWith(final MediaCriteriaProcessor processor) {
    processor.startProcessing();
    if (StringUtil.isDefined(getComponentInstanceId())) {
      processor.processComponentInstance(getComponentInstanceId());
    }
    VISIBILITY theVisibility = getVisibility();
    if (theVisibility == BY_DEFAULT && getRequester() != null && (getRequester().isAccessAdmin() ||
        getRequesterComponentLowerRole().isGreaterThanOrEquals(SilverpeasRole.publisher))) {
      theVisibility = FORCE_GET_ALL;
    }
    processor.then().processVisibility(theVisibility, getReferenceDate());
    if (!getAlbumIds().isEmpty()) {
      processor.then().processAlbums(getAlbumIds());
    }
    if (!getIdentifiers().isEmpty()) {
      processor.then().processIdentifiers(getIdentifiers());
    }
    if (getCreator() != null) {
      processor.then().processCreator(getCreator());
    }
    if (!getMediaTypes().isEmpty()) {
      processor.then().processMediaTypes(getMediaTypes());
    }
    if (!getOrderByList().isEmpty()) {
      processor.then().processOrdering(getOrderByList());
    }

    processor.endProcessing();
  }
}
