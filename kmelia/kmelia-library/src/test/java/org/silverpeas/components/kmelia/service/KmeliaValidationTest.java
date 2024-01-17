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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.contribution.publication.model.PublicationDetail.*;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
class KmeliaValidationTest {

  private static final String INSTANCE_ID = "instanceId";
  private static final String VALIDATOR_ID = "26";

  private KmeliaValidation kmeliaValidation;
  @TestManagedMock
  private KmeliaService kmeliaService;
  private int counter = 0;

  @BeforeEach
  public void setup() {
    counter = 0;
    kmeliaValidation = KmeliaValidation.by(VALIDATOR_ID);
  }

  @Test
  void validatePublicationValidationIsNotRequiredShouldNotPerformValidation() {
    PublicationDetail publication = createPublication(DRAFT_STATUS);
    kmeliaValidation.validate(publication);
    verifyNoInteractions(kmeliaService);
  }

  @Test
  void validateAlreadyValidatedPublicationShouldNotPerformValidation() {
    PublicationDetail publication = createPublication(VALID_STATUS);
    kmeliaValidation.validate(publication);
    verifyNoInteractions(kmeliaService);
  }

  @Test
  void forceValidateAlreadyValidatedPublicationShouldPerformValidation() {
    PublicationDetail publication = createPublication(VALID_STATUS);
    kmeliaValidation.forceValidation()
        .validate(publication);
    verify(kmeliaService, times(1)).validatePublication(publication.getPK(), VALIDATOR_ID, true,
        false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  void validatePublicationValidationIsRequiredShouldPerformValidation() {
    PublicationDetail publication = createPublication(TO_VALIDATE_STATUS);
    kmeliaValidation.validate(publication);
    verify(kmeliaService, times(1)).validatePublication(publication.getPK(), VALIDATOR_ID, false,
        false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  void validateSamePublicationValidationIsRequiredShouldPerformOneTimeOnlyTheValidation() {
    PublicationDetail publication = createPublication(TO_VALIDATE_STATUS);
    kmeliaValidation.validate(Arrays.asList(publication, publication, publication, publication));
    kmeliaValidation.validate(publication);
    kmeliaValidation.validate(publication);
    verify(kmeliaService, times(1)).validatePublication(publication.getPK(), VALIDATOR_ID, false,
        false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  void validateClonedPublicationValidationIsRequiredShouldPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications =
        createClonedPublication(TO_VALIDATE_STATUS);
    PublicationDetail orig = publications.getLeft();
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verify(kmeliaService, times(1)).validatePublication(orig.getPK(), VALIDATOR_ID, false, false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  void validateClonedPublicationValidationIsNotRequiredShouldNotPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications = createClonedPublication(DRAFT_STATUS);
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  void forceValidateClonedPublicationAlreadyValidatedShouldPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications = createClonedPublication(VALID_STATUS);
    PublicationDetail orig = publications.getLeft();
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.forceValidation()
        .validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verify(kmeliaService, times(1)).validatePublication(orig.getPK(), VALIDATOR_ID, true, false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  void setForceAndValidatorHasNoMoreRightIsNotPossible() {
    assertThrows(IllegalArgumentException.class, () -> kmeliaValidation.forceValidation()
        .validatorHasNoMoreRight()
        .validate(createPublication(TO_VALIDATE_STATUS)));
  }

  @Test
  void validatePublicationValidationIsNotRequiredByNoMoreRightUserShouldNotPerformValidation() {
    PublicationDetail publication = createPublication(DRAFT_STATUS);
    kmeliaValidation.validatorHasNoMoreRight()
        .validate(publication);
    verifyNoInteractions(kmeliaService);
  }

  @Test
  void validateAlreadyValidatedPublicationByNoMoreRightShouldNotPerformValidation() {
    PublicationDetail publication = createPublication(VALID_STATUS);
    kmeliaValidation.validatorHasNoMoreRight()
        .validate(publication);
    verifyNoInteractions(kmeliaService);
  }

  @Test
  void validatePublicationValidationIsRequiredByNoMoreRightShouldPerformValidation() {
    PublicationDetail publication = createPublication(TO_VALIDATE_STATUS);
    kmeliaValidation.validatorHasNoMoreRight()
        .validate(publication);
    verify(kmeliaService, times(1)).validatePublication(publication.getPK(), VALIDATOR_ID, false,
        true);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  void validateSamePublicationValidationIsRequiredByNoMoreRightShouldPerformOneTimeOnlyTheValidation() {
    PublicationDetail publication = createPublication(TO_VALIDATE_STATUS);
    kmeliaValidation.validatorHasNoMoreRight()
        .validate(Arrays.asList(publication, publication, publication, publication));
    kmeliaValidation.validate(publication);
    kmeliaValidation.validate(publication);
    verify(kmeliaService, times(1)).validatePublication(publication.getPK(), VALIDATOR_ID, false,
        true);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  void validateClonedPublicationValidationIsRequiredByNoMoreRightShouldPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications =
        createClonedPublication(TO_VALIDATE_STATUS);
    PublicationDetail orig = publications.getLeft();
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.validatorHasNoMoreRight()
        .validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verify(kmeliaService, times(1)).validatePublication(orig.getPK(), VALIDATOR_ID, false, true);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  void validateClonedPublicationValidationIsNotRequiredByNoMoreRightShouldNotPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications = createClonedPublication(DRAFT_STATUS);
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.validatorHasNoMoreRight()
        .validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verifyNoMoreInteractions(kmeliaService);
  }

  private Pair<PublicationDetail, PublicationDetail> createClonedPublication(String status) {
    PublicationDetail orig = createPublication(VALID_STATUS);
    PublicationDetail clone = createPublication(status);
    orig.setCloneStatus(status);
    orig.setCloneId(clone.getId());
    clone.setCloneId(orig.getId());
    return Pair.of(orig, clone);
  }

  private PublicationDetail createPublication(String status) {
    PublicationPK pk = new PublicationPK(String.valueOf(counter++), INSTANCE_ID);
    PublicationDetail publication = builder().setPk(pk)
        .build();
    publication.setStatus(status);
    return publication;
  }
}