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

/**
 * To generate an archive into a pdf file
 */
package com.stratelia.webactiv.newsEdito.control;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.PdfWriter;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.newsEdito.NewsEditoException;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/*
 * CVS Informations
 *
 * $Id: PdfGenerator.java,v 1.6 2007/06/14 08:40:52 neysseri Exp $
 *
 * $Log: PdfGenerator.java,v $
 * Revision 1.6  2007/06/14 08:40:52  neysseri
 * no message
 *
 * Revision 1.5.6.1  2007/06/14 08:22:03  neysseri
 * no message
 *
 * Revision 1.5  2005/09/30 14:19:21  neysseri
 * Centralisation de la gestion des dates
 *
 * Revision 1.4  2004/09/28 09:25:37  neysseri
 * Utilisation de la bibliothèque iText au lieu de Libraries/lowagie + nettoyage sources
 *
 * Revision 1.3  2003/07/22 08:53:06  tleroi
 * *** empty log message ***
 *
 * Revision 1.2  2002/12/18 17:32:24  cbonin
 * 1) Report OCISI : Il n'y a pas de verification javascript sur le format de l'image uploadée
 * 2) on ne verifie pas toutes les extensions d'images possibles
 * 3) NullPointer qd on clique 2 fois sur l'onglet Publication
 * 4) Un publieur n'a pas d'onglet Publication
 * 5) Un gestionnaire ne peut pas copier un article (pas d'icone)
 *
 * Revision 1.1.1.1  2002/11/27 08:54:00  cbonin
 * no message
 *
 * Revision 1.4.38.2  2002/10/31 09:03:42  cbonin
 * Bug path de fichiers & répertoires
 *
 * Revision 1.4.38.1  2002/10/09 15:48:10  scotte
 * Corrections bug Solaris
 *
 * Revision 1.4  2002/01/08 12:00:32  santonio
 * SilverTrace & SilverException
 *
 */

/**
 * Class declaration
 * @author
 */
public class PdfGenerator {

  static private String imagePath = null;

