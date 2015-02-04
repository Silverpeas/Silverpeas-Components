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
package com.silverpeas.delegatednews.dao;

import com.silverpeas.delegatednews.model.DelegatedNews;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.Transaction;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.ServiceProvider;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Arquillian.class)
public class DelegatedNewsDaoTest {

  private static DelegatedNewsRepository repo;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "delegatednews-dataset.xml");

  public DelegatedNewsDaoTest() {
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(DelegatedNewsDaoTest.class)
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
    repo = ServiceProvider.getService(DelegatedNewsRepository.class);
  }

  @Test
  public void testInsertDelegatedNews() throws Exception {
    Transaction.performInOne(() -> {
      Integer pubId = Integer.parseInt("4");
      String instanceId = "kmelia1";
      String contributorId = "1";
      DelegatedNews expectedDetail =
          new DelegatedNews(pubId, instanceId, contributorId, new Date(), new Date(), null);
      expectedDetail = repo.save(expectedDetail);
      DelegatedNews detail = repo.getById(Integer.toString(pubId));
      assertThat(detail, notNullValue());
      assertThat(detail.getPubId(), is(expectedDetail.getPubId()));
      assertThat(detail.getInstanceId(), is(expectedDetail.getInstanceId()));
      assertThat(detail.getContributorId(), is(expectedDetail.getContributorId()));
      return null;
    });
  }

  @Test
  public void testGetDelegatedNews() throws Exception {
    Integer pubId = Integer.parseInt("1");
    DelegatedNews detail = repo.getById(Integer.toString(pubId));
    assertThat(detail, notNullValue());

    pubId = Integer.parseInt("2");
    detail = repo.getById(Integer.toString(pubId));
    assertThat(detail, notNullValue());

    pubId = Integer.parseInt("3");
    detail = repo.getById(Integer.toString(pubId));
    assertThat(detail, notNullValue());
  }


  @Test
  public void testFindDelegatedNewsByStatus() throws Exception {
    String status = DelegatedNews.NEWS_VALID;
    List<DelegatedNews> listDetail = repo.findByStatus(status);
    assertThat(listDetail, notNullValue());
    assertThat(listDetail.size(), is(2));
    DelegatedNews detail = listDetail.get(0);
    assertThat(detail.getPubId(), is(3));
    detail = listDetail.get(1);
    assertThat(detail.getPubId(), is(2));

    status = DelegatedNews.NEWS_REFUSED;
    listDetail = repo.findByStatus(status);
    assertThat(listDetail, notNullValue());
    assertThat(listDetail.size(), is(0));
  }
}
