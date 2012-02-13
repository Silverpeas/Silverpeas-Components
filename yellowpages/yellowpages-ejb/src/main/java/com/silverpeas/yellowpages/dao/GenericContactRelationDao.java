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

import com.silverpeas.yellowpages.model.GenericContactRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GenericContactRelationDao extends JpaRepository<GenericContactRelation, Integer> {

    @Query("FROM GenericContactRelation GCR " +
            "WHERE GCR.genericCompanyId = :genericCompanyId AND " +
            "GCR.relationType = " + GenericContactRelation.RELATION_TYPE_BELONGS_TO + " AND " +
            "GCR.enabled = " + GenericContactRelation.ENABLE_TRUE)
    List<GenericContactRelation> findByGenericCompanyId(@Param("genericCompanyId") int genericCompanyId);

     @Query("FROM GenericContactRelation GCR " +
            "WHERE GCR.genericContactId = :genericContactId AND " +
            "GCR.relationType = " + GenericContactRelation.RELATION_TYPE_BELONGS_TO + " AND " +
            "GCR.enabled = " + GenericContactRelation.ENABLE_TRUE)
    List<GenericContactRelation> findByGenericContactId(@Param("genericContactId") int genericContactId);

    @Query("FROM GenericContactRelation GCR " +
            "WHERE GCR.genericContactId = :genericContactId AND " +
            "GCR.relationType = " + GenericContactRelation.RELATION_TYPE_BELONGS_TO)
    List<GenericContactRelation> findAllByGenericContactId(@Param("genericContactId") int genericContactId);

    @Query("FROM GenericContactRelation GCR " +
           "WHERE GCR.genericContactId = :genericContactId AND " +
           "GCR.genericCompanyId = :genericCompanyId AND " +
           "GCR.relationType = " + GenericContactRelation.RELATION_TYPE_BELONGS_TO + " AND " +
           "GCR.enabled = " + GenericContactRelation.ENABLE_TRUE)
    GenericContactRelation findByGenericCompanyIdAndGenericContactId(@Param("genericCompanyId") int genericCompanyId, @Param("genericContactId") int genericContactId);
}