  /**
   * Method declaration
   * @see
   */
  public static void initRessources() {
    ResourceLocator pubSettings = new ResourceLocator(
        "com.stratelia.webactiv.util.publication.publicationSettings", "");

    imagePath = pubSettings.getString("imagesSubDirectory");
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public static String getImagePath() {
    if (imagePath == null) {
      initRessources();
    }
    return imagePath;
  }

  /**
   * Method declaration
   * @param name
   * @param completePubList
   * @param langue
   * @throws NewsEditoException
   * @see
   */
  public static void generatePubList(String name, Collection completePubList,
      String langue) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "PdfGenerator.generatePubList",
        "NewsEdito.MSG_ENTRY_METHOD", "Pdf name = " + name);
    try {
      CompletePublication first = (CompletePublication) completePubList
          .iterator().next();
      String fileName = FileRepositoryManager.getTemporaryPath(first
          .getPublicationDetail().getPK().getSpace(), first
          .getPublicationDetail().getPK().getComponentName())
          + name;
      ResourceLocator message = new ResourceLocator(
          "com.stratelia.webactiv.newsEdito.multilang.newsEditoBundle", langue);
      // creation of the document with a certain size and certain margins
      Document document = new Document(PageSize.A4, 50, 50, 50, 50);

      // we add some meta information to the document
      document.addAuthor("Generateur de PDF Silverpeas");
      document.addSubject("Compilation de publications Silverpeas");
      document.addCreationDate();

      PdfWriter.getInstance(document, new FileOutputStream(fileName));
      document.open();

      createFirstPage(document, langue);

      HeaderFooter header = new HeaderFooter(new Phrase(message
          .getString("publicationCompilation")), false);
      HeaderFooter footer = new HeaderFooter(new Phrase("Page "), new Phrase(
          "."));

      footer.setAlignment(Element.ALIGN_CENTER);

      document.setHeader(header);
      document.setFooter(footer);

      document.newPage();

      Font titleFont = new Font(Font.HELVETICA, 24, Font.NORMAL, new Color(255,
          255, 255));
      Paragraph cTitle = new Paragraph(message.getString("listPublication"),
          titleFont);
      Chapter chapter = new Chapter(cTitle, 1);

      Iterator i = completePubList.iterator();
      CompletePublication complete = null;
      while (i.hasNext()) {
        complete = (CompletePublication) i.next();

        addPublication(chapter, complete);
      }

      document.add(chapter);

      document.close();
    } catch (Exception e) {
      throw new NewsEditoException("PdfGenerator.generatePubList",
          NewsEditoException.WARNING,
          "NewsEdito.EX_PROBLEM_TO_GENERATE_PUBLI_LIST", e);
    }
  }

  /**
   * Method declaration
   * @param name
   * @param archiveDetail
   * @param publicationBm
   * @param langue
   * @throws NewsEditoException
   * @see
   */
  public static void generateArchive(String name, NodeDetail archiveDetail,
      PublicationBm publicationBm, String langue) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "PdfGenerator.generateArchive",
        "NewsEdito.MSG_ENTRY_METHOD", "Pdf name = " + name);
    try {
      String fileName = FileRepositoryManager
          .getTemporaryPath(archiveDetail.getNodePK().getSpace(), archiveDetail
          .getNodePK().getComponentName())
          + name;
      // creation of the document with a certain size and certain margins
      Document document = new Document(PageSize.A4, 50, 50, 50, 50);

      // we add some meta information to the document
      document.addAuthor("Generateur de PDF Silverpeas");
      document.addSubject("Journal Silverpeas : " + archiveDetail.getName());
      document.addCreationDate();

      PdfWriter.getInstance(document, new FileOutputStream(fileName));
      document.open();

      createFirstPage(document, langue);

      // we define a header and a footer
      String descriptionArchive = archiveDetail.getDescription();
      if (descriptionArchive == null)
        descriptionArchive = " ";

      HeaderFooter header = new HeaderFooter(new Phrase(archiveDetail.getName()
          + " : " + descriptionArchive), false);
      HeaderFooter footer = new HeaderFooter(new Phrase("Page "), new Phrase(
          "."));

      footer.setAlignment(Element.ALIGN_CENTER);

      document.setHeader(header);
      document.setFooter(footer);

      document.newPage();

      PdfGenerator.addEditorial(document, archiveDetail, publicationBm, langue);
      PdfGenerator.addMasterTable(document, archiveDetail, publicationBm);

      document.close();
    } catch (Exception e) {
      throw new NewsEditoException("PdfGenerator.generateArchive",
          NewsEditoException.WARNING,
          "NewsEdito.EX_PROBLEM_TO_GENERATE_ARCHIVE", e);
    }

  }

  /**
   * Method declaration
   * @param document
   * @param langue
   * @throws NewsEditoException
   * @see
   */
  public static void createFirstPage(Document document, String langue)
      throws NewsEditoException {
    try {
      ResourceLocator message = new ResourceLocator(
          "com.stratelia.webactiv.newsEdito.multilang.newsEditoBundle", langue);

      Font masterFont = new Font(Font.HELVETICA, 40, Font.BOLD, new Color(0, 0,
          0));
      Paragraph masterTitle = new Paragraph("\n\n\n\n"
          + message.getString("journalEditorial"), masterFont);

      masterTitle.setAlignment(Element.ALIGN_CENTER);

      // java.text.SimpleDateFormat formatter = new
      // java.text.SimpleDateFormat(message.getString("dateFormat"));
      Font secondFont = new Font(Font.HELVETICA, 20, Font.NORMAL, new Color(0,
          0, 0));
      Paragraph secondTitle = new Paragraph(message.getString("editeLe") + " "
          + DateUtil.getOutputDate(new Date(), langue), secondFont);

      secondTitle.setAlignment(Element.ALIGN_CENTER);

      document.add(masterTitle);
      document.add(secondTitle);
    } catch (Exception e) {
      throw new NewsEditoException("PdfGenerator.createFirstPage",
          NewsEditoException.WARNING,
          "NewsEdito.EX_PROBLEM_TO_GENERATE_PAGE_ONE", e);
    }

  }

  /**
   * Method declaration
   * @param document
   * @param nodeDetail
   * @param publicationBm
   * @see
   */
  public static void addMasterTable(Document document, NodeDetail nodeDetail,
      PublicationBm publicationBm) throws NewsEditoException {
    SilverTrace.info("NewsEdito", "PdfGenerator.addMasterTable",
        "NewsEdito.MSG_ENTRY_METHOD");
    try {
      Collection list = nodeDetail.getChildrenDetails();

      Iterator i = list.iterator();

      int titleCount = 1;
      NodeDetail title = null;
      while (i.hasNext()) {
        title = (NodeDetail) i.next();

        addTitle(document, title, titleCount, publicationBm);
        titleCount++;
      }

    } catch (NewsEditoException e) {
      throw new NewsEditoException("PdfGenerator.addMasterTable",
          NewsEditoException.WARNING, "NewsEdito.EX_NO_MASTER_ADDED", e);
    }

  }

  /**
   * Method declaration
   * @param document
   * @param title
   * @param titleCount
   * @param publicationBm
   * @return
   * @throws NewsEditoException
   * @see
   */
  public static Section addTitle(Document document, NodeDetail title,
      int titleCount, PublicationBm publicationBm) throws NewsEditoException {

    // we define some fonts
    Font titleFont = new Font(Font.HELVETICA, 24, Font.NORMAL, new Color(255,
        255, 255));

    // Font titleFont = new Font(Font.HELVETICA, 20, Font.NORMAL, new Color(0,
    // 0, 255));
    // Paragraph sTitle = new Paragraph(title.getName(), titleFont);
    // Section section = title.addSection(sTitle, 2);
    // if (title.getDescription() != null)
    // section.add(new Paragraph(title.getDescription()));

    Paragraph cTitle = new Paragraph(title.getName(), titleFont);
    Chapter chapter = new Chapter(cTitle, titleCount);

    if (title.getDescription() != null) {
      chapter.add(new Paragraph(title.getDescription()));

    }
    try {
      addPublications(chapter, title, publicationBm);
      document.add(chapter);
    } catch (Exception e) {
      throw new NewsEditoException("PdfGenerator.addTitle",
          NewsEditoException.WARNING, "NewsEdito.EX_NO_TITLE_ADDED", e);
    }

    return chapter;
  }

  /**
   * Method declaration
   * @param section
   * @param title
   * @param publicationBm
   * @throws NewsEditoException
   * @see
   */
  public static void addPublications(Section section, NodeDetail title,
      PublicationBm publicationBm) throws NewsEditoException {
    try {
      Collection pubList = publicationBm
          .getDetailsByFatherPK(title.getNodePK());
      Iterator i = pubList.iterator();
      PublicationDetail detail = null;
      CompletePublication complete = null;
      while (i.hasNext()) {
        detail = (PublicationDetail) i.next();
        complete = publicationBm.getCompletePublication(detail.getPK());

        addPublication(section, complete);
      }
    } catch (Exception e) {
      throw new NewsEditoException("PdfGenerator.addPublications",
          NewsEditoException.WARNING, "NewsEdito.EX_NO_PUBLI_ADDED", e);
    }

  }

  /**
   * Method declaration
   * @param section
   * @param complete
   * @see
   */
  public static void addPublication(Section section,
      CompletePublication complete) {
    Font publicationFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0,
        64, 64));

    try {
      Paragraph pub = new Paragraph(complete.getPublicationDetail().getName(),
          publicationFont);
      Section subsection = section.addSection(pub, 0);

      if (complete.getPublicationDetail().getDescription() != null) {
        subsection.add(new Paragraph(complete.getPublicationDetail()
            .getDescription()));
      }
      if ((complete.getInfoDetail() != null)
          && (complete.getModelDetail() != null)) {
        String toParse = complete.getModelDetail().getHtmlDisplayer();

        Iterator textIterator = complete.getInfoDetail().getInfoTextList()
            .iterator();
        Iterator imageIterator = complete.getInfoDetail().getInfoImageList()
            .iterator();

        int posit = toParse.indexOf("%WA");
        InfoTextDetail textDetail = null;
        Paragraph text = null;
        InfoImageDetail imageDetail = null;
        Image img = null;

        while (posit != -1) {
          if (posit > 0) {
            toParse = toParse.substring(posit);
          }
          if (toParse.startsWith("%WATXTDATA%")) {
            if (textIterator.hasNext()) {
              textDetail = (InfoTextDetail) textIterator.next();
              text = new Paragraph(textDetail.getContent());

              subsection.add(text);
            }
            toParse = toParse.substring(11);
          } else if (toParse.startsWith("%WAIMGDATA%")) {
            if (imageIterator.hasNext()) {
              imageDetail = (InfoImageDetail) imageIterator.next();
              /*
               * tk = Toolkit.getDefaultToolkit(); img = tk.getImage(FileRepositoryManager
               * .getAbsolutePath(imageDetail.getPK().getSpace(),
               * imageDetail.getPK().getComponentName()) + getImagePath() + File.separator +
               * imageDetail.getPhysicalName()); waImage = new WAImage(img);
               */

              String imagePath = FileRepositoryManager
                  .getAbsolutePath(imageDetail.getPK().getComponentName())
                  + getImagePath()
                  + File.separator
                  + imageDetail.getPhysicalName();
              SilverTrace.info("NewsEdito", "PDFGenerator.addPublication",
                  "root.MSG_PARAM_VALUE", "imagePath = " + imagePath);
              img = Image.getInstance(imagePath);

              subsection.add(img);
            }
            toParse = toParse.substring(11);
          }

          // et on recommence
          posit = toParse.indexOf("%WA");
        }
      }

    } catch (Exception e) {
      SilverTrace.warn("NewsEdito", "PdfGenerator.addPublication",
          "NewsEdito.EX_NO_PUBLI_ADDED");
    }
  }

  /**
   * Method declaration
   * @param document
   * @param archiveDetail
   * @param publicationBm
   * @param langue
   * @see
   */
  public static void addEditorial(Document document, NodeDetail archiveDetail,
      PublicationBm publicationBm, String langue) throws NewsEditoException {

    SilverTrace.info("NewsEdito", "PdfGenerator.addEditorial",
        "NewsEdito.MSG_ENTRY_METHOD");

    try {
      ResourceLocator message = new ResourceLocator(
          "com.stratelia.webactiv.newsEdito.multilang.newsEditoBundle", langue);
      Collection pubList = publicationBm.getDetailsByFatherPK(archiveDetail
          .getNodePK());
      Iterator i = pubList.iterator();

      if (i.hasNext()) {
        try {

          Font publicationFont = new Font(Font.HELVETICA, 18, Font.BOLD,
              new Color(0, 64, 64));
          Font titleFont = new Font(Font.HELVETICA, 24, Font.NORMAL, new Color(
              255, 255, 255));

          Paragraph cTitle = new Paragraph(message.getString("editorial"),
              titleFont);
          Chapter chapter = new Chapter(cTitle, 0);

          chapter.setNumberDepth(0);

          PublicationDetail detail = null;
          Paragraph name = null;
          Section subsection = null;
          Image img = null;
          while (i.hasNext()) {

            detail = (PublicationDetail) i.next();
            name = new Paragraph(detail.getName(), publicationFont);
            subsection = chapter.addSection(name, 0);

            subsection.setNumberDepth(0);

            if (detail.getDescription() != null) {
              subsection.add(new Paragraph(detail.getDescription()));

            }
            if (detail.getImage() != null) {

              String imagePath =
                  FileRepositoryManager.getAbsolutePath(detail.getPK().getComponentName()) +
                  getImagePath() + File.separator + detail.getImage();
              try {
                SilverTrace.info("NewsEdito", "PDFGenerator.addEditorial", "root.MSG_PARAM_VALUE",
                    "imagePath = " + imagePath);
                img = Image.getInstance(imagePath);
              } catch (Exception e) {
                SilverTrace.info("NewsEdito", "PDFGenerator.addEditorial",
                    "NewsEdito.MSG_CANNOT_RETRIEVE_IMAGE",
                    "imagePath = " + imagePath);
              }
              if (img == null) {
                SilverTrace.info("NewsEdito", "PdfGenerator.addEditorial",
                    "NewsEdito.MSG_CANNOT_RETRIEVE_IMAGE");
              }
              else {
                subsection.add(img);
              }
            }
          }
          document.add(chapter);

        } catch (DocumentException de) {
          SilverTrace.warn("NewsEdito", "PdfGenerator.addEditorial",
              "NewsEdito.EX_NO_EDITO_ADDED");
        }

      }
    } catch (Exception e) {
      throw new NewsEditoException("PdfGenerator.addEditorial",
          NewsEditoException.WARNING, "NewsEdito.EX_PROBLEM_TO_ADD_EDITO", e);
    }

  }

}