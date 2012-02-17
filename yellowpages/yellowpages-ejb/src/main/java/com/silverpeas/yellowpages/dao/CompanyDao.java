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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.yellowpages.dao;

import com.silverpeas.yellowpages.model.Company;
import com.silverpeas.yellowpages.model.GenericContact;
import com.silverpeas.yellowpages.model.GenericContactRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyDao extends JpaRepository<Company, Integer> {

    // TODO check this request...
    @Query("select comp FROM GenericContact gccontact, GenericContact gccompany, GenericContactRelation rel, Company comp " +
            "WHERE gccontact.contactId = :contactId " +
            "AND gccontact.contactType = " + GenericContact.TYPE_COMPANY + " " +
            "AND gccontact.genericContactId = rel.genericContactId " +
            "AND rel.enabled = " + GenericContactRelation.ENABLE_TRUE + " " +
            "AND rel.genericCompanyId = gccompany.companyId " +
            "AND gccompany.companyId = comp.companyId")
    List<Company> findCompanyListByContactId(@Param("contactId") int contactId);

    @Query("FROM Company comp " +
            "WHERE UPPER(comp.name) like UPPER(CONCAT(CONCAT('%',TRIM(:strPattern)),'%'))")
    List<Company> findCompanyListByPattern(@Param("strPattern") String searchPattern);

    @Query("select gccontact FROM GenericContact gccontact, GenericContact gccompany, GenericContactRelation rel " +
            "WHERE gccontact.genericContactId = rel.genericContactId " +
            "AND rel.genericCompanyId = gccompany.genericContactId " +
            "AND rel.enabled = " + GenericContactRelation.ENABLE_TRUE + " " +
            "AND gccompany.contactType = " + GenericContact.TYPE_COMPANY + " "  +
            "AND gccompany.companyId = :companyId")
    List<GenericContact> findContactListByCompanyId(@Param("companyId") int companyId);

    @Query("select comp FROM Company comp, GenericContact gccompany, GenericContactTopicRelation topicrel " +
            "WHERE :topicId = topicrel.nodeId " +
            "AND topicrel.genericContactId = gccompany.genericContactId " +
            "AND gccompany.companyId = comp.companyId")
    List<Company> findContactListByTopicId(@Param("topicId") int topicId);
}
