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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.model.ContributionValidation;
import org.silverpeas.core.contribution.model.SilverpeasContent;

import javax.persistence.Transient;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.util.StringUtil.EMPTY;

public class FormInstance implements SilverpeasContent {
  private static final long serialVersionUID = -3341454138112938275L;

  public static final int STATE_DRAFT = 0;
  public static final int STATE_UNREAD = 1;
  public static final int STATE_READ = 2;
  public static final int STATE_VALIDATED = 3;
  public static final int STATE_REFUSED = 4;
  public static final int STATE_ARCHIVED = 5;
  public static final int STATE_CANCELED = 6;

  private final FormInstanceValidations validations = new FormInstanceValidations();

  @Transient
  protected transient FormDetail form;
  private String id;
  private int formId = -1;
  private int state = -1;
  private String creatorId = null;
  private Date creationDate = null;
  private String instanceId = null;

  @Transient
  private boolean validationEnabled = false;

  @Transient
  private transient Form formWithData;

  @Override
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = Integer.toString(id);
  }

  public int getIdAsInt() {
    return Integer.parseInt(getId());
  }

  @Override
  public String getComponentInstanceId() {
    return instanceId;
  }

  /**
   * @return the formId
   */
  public int getFormId() {
    return formId;
  }

  /**
   * @param formId the formId to set
   */
  public void setFormId(int formId) {
    this.formId = formId;
  }

  /**
   * @return the state
   */
  public int getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(int state) {
    this.state = state;
  }

  /**
   * @return the creatorId
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * @param creatorId the creatorId to set
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * @return the creationDate
   */
  @Override
  public Date getCreationDate() {
    return creationDate != null ? new Date(creationDate.getTime()) : null;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate != null ? new Date(creationDate.getTime()) : null;
  }

  @Override
  public Date getLastUpdateDate() {
    return getValidationDate() != null ? getValidationDate() : getCreationDate();
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getContributionType() {
    return null;
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    String userId = user.getId();
    return getCreatorId().equals(userId) || form.isValidator(userId) ||
        isHierarchicalValidator(userId);
  }

  @Override
  public boolean canBeDeletedBy(final User user) {
    return ((isDraft() || getForm().isDeleteAfterRequestExchange()) && user.getId().equals(getCreatorId())) ||
        ((isCanceled() || isValidated() || isDenied() || isArchived()) && isOneOfLastValidators(user));
  }

  public String getHierarchicalValidator() {
    return form.getHvManager().getHierarchicalValidatorOf(getCreatorId());
  }

  public boolean isHierarchicalValidator(String userId) {
    return form.isHierarchicalValidation() && userId.equals(getHierarchicalValidator());
  }

  /**
   * Gets all the validations performed on the form instance.
   * @return a {@link FormInstanceValidations} instance.
   */
  public FormInstanceValidations getValidations() {
    return validations;
  }

  public List<FormInstanceValidation> getPreviousValidations() {
    return getValidationsSchema().stream()
        .filter(v -> !v.isPendingValidation())
        .collect(Collectors.toList());
  }

  public List<FormInstanceValidation> getValidationsSchema() {
    return Stream.of(FormInstanceValidationType.values())
        .map(v -> getValidations().getValidationOfType(v).orElseGet(() -> {
          final Function<FormInstance, Supplier<List<User>>> validatorSupplier = form
              .getPossibleRequestValidations().get(v);
          final FormInstanceValidation validation;
          if (validatorSupplier != null) {
            validation = new FormInstanceValidation(this);
            validation.setValidationType(v);
            validation.setStatus(ContributionStatus.PENDING_VALIDATION);
            // adding user who have to validate if it is unique
            final List<User> validators = validatorSupplier.apply(this).get();
            if (validators.size() == 1) {
              validation.setValidator(validators.get(0));
            }
          } else {
            validation = null;
          }
          return validation;
        }))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public FormInstanceValidation getPendingValidation() {
    return getValidationsSchema().stream()
        .filter(FormInstanceValidation::isPendingValidation)
        .findFirst()
        .orElse(null);
  }

  /**
   * @return the validatorId
   */
  public String getValidatorId() {
    return validations.getFinalValidation().map(v -> v.getValidator().getId()).orElse(EMPTY);
  }

  /**
   * @return the validationDate
   */
  public Date getValidationDate() {
    return validations.getFinalValidation().map(ContributionValidation::getDate).orElse(null);
  }

  /**
   * @return the comments
   */
  public String getComments() {
    return validations.getFinalValidation().map(ContributionValidation::getComment).orElse(EMPTY);
  }

  /**
   * @param instanceId the instanceId to set
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public FormDetail getForm() {
    return form;
  }

  public void setForm(final FormDetail form) {
    this.form = form;
  }

  public boolean isRead() {
    return getState() == STATE_READ;
  }

  public boolean isUnread() {
    return getState() == STATE_UNREAD;
  }

  public boolean isCanceled() {
    return getState() == STATE_CANCELED;
  }

  public boolean isValidated() {
    return getState() == STATE_VALIDATED;
  }

  public boolean isDenied() {
    return getState() == STATE_REFUSED;
  }

  public boolean isArchived() {
    return getState() == STATE_ARCHIVED;
  }

  public boolean canBeValidated() {
    return !isCanceled() && !isValidated() && !isDenied() && !isArchived();
  }

  public boolean canBeArchivedBy(final User user) {
    return (isCanceled() || isValidated() || isDenied()) && isOneOfLastValidators(user);
  }

  private boolean isOneOfLastValidators(final User user) {
    boolean isOneOfLastValidator;
    if (form.isFinalValidation()) {
      isOneOfLastValidator = form.isFinalValidator(user.getId());
      if (!isOneOfLastValidator && getValidations().getIntermediateValidation().filter(FormInstanceValidation::isRefused).isPresent()) {
        isOneOfLastValidator = form.isIntermediateValidator(user.getId());
      }
      if (!isOneOfLastValidator && getValidations().getHierarchicalValidation().filter(FormInstanceValidation::isRefused).isPresent()) {
        isOneOfLastValidator = getHierarchicalValidator().equals(user.getId());
      }
    } else if (form.isIntermediateValidation()) {
      isOneOfLastValidator = form.isIntermediateValidator(user.getId());
      if (!isOneOfLastValidator && getValidations().getHierarchicalValidation().filter(FormInstanceValidation::isRefused).isPresent()) {
        isOneOfLastValidator = getHierarchicalValidator().equals(user.getId());
      }
    } else if (form.isHierarchicalValidation()) {
      isOneOfLastValidator = getHierarchicalValidator().equals(user.getId());
    } else {
      isOneOfLastValidator = false;
    }
    return isOneOfLastValidator;
  }

  public boolean canBeCanceledBy(final User user) {
    return canBeValidated() && user.getId().equals(getCreatorId());
  }

  public boolean isDraft() {
    return getState() == STATE_DRAFT;
  }

  @Override
  public User getCreator() {
    return User.getById(getCreatorId());
  }

  @Override
  public User getLastUpdater() {
    return validations.getFinalValidation()
        .map(ContributionValidation::getValidator)
        .orElseGet(this::getCreator);
  }

  public User getValidator() {
    return User.getById(getValidatorId());
  }

  public FormPK getFormPK() {
    return new FormPK(Integer.toString(getFormId()), getComponentInstanceId());
  }

  public boolean isValidationEnabled() {
    return validationEnabled;
  }

  public void setValidationEnabled(final boolean validationEnabled) {
    this.validationEnabled = validationEnabled;
  }

  public Form getFormWithData() {
    return formWithData;
  }

  public void setFormWithData(final Form formWithData) {
    this.formWithData = formWithData;
  }

  public RequestPK getPK() {
    return new RequestPK(getId(), getComponentInstanceId());
  }

  public boolean isVoidable() {
    return isUnread() || isRead();
  }

  public List<String> getValidationsImages() {
    final List<FormInstanceValidation> validationForImages = canBeValidated()
        ? getValidationsSchema()
        : getValidations();
    return Stream.concat(Stream.of(isUnread() ? "vu.png" : "vu-ok.png"),
        validationForImages.stream().map(this::getValidationImage)).collect(Collectors.toList());
  }

  private String getValidationImage(FormInstanceValidation validation) {
    String prefix = "vh";
    if (validation.getValidationType().isIntermediate()) {
      prefix = "vi";
    } else if (validation.getValidationType().isFinal()) {
      prefix = "vf";
    }
    String stateSuffix = "";
    if (validation.isValidated()) {
      stateSuffix = "-ok";
    } else if (validation.isRefused()) {
      stateSuffix = "-nok";
    }
    return prefix + stateSuffix + ".png";
  }
}