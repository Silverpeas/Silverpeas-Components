/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.components.mydb.service.impl;

import org.silverpeas.components.mydb.model.MyDBConnectionInfo;
import org.silverpeas.components.mydb.model.MyDBConnectionInfoRepository;
import org.silverpeas.components.mydb.model.DataSourceDefinition;
import org.silverpeas.components.mydb.service.MyDBConnectionInfoService;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Transactional
public class SimpleMyDBConnectionInfoService implements MyDBConnectionInfoService {

  private static final SettingBundle dataSources =
      ResourceLocator.getSettingBundle("org.silverpeas.mydb.settings.dataSources");

  @Inject
  private MyDBConnectionInfoRepository repository;

  @Override
  public List<MyDBConnectionInfo> getConnectionInfoList(final String instanceId) {
    return repository.findByInstanceId(instanceId);
  }

  @Override
  public void removeConnectionInfo(final MyDBConnectionInfo connectionInfo) {
    repository.delete(connectionInfo);
  }

  @Override
  public void removeConnectionInfoOfComponentInstance(final String componentInstanceId) {
    repository.deleteByInstanceId(componentInstanceId);
  }

  @Override
  public MyDBConnectionInfo getConnectionInfo(final String id) {
    return repository.getById(id);
  }

  @Override
  public MyDBConnectionInfo saveConnectionInfo(
      final MyDBConnectionInfo connectionInfo) {
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