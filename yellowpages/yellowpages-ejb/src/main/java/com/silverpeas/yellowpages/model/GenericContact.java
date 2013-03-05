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
@Table(name = "sc_contact_genericcontact")
public class GenericContact implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int genericContactId;
    @Column(name = "contactType")
    private int contactType;
    @Column(name = "contactId")
    private Integer contactId;
    @Column(name = "companyId")
    private Integer companyId;

    public static final int TYPE_CONTACT = 1;
    public static final int TYPE_COMPANY = 2;

    public GenericContact() {
    }

    public GenericContact(int contactType, Integer contactId, Integer companyId) {
        this.contactType = contactType;
        this.contactId = contactId;
        this.companyId = companyId;
    }

    public int getGenericContactId() {
        return genericContactId;
    }

    public void setGenericContactId(int genericContactId) {
        this.genericContactId = genericContactId;
    }

    public int getContactType() {
        return contactType;
    }

    public void setContactType(int contactType) {
        this.contactType = contactType;
    }

    public Integer getContactId() {
        return contactId;
    }

    public void setContactId(Integer contactId) {
        this.contactId = contactId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }
}
