package com.silverpeas.mydb.data.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date format tool class. Deals with following formats :<br>
 * - standard display : dd/mm/yyyy (fr) or mm/dd/yyyy (en)<br>
 * - SQL format : java.sql.Date or string yyyy-mm-dd<br>
 * - XmlForm format : yyyy/mm/dd
 */
public class DateFormatter {

  private static String SQL_PATTERN = "yyyy-MM-dd";
  private static String FORM_PATTERN = "yyyy/MM/dd";

  SimpleDateFormat sqlFormat;
  SimpleDateFormat stringFormat;
  SimpleDateFormat formStringFormat;

  public DateFormatter(String pattern) {
    sqlFormat = new SimpleDateFormat(SQL_PATTERN);
    formStringFormat = new SimpleDateFormat(FORM_PATTERN);
    stringFormat = new SimpleDateFormat(pattern);
  }

  /**
   * SQL format to HTML display : yyyy-mm-dd -> dd/mm/yyyy
   * 
   * @param sqlDate
   * @return
   * @throws ParseException
   */
  public String sqlToString(String sqlDate) throws ParseException {
    if (sqlDate == null || sqlDate.length() == 0) {
      return sqlDate;
    }
    Date date = sqlFormat.parse(sqlDate);
    return stringFormat.format(date);
  }

  /**
   * HTML display to SQL format : dd/mm/yyyy -> java.sql.Date
   * 
   * @param stringDate
   * @return
   * @throws ParseException
   */
  public java.sql.Date stringToSql(String stringDate) throws ParseException {
    if (stringDate == null || stringDate.length() == 0) {
      return null;
    }
    Date date = stringFormat.parse(stringDate);
    return new java.sql.Date(date.getTime());
  }

  /**
   * HTML display to XML form format : dd/mm/yyyy -> yyyy/mm/dd
   * 
   * @param stringDate
   * @return
   * @throws ParseException
   */
  public String stringToFormString(String stringDate) throws ParseException {
    if (stringDate == null || stringDate.length() == 0) {
      return null;
    }
    Date date = stringFormat.parse(stringDate);
    return formStringFormat.format(date);
  }

}
