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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.almanach.control;

import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.silverpeas.attachment.model.SimpleDocument;

/**
 * Its an event detail dedicated to unit tests. As such, some methods are overriden.
 */
public class TestEventDetail extends EventDetail {
  private static final long serialVersionUID = -3667013459367403791L;

  public TestEventDetail(EventPK pk, String title, Date startDate, Date endDate) {
    super(pk, title, startDate, endDate);
  }

  @Override
  public Collection<SimpleDocument> getAttachments() {
    return new ArrayList<SimpleDocument>();
  }
}
