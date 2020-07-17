/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.formsonline.model;

import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author silveryocha
 */
class FormDetailTest {

  @Test
  void exchangeFeature() throws IllegalAccessException {
    FormDetail form = new FormDetail();
    assertThat(form.getRequestExchangeReceiver().isPresent(), is(false));
    assertThat(form.isDeleteAfterRequestExchange(), is(false));
    // set delete after request exchange, but no exchange receiver
    form.setDeleteAfterRequestExchange(true);
    boolean deleteAfterRequestExchange = (boolean) readDeclaredField(form, "deleteAfterRequestExchange", true);
    assertThat(form.getRequestExchangeReceiver().isPresent(), is(false));
    assertThat(deleteAfterRequestExchange, is(true));
    assertThat(form.isDeleteAfterRequestExchange(), is(false));
    // setting now the receiver
    form.setRequestExchangeReceiver("toto@silverpeas.org");
    deleteAfterRequestExchange = (boolean) readDeclaredField(form, "deleteAfterRequestExchange", true);
    assertThat(form.getRequestExchangeReceiver().orElse(null), is("toto@silverpeas.org"));
    assertThat(deleteAfterRequestExchange, is(true));
    assertThat(form.isDeleteAfterRequestExchange(), is(true));
  }
}