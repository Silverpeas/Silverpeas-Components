/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.model;

import org.silverpeas.components.suggestionbox.repository.SuggestionRepository;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.ValidableContribution;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.ContributionValidation;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.rating.model.ContributionRating;
import org.silverpeas.core.contribution.rating.model.Rateable;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;
import org.silverpeas.core.util.Process;

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

/**
 * This entity represents a suggestion associated to a suggestion box.
 * A suggestion is described by a title and by a rich content. The rich content is managed by
 * the {@link WysiwygController} object and it is not persisted
 * into the database.
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "sc_suggestion")
public class Suggestion extends SilverpeasJpaEntity<Suggestion, UuidIdentifier>
    implements ValidableContribution, SilverpeasContent, Rateable {

  private static final long serialVersionUID = -8559980140411995766L;

  public static final String TYPE = "Suggestion";

  /**
   * Gets the suggestion with the specified unique identifier.
   * @param identifier the unique identifier of a suggestion.
   * @return either NONE suggestion if it exists no suggestions with the specified identifier or
   * the asked suggestion.
   */
  public static Suggestion getById(String identifier) {
    Suggestion suggestion = SuggestionRepository.get().getById(identifier);
    return (suggestion == null ? Suggestion.NONE:suggestion);
  }

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

  @Embedded
  private final ContributionValidation validation = new ContributionValidation();

  @Transient
  private String content = "";

  @Transient
  private boolean contentModified = false;
  @Transient
  private int commentCount;
  @Transient
  private ContributionRating contributionRating;

  protected Suggestion() {
  }

  /**
   * Constructs a new suggestion with the specified title.
   * @param title the suggestion title.
   */
  public Suggestion(String title) {
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

  /**
   * Sets the number of comment that were posted on this suggestion.
   * @param count the number of comments.
   */
  public void setCommentCount(int count) {
    this.commentCount = Math.max(count, 0);
  }

  /**
   * Gets the count of comments on this suggestion.
   * @return the number of comments.
   */
  public int getCommentCount() {
    return this.commentCount;
  }

  @Override
  public ContributionRating getRating() {
    return contributionRating;
  }

  /**
   * Sets the rating on the suggestion.
   * @param contributionRating the rating of the suggestion.
   */
  public void setRating(final ContributionRating contributionRating) {
    this.contributionRating = contributionRating;
  }

  @Override
  public ContributionValidation getValidation() {
    return validation;
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
    final Suggestion suggestion = this;
    Transaction.performInOne((Process<Void>) () -> {
      SuggestionRepository.get().save(suggestion);
      return null;
    });
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
  public boolean isContentModified() {
    return this.contentModified;
  }

  /**
   * Is this suggestion publishable by the specified user?
   * @param user the aimed user.
   * @return true if the suggestion is publishable by the specified user, false otherwise.
   */
  public boolean isPublishableBy(User user) {
    return (getValidation().isInDraft() || getValidation().isRefused()) && (user.isAccessAdmin()
        || (getCreator().equals(user) && getSuggestionBox().getHighestUserRole(user)
        .isGreaterThanOrEquals(SilverpeasRole.WRITER)));
  }

  /**
   * Gets the identifier of the component instance which the suggestion is attached.
   * @return the identifier of the component instance which the suggestion is attached.
   */
  public String getComponentInstanceId() {
    return getSuggestionBox().getComponentInstanceId();
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return ContributionIdentifier.from(getComponentInstanceId(), getId(), getContributionType());
  }

  @Override
  public String getDescription() {
    return getContent();
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  @Override
  public boolean isIndexable() {
    return false;
  }

  @Override
  public String toString() {
    return "Suggestion{" + "suggestionBox=" + suggestionBox.getId() + ", title=" + title
        + ", content=" + content + ", contentModified=" + contentModified + ", validation="
        + getValidation() + ", creationDate=" + getCreationDate() + ", lastUpdateDate="
        + getLastUpdateDate() + '}';
  }
}
