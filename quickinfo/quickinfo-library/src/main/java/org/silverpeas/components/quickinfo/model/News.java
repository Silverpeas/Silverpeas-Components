/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.contribution.ContributionVisibility;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.ContributionModel;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.model.WithAttachment;
import org.silverpeas.core.contribution.model.WithThumbnail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.reminder.WithReminder;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.silverpeas.core.date.TemporalConverter.asDate;
import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;

@Entity
@Table(name = "sc_quickinfo_news")
@NamedQueries({
    @NamedQuery(name = "newsFromComponentInstances", query = "select n from News n where n" +
        ".componentInstanceId in :componentInstanceIds order by n.publishDate DESC" +
        ", n.lastUpdateDate DESC"),
    @NamedQuery(name = "newsByForeignId", query = "select n from News n where n.publicationId = " +
        ":foreignId"),
    @NamedQuery(name = "newsMandatories", query = "select n from News n where n.mandatory = " +
        ":mandatory"),
    @NamedQuery(name = "newsForTicker", query = "select n from News n where n.ticker = :ticker")})
public class News extends SilverpeasJpaEntity<News, UuidIdentifier> implements SilverpeasContent,
    WithAttachment, WithThumbnail, WithReminder {

  public static final String CONTRIBUTION_TYPE = "News";

  private static final long serialVersionUID = 1L;

  @Transient  private PublicationDetail publication;
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
  private String publicationId;

  @Column
  private Date publishDate;

  @Column
  private String publishedBy;

  protected News() {
    // default constructor for the persistence engine
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

  @Override
  public News setId(String id) {
    return super.setId(id);
  }

  @Override
  public String getTitle() {
    return getPublication().getName();
  }

  public void setTitle(String title) {
    getPublication().setName(title);
  }

  @Override
  public String getDescription() {
    return getPublication().getDescription();
  }

  public void setDescription(String desc) {
    getPublication().setDescription(desc);
  }

  public void setCreatorId(String userId) {
    super.createdBy(userId);
    getPublication().setCreatorId(userId);
  }

  public void setUpdaterId(String userId) {
    super.lastUpdatedBy(userId);
    getPublication().setUpdaterId(userId);
  }

  public String getUpdaterId() {
    return getPublication().getUpdaterId();
  }

  public Date getUpdateDate() {
    return getPublication().getUpdateDate();
  }

  public boolean isVisible() {
    return getVisibility().isActive();
  }

  public boolean isNoMoreVisible() {
    return getVisibility().hasBeenActive();
  }

  public boolean isNotYetVisible() {
    return getVisibility().willBeActive();
  }

  public void setVisibilityPeriod(Period period) {
    getPublication().setVisibilityPeriod(period);
  }

  public ContributionVisibility getVisibility() {
    return publication.getVisibility();
  }

  public void setContentToStore(String content) {
    this.content = content;
  }

  public String getContentToStore() {
    return content;
  }

  public String getContent() {
    return getPublication().getContent().getRenderer().renderView();
  }

  public List<Integer> getBroadcastModes() {
    List<Integer> modes = new ArrayList<>();
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

  /**
   * Gets the identifier of the component instance which the news is attached.
   * @return the identifier of the component instance which the news is attached.
   */
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
  public ContributionIdentifier getContributionId() {
    return ContributionIdentifier.from(getComponentInstanceId(), getId(), getContributionType());
  }

  @Override
  public User getLastModifier() {
    return getLastUpdater();
  }

  @Override
  public Date getLastModificationDate() {
    return getLastUpdateDate();
  }

  @Override
  public String getContributionType() {
    return CONTRIBUTION_TYPE;
  }

  @Override
  public boolean isIndexable() {
    return false;
  }

  /**
   * The type of this resource
   *
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return CONTRIBUTION_TYPE;
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

  public int getNbAccess() {
    return getStatisticService().getCount(this);
  }

  public boolean isDraft() {
    return getPublication().isDraft();
  }

  public void setDraft() {
    getPublication().setStatus(PublicationDetail.DRAFT_STATUS);
  }

  public void setPublished() {
    getPublication().setStatus(PublicationDetail.VALID_STATUS);
  }

  public List<ClassifyPosition> getTaxonomyPositions() throws PdcException {
    String silverObjectId = getPublication().getSilverObjectId();
    if (StringUtil.isDefined(silverObjectId)) {
      return getTaxonomyService()
          .getPositions(Integer.parseInt(silverObjectId), getComponentInstanceId());
    }
    return Collections.emptyList();
  }

  public String getPermalink() {
    return URLUtil.getSimpleURL(URLUtil.URL_PUBLI, getPublicationId());
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
    if (getPublishDate() != null) {
      final Date visibilityStart = asDate(asOffsetDateTime(getPublication().getVisibility().getPeriod().getStartDate())
          .atZoneSameInstant(ZoneOffset.systemDefault()));
      if (visibilityStart.after(getPublishDate())) {
        return visibilityStart;
      }
    }
    return getPublishDate();
  }

  public int getNumberOfAttachments() {
    List<SimpleDocument> attachments = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKeyAndType(getForeignPK().toResourceReference(), DocumentType.attachment,
            I18NHelper.defaultLanguage);
    return attachments.size();
  }

  protected void setPublication(PublicationDetail publication) {
    this.publication = publication;
  }

  protected PublicationPK getForeignPK() {
    return new PublicationPK(getPublicationId(), getComponentInstanceId());
  }

  private StatisticService getStatisticService() {
    return StatisticService.get();
  }

  private PdcManager getTaxonomyService() {
    return PdcManager.get();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public ContributionModel getModel() {
    return new NewsModel(this);
  }
}