package org.silverpeas.components.kmelia.model;

import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.core.admin.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class ValidatorsList extends ArrayList<String> {

  private int validationType;

  public ValidatorsList(int validationType) {
    this.validationType = validationType;
  }

  public ValidatorsList(List<String> userIds) {
    this.addAll(userIds);
  }

  public boolean isTargetedValidation() {
    return validationType == KmeliaHelper.VALIDATION_TARGET_1 ||
        validationType == KmeliaHelper.VALIDATION_TARGET_N;
  }

  public boolean isClassicValidation() {
    return validationType == KmeliaHelper.VALIDATION_CLASSIC;
  }

  public boolean isCollegiateValidation() {
    return validationType == KmeliaHelper.VALIDATION_COLLEGIATE;
  }

  public int getValidationType() {
    return validationType;
  }

  public String[] getUserIds() {
    return this.toArray(new String[0]);
  }

  public boolean isAtLeastOnceValidatorActive() {
    return !this.isEmpty();
  }

  public boolean isAtLeastOnceTargetedValidatorActive() {
    return isTargetedValidation() && isAtLeastOnceValidatorActive();
  }

  public boolean isValidationOperational() {
    return isAtLeastOnceValidatorActive();
  }

  public String getValidatorNames() {
    StringBuilder validatorNames = new StringBuilder();
    for (String valId : this) {
      if (validatorNames.length() > 0) {
        validatorNames.append(", ");
      }
      validatorNames.append(User.getById(valId).getDisplayedName());
    }
    return validatorNames.toString();
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}