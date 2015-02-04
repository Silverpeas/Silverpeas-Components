/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.delegatednews.service;

import com.silverpeas.delegatednews.dao.DelegatedNewsDaoTest;
import com.silverpeas.delegatednews.model.DelegatedNews;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.date.Period;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.ServiceProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Arquillian.class)
public class DelegatedNewsServiceTest {

  private static DelegatedNewsService service;

  public DelegatedNewsServiceTest() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "delegatednews-dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(DelegatedNewsServiceTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core");
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:node");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:tagcloud");
          warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:publication");
          warBuilder.addMavenDependencies("org.apache.tika:tika-core");
          warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
          warBuilder.addPackages(true, "com.silverpeas.delegatednews");
          warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
        }).build();
  }

  @Before
  public void generalSetUp() throws Exception {
    service = DelegatedNewsService.get();
  }

  @Test
  public void testAddDelegatedNews() throws Exception {
    String pubId = "4";
    String instanceId = "kmelia1";
    String contributorId = "1";
    PublicationDetail publi = new PublicationDetail();
    publi.setPk(new PublicationPK(pubId, instanceId));
    service.submitNews(pubId, publi, contributorId,
        Period.from(DateUtil.MINIMUM_DATE, DateUtil.MAXIMUM_DATE), contributorId);
  }

  @Test
  public void testGetDelegatedNews() throws Exception {
    int pubId = 1;
    DelegatedNews detail = service.getDelegatedNews(pubId);
    assertThat(detail, notNullValue());
    assertThat(detail.getInstanceId(), is("kmelia1"));
  }


  @Test
  public void testGetAllDelegatedNews() throws Exception {
    List<DelegatedNews> listDetail = service.getAllDelegatedNews();
    assertThat(listDetail, notNullValue());
    assertThat(listDetail.size(), is(3));
  }

  @Test
  public void testGetAllValidDelegatedNews() throws Exception {
    List<DelegatedNews> listDetail = service.getAllValidDelegatedNews();
    assertThat(listDetail, notNullValue());
    assertThat(listDetail.size(), is(2));
  }

  @Test
  public void testValidateDelegatedNews() throws Exception {
    int pubId = 1;
    String validatorId = "2";
    service.validateDelegatedNews(pubId, validatorId);
    DelegatedNews detail = service.getDelegatedNews(pubId);
    assertThat(detail, notNullValue());
    assertThat(detail.getPubId(), is(1));
  }

}
