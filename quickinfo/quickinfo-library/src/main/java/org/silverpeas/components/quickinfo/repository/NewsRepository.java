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

package org.silverpeas.components.quickinfo.repository;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.persistence.model.identifier.UuidIdentifier;
import org.silverpeas.persistence.repository.jpa.NamedParameters;
import org.silverpeas.persistence.repository.jpa.SilverpeasJpaEntityManager;

import java.util.List;

public class NewsRepository extends SilverpeasJpaEntityManager<News, UuidIdentifier> {

  public List<News> getByComponentId(String componentId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("componentInstanceId", componentId);
    return findByNamedQuery("newsFromComponentInstance", parameters);
  }

  public News getByForeignId(String foreignId) {
    NamedParameters parameters = newNamedParameters();
    parameters.add("foreignId", foreignId);
    return findByNamedQuery("newsByForeignId", parameters).get(0);
  }

}
