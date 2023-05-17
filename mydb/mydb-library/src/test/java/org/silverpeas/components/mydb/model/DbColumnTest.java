/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.mydb.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;

import java.sql.Timestamp;
import java.sql.Types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class DbColumnTest {

  private static final String A_VALUE = "aValue";
  private static final String A_VALUE_AN_OTHER_VALUE = "aValue,anOtherValue";
  private static final String A_VALUE_AN_OTHER_VALUE_ESCAPED = "aValue\\,anOtherValue";
  private static final String A_VALUE_SO_ESCAPED_AN_OTHER_VALUE = "aValue,so\\, anOtherValue";
  private static final String SO_ESCAPED_AN_OTHER_VALUE = "so\\, anOtherValue";
  private static final String A_TIMESTAMP = "2019-07-01 12:04:11.566";
  private JdbcRequester.ColumnDescriptor descriptor;
  private DbColumn dbColumn;

  @BeforeEach
  void setup() {
    descriptor = mock(JdbcRequester.ColumnDescriptor.class);
    dbColumn = new DbColumn(descriptor);
  }

  @Test
  void getJdbcValueOfTextValue() {
    when(descriptor.getType()).thenReturn(Types.VARCHAR);
    Object jdbcValue = dbColumn.getJdbcValueOf(null);
    assertThat(jdbcValue, nullValue());
    jdbcValue = dbColumn.getJdbcValueOf("");
    assertThat(jdbcValue, is(""));
    jdbcValue = dbColumn.getJdbcValueOf(A_VALUE);
    assertThat(jdbcValue, is(A_VALUE));
    jdbcValue = dbColumn.getJdbcValueOf(A_VALUE_AN_OTHER_VALUE);
    assertThat(jdbcValue, instanceOf(Object[].class));
    assertThat((Object[]) jdbcValue, arrayContaining(A_VALUE, "anOtherValue"));
    jdbcValue = dbColumn.getJdbcValueOf(A_VALUE_AN_OTHER_VALUE_ESCAPED);
    assertThat(jdbcValue, is(A_VALUE_AN_OTHER_VALUE_ESCAPED));
    jdbcValue = dbColumn.getJdbcValueOf(A_VALUE_SO_ESCAPED_AN_OTHER_VALUE);
    assertThat(jdbcValue, instanceOf(Object[].class));
    assertThat((Object[]) jdbcValue, arrayContaining(A_VALUE, SO_ESCAPED_AN_OTHER_VALUE));
  }

  @Test
  void getJdbcValueOfNumberValue() {
    when(descriptor.getType()).thenReturn(Types.INTEGER);
    Object jdbcValue = dbColumn.getJdbcValueOf(null);
    assertThat(jdbcValue, nullValue());
    jdbcValue = dbColumn.getJdbcValueOf("");
    assertThat(jdbcValue, is(""));
    jdbcValue = dbColumn.getJdbcValueOf(A_VALUE);
    assertThat(jdbcValue, is(A_VALUE));
    jdbcValue = dbColumn.getJdbcValueOf("0");
    assertThat(jdbcValue, is(0));
    jdbcValue = dbColumn.getJdbcValueOf("0,2");
    assertThat(jdbcValue, instanceOf(Object[].class));
    assertThat((Object[]) jdbcValue, arrayContaining(0, 2));
    jdbcValue = dbColumn.getJdbcValueOf("0,2.678");
    assertThat(jdbcValue, is("0,2.678"));
    jdbcValue = dbColumn.getJdbcValueOf("0\\,2.678");
    assertThat(jdbcValue, is("0\\,2.678"));
  }

  @Test
  void getJdbcValueOfTimestampValue() {
    when(descriptor.getType()).thenReturn(Types.TIMESTAMP);
    Object jdbcValue = dbColumn.getJdbcValueOf(null);
    assertThat(jdbcValue, nullValue());
    jdbcValue = dbColumn.getJdbcValueOf("");
    assertThat(jdbcValue, is(""));
    jdbcValue = dbColumn.getJdbcValueOf(A_VALUE);
    assertThat(jdbcValue, is(A_VALUE));
    jdbcValue = dbColumn.getJdbcValueOf(A_TIMESTAMP);
    assertThat(jdbcValue, is(Timestamp.valueOf(A_TIMESTAMP)));
    jdbcValue = dbColumn.getJdbcValueOf(A_TIMESTAMP + "," + A_TIMESTAMP);
    assertThat(jdbcValue, instanceOf(Object[].class));
    assertThat((Object[]) jdbcValue, arrayContaining(Timestamp.valueOf(A_TIMESTAMP), Timestamp.valueOf(A_TIMESTAMP)));
    jdbcValue = dbColumn.getJdbcValueOf(A_TIMESTAMP + "\\," + A_TIMESTAMP);
    assertThat(jdbcValue, is(A_TIMESTAMP + "\\," + A_TIMESTAMP));
  }
}