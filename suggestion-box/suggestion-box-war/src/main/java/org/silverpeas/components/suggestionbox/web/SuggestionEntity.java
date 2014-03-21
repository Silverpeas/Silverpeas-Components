/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.web;

import org.silverpeas.components.suggestionbox.model.Suggestion;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * It represents the state of a suggestion in a suggestion box as transmitted within the body of
 * an HTTP response or an HTTP request.
 * @author mmoquillon
 */
@XmlRootElement
public class SuggestionEntity {

  public static SuggestionEntity fromSuggestion(final Suggestion suggestion) {
    return new SuggestionEntity().withId(suggestion.getId()).withTitle(suggestion.getTitle()).
        withContent(suggestion.getContent());
  }

  @XmlElement
  private String id;
  @XmlElement(nillable = false, required = true)
  @NotNull
  private String title;
  @XmlElement
  private String content;

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  private SuggestionEntity withTitle(String title) {
    this.title = title;
    return this;
  }

  private SuggestionEntity withContent(String content) {
    this.content = content;
    return this;
  }

  private SuggestionEntity withId(String id) {
    this.id = id;
    return this;
  }

  protected SuggestionEntity() {

  }
}
