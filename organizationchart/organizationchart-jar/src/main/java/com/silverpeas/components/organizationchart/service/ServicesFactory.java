/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.components.organizationchart.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ServicesFactory implements ApplicationContextAware {

  public static final String LDAP_ORGANIZATION_CHART_SERVICE = "organizationChartLDAPService";
  public static final String GROUP_ORGANIZATION_CHART_SERVICE = "organizationChartGroupService";

  private ApplicationContext context;
  private static final ServicesFactory instance = new ServicesFactory();

  @Override
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    this.context = ctx;
  }

  private ServicesFactory() {
  }

  public <T> T getBean(String name, Class<T> requiredType) {
    if (context == null) {
      return null;
    }
    return context.getBean(name, requiredType);
  }

  protected static ServicesFactory getInstance() {
    return ServicesFactory.instance;
  }

  public static OrganizationChartService getOrganizationChartService(
      OrganizationChartLDAPConfiguration config) {
    OrganizationChartService service = getInstance().getBean(LDAP_ORGANIZATION_CHART_SERVICE,
        OrganizationChartService.class);
    service.configure(config);
    return service;
  }

  public static OrganizationChartService getOrganizationChartService(
      OrganizationChartConfiguration config) {
    OrganizationChartService service = instance.getBean(
        GROUP_ORGANIZATION_CHART_SERVICE, OrganizationChartService.class);
    service.configure(config);
    return service;
  }
}
