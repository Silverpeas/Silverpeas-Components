/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.classifieds.model;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.classifieds.ClassifiedUtil;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.Date;

public class ClassifiedDetail implements SilverpeasContent {
  private static final long serialVersionUID = -355125879163002184L;

  private String title;
  private int classifiedId;
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

  public static final String DRAFT = "Draft";
  public static final String VALID = "Valid";
  public static final String TO_VALIDATE = "ToValidate";
  public static final String REFUSED = "Unvalidate";
  public static final String UNPUBLISHED = "Unpublished";

  public ClassifiedDetail() {
  }

  public ClassifiedDetail(int classifiedId) {
    this.classifiedId = classifiedId;
  }

  public ClassifiedDetail(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  @Override
  public String getURL() {
    return ClassifiedUtil.getClassifiedUrl(this);
  }

  @Override
  public UserDetail getCreator() {
    OrganizationController controller = new OrganizationController();
    return controller.getUserDetail(creatorId);
  }

  @Override
  public String getId() {
    return String.valueOf(classifiedId);
  }

  @Override
  public String getComponentInstanceId() {
    return instanceId;
  }
}
