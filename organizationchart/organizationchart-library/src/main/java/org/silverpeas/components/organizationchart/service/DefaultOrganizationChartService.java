package org.silverpeas.components.organizationchart.service;

import org.silverpeas.components.organizationchart.model.OrganizationalChart;
import org.silverpeas.components.organizationchart.model.OrganizationalChartType;
import org.silverpeas.core.annotation.Service;

/**
 * @author Yohann Chastagnier
 */
@Service
class DefaultOrganizationChartService implements OrganizationChartService {

  @SuppressWarnings("UnnecessaryLocalVariable")
  @Override
  public OrganizationalChart getOrganizationChart(
      final AbstractOrganizationChartConfiguration config, final String base,
      final OrganizationalChartType type) {
    final OrganizationalChart organizationalChart;
    if (config instanceof LdapOrganizationChartConfiguration) {

      // LDAP case

      String baseOU = base;
      organizationalChart =
          LdapOrganizationChartBuilder.from((LdapOrganizationChartConfiguration) config)
              .buildFor(baseOU, type);

    } else if (config instanceof GroupOrganizationChartConfiguration) {

      // GROUP case

      String groupId = base;
      organizationalChart =
          GroupOrganizationChartBuilder.from((GroupOrganizationChartConfiguration) config)
              .buildFor(groupId, type);

    } else {
      throw new UnsupportedOperationException();
    }
    return organizationalChart;
  }
}
