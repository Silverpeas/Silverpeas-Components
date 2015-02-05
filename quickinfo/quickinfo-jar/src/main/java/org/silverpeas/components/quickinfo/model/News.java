/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quickinfo.model;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceProvider;
import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.stratelia.silverpeas.pdc.control.GlobalPdcManager;
import com.stratelia.silverpeas.pdc.control.PdcManager;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import com.stratelia.webactiv.statistic.control.StatisticService;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.date.Period;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.StringUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sc_quickinfo_news")
@NamedQueries({@NamedQuery(name = "newsFromComponentInstance",
    query = "from News n where n.componentInstanceId = :componentInstanceId " +
        "order by n.publishDate DESC"), @NamedQuery(name = "newsByForeignId",
    query = "from News n where n.publicationId = :foreignId")})
public class News extends AbstractJpaEntity<News, UuidIdentifier> implements SilverpeasContent {

  public static final String CONTRIBUTION_TYPE = "News";

  private static final long serialVersionUID = 1L;

  @Transient
  private PublicationDetail publication;
  @Transient
  private String content;
  @Transient
  private DelegatedNews delegatedNews;

  @Column(name = "instanceId", nullable = false)
  private String componentInstanceId;

  @Column(name = "important", nullable = false)
  @NotNull
  private boolean important = false;

  @Column(name = "broadcastTicker", nullable = false)
  @NotNull
  private boolean ticker = false;

  @Column(name = "broadcastMandatory", nullable = false)
  @NotNull
  private boolean mandatory = false;

  @Column(name = "foreignId", nullable = false)
  @Size(min = 1)
  @NotNull
  private String publicationId;

  @Column
  private Date publishDate;

  @Column
  private String publishedBy;

  protected News() {

  }

  public News(String name, String description, Period visibilityPeriod, boolean important,
      boolean ticker, boolean mandatory) {
    this.publication = new PublicationDetail(name, description, visibilityPeriod, null, null);
    setImportant(important);
    setTicker(ticker);
    setMandatory(mandatory);
  }

  public NewsPK getPK() {
    return new NewsPK(getId(), getComponentInstanceId());
  }

  public News(PublicationDetail publication) {
    setPublication(publication);
  }

  public PublicationDetail getPublication() {
    return publication;
  }

  public News setId(String id) {
    return super.setId(id);
  }

  public String getTitle() {
    return getPublication().getName();
  }

  public void setTitle(String title) {
    getPublication().setName(title);
  }

  public String getDescription() {
    return getPublication().getDescription();
  }

  public void setDescription(String desc) {
    getPublication().setDescription(desc);
  }

  public void setCreatorId(String userId) {
    super.setCreatedBy(userId);
    getPublication().setCreatorId(userId);
  }

  public String getCreatorId() {
    return getCreatedBy();
  }

  public void setUpdaterId(String userId) {
    super.setLastUpdatedBy(userId);
    getPublication().setUpdaterId(userId);
  }

  public String getUpdaterId() {
    return getPublication().getUpdaterId();
  }

  public Date getUpdateDate() {
    return getPublication().getUpdateDate();
  }

  public boolean isVisible() {
    return getVisibilityPeriod().contains(new Date());
  }

  public boolean isNoMoreVisible() {
    return new Date().after(getVisibilityPeriod().getEndDate());
  }

  public boolean isNotYetVisible() {
    return new Date().before(getVisibilityPeriod().getBeginDate());
  }

  public void setVisibilityPeriod(Period period) {
    getPublication().setVisibilityPeriod(period);
  }

  public Period getVisibilityPeriod() {
    return getPublication().getVisibilityPeriod();
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public List<Integer> getBroadcastModes() {
    List<Integer> modes = new ArrayList<Integer>();
    modes.add(getPublication().getImportance());
    return modes;
  }

  public ThumbnailDetail getThumbnail() {
    ThumbnailDetail thumbnail =
        new ThumbnailDetail(getComponentInstanceId(), Integer.parseInt(getPublicationId()),
            ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
    return ThumbnailController.getCompleteThumbnail(thumbnail);
  }

  public int getNumberOfComments() {
    CommentService commentService = CommentServiceProvider.getCommentService();
    return commentService.getCommentsCountOnPublication(CONTRIBUTION_TYPE, getPK());
  }

  @Override
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  public void setComponentInstanceId(String componentId) {
    getPublication().getPK().setComponentName(componentId);
    this.componentInstanceId = componentId;
  }

  @Override
  public String getSilverpeasContentId() {
    return getPublication().getSilverObjectId();
  }

  @Override
  public Date getCreationDate() {
    return getCreateDate();
  }

  @Override
  public String getContributionType() {
    return CONTRIBUTION_TYPE;
  }

  @Override
  public boolean canBeAccessedBy(UserDetail user) {
    return OrganizationControllerProvider.getOrganisationController()
        .isComponentAvailable(getComponentInstanceId(), user.getId());
  }

  public void setImportant(boolean important) {
    this.important = important;
  }

  public boolean isImportant() {
    return important;
  }

  public void setTicker(boolean ticker) {
    this.ticker = ticker;
  }

  public boolean isTicker() {
    return ticker;
  }

  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setPublicationId(String publicationId) {
    this.publicationId = publicationId;
  }

  public String getPublicationId() {
    return publicationId;
  }

  protected void setPublication(PublicationDetail publication) {
    this.publication = publication;
    this.content = getPublication().getWysiwyg();
  }

  protected PublicationPK getForeignPK() {
    return new PublicationPK(getPublicationId(), getComponentInstanceId());
  }

  public int getNbAccess() {
    return getStatisticService().getCount(this);
  }

  public boolean isDraft() {
    return getPublication().isDraft();
  }

  public void setDraft() {
    getPublication().setStatus(PublicationDetail.DRAFT);
  }

  public void setPublished() {
    getPublication().setStatus(PublicationDetail.VALID);
  }

  private StatisticService getStatisticService() {
    return ServiceProvider.getService(StatisticService.class);
  }

  public List<ClassifyPosition> getTaxonomyPositions() throws PdcException {
    String silverObjectId = getPublication().getSilverObjectId();
    if (StringUtil.isDefined(silverObjectId)) {
      return getTaxonomyService()
          .getPositions(Integer.parseInt(silverObjectId), getComponentInstanceId());
    }
    return Collections.emptyList();
  }

  private PdcManager getTaxonomyService() {
    return new GlobalPdcManager();
  }

  public String getPermalink() {
    return URLManager.getSimpleURL(URLManager.URL_PUBLI, getPublicationId());
  }

  public void setPublishDate(Date publishDate) {
    this.publishDate = publishDate;
  }

  public Date getPublishDate() {
    return publishDate;
  }

  public void setPublishedBy(String publishedBy) {
    this.publishedBy = publishedBy;
  }

  public String getPublishedBy() {
    return publishedBy;
  }

  public boolean isUpdatedAfterBePublished() {
    if (getPublishDate() == null) {
      return false;
    }
    return DateUtil.compareTo(getUpdateDate(), getPublishDate(), false) > 0;
  }

  public DelegatedNews getDelegatedNews() {
    return delegatedNews;
  }

  public void setDelegatedNews(DelegatedNews dn) {
    this.delegatedNews = dn;
  }

  public boolean isCanBeSubmittedOnHomepage() {
    return delegatedNews == null || delegatedNews.isDenied();
  }

  public Date getOnlineDate() {
    if (getPublishDate() != null && getVisibilityPeriod().getBeginDate().after(getPublishDate())) {
      return getVisibilityPeriod().getBeginDate();
    }
    return getPublishDate();
  }

}