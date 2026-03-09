package org.silverpeas.components.suggestionbox.web;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.webapi.comment.CommentEntity;
import org.silverpeas.components.suggestionbox.model.Suggestion;

import jakarta.xml.bind.annotation.XmlElement;

import java.util.Objects;

/**
 * A comment posted about a given suggestion. It extends the comment by adding it additional
 * information like the title and the author of the commented suggestion.
 * @author mmoquillon
 */
public class SuggestionCommentEntity  extends CommentEntity {

  public static SuggestionCommentEntity fromComment(final Comment comment, String language) {
    return new SuggestionCommentEntity(comment, language);
  }

  @XmlElement
  private String suggestionTitle;
  @XmlElement
  private String suggestionAuthorName;

  public SuggestionCommentEntity onSuggestion(final Suggestion suggestion) {
    this.suggestionTitle = suggestion.getTitle();
    this.suggestionAuthorName = suggestion.getCreator().getDisplayedName();
    return this;
  }

  public String getSuggestionTitle() {
    return suggestionTitle;
  }

  public String getSuggestionAuthorName() {
    return suggestionAuthorName;
  }

  private SuggestionCommentEntity(final Comment comment, String language) {
    super(comment, language);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SuggestionCommentEntity)) return false;
    if (!super.equals(o)) return false;
    SuggestionCommentEntity that = (SuggestionCommentEntity) o;
    return Objects.equals(suggestionTitle, that.suggestionTitle)
           && Objects.equals(suggestionAuthorName, that.suggestionAuthorName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), suggestionTitle, suggestionAuthorName);
  }
}
