/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.test;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

/**
 * Builder of the archives to deploy for the integration tests on the persistence of the FAQs.
 */
public class QuestionReplyTestContext {

  private static final String RESOURCES = "/org/silverpeas/components/questionreply/";

  private QuestionReplyTestContext() {
  }

  /**
   * Builds the archive to deploy for the specified test class: the model and the repositories of
   * the FAQs with the persistence of Silverpeas.
   * @param test the class of the integration test.
   * @return the archive to deploy.
   */
  public static WebArchive warForTestClass(Class<?> test) {
    return BasicWarBuilder.onWarForTestClass(test)
        .testFocusedOn(warBuilder -> warBuilder
            .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
            .addPackages(true, "org.silverpeas.components.questionreply.model")
            .addPackages(true, "org.silverpeas.components.questionreply.repository")
            .addClasses(QuestionReplyTestContext.class)
            .addAsResource("org/silverpeas"))
        .build();
  }

  /**
   * Builds the archive to deploy for the specified test class on the services of the FAQs: the
   * model, the repositories and the services of the FAQs with the persistence of Silverpeas and
   * the PdC.
   * @param test the class of the integration test.
   * @return the archive to deploy.
   */
  public static WebArchive warForServiceTestClass(Class<?> test) {
    return BasicWarBuilder.onWarForTestClass(test)
        .testFocusedOn(warBuilder -> warBuilder
            .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
            .addMavenDependencies("org.silverpeas.core.services:silverpeas-core-pdc")
            .addPackages(true, "org.silverpeas.components.questionreply.model")
            .addPackages(true, "org.silverpeas.components.questionreply.repository")
            .addPackages(true, "org.silverpeas.components.questionreply.service")
            .addPackages(true, "org.silverpeas.components.questionreply.index")
            .addClasses(QuestionReplyException.class, QuestionReplyTestContext.class)
            .addAsResource("org/silverpeas"))
        .build();
  }

  /**
   * Creates the tables of the FAQs and loads two FAQs: questionReply2 and questionReply5.
   * @return the rule setting up the database.
   */
  public static DbSetupRule twoFaqs() {
    return DbSetupRule.createTablesFrom(RESOURCES + "create-database.sql")
        .loadInitialDataSetFrom(RESOURCES + "questionreply-dataset.sql");
  }
}
