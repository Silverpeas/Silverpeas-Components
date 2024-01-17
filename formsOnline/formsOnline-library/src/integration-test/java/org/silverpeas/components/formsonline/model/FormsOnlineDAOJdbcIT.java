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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.formsonline.model;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.util.SilverpeasList;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.components.formsonline.model.FormInstanceValidationType.*;
import static org.silverpeas.components.formsonline.model.RequestCriteria.QUERY_ORDER_BY.CREATION_DATE_ASC;
import static org.silverpeas.components.formsonline.model.RequestCriteria.QUERY_ORDER_BY.ID_ASC;
import static org.silverpeas.components.formsonline.model.RequestValidationCriteria.withValidatorId;
import static org.silverpeas.core.contribution.ContributionStatus.VALIDATED;
import static org.silverpeas.core.util.CollectionUtil.asSet;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class FormsOnlineDAOJdbcIT extends AbstractFormsOnlineIT {

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(FormsOnlineDAOJdbcIT.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
          warBuilder.addPackages(true, "org.silverpeas.components.formsonline");
        }).build();
  }

  @Inject
  private FormsOnlineDAOJdbc dao;

  /**
   * Test of createForm method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testCreateForm() throws Exception {
    FormDetail formDetail = new FormDetail();
    formDetail.setInstanceId("formsOnline15");
    formDetail.setCreatorId("0");
    formDetail.setName("Demande de construction de salle...");
    formDetail.setState(FormInstance.STATE_UNREAD);
    formDetail.setTitle("Titre");
    formDetail.setXmlFormName("descriptif_salle.xml");
    FormDetail result = dao.createForm(formDetail);
    assertThat(result, sameInstance(formDetail));
    assertThat(result.getId(), is(1002));
    result = dao.getForm(formDetail.getPK());
    assertThat(result, not(sameInstance(formDetail)));
    assertThat(result.getName(), is("Demande de construction de salle..."));
    assertThat(result.getState(), is(FormInstance.STATE_UNREAD));
    assertThat(result.getTitle(), is("Titre"));
    assertThat(result.getXmlFormName(), is("descriptif_salle.xml"));
    assertThat(result.isHierarchicalValidation(), is(false));
    assertThat(result.getRequestExchangeReceiver().isPresent(), is(false));
    assertThat(result.isDeleteAfterRequestExchange(), is(false));
  }

  @Test
  public void testCreateFormWithHierarchicalValidation() throws Exception {
    FormDetail formDetail = new FormDetail();
    formDetail.setInstanceId("formsOnline15");
    formDetail.setCreatorId("0");
    formDetail.setName("Demande de construction de salle...");
    formDetail.setState(FormInstance.STATE_UNREAD);
    formDetail.setTitle("Titre");
    formDetail.setXmlFormName("descriptif_salle.xml");
    formDetail.setHierarchicalValidation(true);
    dao.createForm(formDetail);
    final FormDetail result = dao.getForm(formDetail.getPK());
    assertThat(result, not(sameInstance(formDetail)));
    assertThat(result.isHierarchicalValidation(), is(true));
  }

  @Test
  public void testCreateFormByDeletingRequestAfterExchangeButNoReceiver() throws Exception {
    FormDetail formDetail = new FormDetail();
    formDetail.setInstanceId("formsOnline15");
    formDetail.setCreatorId("0");
    formDetail.setName("Demande de construction de salle...");
    formDetail.setState(FormInstance.STATE_DRAFT);
    formDetail.setTitle("Titre");
    formDetail.setXmlFormName("descriptif_salle.xml");
    formDetail.setDeleteAfterRequestExchange(true);
    dao.createForm(formDetail);
    final FormDetail result = dao.getForm(formDetail.getPK());
    assertThat(result, not(sameInstance(formDetail)));
    assertThat(result.getRequestExchangeReceiver().isPresent(), is(false));
    assertThat(result.isDeleteAfterRequestExchange(), is(false));
    boolean deleteAfterRequestExchange = (boolean) readDeclaredField(result, "deleteAfterRequestExchange", true);
    assertThat(deleteAfterRequestExchange, is(false));
  }

  @Test
  public void testCreateFormByDeletingRequestAfterExchangeWithReceiver() throws Exception {
    FormDetail formDetail = new FormDetail();
    formDetail.setInstanceId("formsOnline15");
    formDetail.setCreatorId("0");
    formDetail.setName("Demande de construction de salle...");
    formDetail.setState(FormInstance.STATE_UNREAD);
    formDetail.setTitle("Titre");
    formDetail.setXmlFormName("descriptif_salle.xml");
    formDetail.setRequestExchangeReceiver("toto@silverpeas.org");
    formDetail.setDeleteAfterRequestExchange(true);
    dao.createForm(formDetail);
    final FormDetail result = dao.getForm(formDetail.getPK());
    assertThat(result, not(sameInstance(formDetail)));
    assertThat(result.getRequestExchangeReceiver().orElse(null), is("toto@silverpeas.org"));
    assertThat(result.isDeleteAfterRequestExchange(), is(true));
  }

  /**
   * Test of getForm method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testGetForm() throws Exception {
    int formId = 1000;
    FormDetail expResult = getFormDetailExpectedResult();
    FormDetail result = dao.getForm(new FormPK(formId, DEFAULT_INSTANCE_ID));
    assertThat(expResult.equals(result), is(true));
  }

  private FormDetail getFormDetailExpectedResult() {
    FormDetail expResult = new FormDetail();
    expResult.setId(1000);
    expResult.setCreatorId("1");
    expResult.setCreationDate(Timestamp.valueOf("2012-01-09 00:00:00.0"));
    expResult.setDescription("Formulaire de description d'une salle");
    expResult.setInstanceId(DEFAULT_INSTANCE_ID);
    expResult.setName("Référencement des salles");
    expResult.setState(FormInstance.STATE_UNREAD);
    expResult.setTitle("Titre de mon formulaire en ligne");
    expResult.setXmlFormName("descriptif_salle.xml");
    return expResult;
  }

  /**
   * Test of findAllForms method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testFindAllForms() throws Exception {
    List<FormDetail> result = dao.findAllForms(DEFAULT_INSTANCE_ID,ORDERBY_DEFAULT);
    assertThat(result.size(), is(2));
  }

  @Test
  public void testDeleteForm() throws Exception {
    List<FormDetail> result = dao.findAllForms(DEFAULT_INSTANCE_ID,ORDERBY_DEFAULT);
    assertThat(result.size(), is(2));
    final FormPK formPk = result.iterator().next().getPK();
    Transaction.performInOne(() -> {
      FormsOnlineDAOJdbc formsOnlineDAOJdbc = dao;
      for (FormInstance formInstance : dao.getAllRequests(formPk)) {
        RequestPK pk = formInstance.getPK();
        formsOnlineDAOJdbc.deleteRequest(pk);
      }
      return dao.deleteForm(formPk);
    });
    result = dao.findAllForms(DEFAULT_INSTANCE_ID,ORDERBY_DEFAULT);
    assertThat(result.size(), is(1));
  }

  /**
   * Test of updateForm method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testUpdateForm() throws Exception {
    FormDetail curForm = getFormDetailExpectedResult();
    curForm.setDescription("Formulaire de description d'une salle UPDATED");
    curForm.setName("Référencement des salles");
    curForm.setState(FormInstance.STATE_READ);
    curForm.setTitle("Titre de mon formulaire en ligne UPDATED");
    curForm.setXmlFormName("descriptif_salle.xml UPDATED");
    curForm.setHierarchicalValidation(true);
    curForm.setRequestExchangeReceiver("toto@silverpeas.org");
    curForm.setDeleteAfterRequestExchange(true);
    dao.updateForm(curForm);
    FormDetail updatedForm = dao.getForm(curForm.getPK());
    assertThat(curForm.equals(updatedForm), is(true));
    assertThat(updatedForm.getRequestExchangeReceiver().orElse(null), is("toto@silverpeas.org"));
  }

  @Test
  public void testCreateRequest() throws FormsOnlineException {
    final FormPK formPK = new FormPK(1000, DEFAULT_INSTANCE_ID);
    assertThat(dao.getAllRequests(formPK), hasSize(1));
    final FormInstance request = new FormInstance();
    request.setFormId(1000);
    request.setState(FormInstance.STATE_UNREAD);
    request.setCreatorId(DEFAULT_CREATOR_IDS[0]);
    Timestamp oldDate = Timestamp.valueOf("2019-01-01 00:00:00.0");
    request.setCreationDate(oldDate);
    request.setInstanceId(DEFAULT_INSTANCE_ID);
    Transaction.performInOne(() -> dao.saveRequest(request));
    assertThat(dao.getAllRequests(formPK), hasSize(2));
    final FormInstance savedRequest = dao.getRequest(request.getPK());
    assertThat(savedRequest.getId(), is(request.getId()));
    assertThat(savedRequest.getFormId(), is(request.getFormId()));
    assertThat(savedRequest.getState(), is(request.getState()));
    assertThat(savedRequest.getCreatorId(), is(request.getCreatorId()));
    assertThat(savedRequest.getComponentInstanceId(), is(request.getComponentInstanceId()));
    assertThat(savedRequest.getCreationDate(), greaterThan(request.getCreationDate()));
    assertThat(savedRequest.getValidations(), empty());
  }

  @Test
  public void testCreateRequestWithValidations() throws FormsOnlineException {
    final FormPK formPK = new FormPK(1000, DEFAULT_INSTANCE_ID);
    assertThat(dao.getAllRequests(formPK), hasSize(1));
    final FormInstance request = new FormInstance();
    request.setFormId(1000);
    request.setState(FormInstance.STATE_UNREAD);
    request.setCreatorId(DEFAULT_CREATOR_IDS[0]);
    Timestamp oldDate = Timestamp.valueOf("2019-01-01 00:00:00.0");
    request.setCreationDate(oldDate);
    request.setInstanceId(DEFAULT_INSTANCE_ID);
    final FormInstanceValidation validation1 = new FormInstanceValidation(request);
    validation1.setValidationBy(VALIDATOR_ID_30);
    validation1.setDate(Timestamp.valueOf(H_DATE));
    validation1.setValidationType(INTERMEDIATE);
    validation1.setComment("Comment 1");
    final FormInstanceValidation validation2 = new FormInstanceValidation(request);
    validation2.setValidationBy(VALIDATOR_ID_31);
    validation2.setDate(Timestamp.valueOf(F_DATE));
    validation2.setValidationType(FINAL);
    validation2.setComment("Comment 2");
    request.getValidations().add(validation1);
    request.getValidations().add(validation2);
    Transaction.performInOne(() -> dao.saveRequest(request));
    assertThat(dao.getAllRequests(formPK), hasSize(2));
    final FormInstance savedRequest = dao.getRequest(request.getPK());
    assertThat(savedRequest.getId(), is(request.getId()));
    assertThat(savedRequest.getFormId(), is(request.getFormId()));
    assertThat(savedRequest.getState(), is(request.getState()));
    assertThat(savedRequest.getCreatorId(), is(request.getCreatorId()));
    assertThat(savedRequest.getComponentInstanceId(), is(request.getComponentInstanceId()));
    assertThat(savedRequest.getCreationDate(), greaterThan(request.getCreationDate()));
    assertThat(savedRequest.getValidations(), hasSize(2));
  }

  @Test
  public void testUpdateRequest() throws FormsOnlineException {
    final FormPK formPK = new FormPK(1000, DEFAULT_INSTANCE_ID);
    final SilverpeasList<FormInstance> allRequests = dao.getAllRequests(formPK);
    assertThat(allRequests, hasSize(1));
    final FormInstance request = allRequests.iterator().next();
    RequestPK requestPK = request.getPK();
    String creatorIdBeforeSave = request.getCreatorId();
    Date creationDateBeforeSave = request.getCreationDate();
    int stateBeforeUpdate = request.getState();
    request.setCreationDate(Timestamp.from(Instant.now()));
    request.setState(stateBeforeUpdate + 1);
    request.setCreatorId("78");
    Transaction.performInOne(() -> dao.saveRequest(request));
    assertThat(dao.getAllRequests(formPK), hasSize(1));
    final FormInstance savedRequest = dao.getRequest(requestPK);
    assertThat(savedRequest.getId(), is(request.getId()));
    assertThat(savedRequest.getFormId(), is(request.getFormId()));
    assertThat(savedRequest.getState(), not(is(stateBeforeUpdate)));
    assertThat(savedRequest.getCreatorId(), is(creatorIdBeforeSave));
    assertThat(savedRequest.getCreationDate(), is(creationDateBeforeSave));
    assertThat(savedRequest.getComponentInstanceId(), is(request.getComponentInstanceId()));
  }

  @Test
  public void testUpdateRequestValidation() throws FormsOnlineException {
    final FormPK formPK = new FormPK(1000, DEFAULT_INSTANCE_ID);
    final SilverpeasList<FormInstance> allRequests = dao.getAllRequests(formPK);
    assertThat(allRequests, hasSize(1));
    final FormInstance request = allRequests.iterator().next();
    final FormInstanceValidations requestValidations = request.getValidations();
    assertThat(requestValidations, hasSize(1));
    RequestPK requestPK = request.getPK();
    requestValidations.iterator().next().setComment("UPDATED COMMENT");
    final FormInstanceValidation validation1 = new FormInstanceValidation(request);
    validation1.setValidationBy(VALIDATOR_ID_31);
    validation1.setValidationType(INTERMEDIATE);
    validation1.setComment("INSERTED COMMENT");
    requestValidations.add(validation1);
    Transaction.performInOne(() -> dao.saveRequest(request));
    assertThat(dao.getAllRequests(formPK), hasSize(1));
    final FormInstance savedRequest = dao.getRequest(requestPK);
    final FormInstanceValidations savedValidations = savedRequest.getValidations();
    assertThat(savedValidations, hasSize(2));
    assertThat(savedValidations.stream()
        .map(FormInstanceValidation::getComment)
        .sorted()
        .collect(joining(",")), is("INSERTED COMMENT,UPDATED COMMENT"));
  }

  @Test
  public void testUpdateRequestState() throws FormsOnlineException {
    final FormPK formPK = new FormPK(1000, DEFAULT_INSTANCE_ID);
    final SilverpeasList<FormInstance> allRequests = dao.getAllRequests(formPK);
    assertThat(allRequests, hasSize(1));
    final FormInstance request = allRequests.iterator().next();
    RequestPK requestPK = request.getPK();
    int stateBeforeUpdate = request.getState();
    request.setState(stateBeforeUpdate + 1);
    dao.saveRequestState(request);
    assertThat(dao.getAllRequests(formPK), hasSize(1));
    final FormInstance savedRequest = dao.getRequest(requestPK);
    assertThat(savedRequest.getState(), not(is(stateBeforeUpdate)));
    assertThat(savedRequest.getState(), is(stateBeforeUpdate + 1));
  }

  @Test
  public void testGetReceivedRequests() throws Exception {
    final List<FormInstance> forms = dao
        .getReceivedRequests(dao.getForm(new FormPK(1000, DEFAULT_INSTANCE_ID)), null, null, null);
    assertThat(forms, hasSize(1));
    final FormInstance formInstance = forms.get(0);
    assertThat(formInstance.getFormId(), is(1000));
    assertThat(formInstance.getId(), is("20"));
    assertThat(formInstance.getState(), is(3));
    assertThat(formInstance.getComments(), is("Salle 18 disponible"));
    assertThat(formInstance.getValidatorId(), is(VALIDATOR_ID_29));
    assertThat(formInstance.getCreationDate(), is(Timestamp.valueOf("2012-01-28 10:50:34.26")));
    assertThat(formInstance.getValidationDate(), is(Timestamp.valueOf("2012-01-29 11:03:38.07")));
    assertThat(formInstance.getValidations(), hasSize(1));
    final FormInstanceValidation validation = formInstance.getValidations().iterator().next();
    assertThat(validation.getId(), is(5));
    assertThat(validation.getFormInstance().getId(), is(formInstance.getId()));
    assertThat(validation.getValidator().getId(), is(VALIDATOR_ID_29));
    assertThat(validation.getValidationType(), is(FormInstanceValidationType.FINAL));
    assertThat(validation.getStatus(), is(VALIDATED));
    assertThat(validation.isFollower(), is(false));
    assertThat(validation.getDate(), is(Timestamp.valueOf("2012-01-29 11:03:38.07")));
    assertThat(validation.getComment(), is("Salle 18 disponible"));
  }

  @Test
  public void testGetRequestsByCriteriaWithoutComponentInstanceCriteria() throws Exception {
    final List<FormInstance> requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds()
        .andCreatorId("1"));
    assertThat(requests, empty());
  }

  @Test
  public void testGetRequestsIntoContextOfValidator()
      throws Exception {
    createDefaultDynamicContextOfData(1, DEFAULT_VALIDATION_CYCLE.length);
    final Set<String> senderIds = asSet("1", "67");
    // VALIDATOR_ID_29
    SilverpeasList<FormInstance> requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_29, null)));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 10, 9, 8, 7, 6, 5, 4, 3, 2);
    assertThat(requests.iterator().next().getValidations(), hasSize(3));
    // VALIDATOR_ID_29 or no validation
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_29, null).orNoValidator()));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
    assertThat(requests.iterator().next().getValidations(), hasSize(3));
    // VALIDATOR_ID_29 or Sender ID
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_29, new MemoizedSupplier<>(() -> senderIds))
                               .orValidatorIsHierarchicalOne()));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
    assertThat(requests.iterator().next().getValidations(), hasSize(3));
    // VALIDATOR_ID_29 (only to validate by validator)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_29, null)
                               .andAvoidValidatedByValidator()));
    assertThat(requests, empty());
    // VALIDATOR_ID_29 or HIERARCHICAL validated  (only to validate by validator)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_29, null)
                               .andAvoidValidatedByValidator()
                               .orLastValidationType(HIERARCHICAL)));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 2);
    // VALIDATOR_ID_29 or HIERARCHICAL validated  (only to validate by validator) but skipping validation filtering!!!
    final RequestValidationCriteria validationCriteria =
        withValidatorId(VALIDATOR_ID_29, null)
            .andAvoidValidatedByValidator()
            .orLastValidationType(HIERARCHICAL);
    validationCriteria.skipValidationFiltering();
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(validationCriteria));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
    // VALIDATOR_ID_29 or INTERMEDIATE validated (validator is last one)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_29, null)
            .invert()
            .andAvoidValidatedByValidator()
            .orLastValidationType(HIERARCHICAL)));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 1);
    // VALIDATOR_ID_29 or HIERARCHICAL validated (still need validation)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_29, null)
                               .andStillNeedValidation()
                               .orLastValidationType(HIERARCHICAL)));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 2);
    // VALIDATOR_ID_30
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 11, 9, 8);
    // VALIDATOR_ID_30 or HIERARCHICAL validated
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null).orLastValidationType(HIERARCHICAL)));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 11, 9, 8, 2);
    // VALIDATOR_ID_30 (only to validate by validator)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)
                               .andAvoidValidatedByValidator()));
    assertThat(requests, empty());
    // VALIDATOR_ID_30 or HIERARCHICAL validated (only to validate by validator)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)
                               .andAvoidValidatedByValidator()
                               .orLastValidationType(HIERARCHICAL)));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 2);
    // VALIDATOR_ID_30 or HIERARCHICAL validated (still need validation)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)
                               .andStillNeedValidation()
                               .orLastValidationType(HIERARCHICAL)));
    assertThat(requests, empty());
    // VALIDATOR_ID_30 (still need validation)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)
            .andStillNeedValidation()));
    assertThat(requests, empty());
    // VALIDATOR_ID_30 or INTERMEDIATE validated
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)
                               .orLastValidationType(INTERMEDIATE)));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 11, 9, 8, 3);
    // VALIDATOR_ID_30 or HIERARCHICAL and INTERMEDIATE validated
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)
                               .orLastValidationType(INTERMEDIATE, HIERARCHICAL)));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 11, 9, 8, 3, 2);
    // VALIDATOR_ID_30 or HIERARCHICAL and INTERMEDIATE validated (only to validate by validator)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)
                               .orLastValidationType(INTERMEDIATE, HIERARCHICAL)
                               .andAvoidValidatedByValidator()));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 8, 3, 2);
    // VALIDATOR_ID_30 or HIERARCHICAL and INTERMEDIATE validated (still need validation)
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)
                               .orLastValidationType(INTERMEDIATE, HIERARCHICAL)
                               .andStillNeedValidation()));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 8);
    // VALIDATOR_ID_30 or no validator
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)
                               .orNoValidator()));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 11, 9, 8, 1);
    // VALIDATOR_ID_30 or Sender ID
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, new MemoizedSupplier<>(() -> senderIds))
                               .orValidatorIsHierarchicalOne()));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 12, 11, 9, 8, 1);
    // unknown validator
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId("69", null)));
    assertThat(requests, empty());
    // unknown validator or no validator
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId("69", null).orNoValidator()));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 1);
    // unknown validator or sender id
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId("69", new MemoizedSupplier<>(() -> senderIds))
                               .orValidatorIsHierarchicalOne()));
    assertContainsIdsWithOffsetAutomaticallyApplied(requests, 1);
  }

  @Test
  public void testGetRequestsByValidator() throws Exception {
    createDefaultDynamicContextOfData(1, DEFAULT_VALIDATION_CYCLE.length);
    // VALIDATOR_ID_29
    SilverpeasList<FormInstance> requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_29, null)));
    assertThat(requests, hasSize(DEFAULT_VALIDATION_CYCLE.length - 2));
    assertThat(requests.iterator().next().getValidations(), hasSize(3));
    // VALIDATOR_ID_30
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId(VALIDATOR_ID_30, null)));
    assertThat(requests, hasSize(4));
    // unknown validator
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andValidationCriteria(withValidatorId("69", null)));
    assertThat(requests, empty());
  }

  @Test
  public void testGetRequestsByIdCriteria() throws Exception {
    createDefaultDynamicContextOfData(1, DEFAULT_VALIDATION_CYCLE.length);
    // 100008
    SilverpeasList<FormInstance> requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andIds("100008"));
    assertThat(requests, hasSize(1));
    assertThat(requests.iterator().next().getValidations(), hasSize(2));
    // 100008 100012
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andIds("100008", "100012"));
    assertThat(requests, hasSize(2));
    assertThat(requests.iterator().next().getValidations(), hasSize(3));
  }

  @Test
  public void testGetRequestsByFormIdCriteria() throws Exception {
    createDefaultDynamicContextOfData(3, DEFAULT_VALIDATION_CYCLE.length * 10);
    // 100001
    SilverpeasList<FormInstance> requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andFormIds("100001"));
    assertThat(requests, hasSize(DEFAULT_VALIDATION_CYCLE.length * 10));
    // 100001 100003
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andFormIds("100001", "100003"));
    assertThat(requests, hasSize(DEFAULT_VALIDATION_CYCLE.length * 20));
  }

  @Test
  public void testGetRequestsByCreatorIdCriteria() throws Exception {
    final int nbRequestsPerForm = DEFAULT_VALIDATION_CYCLE.length * 10;
    createDefaultDynamicContextOfData(1, nbRequestsPerForm);
    // 1
    SilverpeasList<FormInstance> requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andCreatorId(DEFAULT_CREATOR_IDS[0]));
    assertThat(requests, hasSize(nbRequestsPerForm / 2));
    final FormInstance request1 = requests.iterator().next();
    // 2
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andCreatorId(DEFAULT_CREATOR_IDS[1]));
    assertThat(requests, hasSize(nbRequestsPerForm / 2));
    assertThat(requests.iterator().next().getId(), not(is(request1.getId())));
  }

  @Test
  public void testGetRequestsByStateCriteria() throws Exception {
    final int nbRequestsPerForm = DEFAULT_VALIDATION_CYCLE.length * 10;
    createDefaultDynamicContextOfData(1, nbRequestsPerForm);
    // UNREAD
    SilverpeasList<FormInstance> requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andStates(DEFAULT_FORM_STATES[0]));
    assertThat(requests, hasSize(nbRequestsPerForm / 2));
    // READ
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andStates(DEFAULT_FORM_STATES[1]));
    assertThat(requests, hasSize(nbRequestsPerForm / 2));
    // BOTH
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .andStates(DEFAULT_FORM_STATES[1], DEFAULT_FORM_STATES[0]));
    assertThat(requests, hasSize(nbRequestsPerForm));
  }

  @Test
  public void testGetRequestsByCriteriaIntoHugeContextOfData() throws Exception {
    final List<String> requestIds = createDefaultDynamicContextOfData(10, 1000);
    // all of MASSIVE_INSTANCE_ID and DEFAULT_INSTANCE_ID
    SilverpeasList<FormInstance> requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID, DEFAULT_INSTANCE_ID));
    assertThat(requests, hasSize(requestIds.size() + 2));
    // default order by
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID));
    assertThat(requests, hasSize(requestIds.size()));
    FormInstance request = requests.get(0);
    assertThat(request.getIdAsInt(), is(100000 + requestIds.size()));
    // default order by and pagination
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .paginateBy(new PaginationPage(2, 10)));
    assertThat(requests, hasSize(10));
    assertThat(requests.originalListSize(), is((long) requestIds.size()));
    request = requests.get(0);
    assertThat(request.getIdAsInt(), is(100000 + requestIds.size() - 10));
    // default order by SC and verifying validation coherency
    requests = dao.getRequestsByCriteria(RequestCriteria
        .onComponentInstanceIds(DYNAMIC_DATA_INSTANCE_ID)
        .orderBy(CREATION_DATE_ASC, ID_ASC));
    assertThat(requests, hasSize(requestIds.size()));
    request = requests.get(0);
    assertThat(request.getIdAsInt(), is(100001));
    for(int i = 0 ; i < DEFAULT_VALIDATION_CYCLE.length ; i++) {
      request = requests.get(i);
      final List<String> expectedList = new ArrayList<>();
      final Object[][] expectedValidations = (Object[][]) DEFAULT_VALIDATION_CYCLE[i];
      if (expectedValidations != null) {
        for (final Object[] expectedValidation : expectedValidations) {
          expectedList.add(of(expectedValidation).map(Object::toString).collect(joining(",")));
        }
      }
      final FormInstanceValidations actualValidations = request.getValidations();
      assertThat(actualValidations, hasSize(expectedList.size()));
      if (!actualValidations.isEmpty()) {
        final List<String> actualList = actualValidations.stream().map(
            v -> String.format("%s,%s,%s,%s,%s,%s", v.getValidator().getId(), v.getValidationType(),
                v.getStatus(), v.isFollower(), v.getDate(), v.getComment()))
            .collect(Collectors.toList());
        assertThat(actualList, containsInAnyOrder(expectedList.toArray()));
      }
    }
  }
}
