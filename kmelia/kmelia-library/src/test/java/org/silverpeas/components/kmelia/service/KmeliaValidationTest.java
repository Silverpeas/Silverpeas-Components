/*
 * Copyright (C) 2000 - 2017 Silverpeas
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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.test.rule.CommonAPI4Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.silverpeas.core.contribution.publication.model.PublicationDetail.*;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaValidationTest {

  private static final String INSTANCE_ID = "instanceId";
  private static final String VALIDATOR_ID = "26";

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  private KmeliaValidation kmeliaValidation;
  private KmeliaService kmeliaService;
  private int counter = 0;

  @Before
  public void setup() {
    counter = 0;
    kmeliaService = commonAPI4Test.injectIntoMockedBeanContainer(mock(KmeliaService.class));
    kmeliaValidation = KmeliaValidation.by(VALIDATOR_ID);
  }

  @Test
  public void validatePublicationValidationIsNotRequiredShouldNotPerformValidation() {
    PublicationDetail publication = createPublication(DRAFT);
    kmeliaValidation.validate(publication);
    verifyZeroInteractions(kmeliaService);
  }

  @Test
  public void validateAlreadyValidatedPublicationShouldNotPerformValidation() {
    PublicationDetail publication = createPublication(VALID);
    kmeliaValidation.validate(publication);
    verifyZeroInteractions(kmeliaService);
  }

  @Test
  public void forceValidateAlreadyValidatedPublicationShouldPerformValidation() {
    PublicationDetail publication = createPublication(VALID);
    kmeliaValidation.forceValidation().validate(publication);
    verify(kmeliaService, times(1))
        .validatePublication(publication.getPK(), VALIDATOR_ID, true, false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  public void validatePublicationValidationIsRequiredShouldPerformValidation() {
    PublicationDetail publication = createPublication(TO_VALIDATE);
    kmeliaValidation.validate(publication);
    verify(kmeliaService, times(1))
        .validatePublication(publication.getPK(), VALIDATOR_ID, false, false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  public void validateSamePublicationValidationIsRequiredShouldPerformOneTimeOnlyTheValidation() {
    PublicationDetail publication = createPublication(TO_VALIDATE);
    kmeliaValidation.validate(Arrays.asList(publication, publication, publication, publication));
    kmeliaValidation.validate(publication);
    kmeliaValidation.validate(publication);
    verify(kmeliaService, times(1))
        .validatePublication(publication.getPK(), VALIDATOR_ID, false, false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  public void validateClonedPublicationValidationIsRequiredShouldPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications = createClonedPublication(TO_VALIDATE);
    PublicationDetail orig = publications.getLeft();
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verify(kmeliaService, times(1)).validatePublication(orig.getPK(), VALIDATOR_ID, false, false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  public void validateClonedPublicationValidationIsNotRequiredShouldNotPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications = createClonedPublication(DRAFT);
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  public void forceValidateClonedPublicationAlreadyValidatedShouldPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications = createClonedPublication(VALID);
    PublicationDetail orig = publications.getLeft();
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.forceValidation().validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verify(kmeliaService, times(1)).validatePublication(orig.getPK(), VALIDATOR_ID, true, false);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setForceAndValidatorHasNoMoreRightIsNotPossible() {
    kmeliaValidation.forceValidation().validatorHasNoMoreRight()
        .validate(createPublication(TO_VALIDATE));
  }

  @Test
  public void
  validatePublicationValidationIsNotRequiredByNoMoreRightUserShouldNotPerformValidation() {
    PublicationDetail publication = createPublication(DRAFT);
    kmeliaValidation.validatorHasNoMoreRight().validate(publication);
    verifyZeroInteractions(kmeliaService);
  }

  @Test
  public void validateAlreadyValidatedPublicationByNoMoreRightShouldNotPerformValidation() {
    PublicationDetail publication = createPublication(VALID);
    kmeliaValidation.validatorHasNoMoreRight().validate(publication);
    verifyZeroInteractions(kmeliaService);
  }

  @Test
  public void validatePublicationValidationIsRequiredByNoMoreRightShouldPerformValidation() {
    PublicationDetail publication = createPublication(TO_VALIDATE);
    kmeliaValidation.validatorHasNoMoreRight().validate(publication);
    verify(kmeliaService, times(1))
        .validatePublication(publication.getPK(), VALIDATOR_ID, false, true);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  public void
  validateSamePublicationValidationIsRequiredByNoMoreRightShouldPerformOneTimeOnlyTheValidation() {
    PublicationDetail publication = createPublication(TO_VALIDATE);
    kmeliaValidation.validatorHasNoMoreRight()
        .validate(Arrays.asList(publication, publication, publication, publication));
    kmeliaValidation.validate(publication);
    kmeliaValidation.validate(publication);
    verify(kmeliaService, times(1))
        .validatePublication(publication.getPK(), VALIDATOR_ID, false, true);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  public void validateClonedPublicationValidationIsRequiredByNoMoreRightShouldPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications = createClonedPublication(TO_VALIDATE);
    PublicationDetail orig = publications.getLeft();
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.validatorHasNoMoreRight().validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verify(kmeliaService, times(1)).validatePublication(orig.getPK(), VALIDATOR_ID, false, true);
    verifyNoMoreInteractions(kmeliaService);
  }

  @Test
  public void
  validateClonedPublicationValidationIsNotRequiredByNoMoreRightShouldNotPerformValidation() {
    Pair<PublicationDetail, PublicationDetail> publications = createClonedPublication(DRAFT);
    PublicationDetail clone = publications.getRight();
    when(kmeliaService.getPublicationDetail(clone.getPK())).thenReturn(clone);
    kmeliaValidation.validatorHasNoMoreRight().validate(clone);
    verify(kmeliaService, times(1)).getPublicationDetail(clone.getPK());
    verifyNoMoreInteractions(kmeliaService);
  }

  private Pair<PublicationDetail, PublicationDetail> createClonedPublication(String status) {
    PublicationDetail orig = createPublication(VALID);
    PublicationDetail clone = createPublication(status);
    orig.setCloneStatus(status);
    orig.setCloneId(clone.getId());
    clone.setCloneId(orig.getId());
    return Pair.of(orig, clone);
  }

  private PublicationDetail createPublication(String status) {
    PublicationDetail publication = new PublicationDetail();
    PublicationPK pk = new PublicationPK(String.valueOf(counter++), INSTANCE_ID);
    publication.setPk(pk);
    publication.setStatus(status);
    return publication;
  }
}