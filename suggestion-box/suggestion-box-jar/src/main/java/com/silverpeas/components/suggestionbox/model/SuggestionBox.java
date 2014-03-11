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
package com.silverpeas.components.suggestionbox.model;

import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * This entity represents a suggestion box.
 * User: Yohann Chastagnier
 * Date: 11/03/14
 */
@Entity
@Table(name = "sc_suggestion_box")
public class SuggestionBox extends AbstractJpaEntity<SuggestionBox, UuidIdentifier> {

  @Column(name = "componentInstanceId", nullable = false)
  private String componentInstanceId;

  @OneToMany(mappedBy = "suggestionBox", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
  private List<Suggestion> suggestions;

  @Transient
  private ComponentInstLight componentInst;

  /**
   * Gets the component instance identifier which is the identifier of a suggestion box.
   * @return the suggestion box component identifier.
   */
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Gets the title of the suggestion box according to requested language.
   * @return the suggestion box title according to requested language.
   */
  public String getTitle(String language) {
    return getComponentInst().getName(language);
  }

  /**
   * Gets the description of the suggestion box according to requested language.
   * @return the description box description according to requested language.
   */
  public String getDescription(String language) {
    return getComponentInst().getDescription(language);
  }

  /**
   * Loads and caches into current instance data of the component which represents the suggestion
   * box.
   * @return component data which represents the suggestion box.
   */
  private ComponentInstLight getComponentInst() {
    if (componentInst == null) {
      componentInst = OrganisationControllerFactory.getOrganisationController()
          .getComponentInstLight(getComponentInstanceId());
    }
    return componentInst;
  }
}
