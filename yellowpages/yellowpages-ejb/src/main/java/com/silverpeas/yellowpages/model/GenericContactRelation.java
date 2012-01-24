/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.yellowpages.model;

import javax.persistence.*;

@Entity
@Table(name = "sc_contact_genericcontact_rel")
public class GenericContactRelation implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int relationId;
    @Column(name = "genericContactId")
    private int genericContactId;
    @Column(name = "genericCompanyId")
    private int genericCompanyId;
    @Column(name = "relationType")
    private int relationType;
    @Column(name = "enabled")
    private int enabled;

    public static final int ENABLE_FALSE = 0;
    public static final int ENABLE_TRUE = 1;

    public static final int RELATION_TYPE_BELONGS_TO = 0;

    public GenericContactRelation() {
    }

    public GenericContactRelation(int genericContactId, int genericCompanyId, int relationType, int enabled) {
        super();
        this.genericContactId = genericContactId;
        this.genericCompanyId = genericCompanyId;
        this.relationType = relationType;
        this.enabled = enabled;
    }

    public int getGenericContactId() {
        return genericContactId;
    }

    public void setGenericContactId(int genericContactId) {
        this.genericContactId = genericContactId;
    }

    public int getGenericCompanyId() {
        return genericCompanyId;
    }

    public void setGenericCompanyId(int genericCompanyId) {
        this.genericCompanyId = genericCompanyId;
    }

    public int getRelationType() {
        return relationType;
    }

    public void setRelationType(int relationType) {
        this.relationType = relationType;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public int getRelationId() {
        return relationId;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }
}
