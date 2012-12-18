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
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class GenericContactRelationDaoTest extends SpringDbTest{

    @Inject private GenericContactRelationDao dao;

    @Test
    public void testSaveGenericContactRelation() throws Exception {

        // Genericcontact contact = "Obama"
        int genContactId = 217;
        // Genericcontact entreprise = "Boïng"
        int genCompanyId = 115;

        GenericContactRelation relation = new GenericContactRelation(genContactId, genCompanyId);
        GenericContactRelation result = dao.save(relation);
        assertNotNull(result);

        GenericContactRelation relationFromDb = dao.findByGenericCompanyIdAndGenericContactId(genCompanyId, genContactId);
        assertNotNull(relationFromDb);
        assertEquals(genCompanyId, relationFromDb.getGenericCompanyId());
        assertEquals(genContactId, relationFromDb.getGenericContactId());
    }

    @Test
    public void testDeleteGenericContactRelation() throws Exception {
        // Generic contact à supprimer
        int genContactId = 217;
        int genCompanyId = 110;

        GenericContactRelation relation = dao.findByGenericCompanyIdAndGenericContactId(genCompanyId, genContactId);
        dao.delete(relation);

        GenericContactRelation relationDeleted = dao.findByGenericCompanyIdAndGenericContactId(genCompanyId, genContactId);
        assertNull(relationDeleted);

        GenericContactRelation relationReallyDelete = dao.findOne(relation.getRelationId());
        assertNull(relationReallyDelete);
    }

    @Test
    public void testFindByGenericCompanyId() throws Exception {
        int genericCompanyId = 113;
        List<GenericContactRelation> liste = dao.findByGenericCompanyId(genericCompanyId);
        assertNotNull(liste);
        assertFalse(liste.isEmpty());
        assertEquals(1, liste.size());
    }

    @Test
    public void testFindByGenericContactId() throws Exception {
        int genericContactId = 217;
        List<GenericContactRelation> liste = dao.findByGenericContactId(genericContactId);
        assertNotNull(liste);
        assertEquals(3, liste.size());
    }

    @Test
    public void findByGenericCompanyIdAndGenericContactId() throws Exception {
        int genericContactId = 217;
        int genericCompanyId = 110;
        GenericContactRelation relation = dao.findByGenericCompanyIdAndGenericContactId(genericCompanyId, genericContactId);
        assertNotNull(relation);
    }

}
