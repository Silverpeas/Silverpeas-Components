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
package org.silverpeas.components.suggestionbox.model;

import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.contribution.ContributionStatus;
import org.silverpeas.contribution.ValidableContribution;
import org.silverpeas.contribution.model.ContributionValidation;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * This entity represents a suggestion associated to a suggestion box.
 * A suggestion is described by a title and by a rich content. The rich content is managed by
 * the {@link org.silverpeas.wysiwyg.control.WysiwygController} object and it is not persisted
 * into the database.
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "sc_suggestion")
public class Suggestion extends AbstractJpaEntity<Suggestion, UuidIdentifier>
    implements ValidableContribution {

  private static final long serialVersionUID = -8559980140411995766L;

  public static final String TYPE = "Suggestion";

  /**
   * The NONE suggestion. It represents no suggestions and it is dedicated to be used in place of
   * null.
   */
  public static final Suggestion NONE = new Suggestion();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "suggestionBoxId", referencedColumnName = "id", nullable = false)
  private SuggestionBox suggestionBox;

  @Column(name = "title", nullable = false)
  @Size(min = 1)
  @NotNull
  private String title;

  @Column(name = "status", nullable = false)
  private String status = ContributionStatus.DRAFT.name();

  @Embedded
  private ContributionValidation validation;

  @Transient
  private String content = "";
  @Transient
  private boolean contentModified = false;

  protected Suggestion() {
    this.validation = ContributionValidation.NONE_VALIDATION;
  }

  /**
   * Constructs a new suggestion with the specified title.
   * @param title the suggestion title.
   */
  public Suggestion(String title) {
    this.validation = ContributionValidation.NONE_VALIDATION;
    this.title = title;
  }

  /**
   * Gets the foreign suggestion box of the suggestion.
   * @return the suggestion box of the suggestion.
   */
  public SuggestionBox getSuggestionBox() {
    return suggestionBox;
  }

  /**
   * Gets the title of this suggestion.
   * @return the suggestion title.
   */
  @Override
  public String getTitle() {
    return this.title;
  }

  /**
   * Sets a new title to this suggestion.
   * @param newTitle the new title. Cannot be null or empty.
   */
  public void setTitle(String newTitle) {
    this.title = newTitle;
  }

  /**
   * Technical method to initialize the content of the suggestion.
   * @param content the content to initialize
   */
  void initializeContent(String content) {
    this.content = content;
  }

  /**
   * Sets the specified content to this suggestion.
   * @param content the suggestion's content to set.
   */
  public void setContent(String content) {
    String contentToSet = (content == null ? "" : content);
    if (!contentToSet.equals(this.content)) {
      this.content = contentToSet;
      this.contentModified = true;
    }
  }

  /**
   * Gets the content of this suggestion.
   * @return the suggestion's content.
   */
  public String getContent() {
    return (this.content == null ? "" : this.content);
  }

  @Override
  public ContributionValidation getValidation() {
    return validation;
  }

  @Override
  public void setValidation(ContributionValidation validation) {
    this.validation = validation;
  }

  /**
   * Is this suggestion defined? It is defined if and only if it isn't NONE.
   * @return true if this suggestion is defined and thus not NONE.
   */
  public boolean isDefined() {
    return this != NONE;
  }

  /**
   * Is this suggestion NONE?
   * @return true if this suggestion isn't defined and thus it is NONE.
   */
  public boolean isNotDefined() {
    return this == NONE;
  }

  /**
   * Saves the state of this suggestion so that is will persist over the current context of use.
   */
  public void save() {
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    suggestionBoxService.updateSuggestion(this);
  }

  /**
   * Sets the suggestion box to which this suggestion belongs.
   * @param box a suggestion box.
   */
  protected void setSuggestionBox(final SuggestionBox box) {
    this.suggestionBox = box;
  }

  /**
   * Is the content of this suggestion was modified?
   * @return true if the suggestion's content was modified.
   */
  protected boolean isContentModified() {
    return this.contentModified;
  }

  @Override
  public void setStatus(final ContributionStatus status) {
    this.status = status.name();
  }

  @Override
  public ContributionStatus getStatus() {
    return ContributionStatus.from(status);
  }

  @Override
  public boolean isInDraft() {
    return getStatus().isInDraft();
  }

  @Override
  public boolean isRefused() {
    return getStatus().isRefused();
  }

  @Override
  public boolean isPendingValidation() {
    return getStatus().isPendingValidation();
  }

  @Override
  public boolean isValidated() {
    return getStatus().isValidated();
  }

  /**
   * Is this suggestion publishable by the specified user?
   * @param user the aimed user.
   * @return true if the suggestion is publishable by the specified user, false otherwise.
   */
  public boolean isPublishableBy(UserDetail user) {
    return (isInDraft() || isRefused()) && (user.isAccessAdmin() || (getCreator().equals(user)
        && getSuggestionBox().getGreaterUserRole(user).isGreaterThanOrEquals(SilverpeasRole.writer)));
  }

  @Override
  public String getComponentInstanceId() {
    return getSuggestionBox().getComponentInstanceId();
  }

  @Override
  public String getSilverpeasContentId() {
    return null;
  }

  @Override
  public Date getCreationDate() {
    return getCreateDate();
  }

  @Override
  public String getDescription() {
    return getContent();
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  /**
   * Is the specified user can access this post?
   * <p/>
   * A user can access a post if it has enough rights to access the suggestion box instance in
   * which is managed this suggestion.
   * @param user a user in Silverpeas.
   * @return true if the user can access this suggestion, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<String> accessController = AccessControllerProvider.getAccessController(
        "componentAccessController");
    return accessController.isUserAuthorized(user.getId(), getComponentInstanceId());
  }

  private SuggestionBoxService getSuggestionBoxService() {
    SuggestionBoxServiceFactory serviceFactory = SuggestionBoxServiceFactory.getFactory();
    return serviceFactory.getSuggestionBoxService();
  }

  @Override
  public String toString() {
    return "Suggestion{" + "suggestionBox=" + suggestionBox.getId() + ", title=" + title
        + ", state=" + status + ", content=" + content + ", contentModified=" + contentModified
        + ", validation=" + getValidation() + ", creationDate=" + getCreationDate()
        + ", lastUpdateDate=" + getLastUpdateDate() + '}';
  }
}
