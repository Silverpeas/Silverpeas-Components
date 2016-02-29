package org.silverpeas.components.suggestionbox.web;

import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.web.CommentEntity;
import org.silverpeas.components.suggestionbox.model.Suggestion;

import javax.xml.bind.annotation.XmlElement;

/**
 * A comment posted about a given suggestion. It extends the comment by adding it additional
 * information like the title and the author of the commented suggestion.
 * @author mmoquillon
 */
public class SuggestionCommentEntity  extends CommentEntity {

  public static SuggestionCommentEntity fromComment(final Comment comment) {
    return new SuggestionCommentEntity(comment);
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

  private SuggestionCommentEntity(final Comment comment) {
    super(comment);
  }
}
