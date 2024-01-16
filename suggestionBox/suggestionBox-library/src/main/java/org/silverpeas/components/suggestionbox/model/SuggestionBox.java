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
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.suggestionbox.model;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.SilverpeasJpaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This entity represents a suggestion box.
 * @author Yohann Chastagnier
 */
@Entity
@NamedQuery(name = "suggestionBoxFromComponentInstance",
    query = "from SuggestionBox s where s.componentInstanceId = :componentInstanceId")
@Table(name = "sc_suggestion_box")
public class SuggestionBox extends SilverpeasJpaEntity<SuggestionBox, UuidIdentifier> {

  private static final long serialVersionUID = -3216638631298619076L;

  @Column(name = "instanceId", nullable = false)
  private String componentInstanceId;

  @OneToMany(mappedBy = "suggestionBox", fetch = FetchType.LAZY, cascade = {CascadeType.ALL},
      orphanRemoval = true)
  private List<Suggestion> suggestions;

  @Transient
  private ComponentInstLight componentInst;

  /**
   * Gets the suggestion box represented by the specified identifier.
   * @param suggestionBoxId the identifier of the required suggestion box.
   * @return the suggestion box instance if exists, null otherwise.
   */
  public static SuggestionBox getByComponentInstanceId(String suggestionBoxId) {
    SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
    return suggestionBoxService.getByComponentInstanceId(suggestionBoxId);
  }

  /**
   * Creates a new suggestion box with the specified title and with the specified short
   * description.
   * @param componentInstanceId the unique identifier of the application to which the suggestion
   * box
   * belongs.
   */
  public SuggestionBox(String componentInstanceId) {
    // actually, the title and description are set in the ComponentInst object representing the
    // application suggestion box (for instance, a suggestion box application is made up of one and
    // only one suggestion box.
    this.componentInstanceId = componentInstanceId;
    this.suggestions = new ArrayList<Suggestion>();
  }

  /**
   * Gets the component instance identifier which is the identifier of a suggestion box.
   * @return the suggestion box component identifier.
   */
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Gets the title of the suggestion box according to requested language.
   * @param language the language at which the title has to be returned.
   * @return the suggestion box title according to requested language.
   */
  public String getTitle(String language) {
    return getComponentInst().getName(language);
  }

  /**
   * Gets the description of the suggestion box according to requested language.
   * @param language the language at which the title has to be returned.
   * @return the description box description according to requested language.
   */
  public String getDescription(String language) {
    return getComponentInst().getDescription(language);
  }

  public SuggestionCollection getSuggestions() {
    return new SuggestionCollection(this);
  }

  protected SuggestionBox() {
  }

  /**
   * Loads and caches into current instance data of the component which represents the suggestion
   * box.
   * @return component data which represents the suggestion box.
   */
  private ComponentInstLight getComponentInst() {
    if (componentInst == null) {
      componentInst = OrganizationControllerProvider.getOrganisationController()
          .getComponentInstLight(getComponentInstanceId());
    }
    return componentInst;
  }

  /**
   * Gets the actual persisted suggestions in this suggestion box. This operation is a side effect
   * one as it performs some accesses to the underlying data source.
   * @return the list of actual suggestions in this suggestion box.
   */
  protected List<Suggestion> persistedSuggestions() {
    return this.suggestions;
  }

  /**
   * Gets the highest role of the specified user on the suggestion box.
   * @param user the aimed user.
   * @return a {@link SilverpeasRole} instance.
   */
  public SilverpeasRole getHighestUserRole(User user) {
    String[] profiles = OrganizationControllerProvider.getOrganisationController()
        .getUserProfiles(user.getId(), getComponentInstanceId());
    Set<SilverpeasRole> userRoles = SilverpeasRole.from(profiles);
    SilverpeasRole role = SilverpeasRole.getHighestFrom(userRoles);
    return role != null ? role : SilverpeasRole.reader;
  }

  private static SuggestionBoxService getSuggestionBoxService() {
    return SuggestionBoxService.get();
  }
}
