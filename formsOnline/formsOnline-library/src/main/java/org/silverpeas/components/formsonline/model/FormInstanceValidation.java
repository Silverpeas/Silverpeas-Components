/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.formsonline.model;

import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.model.ContributionValidation;

/**
 * Represents the data of a validation into context of FormsOnline component.
 * <p>
 * It is extending the transversal definition {@link ContributionValidation} by adding the
 * management of:
 * <ul>
 *   <li>a unique identifier</li>
 *   <li>the identifier of the linked {@link FormInstance}</li>
 *   <li>the type of the validation, cf. {@link FormInstanceValidationType}</li>
 *   <li>the following of the next validations by the specified validator</li>
 * </ul>
 * </p>
 * <p>
 * Into the context of FormsOnline component, there is only 3 possible statuses:
 * <ul>
 *   <li>{@link ContributionStatus#UNKNOWN}</li>
 *   <li>{@link ContributionStatus#VALIDATED}</li>
 *   <li>{@link ContributionStatus#REFUSED}</li>
 * </ul>
 * </p>
 * @author silveryocha
 */
public class FormInstanceValidation extends ContributionValidation {
  private static final long serialVersionUID = -829289141829243053L;

  private final FormInstance formInstance;
  private Integer id = null;
  private String validationType = FormInstanceValidationType.FINAL.name();
  private boolean follower = false;

  /**
   * By default, the status of the validation is unknown.
   * @param formInstance the {@link FormInstance} the validation is linked to.
   */
  FormInstanceValidation(final FormInstance formInstance) {
    this.formInstance = formInstance;
    setStatus(ContributionStatus.UNKNOWN);
  }

  public Integer getId() {
    return id;
  }

  void setId(final Integer id) {
    this.id = id;
  }

  public FormInstance getFormInstance() {
    return formInstance;
  }

  /**
   * Gets the type of the validation.
   * @return a {@link FormInstanceValidationType} instance.
   */
  public FormInstanceValidationType getValidationType() {
    return FormInstanceValidationType.valueOf(validationType);
  }

  /**
   * Sets the type of the validation.
   * @param validationType a {@link FormInstanceValidationType} instance.
   */
  public void setValidationType(final FormInstanceValidationType validationType) {
    this.validationType = validationType.name();
  }

  /**
   * Indicates if the registered validator is following the next validations.
   * @return true if the validator is following, false otherwise.
   */
  public boolean isFollower() {
    return follower;
  }

  /**
   * Sets the behavior of the validator according to the following of next validations.
   * @param follower true to follow, false otherwise.
   */
  public void setFollower(final boolean follower) {
    this.follower = follower;
  }
}
