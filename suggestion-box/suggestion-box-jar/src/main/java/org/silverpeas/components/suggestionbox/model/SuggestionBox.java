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

import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.components.suggestionbox.notification
    .SuggestionPendingValidationUserNotification;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.silverpeas.components.suggestionbox.model.SuggestionCriteria.QUERY_ORDER_BY
    .LAST_UPDATE_DATE_ASC;
import static org.silverpeas.contribution.ContributionStatus.DRAFT;
import static org.silverpeas.contribution.ContributionStatus.REFUSED;

/**
 * This entity represents a suggestion box.
 * @author Yohann Chastagnier
 */
@Entity
@Table(name = "sc_suggestion_box")
public class SuggestionBox extends AbstractJpaEntity<SuggestionBox, UuidIdentifier> {

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
   * Creates a new suggestion box with the specified title and with the specified short description.
   * @param componentInstanceId the unique identifier of the application to which the suggestion box
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

  public Suggestions getSuggestions() {
    return new Suggestions();
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
      componentInst = OrganisationControllerFactory.getOrganisationController()
          .getComponentInstLight(getComponentInstanceId());
    }
    return componentInst;
  }

  /**
   * Adds the specified suggestions into this suggestion box.
   * @param newSuggestion the suggestion to add.
   */
  protected void addSuggestion(final Suggestion newSuggestion) {
    newSuggestion.setSuggestionBox(this);
    this.suggestions.add(newSuggestion);
  }

  /**
   * Gets the greater role of the specified user on the suggestion box.
   * @param user the aimed user.
   * @return a {@link SilverpeasRole} instance.
   */
  public SilverpeasRole getGreaterUserRole(UserDetail user) {
    String[] profiles = OrganisationControllerFactory.getOrganisationController()
        .getUserProfiles(user.getId(), getComponentInstanceId());
    Set<SilverpeasRole> userRoles = SilverpeasRole.from(profiles);
    return SilverpeasRole.getGreaterFrom(userRoles);
  }

  public class Suggestions {

    /**
     * Adds the specified suggestion among the other suggestions of the suggestion box.
     * <p>
     * The suggestion will be persisted automatically once added.
     * @param suggestion the suggestion to add.
     */
    public void add(final Suggestion suggestion) {
      SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
      suggestionBoxService.addSuggestion(SuggestionBox.this, suggestion);
    }

    /**
     * Removes the specified suggestion from the suggestion box.
     * <p>
     * If the suggestion doesn't exist in the suggestion box, then nothing is done.
     * @param suggestion the suggestion to remove.
     */
    public void remove(final Suggestion suggestion) {
      SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
      suggestionBoxService.removeSuggestion(SuggestionBox.this, suggestion);
    }

    /**
     * Gets the suggestion with the specified identifier from the suggestions of the suggestion box.
     * @param suggestionId the unique identifier of the suggestion to get.
     * @return the suggestion matching the specified identifier or NONE if no such suggestion exists
     * in the suggestions of the suggestion box.
     */
    public Suggestion get(String suggestionId) {
      SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
      return suggestionBoxService.findSuggestionById(SuggestionBox.this, suggestionId);
    }

    /**
     * Finds the list of suggestions that are not published (draft end refused status) and which
     * the creator is those specified.
     * @param user the creator of the returned suggestions.
     * @return the list of suggestions as described above and ordered by ascending last update date.
     */
    public List<Suggestion> findNotPublishedFor(final UserDetail user) {
      SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
      SuggestionCriteria criteria = SuggestionCriteria.from(SuggestionBox.this).createdBy(user).
          statusIsOneOf(DRAFT, REFUSED)
          .orderedBy(LAST_UPDATE_DATE_ASC);
      return suggestionBoxService.findSuggestionsByCriteria(criteria);
    }

    /**
     * Publishes from the specified suggestion box the specified suggestion.
     * <p/>
     * The publication of a suggestion consists in changing its status from DRAFT to
     * PENDING_VALIDATION and sending a notification to the moderator if the updater is at most a
     * writer on the suggestion box.
     * <p/>
     * If the suggestion doesn't exist in the suggestion box, then nothing is done.
     * @param suggestion the suggestion to publish.
     * @return the suggestion updated.
     */
    public Suggestion publish(final Suggestion suggestion) {
      SuggestionBoxService suggestionBoxService = getSuggestionBoxService();
      Suggestion actual = suggestionBoxService.publishSuggestion(SuggestionBox.this, suggestion);
      switch (actual.getStatus()) {
        case PENDING_VALIDATION:
          UserNotificationHelper
              .buildAndSend(new SuggestionPendingValidationUserNotification(actual));
          break;
        case VALIDATED:
          break;
      }
      return actual;
    }
  }

  private static SuggestionBoxService getSuggestionBoxService() {
    SuggestionBoxServiceFactory serviceFactory = SuggestionBoxServiceFactory.getFactory();
    return serviceFactory.getSuggestionBoxService();
  }
}
