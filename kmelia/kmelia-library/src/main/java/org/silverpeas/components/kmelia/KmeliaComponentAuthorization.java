/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.kmelia;

import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.util.MapUtil;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.silverpeas.core.util.StringUtil.isDefined;

@Named
public class KmeliaComponentAuthorization implements ComponentAuthorization {

  private static final String PUBLICATION_TYPE = "Publication";
  private static final String NODE_TYPE = "Node";
  private static final String ATTACHMENT_TYPE = "Attachment";
  private static final String VERSION_TYPE = "Version";

  private KmeliaComponentAuthorization() {
    // Instantiated by IoC only.
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("kmelia");
  }

  @Override
  public <T> Stream<T> filter(final Collection<T> resources,
      final Function<T, ComponentResourceReference> converter, final String userId,
      final AccessControlOperation... operations) {
    final Map<PublicationPK, Set<ComponentResourceReference>> pubPks = new HashMap<>(resources.size());
    final Map<NodePK, Set<ComponentResourceReference>> nodePks = new HashMap<>(resources.size());
    final Map<String, Set<ComponentResourceReference>> instanceIds = new HashMap<>(resources.size());
    final Set<ComponentResourceReference> authorized = new HashSet<>(resources.size());
    resources.forEach(r -> {
      final ComponentResourceReference resourceRef = converter.apply(r);
      final String resourceType = resourceRef.getType();
        if (isHandledKmeliaResourceType(resourceType)) {
          if (StringUtil.isLong(resourceRef.getLocalId())) {
            MapUtil.putAddSet(pubPks,
                new PublicationPK(resourceRef.getLocalId(), resourceRef.getInstanceId()),
                resourceRef);
          }
        } else if (NODE_TYPE.equalsIgnoreCase(resourceType)) {
          MapUtil
              .putAddSet(nodePks, new NodePK(resourceRef.getLocalId(), resourceRef.getInstanceId()),
                  resourceRef);
        } else if (isDefined(resourceRef.getInstanceId())) {
          MapUtil.putAddSet(instanceIds, resourceRef.getInstanceId(), resourceRef);
        } else {
          authorized.add(resourceRef);
        }
    });
    PublicationAccessControl.get().filterAuthorizedByUser(pubPks.keySet(), userId, AccessControlContext
        .init().onOperationsOf(operations))
        .forEach(p -> authorized.addAll(pubPks.get(p)));
    NodeAccessControl.get().filterAuthorizedByUser(nodePks.keySet(), userId, AccessControlContext
        .init().onOperationsOf(operations))
        .forEach(p -> authorized.addAll(nodePks.get(p)));
    ComponentAccessControl.get().filterAuthorizedByUser(instanceIds.keySet(), userId, AccessControlContext
        .init().onOperationsOf(operations))
        .forEach(p -> authorized.addAll(instanceIds.get(p)));
    return resources.stream().filter(r -> authorized.contains(converter.apply(r)));
  }

  private boolean isHandledKmeliaResourceType(String objectType) {
    return objectType != null && (PUBLICATION_TYPE.equalsIgnoreCase(objectType)
        || objectType.startsWith(ATTACHMENT_TYPE) || objectType.startsWith(VERSION_TYPE));
  }
}
