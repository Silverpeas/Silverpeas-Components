/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * AlmanachPdfGenerator.java
 *
 */

package com.stratelia.webactiv.almanach.control;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.PdfWriter;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachException;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachRuntimeException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author squere
 * @version
 */
public class AlmanachPdfGenerator {

  public static final String PDF_MONTH_ALLDAYS = "PdfMonthAllDays";
  public static final String PDF_MONTH_EVENTSONLY = "PdfMonthEventsOnly";
  public static final String PDF_YEAR_EVENTSONLY = "PdfYearEventsOnly";

  static public void buildPdf(String name, AlmanachSessionController almanach,
      String mode) throws AlmanachRuntimeException {
    try {
      SilverTrace.info("almanach", "AlmanachPdfGenerator.buildPdf()",
          "root.MSG_GEN_ENTER_METHOD");

      String fileName = FileRepositoryManager.getTemporaryPath(almanach
          .getSpaceId(), almanach.getComponentId())
          + name;
      Document document = new Document(PageSize.A4, 50, 50, 50, 50);

      // we add some meta information to the document
      document.addAuthor(almanach.getSettings().getString("author", ""));
      document.addSubject(almanach.getSettings().getString("subject", ""));
      document.addCreationDate();

      PdfWriter.getInstance(document, new FileOutputStream(fileName));
      document.open();

      try {
        Calendar currentDay = Calendar.getInstance();
        currentDay.setTime(almanach.getCurrentDay());
        String sHeader = almanach.getString("events");
        if (mode.equals(PDF_MONTH_ALLDAYS) || mode.equals(PDF_MONTH_EVENTSONLY)) {
          sHeader +=
              " " + almanach.getString("GML.mois" + currentDay.get(Calendar.MONTH));
        }
        sHeader += " " + currentDay.get(Calendar.YEAR);
        HeaderFooter header = new HeaderFooter(new Phrase(sHeader), false);
        HeaderFooter footer = new HeaderFooter(new Phrase("Page "), true);
        footer.setAlignment(Element.ALIGN_CENTER);

        document.setHeader(header);
        document.setFooter(footer);

        createFirstPage(almanach, document);
        document.newPage();

        Font titleFont = new Font(Font.HELVETICA, 24, Font.NORMAL, new Color(
            255, 255, 255));
        Paragraph cTitle = new Paragraph(almanach.getString("Almanach")
            + " "
            + almanach.getString("GML.mois"
                + currentDay.get(Calendar.MONTH)) + " "
            + currentDay.get(Calendar.YEAR), titleFont);
        Chapter chapter = new Chapter(cTitle, 1);

        // Collection<EventDetail> events =
        // almanach.getListRecurrentEvent(mode.equals(PDF_YEAR_EVENTSONLY));
        AlmanachCalendarView almanachView;
        if (PDF_YEAR_EVENTSONLY.equals(mode)) {
          almanachView = almanach.getYearlyAlmanachCalendarView();
        } else {
          almanachView = almanach.getMonthlyAlmanachCalendarView();
        }

        List<DisplayableEventOccurrence> occurrences = almanachView.getEvents();
        generateAlmanach(chapter, almanach, occurrences, mode);

        document.add(chapter);
      } catch (Exception ex) {
        throw new AlmanachRuntimeException("PdfGenerator.generate",
            AlmanachRuntimeException.WARNING,
            "AlmanachRuntimeException.EX_PROBLEM_TO_GENERATE_PDF", ex);
      }

      document.close();
      SilverTrace.info("almanach", "AlmanachPdfGenerator.buildPdf()",
          "root.MSG_GEN_EXIT_METHOD");

    } catch (Exception e) {
      throw new AlmanachRuntimeException("PdfGenerator.generate",
          AlmanachRuntimeException.WARNING,
          "AlmanachRuntimeException.EX_PROBLEM_TO_GENERATE_PDF", e);
    }

  }

  private static void createFirstPage(AlmanachSessionController almanach,
      Document document) throws AlmanachException {
    try {
      Font masterFont = new Font(Font.HELVETICA, 40, Font.BOLD, new Color(0, 0,
          0));
      Paragraph masterTitle = new Paragraph("\n\n\n\n"
          + almanach.getComponentLabel(), masterFont);

      masterTitle.setAlignment(Element.ALIGN_CENTER);

      Font secondFont = new Font(Font.HELVETICA, 14, Font.NORMAL, new Color(0,
          0, 0));
      Paragraph secondTitle = new Paragraph(almanach.getString("editeLe") + " "
          + DateUtil.getOutputDate(new Date(), almanach.getLanguage())
          + almanach.getString("Silverpeas"), secondFont);

      secondTitle.setAlignment(Element.ALIGN_CENTER);

      document.add(masterTitle);
      document.add(secondTitle);
    } catch (DocumentException e) {
      throw new AlmanachException("AlmanachPdfGenerator.createFirstPage()",
          SilverpeasException.ERROR, "almanach.EX_CANT_CREATE_FIRST_PAGE", e);
    }
  }

