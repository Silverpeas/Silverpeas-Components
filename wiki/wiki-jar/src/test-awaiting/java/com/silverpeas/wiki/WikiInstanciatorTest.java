/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.wiki;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.wiki.control.WikiPageDAO;
import com.silverpeas.wiki.control.model.PageDetail;
import java.io.File;
import java.util.Collection;
import java.util.Properties;
import javax.naming.Context;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 * @author ehugonnet
 */
public class WikiInstanciatorTest extends AbstractTestDao {

  @Rule
  private TemporaryFolder folder = new TemporaryFolder();

  public WikiInstanciatorTest() {
  }

  @Override
  protected String getDatasetFileName() {
    return "test-wiki-dao-dataset.xml";
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    folder.create();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    folder.delete();
  }


  /**
   * Test of createPages method, of class WikiInstanciator.
   */
  @Test
  public void testCreatePages() throws Exception {
    String instanceId = "wiki18";
    WikiPageDAO dao = mock(WikiPageDAO.class);
    WikiInstanciator instance = new WikiInstanciator(dao);
    File directory = folder.newFolder("Test de base");
    FileUtils.forceDeleteOnExit(directory);
    PageDetail aboutPage = new PageDetail();
    aboutPage.setInstanceId(instanceId);
    aboutPage.setPageName("About");
    PageDetail approvalRequiredForPageChanges = new PageDetail();
    approvalRequiredForPageChanges.setInstanceId(instanceId);
    approvalRequiredForPageChanges.setPageName("ApprovalRequiredForPageChanges");

    instance.createPages(directory, instanceId);
    assertNotNull(directory);
    verify(dao).createPage(aboutPage);
    verify(dao).createPage(approvalRequiredForPageChanges);

    Collection uncompressedFiles = FileUtils.listFiles(directory, new String[]{"txt"}, true);
    assertNotNull(uncompressedFiles.size());
    assertEquals(29, uncompressedFiles.size());

  }

  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }
}
