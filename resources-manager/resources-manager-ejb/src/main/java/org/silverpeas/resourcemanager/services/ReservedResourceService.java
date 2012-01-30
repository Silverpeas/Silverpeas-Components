/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.resourcemanager.services;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.silverpeas.resourcemanager.model.ReservedResource;
import org.silverpeas.resourcemanager.model.ReservedResourcePk;
import org.silverpeas.resourcemanager.repository.ReservedResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ehugonnet
 */
@Named
@Service
@Transactional
public class ReservedResourceService {

  @Inject
  ReservedResourceRepository repository;

  public ReservedResource getReservedResource(int resourceId, int reservationId) {
    return repository.findOne(new ReservedResourcePk(resourceId, reservationId));
  }

  public void update(ReservedResource resource) {
    repository.saveAndFlush(resource);
  }
  
  public void delete(ReservedResource reservedResource) {
    repository.delete(reservedResource);
  }

  public List<ReservedResource> findAllReservedResourcesWithProblem(int currentReservationId,
      List<Integer> futureReservedResourceIds, String startPeriod, String endPeriod) {
    return repository.findAllReservedResourcesWithProblem(currentReservationId,
        futureReservedResourceIds, startPeriod, endPeriod);
  }
}
