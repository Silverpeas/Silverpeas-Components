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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.classifieds.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.model.SilverpeasContent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClassifiedDetail implements SilverpeasContent {
  private static final long serialVersionUID = -355125879163002184L;

  private String title;
  private int classifiedId;
  private String description;
  private Integer price = 0;
  private String instanceId;
  private String creatorId;
  private String creatorName;
  private String creatorEmail;
  private Date creationDate;
  private Date updateDate;
  private String status;
  private String validatorId;
  private String validatorName;
  private Date validateDate;
  private String searchValueId1;
  private String searchValueId2;
  private String searchValue1;
  private String searchValue2;
  private List<SimpleDocument> images = new ArrayList<>();

  public static final String DRAFT = "Draft";
  public static final String VALID = "Valid";
  public static final String TO_VALIDATE = "ToValidate";
  public static final String REFUSED = "Unvalidate";
  public static final String UNPUBLISHED = "Unpublished";

  public static final String TYPE = "Classified";

  public ClassifiedDetail() {
  }

  public ClassifiedDetail(int classifiedId) {
    this.classifiedId = classifiedId;
  }

  public ClassifiedDetail(String title, String description) {
    this.title = title;
    this.description = description;
  }

  @Override
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getPrice() {
    return this.price;
  }

  public void setPrice(Integer price) {
    this.price = price;
  }

  public int getClassifiedId() {
    return classifiedId;
  }

  public void setClassifiedId(int classifiedId) {
    this.classifiedId = classifiedId;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  public String getCreatorEmail() {
    return creatorEmail;
  }

  public void setCreatorEmail(String creatorEmail) {
    this.creatorEmail = creatorEmail;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getValidatorId() {
    return validatorId;
  }

  public void setValidatorId(String validatorId) {
    this.validatorId = validatorId;
  }

  public Date getValidateDate() {
    return validateDate;
  }

  public void setValidateDate(Date validateDate) {
    this.validateDate = validateDate;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getValidatorName() {
    return validatorName;
  }

  public void setValidatorName(String validatorName) {
    this.validatorName = validatorName;
  }

  @Override
  public Date getLastUpdateDate() {
    return getUpdateDate();
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  @Override
  public User getCreator() {
    return User.getById(creatorId);
  }

  @Override
  public User getLastUpdater() {
    return getCreator();
  }

  @Override
  public String getId() {
    return String.valueOf(classifiedId);
  }

  @Override
  public String getComponentInstanceId() {
    return getInstanceId();
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return TYPE;
  }

  public String getSearchValueId1() {
    return this.searchValueId1;
  }

  public void setSearchValueId1(String searchValueId1) {
    this.searchValueId1 = searchValueId1;
  }

  public String getSearchValueId2() {
    return this.searchValueId2;
  }

  public void setSearchValueId2(String searchValueId2) {
    this.searchValueId2 = searchValueId2;
  }

  public String getSearchValue1() {
    return this.searchValue1;
  }

  public void setSearchValue1(String searchValue1) {
    this.searchValue1 = searchValue1;
  }

  public String getSearchValue2() {
    return this.searchValue2;
  }

  public void setSearchValue2(String searchValue2) {
    this.searchValue2 = searchValue2;
  }

  public List<SimpleDocument> getImages() {
    return images;
  }

  public void setImages(List<SimpleDocument> images) {
    this.images = images;
  }

  public boolean isDraft() {
    return DRAFT.equals(getStatus());
  }

  public boolean isToValidate() {
    return TO_VALIDATE.equals(getStatus());
  }

  public boolean isValid() {
    return VALID.equals(getStatus());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ClassifiedDetail that = (ClassifiedDetail) o;

    if (classifiedId != that.classifiedId) {
      return false;
    }
    return instanceId.equals(that.instanceId);
  }

  @Override
  public int hashCode() {
    int result = classifiedId;
    result = 31 * result + instanceId.hashCode();
    return result;
  }
}