  private static void generateAlmanach(Chapter chapter,
      AlmanachSessionController almanach, List<DisplayableEventOccurrence> occurrences,
      String mode) throws AlmanachException {

    boolean monthScope =
        AlmanachPdfGenerator.PDF_MONTH_EVENTSONLY.equals(mode) ||
            AlmanachPdfGenerator.PDF_MONTH_ALLDAYS.equals(mode);
    boolean yearScope = AlmanachPdfGenerator.PDF_YEAR_EVENTSONLY.equals(mode);

    int currentDay = -1;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(almanach.getCurrentDay());
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    int currentMonth = calendar.get(Calendar.MONTH);
    int currentYear = calendar.get(Calendar.YEAR);

    if (yearScope) {
      // start from begin of current year
      calendar.set(Calendar.MONTH, 0);
    }

    // for each day of the current month
    while ((monthScope && currentMonth == calendar.get(Calendar.MONTH)) ||
        (yearScope && currentYear == calendar.get(Calendar.YEAR))) {
      Section section = null;
      if (AlmanachPdfGenerator.PDF_MONTH_ALLDAYS.equals(mode)) {
        section = chapter.addSection(generateParagraph(calendar, almanach), 0);
      }

      Font titleTextFont = new Font(Font.BOLD, 12, Font.SYMBOL, new Color(0, 0,
          0));

      // get the events of the current day
      for (DisplayableEventOccurrence occurrence : occurrences) {
        EventDetail event = occurrence.getEventDetail();
        String theDay = DateUtil.date2SQLDate(calendar.getTime());
        String startDay = DateUtil.date2SQLDate(occurrence.getStartDate().asDate());
        String startHour = event.getStartHour();
        String endHour = event.getEndHour();

        if (startDay.compareTo(theDay) > 0) {
          continue;
        }

        String endDay = startDay;
        if (event.getEndDate() != null) {
          endDay = DateUtil.date2SQLDate(occurrence.getEndDate().asDate());
        }

        if (endDay.compareTo(theDay) < 0) {
          continue;
        }

        if (calendar.get(Calendar.DAY_OF_MONTH) != currentDay) {
          if (AlmanachPdfGenerator.PDF_MONTH_EVENTSONLY.equals(mode) ||
              AlmanachPdfGenerator.PDF_YEAR_EVENTSONLY.equals(mode)) {
            section = chapter.addSection(generateParagraph(calendar, almanach), 0);
          }
          currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        }

        Font textFont;
        if (event.getPriority() == 0) {
          textFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0, 0, 0));
        } else {
          textFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 0, 0));
        }

        String eventTitle = event.getTitle();
        if (startDay.compareTo(theDay) == 0 && startHour != null
            && startHour.length() != 0) {
          eventTitle += " (" + startHour;
          if (endDay.compareTo(theDay) == 0 && endHour != null
              && endHour.length() != 0) {
            eventTitle += "-" + endHour;
          }
          eventTitle += ")";
        }

        section.add(new Paragraph(eventTitle, titleTextFont));
        if (StringUtil.isDefined(event.getPlace())) {
          section.add(new Paragraph(event.getPlace(), titleTextFont));
        }
        if (StringUtil.isDefined(event.getDescription(almanach.getLanguage()))) {
          section.add(new Paragraph(event
              .getDescription(almanach.getLanguage()), textFont));
        }
        section.add(new Paragraph("\n"));

      } // end for
      calendar.add(Calendar.DAY_OF_MONTH, 1);
    }
  }

  private static Paragraph generateParagraph(Calendar calendar,
      AlmanachSessionController almanach) {
    Font dateFont = new Font(Font.HELVETICA, 14, Font.NORMAL, new Color(0, 0,
        255));
    Paragraph dateSection = new Paragraph(almanach.getString("GML.jour"
        + calendar.get(Calendar.DAY_OF_WEEK))
        + " "
        + calendar.get(Calendar.DAY_OF_MONTH)
        + " "
        + almanach.getString("GML.mois" + calendar.get(Calendar.MONTH))
        + " "
        + calendar.get(Calendar.YEAR), dateFont);
    return dateSection;
  }

}
