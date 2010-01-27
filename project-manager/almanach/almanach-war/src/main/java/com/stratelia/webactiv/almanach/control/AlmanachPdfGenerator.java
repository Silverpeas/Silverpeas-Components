/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

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
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachException;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachRuntimeException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * 
 * @author squere
 * @version
 */
public class AlmanachPdfGenerator {
  static public void buildPdf(String name, AlmanachSessionController almanach,
      boolean bCompleteMonth) throws AlmanachRuntimeException {
    try {
      SilverTrace.info("almanach", "AlmanachPdfGenerator.buildPdf()",
          "root.MSG_GEN_ENTER_METHOD");

      String fileName = FileRepositoryManager.getTemporaryPath(almanach
          .getSpaceId(), almanach.getComponentId())
          + name;
      Document document = new Document(PageSize.A4, 50, 50, 50, 50);

      // we add some meta information to the document
      document.addAuthor(SilverpeasSettings.readString(almanach.getSettings(),
          "author", ""));
      document.addSubject(SilverpeasSettings.readString(almanach.getSettings(),
          "subject", ""));
      document.addCreationDate();

      PdfWriter.getInstance(document, new FileOutputStream(fileName));
      document.open();

      try {
        HeaderFooter header = new HeaderFooter(new Phrase(almanach
            .getString("events")
            + " "
            + almanach.getString("mois"
                + almanach.getCurrentDay().get(Calendar.MONTH))
            + " "
            + almanach.getCurrentDay().get(Calendar.YEAR)), false);
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
            + almanach.getString("mois"
                + almanach.getCurrentDay().get(Calendar.MONTH)) + " "
            + almanach.getCurrentDay().get(Calendar.YEAR), titleFont);
        Chapter chapter = new Chapter(cTitle, 1);

        Collection events = almanach.getListRecurrentEvent();

        generateAlmanach(chapter, almanach, events, bCompleteMonth);

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
      AlmanachSessionController almanach, Collection events,
      boolean bCompleteMonth) throws AlmanachException {
    int currentDay = -1;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(almanach.getCurrentDay().getTime());
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    int currentMonth = calendar.get(Calendar.MONTH);

    // for each day of the current month
    while (currentMonth == calendar.get(Calendar.MONTH)) {
      Section section = null;
      if (bCompleteMonth) {
        section = chapter.addSection(generateParagraph(calendar, almanach), 0);
      }

      Font titleTextFont = new Font(Font.BOLD, 12, Font.SYMBOL, new Color(0, 0,
          0));

      // get the events of the current day
      EventDetail event = null;
      String theDay = null;
      String startDay = null;
      String endDay = null;
      String eventTitle = null;
      String startHour = null;
      String endHour = null;
      for (Iterator i = events.iterator(); i.hasNext();) {
        event = (EventDetail) i.next();
        theDay = DateUtil.date2SQLDate(calendar.getTime());
        startDay = DateUtil.date2SQLDate(event.getStartDate());
        startHour = event.getStartHour();
        endHour = event.getEndHour();

        if (startDay.compareTo(theDay) > 0) {
          continue;
        }

        endDay = startDay;

        if (event.getEndDate() != null) {
          endDay = DateUtil.date2SQLDate(event.getEndDate());
        }

        if (endDay.compareTo(theDay) < 0) {
          continue;
        }

        if (calendar.get(Calendar.DAY_OF_MONTH) != currentDay) {
          if (!bCompleteMonth) {
            section = chapter.addSection(generateParagraph(calendar, almanach),
                0);
          }
          currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        }

        Font textFont;
        if (event.getPriority() == 0) {
          textFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0, 0,
              0));
        } else {
          textFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 0, 0));
        }

        eventTitle = event.getTitle();

        if (startDay.compareTo(theDay) == 0 && startHour != null
            && startHour.length() != 0) {
          eventTitle += " (" + startHour;
          if (endDay.compareTo(theDay) == 0 && endHour != null
              && endHour.length() != 0)
            eventTitle += "-" + endHour;
          eventTitle += ")";
        }

        section.add(new Paragraph(eventTitle, titleTextFont));
        if (StringUtil.isDefined(event.getPlace()))
          section.add(new Paragraph(event.getPlace(), titleTextFont));
        if (StringUtil.isDefined(event.getDescription(almanach.getLanguage())))
          section.add(new Paragraph(event
              .getDescription(almanach.getLanguage()), textFont));
        section.add(new Paragraph("\n"));

      } // end for
      calendar.add(Calendar.DAY_OF_MONTH, 1);
    }
  }

  private static Paragraph generateParagraph(Calendar calendar,
      AlmanachSessionController almanach) {
    Font dateFont = new Font(Font.HELVETICA, 14, Font.NORMAL, new Color(0, 0,
        255));
    Paragraph dateSection = new Paragraph(almanach.getString("jour"
        + calendar.get(Calendar.DAY_OF_WEEK))
        + " "
        + calendar.get(Calendar.DAY_OF_MONTH)
        + " "
        + almanach.getString("mois" + calendar.get(Calendar.MONTH))
        + " "
        + calendar.get(Calendar.YEAR), dateFont);
    return dateSection;
  }

}
