/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.processmanager;

import org.silverpeas.core.admin.component.model.ComponentInstPath;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.kernel.logging.Level;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.workflow.api.WorkflowException;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;

/**
 * This class allows to write reporting about reassignment processes.
 * <p>
 *  It is based upon the {@link SilverLogger} API in order to get the logging features.
 * </p>
 * <p>
 *  The log data can be retrieved into {@code $SILVERPEAS_HOME/log/silverpeas-workflow
 *  -reassignment.log} file.
 * </p>
 * <p>
 *   When getting a new instance of the report, a timestamp is generated. It is used into logged
 *   data when DEBUG level is set to the logger namespace
 *   {@code silverpeas.components .processmanager.reassignment}
 * </p>
 * @author silveryocha
 */
public class ReassignmentReport {

  private static final SilverLogger logger = SilverLogger.getLogger(
      "silverpeas.components.processmanager.reassignment");
  private static final String LANGUAGE = "en";

  private final String id;
  private final ProcessManagerSessionController controller;
  private final User incumbent;
  private final User substitute;
  private final User supervisor;
  private long start = 0L;

  public ReassignmentReport(final ProcessManagerSessionController controller,
      final String incumbentId, final String substituteId) {
    id = format("%s-%s", currentTimeMillis(), Thread.currentThread().getId());
    this.controller = controller;
    incumbent = User.getById(incumbentId);
    substitute = User.getById(substituteId);
    supervisor = controller.getUserDetail();
  }

  /**
   * Reports start of the reassignment processing.
   * <p>
   *   According to logger level, more or less data are written into log file.
   * </p>
   */
  public void start() {
    start = currentTimeMillis();
    logger.debug(() -> normalize("Starting reassignment"));
  }

  /**
   * @see #end(WorkflowException)
   */
  public void end() {
    end(null);
  }

  /**
   * Reports end of reassignment processing with error.
   * <p>
   *   Main log data are written.
   * </p>
   * <p>
   *   According to logger level, more or less data could be additionally written.
   * </p>
   * <p>
   *   If the given exception exists, the message of the error is written into logs.
   * </p>
   */
  public void end(final WorkflowException e) {
    logger.info(normalize(getMainLogData()));
    if (e != null) {
      logger.error(normalize(e.getMessage()));
    }
    logger.debug(() -> normalize(format("Ending reassignment in %s", getDurationFromStart())));
  }

  /**
   * Produces the main log data without taking care about logger level.
   * @return a string with main data.
   */
  private String getMainLogData() {
    final ComponentInstPath path = ComponentInstPath.getPath(controller.getComponentId());
    return format("%s (%s): incumbent %s (%s) -> substitute %s (%s) by supervisor %s (%s)",
        path.format(LANGUAGE, true), controller.getComponentId(), incumbent.getDisplayedName(),
        incumbent.getId(), substitute.getDisplayedName(), substitute.getId(),
        supervisor.getDisplayedName(), supervisor.getId());
  }

  /**
   * Applies the identifier if necessary.
   * @param logData the log data.
   * @return the normalized log data.
   */
  private String normalize(final String logData) {
    return logger.isLoggable(Level.DEBUG) ? format("[%s] %s", id, logData) : logData;
  }

  private String getDurationFromStart() {
    return formatDurationHMS(currentTimeMillis() - start);
  }
}
