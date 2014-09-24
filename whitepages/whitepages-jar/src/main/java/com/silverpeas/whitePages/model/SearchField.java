/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.whitePages.model;

import org.silverpeas.util.StringUtil;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "sc_whitepages_searchfields")
@NamedQuery(name = "findByInstanceId", query = "from SearchField where instanceId = :instanceId")
public class SearchField implements Serializable {

  private static final long serialVersionUID = -2840717090501728479L;
  @Id
  private String id;
  private String instanceId;
  private String fieldId;
  @Transient
  private String label;

  public String getId() {
    return id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((fieldId == null) ? 0 : fieldId.hashCode());
    result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SearchField other = (SearchField) obj;
    return equals(other);
  }

  public boolean equals(SearchField obj) {
    if (instanceId.equals(obj.getInstanceId()) && fieldId.equals(obj.getFieldId())) {
      return true;
    } else {
      return false;
    }
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    if (StringUtil.isDefined(label)) {
      return label;
    }
    return getFieldName();
  }

  public String getFieldName() {
    return getFieldId().substring(4, getFieldId().length());
  }

  @PrePersist
  protected void generateId() {
    this.id = UUID.randomUUID().toString();
  }
}
