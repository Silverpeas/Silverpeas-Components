package org.silverpeas.components.quickinfo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.date.Period;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;

@Entity
@Table(name = "sc_quickinfo_news")
/*@NamedQuery(name = "newsFromComponentInstance", query = "from News n where n.componentInstanceId = :componentInstanceId")*/
@NamedQueries({@NamedQuery(name = "newsFromComponentInstance", query = "from News n where n.componentInstanceId = :componentInstanceId order by n.publishDate DESC"),
  @NamedQuery(name = "newsByForeignId", query = "from News n where n.publicationId = :foreignId")})
public class News extends AbstractJpaEntity<News, UuidIdentifier> implements SilverpeasContent {
  
  public static final String CONTRIBUTION_TYPE = "News";
  
  private static final long serialVersionUID = 1L;
  
  @Transient
  private PublicationDetail publication;
  @Transient
  private String content;
  
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
  
  public News(String name, String description, Period visibilityPeriod, boolean important, boolean ticker, boolean mandatory) {
    PublicationDetail publi = new PublicationDetail(name, description, visibilityPeriod, null, null);
    this.publication = publi;
    setImportant(important);
    setTicker(ticker);
    setMandatory(mandatory);
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
    CommentService commentService = CommentServiceFactory.getFactory().getCommentService();
    return commentService.getCommentsCountOnPublication(CONTRIBUTION_TYPE, getPublication().getPK());
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
    return OrganisationControllerFactory.getOrganisationController().isComponentAvailable(
        getComponentInstanceId(), user.getId());
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
  
  private StatisticBm getStatisticService() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBm.class);
  }
  
  public List<ClassifyPosition> getTaxonomyPositions() throws PdcException {
    String silverObjectId = getPublication().getSilverObjectId();
    return getTaxonomyService().getPositions(Integer.parseInt(silverObjectId), getComponentInstanceId());
  }
  
  private PdcBm getTaxonomyService() {
    return new PdcBmImpl();
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
  
}