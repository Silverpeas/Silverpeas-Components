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

package com.stratelia.webactiv.yellowpages.model.beans;

public class Company {

    private CompanyPK pk;
    private String name;
    private String email;
    private String phone;
    private String fax;
    private String creationDate;
    private String creatorId;

    public Company(CompanyPK pk, String name, String email, String phone, String fax, String creationDate, String creatorId) {
        this.pk = pk;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.fax = fax;
        this.creationDate = creationDate;
        this.creatorId = creatorId;
    }

    public Company(String id, String name, String email, String phone, String fax, String creationDate, String creatorId) {
        this.pk = new CompanyPK(id);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.fax = fax;
        this.creationDate = creationDate;
        this.creatorId = creatorId;
    }

    public CompanyPK getPk() {
        return pk;
    }

    public void setPk(CompanyPK pk) {
        this.pk = pk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

}
