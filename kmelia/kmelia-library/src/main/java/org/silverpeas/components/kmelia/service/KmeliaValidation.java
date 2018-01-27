/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.kmelia.service;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This method handles the validation of a batch of publications. Given publications could be clone
 * or not, there is no matter, the performer will handle it.
 * <p/>
 * TODO in the future, this class must handle the entire process of validation instead of calling
 * services from Kmelia.
 * <p/>
 * @author Yohann Chastagnier
 */
class KmeliaValidation {

  private final String validatorId;
  private boolean force = false;
  private boolean validatorHasNoMoreRight = false;

  private Set<PublicationPK> pubPkOfPerformedValidations = new HashSet<PublicationPK>(500);

  /**
   * Hidden constructor.
   * @param validatorId the identifier of the validator.
   */
  private KmeliaValidation(final String validatorId) {
    this.validatorId = validatorId;
  }

  /**
   * Initializing an instance by giving the identifier of user which is or has been a validator.
   * @param validatorId a user identifier as string.
   * @return the new {@link KmeliaValidation} instance.
   */
  public static KmeliaValidation by(final String validatorId) {
    return new KmeliaValidation(validatorId);
  }

  /**
   * Calling this method indicates that the validation must be performed even if the state is not
   * into validation request.
   * @return the instance itself.
   */
  KmeliaValidation forceValidation() {
    if (validatorHasNoMoreRight) {
      throw new IllegalArgumentException(
          "forces the validation whereas the validator has no more right to validate");
    }
    force = true;
    return this;
  }

  /**
   * Calling this method indicates that the given validatorId has no more validation right on given
   * publications.
   * @return the instance itself.
   */
  KmeliaValidation validatorHasNoMoreRight() {
    if (force) {
      throw new IllegalArgumentException(
          "indicates the validator ha no more right to validate whereas the validation is " +
              "indicated to be forced");
    }
    validatorHasNoMoreRight = true;
    return this;
  }

  /**
   * Handles the validation of given publications.<br/>
   * Be CAREFUL, the context of validation instance is not reset, so if the method is called
   * several times, the treatment will take into account the previous calls.
   * @param publications
   */
  void validate(final List<PublicationDetail> publications) {
    for (PublicationDetail publication : publications) {
      validate(publication);
    }
  }

  /**
   * Handles the validation of given publication.<br/>
   * Be CAREFUL, the context of validation instance is not reset, so if the method is called
   * several times, the treatment will take into account the previous calls.
   * @param publication a publication to validate.
   */
  void validate(final PublicationDetail publication) {
    if (!force && !publication.isClone() && !publication.isValidationRequired()) {
      // Publication is not into a state of requested validation, so nothing is validated.
      return;
    }

    PublicationPK publicationPkToValidate = null;
    if (publication.isClone()) {
      PublicationDetail clone = getKmeliaService().getPublicationDetail(publication.getPK());
      if (clone != null && (force || clone.isValidationRequired())) {
        publicationPkToValidate = publication.getClonePK();
      }
    } else {
      publicationPkToValidate = publication.getPK();
    }

    if (publicationPkToValidate != null &&
        !pubPkOfPerformedValidations.contains(publicationPkToValidate)) {
      // The publication is not a clone and its state indicates a validation request.
      getKmeliaService()
          .validatePublication(publicationPkToValidate, validatorId, force, validatorHasNoMoreRight);

      // Register into an internal cache the identifier of the publication for which the validation
      // has been performed.
      pubPkOfPerformedValidations.add(publicationPkToValidate);
    }
  }

  private KmeliaService getKmeliaService() {
    return KmeliaService.get();
  }
}
