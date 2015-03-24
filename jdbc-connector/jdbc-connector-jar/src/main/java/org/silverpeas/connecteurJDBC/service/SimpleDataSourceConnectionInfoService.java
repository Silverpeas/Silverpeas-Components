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
package org.silverpeas.connecteurJDBC.service;

import org.silverpeas.connecteurJDBC.control.DataSourceConnectionInfoService;
import org.silverpeas.connecteurJDBC.model.DataSourceConnectionInfo;
import org.silverpeas.connecteurJDBC.model.DataSourceConnectionInfoRepository;
import org.silverpeas.connecteurJDBC.model.DataSourceDefinition;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Transactional
public class SimpleDataSourceConnectionInfoService implements DataSourceConnectionInfoService {

  private static final ResourceLocator dataSources =
      new ResourceLocator("org.silverpeas.connecteurJDBC.settings.dataSources", "");

  @Inject
  private DataSourceConnectionInfoRepository repository;

  @Override
  public List<DataSourceConnectionInfo> getConnectionInfoList(final String instanceId) {
    return repository.findByInstanceId(instanceId);
  }

  @Override
  public void removeConnectionInfo(final DataSourceConnectionInfo connectionInfo) {
    repository.delete(connectionInfo);
  }

  @Override
  public void removeConnectionInfoOfComponentInstance(final String componentInstanceId) {
    repository.deleteByInstanceId(componentInstanceId);
  }

  @Override
  public DataSourceConnectionInfo getConnectionInfo(final String id) {
    return repository.getById(id);
  }

  @Override
  public DataSourceConnectionInfo saveConnectionInfo(
      final DataSourceConnectionInfo connectionInfo) {
    return repository.save(connectionInfo);
  }

  @Override
  public List<DataSourceDefinition> getAllDataSourceDefinitions() {
    int count = dataSources.getInteger("dataSource.count", 0);
    List<DataSourceDefinition> dataSourceDefinitions = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      String jndiName = dataSources.getString("dataSource." + i + ".name");
      String description = dataSources.getString("dataSource." + i + ".description");
      if (StringUtil.isNotDefined(description)) {
        description = "";
      }
      dataSourceDefinitions.add(new DataSourceDefinition(jndiName, description));
    }
    return dataSourceDefinitions;
  }

}