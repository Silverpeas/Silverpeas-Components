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
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class WikiInstanciatorTest extends AbstractTestDao {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  public WikiInstanciatorTest() {
  }

  @Override
  protected String getDatasetFileName() {
    return "test-wiki-dao-dataset.xml";
  }

  @Override
  protected void setUp() throws Exception {
    Properties props = new Properties();
    props.load(this.getClass().getClassLoader().getResourceAsStream(
        "jndi.properties"));
    File jndiDir = new File(props.getProperty(Context.PROVIDER_URL).substring(8));
    jndiDir.mkdirs();
    super.setUp();
  }


  /**
   * Test of createPages method, of class WikiInstanciator.
   */
  @org.junit.Test
  public void testCreatePages() throws Exception {
    System.out.println("createPages");
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

    Collection  uncompressedFiles = FileUtils.listFiles(directory, new String[]{"txt"}, true);
    assertNotNull(uncompressedFiles.size());
    assertEquals(29, uncompressedFiles.size());

  }

}
