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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.webactiv.kmelia.control;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.ListItem;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.comment.model.CommentPK;
import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.ClassifyValue;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.kmelia.model.UserPublication;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;
import org.apache.commons.io.FilenameUtils;
import org.w3c.tidy.Tidy;

public class PdfGenerator extends PdfPageEventHelper {

  private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
  private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
  private static final String PATH_SEPARATOR = " / ";
  /** Current KmeliaSessionController */
  private KmeliaSessionController kmelia = null;
  /** Language */
  private String language;
  /** ResourceBundle kmelia */
  private ResourceLocator message = null;
  /** Current Complete publication */
  private CompletePublication completePublicationDetail;
  /** Current publication */
  private PublicationDetail publicationDetail;
  /** Content language */
  private String publiContentLanguage;
  /** Image Full Star */
  private Image fullStar;
  /** Image Empty Star */
  private Image emptyStar;
  /** The headertable. */
  private PdfPTable pdfTableHeader;
  /** A template that will hold the total number of pages. */
  private PdfTemplate pdfTemplate;
  /** The font that will be used. */
  private BaseFont baseFontHelv;
  private String serverURL;

  public void generate(OutputStream out, CompletePublication currentPublication,
      KmeliaSessionController scc) throws KmeliaRuntimeException {
    try {
      kmelia = scc;
      language = kmelia.getLanguage();
      message = new ResourceLocator(
          "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", language);
      completePublicationDetail = currentPublication;
      publicationDetail = currentPublication.getPublicationDetail();
      publiContentLanguage = kmelia.getCurrentLanguage();
      try {
        serverURL = scc.getOrganizationController().getDomain(scc.getUserDetail().getDomainId()).
            getSilverpeasServerURL();
        String classesPath = new File(this.getClass().getClassLoader().getResource("/").toURI()).
            getPath();
        fullStar = Image.getInstance(FilenameUtils.normalize(classesPath
            + "../../util/icons/starFilled.gif"));
        emptyStar = Image.getInstance(FilenameUtils.normalize(classesPath
            + "../../util/icons/starEmpty.gif"));
      } catch (Exception e) {
        SilverTrace.warn("kmelia", "PdfGenerator.generate", "root.EX_REMOTE_EXCEPTION", e);
        fullStar = null;
        emptyStar = null;
      }

      Document document = new Document(PageSize.A4, 50, 50, 115, 50);
      document.addCreator(message.getString("GeneratorPdf"));
      document.addSubject(message.getString("SubjectPdf"));
      document.addCreationDate();

      PdfWriter pdfWriter = PdfWriter.getInstance(document, out);
      pdfWriter.setPageEvent(this);
      document.open();
      // En-Tete
      generateHeaderPage(document);
      // Fichiers joints
      generateFilesTable(document);
      // Voir aussi
      generateSeeAlso(document);
      // Plan de classement
      generateCategorization(document);
      // Commentaires
      generateComments(document);
      // Contenu
      generateContent(document);
      document.close();
    } catch (Exception e) {
      throw new KmeliaRuntimeException("PdfGenerator.generate", SilverpeasRuntimeException.WARNING,
          "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", e);
    }
  }

  /**
   * @see com.lowagie.text.pdf.PdfPageEventHelper#onOpenDocument(com.lowagie.text.pdf.PdfWriter,
   * com.lowagie.text.Document)
   */
  @Override
  public void onOpenDocument(PdfWriter writer, Document document) {
    pdfTableHeader = new PdfPTable(2);
    Phrase p = new Phrase();
    Chunk ck = new Chunk(publicationDetail.getName(publiContentLanguage) + "\n", new Font(
        Font.HELVETICA, 12, Font.BOLD));
    p.add(ck);
    document.addTitle(publicationDetail.getName(publiContentLanguage));

    if (kmelia.isPublicationIdDisplayed()) {
      String contentHeaderText = message.getString("Codification") + " : " + publicationDetail.getPK().
          getId();
      // Version : optionnel
      if (kmelia.isFieldVersionVisible()) {
        if (StringUtil.isDefined(publicationDetail.getVersion())) {
          contentHeaderText += "\n" + message.getString("PubVersion") + " : " + publicationDetail.
              getVersion();
        }
      }
      ck = new Chunk(contentHeaderText, NORMAL_FONT);
      p.add(ck);
    }
    pdfTableHeader.getDefaultCell().setBorderWidth(1);
    pdfTableHeader.getDefaultCell().setBorderWidthLeft(0);
    pdfTableHeader.getDefaultCell().setBorderWidthRight(0);
    pdfTableHeader.getDefaultCell().setPaddingTop(12);
    pdfTableHeader.getDefaultCell().setPaddingBottom(5);
    pdfTableHeader.addCell(p);
    pdfTableHeader.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
    pdfTableHeader.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
    Image headerImage = getImageHeader();
    if (headerImage != null) {
      PdfPCell imageCell = new PdfPCell(headerImage, false);
      imageCell.setBorderWidth(1);
      imageCell.setBorderWidthLeft(0);
      imageCell.setBorderWidthRight(0);
      imageCell.setPaddingTop(12);
      imageCell.setPaddingBottom(5);
      imageCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
      imageCell.setVerticalAlignment(Element.ALIGN_CENTER);
      pdfTableHeader.addCell(imageCell);
    }
    // initialization of the Graphic State
    PdfGState pdfGState = new PdfGState();
    pdfGState.setFillOpacity(0.3f);
    pdfGState.setStrokeOpacity(0.3f);
    // initialization of the template
    pdfTemplate = writer.getDirectContent().createTemplate(100, 100);
    pdfTemplate.setBoundingBox(new Rectangle(-20, -20, 100, 100));
    // initialization of the font
    try {
      baseFontHelv = BaseFont.createFont("Helvetica", BaseFont.WINANSI, false);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "PdfGenerator.onOpenDocument", "root.EX_REMOTE_EXCEPTION", e);
    }
  }

  private Image getImageHeader() {
    try {
      String logo = kmelia.getSettings().getString("logoPdf");
      URL url;
      try {
        url = new URL(logo);
      } catch (MalformedURLException ex) {
        url = new URL(serverURL + logo);
      }
      Image image = Image.getInstance(url);
      float scale =  (24f   * 100) / image.getPlainHeight();
      image.scalePercent(scale);
      return image;
    } catch (Exception e) {
      SilverTrace.error("kmelia", "PdfGenerator.onOpenDocument", "root.EX_REMOTE_EXCEPTION", e);
    }
    return null;
  }

  /**
   * @see com.lowagie.text.pdf.PdfPageEventHelper#onEndPage(com.lowagie.text.pdf.PdfWriter,
   * com.lowagie.text.Document)
   */
  @Override
  public void onEndPage(PdfWriter writer, Document document) {
    PdfContentByte cb = writer.getDirectContent();
    cb.saveState();
    // write the headertable
    pdfTableHeader.setTotalWidth(document.right() - document.left());
    pdfTableHeader.writeSelectedRows(0, -1, document.left(), document.getPageSize().getHeight() - 50,
        cb);

    StringBuilder text = new StringBuilder(256);
    text.append(message.getString("editeLe")).append(" ").
        append(DateUtil.dateToString(new Date(), language)).append(" ").
        append(message.getString("For")).append(" ").
        append(kmelia.getUserDetail().getDisplayedName()).append(" - ").
        append(message.getString("Page")).append(" ").append(writer.getPageNumber()).append("/");
    float textSize = 0;
    if (baseFontHelv != null) {
      textSize = baseFontHelv.getWidthPoint(text.toString(), 8);
    }
    float textBase = document.bottom() - 20;
    cb.beginText();
    float adjust = 0;
    if (baseFontHelv != null) {
      cb.setFontAndSize(baseFontHelv, 8);
      adjust = baseFontHelv.getWidthPoint("0", 8);
    }
    cb.setTextMatrix(document.right() - textSize - adjust, textBase);
    cb.showText(text.toString());
    cb.endText();
    cb.addTemplate(pdfTemplate, document.right() - adjust, textBase);
    cb.restoreState();
  }

  /**
   * @see com.lowagie.text.pdf.PdfPageEventHelper#onCloseDocument(com.lowagie.text.pdf.PdfWriter,
   * com.lowagie.text.Document)
   */
  @Override
  public void onCloseDocument(PdfWriter writer, Document document) {
    pdfTemplate.beginText();
    if (baseFontHelv != null) {
      pdfTemplate.setFontAndSize(baseFontHelv, 8);
    }
    pdfTemplate.setTextMatrix(0, 0);
    pdfTemplate.showText("" + (writer.getPageNumber() - 1));
    pdfTemplate.endText();
  }

  private void addRowToTable(Table tbl, Font fnt, String[] cells, Color bg_colour, boolean isBorder,
      boolean first_column_bold) {
    Cell cl = null;
    Chunk chnk;
    Font font = null;
    if (fnt != null) {
      font = fnt;
    }
    for (int i = 0; i < cells.length; i++) {
      if (i == 0 && first_column_bold && fnt == null) {
        font = BOLD_FONT;
      } else if (fnt == null) {
        font = NORMAL_FONT;
      }
      try {
        chnk = new Chunk(cells[i], font);
        cl = new Cell(chnk);
      } catch (Exception ex) {
        cl = new Cell();
      }
      if (!isBorder) {
        cl.setBorderWidth(0);
      }
      if (bg_colour != null) {
        cl.setBackgroundColor(bg_colour);
      }
      tbl.addCell(cl);
    }
  }

  private void addRowToTable(Table tbl, Font fnt, String[] cells, boolean isBorder,
      boolean first_column_bold) {
    addRowToTable(tbl, fnt, cells, null, isBorder, first_column_bold);
  }

  private void addRowImagesToTable(Table tbl, String cell,
      Image[] tabImage, boolean isBorder) {
    Cell cl = null;
    // cell 1
    Chunk chnk = new Chunk(cell, BOLD_FONT);
    try {
      cl = new Cell(chnk);
    } catch (Exception ex) {
    }
    if (!isBorder) {
      cl.setBorderWidth(0);
    }
    tbl.addCell(cl);

    // cell 2
    Phrase images = new Phrase();
    for (int i = 0; i < tabImage.length; i++) {
      chnk = new Chunk(tabImage[i], 0, -5);
      images.add(chnk);
    }
    try {
      cl = new Cell(images);
    } catch (Exception ex) {
    }
    if (!isBorder) {
      cl.setBorderWidth(0);
    }
    tbl.addCell(cl);

  }

  private void addRowLinkToTable(Table tbl, String[] cells,
      Color[] colours, boolean isBorder, boolean first_column_bold) {
    Cell cl = null;
    Chunk chnk;
    Font font = null;
    for (int i = 0; i < cells.length; i++) {
      if (i == 0 && first_column_bold) {
        font = new Font(Font.HELVETICA, 10, Font.BOLD);
      } else {
        font = new Font(Font.HELVETICA, 10, Font.NORMAL);
        font.setStyle(Font.UNDERLINE);
      }

      if (colours != null) {
        font.setColor(colours[i]);
      }

      chnk = new Chunk(cells[i], font);
      try {
        if (i > 0) {
          chnk.setAnchor(new URL(cells[i]));
        }
      } catch (Exception ex) {
      }
      try {
        cl = new Cell(chnk);
      } catch (Exception ex) {
      }
      if (!isBorder) {
        cl.setBorderWidth(0);
      }
      tbl.addCell(cl);
    }
  }

  private void addRowImageToTable(Table tbl, String cell, Image image, boolean isBorder) {
    Cell cl = null;
    // cell 1
    Chunk chnk = new Chunk(cell, new Font(Font.HELVETICA, 10, Font.BOLD));
    try {
      cl = new Cell(chnk);
    } catch (Exception ex) {
    }
    if (!isBorder) {
      cl.setBorderWidth(0);
    }
    tbl.addCell(cl);

    // cell 2
    chnk = new Chunk(image, 0, 0);
    try {
      cl = new Cell(chnk);
    } catch (Exception ex) {
    }
    if (!isBorder) {
      cl.setBorderWidth(0);
    }
    tbl.addCell(cl);

  }

  private void addRowToTable(Table tbl, String[] cells) {
    for (int i = 0; i < cells.length; i++) {
      Font font;
      if (i == 0 || i == 2) {
        font = BOLD_FONT;
      } else {
        font = NORMAL_FONT;
      }
      try {
        Cell cl =  new Cell(new Chunk(cells[i], font));
        cl.setBorderWidth(0);
        tbl.addCell(cl);
      } catch(BadElementException ex)  {
         Cell cl =  new Cell(cells[i]);
        cl.setBorderWidth(0);
        tbl.addCell(cl);
      }
    }
  }

  private String getTopicPath() throws RemoteException {
    String out = "";
    Collection<Collection<NodeDetail>> pathList = kmelia.getPathList(publicationDetail.getPK().getId());
    Iterator<Collection<NodeDetail>> i = pathList.iterator();
    while (i.hasNext()) {
      Collection<NodeDetail> path = i.next();
      Iterator<NodeDetail> j = path.iterator();
      String pathString = "";
      int nbItemInPath = path.size();
      boolean alreadyCut = false;
      int nb = 0;
      while (j.hasNext()) {
        NodeDetail nodeInPath = j.next();
        if ((nb <= 3) || (nb + 3 >= nbItemInPath - 1)) {
          pathString = nodeInPath.getName() + " " + pathString;
          if (j.hasNext()) {
            pathString = " > " + pathString;
          }
        } else {
          if (!alreadyCut) {
            pathString += " ... > ";
            alreadyCut = true;
          }
        }
        nb++;
      }
      out += pathString + "\n";
    }
    return out;
  }

  private PdfPTable addHeaderToSection(String title) throws DocumentException {
    PdfPTable tblHeader = new PdfPTable(1);
    tblHeader.setWidthPercentage(100f);
    PdfPCell cl = new PdfPCell(new Phrase(title, new Font(Font.HELVETICA, 10, Font.BOLD)));
    cl.setHorizontalAlignment(Element.ALIGN_CENTER);
    cl.setBorderWidthTop(1);
    cl.setBorderWidthBottom(1);
    cl.setBorderWidthLeft(0);
    cl.setBorderWidthRight(0);
    cl.setPaddingTop(7);
    cl.setPaddingBottom(5);
    tblHeader.addCell(cl);
    return tblHeader;
  }

  private void generateHeaderPage(Document document) throws DocumentException, RemoteException {
    document.add(new Paragraph("\n"));
    PdfPTable tblHeader = addHeaderToSection(message.getString("Header").toUpperCase());
    document.add(tblHeader);

    Table tbl = new Table(2);
    tbl.setBorderWidth(0);
    tbl.setAlignment(Element.ALIGN_LEFT);
    tbl.setWidth(100);
    tbl.setWidths(new int[]{25, 75});

    // Emplacement
    addRowToTable(tbl, null, new String[]{message.getString("TopicPath") + " :", getTopicPath()},
        false, true);

    // Permalien
    boolean displayLinks = URLManager.displayUniversalLinks();
    if (displayLinks) {
      addRowLinkToTable(tbl, new String[]{message.getString("Link") + " :", serverURL
            + URLManager.getSimpleURL(URLManager.URL_PUBLI, publicationDetail.getPK().getId())},
          new Color[]{Color.BLACK, Color.BLUE}, false, true);
    }

    // Vignette : optionnel
    ResourceLocator settings = new ResourceLocator(
        "com.stratelia.webactiv.kmelia.settings.kmeliaSettings", language);
    if (settings.getBoolean("isVignetteVisible", false)) {
      String imageFileName = null;
      try {
        imageFileName = publicationDetail.getImage();
      } catch (Exception e) {
        SilverTrace.error("kmelia", "PdfGenerator.onOpenDocument", "root.EX_REMOTE_EXCEPTION", e);
      }
      if (imageFileName != null) {
        try {
          File vignette = new File(FileRepositoryManager.getAbsolutePath(kmelia.getComponentId()) + kmelia.
              getPublicationSettings().getString("imagesSubDirectory") + File.separatorChar
              + publicationDetail.getImage());
          Image image = Image.getInstance(vignette.toURI().toURL());
          float scale = (100f   * 100) / image.getPlainHeight();
          image.scalePercent(scale);
          addRowImageToTable(tbl, message.getString("Thumbnail") + " :", image, false);
        } catch (Exception e) {
          SilverTrace.error("kmelia", "PdfGenerator.onOpenDocument", "root.EX_REMOTE_EXCEPTION", e);
        }
      }
    }

    // Description : optionnel
    if (kmelia.isFieldDescriptionVisible()) {
      String sDescription = publicationDetail.getDescription(publiContentLanguage);
      if (StringUtil.isDefined(sDescription)) {
        addRowToTable(tbl, null, new String[]{message.getString("Description") + " :", sDescription},
            false, true);
      }
    }

    // Mots clés : optionnel
    if (kmelia.isFieldKeywordsVisible()) {
      String sKeywords = publicationDetail.getKeywords(publiContentLanguage);
      if (sKeywords != null && sKeywords.length() > 0) {
        addRowToTable(tbl, null, new String[]{message.getString("PubMotsCles") + " :", sKeywords},
            false, true);
      }
    }

    // Auteur : optionnel
    if (kmelia.isAuthorUsed()) {
      String sAuthor = publicationDetail.getAuthor();
      if (StringUtil.isDefined(sAuthor)) {
        addRowToTable(tbl, null, new String[]{message.getString("GML.author", language) + " :",
              sAuthor}, false, true);
        document.addAuthor(sAuthor);
      }
    }

    // Importance : optionnel
    if (kmelia.isFieldImportanceVisible() && fullStar != null
        && emptyStar != null) {
      Image[] tabImage = new Image[5];
      // display full Stars
      for (int i = 0; i < publicationDetail.getImportance(); i++) {
        tabImage[i] = fullStar;
      }
      // display empty stars
      for (int i = publicationDetail.getImportance(); i < 5; i++) {
        tabImage[i] = emptyStar;
      }
      addRowImagesToTable(tbl, message.getString("PubImportance") + " :", tabImage, false);
    }

    // Valideurs publication
    if (publicationDetail.getValidatorId() != null) {
      String validatorDate = kmelia.getUserDetail(
          publicationDetail.getValidatorId()).getDisplayedName();
      if (publicationDetail.getValidateDate() != null) {
        validatorDate += " (" + DateUtil.getOutputDate(publicationDetail.getValidateDate(),
            language) + ")";
      }
      addRowToTable(tbl, null, new String[]{message.getString("kmelia.Valideur") + " :",
            validatorDate}, false, true);
    }
    document.add(tbl);
    Table tbl2 = new Table(4);
    tbl2.setBorderWidth(0);
    tbl2.setAlignment(Element.ALIGN_LEFT);
    tbl2.setWidth(100);
    tbl2.setWidths(new int[]{25, 30, 15, 30});

    String creatorName = "";
    UserDetail ownerDetail = kmelia.getUserCompletePublication(publicationDetail.getPK().getId()).
        getOwner();
    if (ownerDetail != null) {
      creatorName = ownerDetail.getDisplayedName();
    } else {
      creatorName = message.getString("UnknownAuthor");
    }
    // Créé par,
    addRowToTable(tbl2, new String[]{message.getString("PubDateCreation") + " :",
          DateUtil.getOutputDate(publicationDetail.getCreationDate(), language)
          + " " + message.getString("kmelia.By") + " " + creatorName});

    String messageUpdatedDate = "";
    String sUpdated = "";
    String updateDate = null;
    if (publicationDetail.getUpdateDate() != null) {
      updateDate = DateUtil.getOutputDate(publicationDetail.getUpdateDate(), language);
    }
    String updaterName = null;
    UserDetail updater = kmelia.getUserDetail(publicationDetail.getUpdaterId());
    if (updater != null) {
      updaterName = updater.getDisplayedName();
    }
    if (StringUtil.isDefined(updateDate) && StringUtil.isDefined(updaterName)) {
      messageUpdatedDate = message.getString("PubDateUpdate") + " :";
      sUpdated = updateDate + " " + message.getString("kmelia.By") + " "
          + updaterName;
      // Modifiée par
      addRowToTable(tbl2, new String[]{messageUpdatedDate, sUpdated});
    }
    String beginDate = "";
    if (publicationDetail.getBeginDate() != null) {
      beginDate = DateUtil.getInputDate(publicationDetail.getBeginDate(),
          language);
    }
    String beginHour = "";
    if (beginDate.length() > 0) {
      beginHour = publicationDetail.getBeginHour();
    }

    String endDate = "";
    if (publicationDetail.getEndDate() != null) {
      if (DateUtil.date2SQLDate(publicationDetail.getEndDate()).equals(
          "1000/01/01")) {
        endDate = "";
      } else {
        endDate = DateUtil.getInputDate(publicationDetail.getEndDate(),
            language);
      }
    }
    String endHour = "";
    if (endDate.length() > 0) {
      endHour = publicationDetail.getEndHour();
    }

    if (beginDate.length() > 0 && endDate.length() > 0) {
      // Consultable du 'Date' à 'Heure' au 'Date' à 'Heure'
      addRowToTable(tbl2, new String[]{
            message.getString("PubDateDebut") + " :",
            beginDate + " " + message.getString("ToHour") + " " + beginHour,
            message.getString("PubDateFin") + " :",
            endDate + " " + message.getString("ToHour") + " " + endHour, "", ""});
    } else if (beginDate.length() > 0) {
      // Consultable à partir du 'Date' à 'Heure'
      addRowToTable(tbl2, new String[]{
            message.getString("PubDateDebutPdf") + " :",
            beginDate + " " + message.getString("ToHour") + " " + beginHour, "",
            "", "", ""});
    } else if (endDate.length() > 0) {
      // Consultable jusqu'au 'Date' à 'Heure'
      addRowToTable(tbl2, new String[]{
            message.getString("PubDateFinPdf") + " :",
            endDate + " " + message.getString("ToHour") + " " + endHour, "", "",
            "", ""});
    }

    document.add(tbl2);
    document.add(new Paragraph("\n"));
  }

  private void addRowToTable(Table tbl, Font fnt, String[] cells,
      Color bg_colour, boolean isBorder) {
    addRowToTable(tbl, fnt, cells, bg_colour, isBorder, false);
  }

  private Table addTableAttachments(String mode) throws DocumentException {
    Table tbl = null;

    Font header_font = BOLD_FONT;
    header_font.setColor(new Color(255, 255, 255));

    ResourceLocator messageAttachment = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.multilang.attachment", language);

    if ("A".equals(mode)) {
      tbl = new Table(5);
      int headerwidths[] = {25, 25, 30, 10, 10};
      tbl.setWidths(headerwidths);
      tbl.setWidth(100);
      tbl.setBorderWidth(0);
      tbl.setPadding(2);
      addRowToTable(tbl, header_font, new String[]{
            messageAttachment.getString("fichier"),
            messageAttachment.getString("Title"),
            messageAttachment.getString("Description"),
            messageAttachment.getString("taille"),
            messageAttachment.getString("uploadDate")}, Color.LIGHT_GRAY, true);
    } else if ("V".equals(mode)) {

      ResourceLocator messageVersioning = new ResourceLocator(
          "com.stratelia.silverpeas.versioningPeas.multilang.versioning",
          language);

      // Versioning links
      tbl = new Table(6);
      int headerwidths[] = {20, 15, 20, 10, 15, 20};
      tbl.setWidths(headerwidths);
      tbl.setWidth(100);
      tbl.setBorderWidth(0);
      tbl.setPadding(2);
      addRowToTable(tbl, header_font, new String[]{messageAttachment.getString("fichier"),
            messageVersioning.getString("name"), messageAttachment.getString("Description"),
            messageVersioning.getString("version"), messageVersioning.getString("date"),
            messageVersioning.getString("validator")},
          Color.LIGHT_GRAY, true);
    }
    if (tbl != null) {
      tbl.endHeaders();
    }

    return tbl;
  }

  /**
   * @param documentPdf
   * @throws DocumentException
   * @throws RemoteException
   */
  private void generateFilesTable(Document documentPdf) throws DocumentException, RemoteException {
    if (kmelia.isVersionControlled()) {
      generateVersionnedFileTable(documentPdf);
    } else {
      generateSimpleFilesTable(documentPdf);
    }
  }

  /**
   * Get all the simple attachments and add them to the file tables.
   * @param documentPdf
   * @throws DocumentException
   */
  private void generateSimpleFilesTable(Document documentPdf) throws DocumentException {
    String idPubli = publicationDetail.getPK().getId();
    String instanceId = kmelia.getComponentId();
    AttachmentPK foreignKey = new AttachmentPK(idPubli, instanceId);
    Collection<AttachmentDetail> attachments =
        AttachmentController.searchAttachmentByPKAndContext(foreignKey, "Images");
    if (attachments != null && !attachments.isEmpty()) {
      PdfPTable tblHeader = addHeaderToSection(message.getString("Attachments").toUpperCase());
      documentPdf.add(tblHeader);
      // En-tete tableau des fichiers attachés
      Table tbl = addTableAttachments("A");
      for (AttachmentDetail attachmentDetail : attachments) {
        addRowToTable(tbl, null, new String[]{attachmentDetail.getLogicalName(publiContentLanguage),
              attachmentDetail.getTitle(publiContentLanguage),
              attachmentDetail.getInfo(publiContentLanguage),
              attachmentDetail.getAttachmentFileSize(publiContentLanguage),
              DateUtil.getOutputDate(attachmentDetail.getCreationDate(publiContentLanguage),
              language)}, true, false);
      }

      documentPdf.add(tbl);
    }
  }

  private void generateVersionnedFileTable(Document documentPdf)
      throws RemoteException, DocumentException {
    String idPubli = publicationDetail.getPK().getId();
    String instanceId = kmelia.getComponentId();
    VersioningUtil versioningUtil = new VersioningUtil();
    ForeignPK foreignKey = new ForeignPK(idPubli, instanceId);
    @SuppressWarnings("unchecked")
    List<com.stratelia.silverpeas.versioning.model.Document> documents =
        versioningUtil.getDocuments(foreignKey);
    if (documents != null && !documents.isEmpty()) {
      // Titre
      PdfPTable tblHeader = addHeaderToSection(message.getString("Attachments").toUpperCase());
      documentPdf.add(tblHeader);

      // En-tete tableau des fichiers attachés
      Table tbl = addTableAttachments("V");
      int user_id = Integer.parseInt(kmelia.getUserId());
      String creatorOrValidators = "";
      for (com.stratelia.silverpeas.versioning.model.Document document : documents) {
        if (versioningUtil.isReader(document, user_id) || versioningUtil.isWriter(document, user_id)
            || kmelia.isAdmin()) {
          ArrayList<DocumentVersion> versions = versioningUtil.getDocumentFilteredVersions(
              document.getPk(), user_id);
          if (!versions.isEmpty()) {
            DocumentVersion document_version = versions.get(0); // current version
            String creation_date = DateUtil.dateToString(document.getLastCheckOutDate(), language);

            if (document_version.getSize() != 0 || !"dummy".equals(
                document_version.getLogicalName())) {
              if (2 == document.getTypeWorkList()) {// Ordonné avec
                // Approbation
                ArrayList<Worker> users = document.getWorkList();
                for (Worker user : users) {
                  if (user.isApproval()) {
                    creatorOrValidators += kmelia.getOrganizationController().getUserDetail(
                        String.valueOf(user.getUserId())).getDisplayedName() + ", ";
                  }
                }
                if (creatorOrValidators.length() > 0) {
                  creatorOrValidators = creatorOrValidators.substring(0,
                      creatorOrValidators.length() - 2);
                }
              } else {// Autres cas
                creatorOrValidators = kmelia.getOrganizationController().getUserDetail(String.
                    valueOf(document_version.getAuthorId())).getDisplayedName();
              }
              addRowToTable(tbl, null, new String[]{document_version.getLogicalName(),
                    document.getName(), document.getDescription(),
                    document_version.getMajorNumber() + "." + document_version.getMinorNumber(),
                    creation_date, creatorOrValidators}, true, false);
            } else {
              addRowToTable(tbl, null, new String[]{document_version.getLogicalName(),
                    document.getName(), document.getDescription(), "", creation_date,
                    kmelia.getOrganizationController().getUserDetail(
                    String.valueOf(document.getOwnerId())).getDisplayedName()}, true, false);
            }
          }
        }
      }
      documentPdf.add(tbl);
    }
  }

  private String getUserName(UserPublication userPub) {
    UserDetail user = userPub.getOwner(); // contains creator
    PublicationDetail pub = userPub.getPublication();
    String updaterId = pub.getUpdaterId();
    UserDetail updater = null;
    if (updaterId != null && updaterId.length() > 0) {
      updater = kmelia.getUserDetail(updaterId);
    }
    if (updater == null) {
      updater = user;
    }

    String userName;
    if (updater != null && (StringUtil.isDefined(updater.getFirstName())
        || StringUtil.isDefined(updater.getLastName()))) {
      userName = updater.getFirstName() + " " + updater.getLastName();
    } else {
      userName = message.getString("kmelia.UnknownUser");
    }

    return userName;
  }

  private void generateSeeAlso(Document document) throws IOException, DocumentException {
    List<ForeignPK> targets = completePublicationDetail.getLinkList();
    if (targets != null && !targets.isEmpty()) {
      PdfPTable tblHeader = addHeaderToSection(message.getString("PubReferenceeParAuteur").
          toUpperCase());
      document.add(tblHeader);
      com.lowagie.text.List list = new com.lowagie.text.List(false, 20);
      list.setListSymbol(new Chunk("\u2022", new Font(Font.HELVETICA, 20,
          Font.BOLD, new Color(0, 0, 0))));
      Collection<UserPublication> linkedPublications = kmelia.getPublications(
          targets);
      Iterator<UserPublication> iterator = linkedPublications.iterator();
      UserPublication userPub;
      PublicationDetail pub;
      Chunk permalinkPubli;
      String importance;
      int sImportance;
      Image[] tabImage;
      Phrase permalinkStar;
      Chunk chnk;
      ListItem listItem;
      com.lowagie.text.List sublist;
      ListItem subListItem;
      if (iterator.hasNext()) {
        while (iterator.hasNext()) {
          userPub = iterator.next();
          pub = userPub.getPublication();

          if (pub.getStatus() != null && pub.getStatus().equals("Valid")
              && !pub.getPK().getId().equals(publicationDetail.getPK().getId())) {
            permalinkPubli = new Chunk(pub.getPK().getId() + " - "
                + pub.getName(publiContentLanguage), FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 10, Font.UNDERLINE, Color.BLUE));
            permalinkPubli.setAnchor(new URL(serverURL
                + URLManager.getSimpleURL(URLManager.URL_PUBLI, pub.getPK().getId())));
            permalinkStar = new Phrase();
            permalinkStar.add(permalinkPubli);

            // Importance : optionnel
            if (kmelia.isFieldImportanceVisible()
                && fullStar != null && emptyStar != null) {
              importance = new Integer(pub.getImportance()).toString();
              if (importance.equals("")) {
                importance = "1";
              }
              sImportance = Integer.parseInt(importance);
              tabImage = new Image[5];
              // display full Stars
              for (int i = 0; i < sImportance; i++) {
                tabImage[i] = fullStar;
              }
              // display empty stars
              for (int i = sImportance; i < 5; i++) {
                tabImage[i] = emptyStar;
              }
              for (int i = 0; i < tabImage.length; i++) {
                chnk = new Chunk(tabImage[i], 0, -5);
                permalinkStar.add(chnk);
              }
            }

            listItem = new ListItem(permalinkStar);
            list.add(listItem);

            sublist = new com.lowagie.text.List(false, false, 8);
            sublist.setListSymbol(new Chunk("", FontFactory.getFont(
                FontFactory.HELVETICA, 8)));
            chnk = new Chunk(getUserName(userPub) + " - "
                + DateUtil.getOutputDate(pub.getUpdateDate(), language) + "\n"
                + pub.getDescription(publiContentLanguage), FontFactory.getFont(
                FontFactory.HELVETICA, 8, Font.NORMAL));
            subListItem = new ListItem(chnk);
            sublist.add(subListItem);
            list.add(sublist);
          }
        } // End while
      } // End if

      document.add(list);
    }
  }

  private void generateComments(Document document) throws DocumentException, RemoteException,
      ParseException {
    CommentService commentService = CommentServiceFactory.getFactory().getCommentService();
    List<Comment> comments = commentService.getAllCommentsOnPublication(new CommentPK(
        publicationDetail.getPK().getId(), null, kmelia.getComponentId()));
    if (comments != null && !comments.isEmpty()) {
      PdfPTable tblHeader = addHeaderToSection(message.getString("Comments").toUpperCase());
      document.add(tblHeader);
      ResourceLocator messageComment = new ResourceLocator(
          "com.stratelia.webactiv.util.comment.multilang.comment", language);

      Font header_font = BOLD_FONT;
      header_font.setColor(new Color(255, 255, 255));
      Table tbl = new Table(4);
      int headerwidths[] = {25, 55, 20, 20};
      tbl.setWidths(headerwidths);
      tbl.setWidth(100);
      tbl.setBorderWidth(0);
      tbl.setPadding(2);
      addRowToTable(tbl, header_font, new String[]{messageComment.getString("author"),
            messageComment.getString("c_comment"), messageComment.getString("created"),
            messageComment.getString("modified")}, Color.LIGHT_GRAY, true);
      tbl.endHeaders();

      for (Comment comment : comments) {
        addRowToTable(tbl, null, new String[]{comment.getOwner(), comment.getMessage(),
              DateUtil.getOutputDate(comment.getCreationDate(), language),
              DateUtil.getOutputDate(comment.getModificationDate(), language)}, true, false);
      }

      document.add(tbl);
    }
  }

  /**
   * Cette methode construit un hyperlien a partir d'un nom et de son lien
   * @param unit - un objet contenant un nom et un lien. Cette valeur ne doit pas etre nulle
   * @param isLinked - vrai si l'on souhaite un hyperlien faux si l'on ne veut que du texte
   * @return le texte en dur ou au format hypelien
   */
  private String linkedNode(Value unit, boolean isLinked) {
    // Attention la partie hyperlink est a faire !!!!
    if (isLinked) {
      StringBuilder buffer = new StringBuilder(256);
      buffer.append("<a href=").append(unit.getPath()).append(">").append(unit.getName(language)).
          append("</a>");
      return buffer.toString();
    }
    return unit.getName(language);
  }

  /**
   * Cette methode construit le chemin complet tronque pour acceder a une valeur
   * @param list - un objet contenant une liste de liste(nom+url). Cette valeur ne doit pas etre
   * nulle
   * @param completPath - le chemin que l'on veut tronquer
   * @param withLastValue - on garde ou non la valeur selectionnee
   * @return completPath - le chemin fabrique
   */
  private String troncatePath(String completPath, List<Value> list, boolean isLinked,
      int withLastValue) {
    int nbShowedEltAuthorized = 2; // nombre de noeud que l'on veut afficher avant les ...
    String troncateSeparator = " ... ";
    StringBuilder path = new StringBuilder(completPath);
    Value value = null;
    // prend les nbShowedEltAuthorized 1er elements
    for (int nb = 0; nb < nbShowedEltAuthorized; nb++) {
      value = list.get(nb);
      path.append(linkedNode(value, isLinked)).append(PATH_SEPARATOR);
    }
    // colle ici les points de suspension
    path.append(troncateSeparator).append(PATH_SEPARATOR);

    // prend les nbShowedEltAuthorized derniers elements
    for (int nb = nbShowedEltAuthorized + withLastValue; nb > withLastValue; nb--) {
      value = list.get(list.size() - nb);
      path.append(linkedNode(value, isLinked)).append(PATH_SEPARATOR);
    }
    return path.toString();
  }

  /**
   * Cette methode construit le chemin complet pour acceder a une valeur
   * @param list - un objet contenant une liste de liste(nom+url). Cette valeur ne doit pas etre
   * nulle
   * @param isLinked - vrai si l'on souhaite un hyperlien faux si l'on ne veut que du texte
   * @param withLastNode - 0 si l'on veut afficher le chemin complet de la valeur selectionnee. 1 si
   * l'on ne souhaite afficher que le chemin complet sans la valeur selectionnee
   * @return completPath - le chemin fabrique
   */
  private String buildCompletPath(List<Value> list, boolean isLinked,
      int withLastValue) {
    int maxEltAuthorized = 5; // nombre min d'elements avant la troncature du chemin

    StringBuilder completPath = new StringBuilder("");
    // on regarde d'en un 1er temps le nombre d'element de la liste que l'on
    // recoit.
    // si ce nombre est strictement superieur a maxEltAuthorized alors on doit
    // tronquer le chemin complet
    // et l'afficher comme suit : noeud1 / noeud2 / ... / noeudn-1 / noeudn
    Value value = null;
    if (list.size() > maxEltAuthorized) {
      completPath.append(troncatePath("", list, isLinked, withLastValue));
    } else {
      for (int nb = 0; nb < list.size() - withLastValue; nb++) {
        value = list.get(nb);
        completPath.append(linkedNode(value, isLinked)).append(PATH_SEPARATOR);
      }
    }
    String result = completPath.toString();
    if ("".equals(result) || "/".equals(result)) {
      return null;
    }
    return result.substring(0, result.length() - PATH_SEPARATOR.length());
  }

  private String buildCompletPath(List<Value> list, boolean isLinked) {
    return buildCompletPath(list, isLinked, 0);
  }

  private void generateCategorization(Document document)
      throws DocumentException, PdcException {
    PdcBm pdcBm = (PdcBm) new PdcBmImpl();
    List<ClassifyPosition> listPositions = pdcBm.getPositions(kmelia.getSilverObjectId(publicationDetail.
        getPK().getId()),
        kmelia.getComponentId());
    if (listPositions != null && !listPositions.isEmpty()) {
      PdfPTable tblHeader = addHeaderToSection(message.getString("GML.PDC").toUpperCase());
      document.add(tblHeader);

      ResourceLocator messagePdc = new ResourceLocator(
          "com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle", language);
      for (int i = 0; i < listPositions.size(); i++) {
        ClassifyPosition position = listPositions.get(i);
        String nI = String.valueOf(i + 1);
        Paragraph paragraph = new Paragraph(messagePdc.getString("pdcPeas.position") + " " + nI,
            NORMAL_FONT);
        document.add(paragraph);
        com.lowagie.text.List list = new com.lowagie.text.List(false, 20);
        list.setListSymbol(new Chunk("\u2022", new Font(Font.HELVETICA, 20, Font.BOLD, Color.BLACK)));
        List<ClassifyValue> values = position.getValues();
        for (ClassifyValue value : values) {
          List<Value> pathValues = value.getFullPath();
          String path = buildCompletPath(pathValues, false);
          if (path == null) {
            path = PATH_SEPARATOR;
          }
          Chunk chnk = new Chunk(path, FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL));
          list.add(new ListItem(chnk));
        } // fin du for termes
        document.add(list);
      } // fin du for position
    }
  }

  private void parseHTML(Document document, String text)
      throws KmeliaRuntimeException {
    SilverTrace.info("kmelia", "PdfGenerator.parseHTML",
        "root.MSG_ENTRY_METHOD");

    // 1 - Parsing texte html -> replace
    // src="/silverpeas/FileServer par src="/silverpeas/OnlineFileServer
    String textTransformPathFile = text.replaceAll(
        "src=\"" + URLManager.getApplicationURL() + "/FileServer", "src=\""
        + URLManager.getApplicationURL() + "/OnlineFileServer");

    textTransformPathFile = textTransformPathFile.replaceAll(
        "src=\"" + URLManager.getApplicationURL(), "src=\"" + serverURL + URLManager.
        getApplicationURL());

    // Fix : Images from galleries does not appears !
    textTransformPathFile = textTransformPathFile.replaceAll(
        "&amp;ComponentId=", "&ComponentId=");

    // relative font-size do not supported by HtmlWorker !
    textTransformPathFile = textTransformPathFile.replaceAll(
        "font-size: smaller", "font-size: 6pt");
    textTransformPathFile = textTransformPathFile.replaceAll(
        "font-size: larger", "font-size: 6.9pt");
    textTransformPathFile = textTransformPathFile.replaceAll(
        "font-size: xx-small", "font-size: 6.9pt");
    textTransformPathFile = textTransformPathFile.replaceAll(
        "font-size: x-small", "font-size: 8.3pt");
    textTransformPathFile = textTransformPathFile.replaceAll(
        "font-size: small", "font-size: 10pt");
    textTransformPathFile = textTransformPathFile.replaceAll(
        "font-size: medium", "font-size: 12pt");
    textTransformPathFile = textTransformPathFile.replaceAll(
        "font-size: large", "font-size: 14.4pt");
    textTransformPathFile = textTransformPathFile.replaceAll(
        "font-size: x-large", "font-size: 17.28pt");
    textTransformPathFile = textTransformPathFile.replaceAll(
        "font-size: xx-large", "font-size: 20.7pt");

    ByteArrayInputStream inputHtml;
    try {
      inputHtml = new ByteArrayInputStream(textTransformPathFile.getBytes("UTF-8"));

      // 3 - Transformation HTML en XHTML
      ByteArrayOutputStream xhtml = new ByteArrayOutputStream();
      Tidy tidy = new Tidy();
      tidy.setXHTML(true);
      tidy.setDocType("strict");// omit
      tidy.setMakeClean(true);
      tidy.setQuiet(false);
      tidy.setIndentContent(true);
      tidy.setSmartIndent(true);
      tidy.setIndentAttributes(true);
      tidy.setWord2000(true);
      tidy.setShowWarnings(false);
      //tidy.setCharEncoding(Configuration.UTF8);
      tidy.parseDOM(inputHtml, xhtml);
      InputStream inputXhtml = new ByteArrayInputStream(xhtml.toByteArray());
      // 4 - Transformation XHTML en PDF
      HTMLWorker htmlWorker = new HTMLWorker(document);
      Reader reader = new InputStreamReader(inputXhtml, "UTF-8");
      htmlWorker.parse(reader);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "PdfGenerator.parseHTML", "kmelia.CANT_PARSE_HTML", e);
    }
  }

  private void parseModelHTML(Document document, String text, Iterator<InfoTextDetail> textIterator,
      Iterator<InfoImageDetail> imageIterator) throws KmeliaRuntimeException {
    SilverTrace.info("kmelia", "PdfGenerator.parseModelHTML", "root.MSG_ENTRY_METHOD");
    try {
      CharArrayReader html = new CharArrayReader(text.toCharArray());
      ParserDelegator parser = new ParserDelegator();
      ParserCallback callbacktablecolumncounter = new CallbackInfoCollector();
      parser.parse(html, callbacktablecolumncounter, true);
      List<String> columns = ((CallbackInfoCollector) callbacktablecolumncounter).
          getTableColumnCount();
      html = new CharArrayReader(text.toCharArray());
      ParserCallback callback = new Callback(document, columns, textIterator, imageIterator);
      parser.parse(html, callback, false);
    } catch (Exception ex) {
      throw new KmeliaRuntimeException("PdfGenerator.parseModelHTML",
          SilverpeasRuntimeException.WARNING, "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
    }
  }

  private void generateContent(Document document) throws DocumentException, WysiwygException,
      PublicationTemplateException, FormException {
    PdfPTable tblHeader = addHeaderToSection(message.getString("Model").toUpperCase());

    String componentId = kmelia.getComponentId();
    String objectId = publicationDetail.getPK().getId();
    // get displayed language
    String languageToDisplay = publicationDetail.getLanguageToDisplay(publiContentLanguage);
    String sWysiwyg = WysiwygController.load(componentId, objectId, languageToDisplay);
    if (StringUtil.isDefined(sWysiwyg)) {// Wysiwyg
      document.add(tblHeader);
      parseHTML(document, sWysiwyg);
    } else {
      if ((completePublicationDetail.getInfoDetail() != null)
          && (completePublicationDetail.getModelDetail() != null)) {// Modèles
        document.add(tblHeader);
        String toParse = completePublicationDetail.getModelDetail().getHtmlDisplayer();
        Iterator<InfoTextDetail> textIterator = completePublicationDetail.getInfoDetail().
            getInfoTextList().iterator();
        Iterator<InfoImageDetail> imageIterator = completePublicationDetail.getInfoDetail().
            getInfoImageList().iterator();
        parseModelHTML(document, toParse, textIterator, imageIterator);
      } else {// Modèles XML
        String infoId = publicationDetail.getInfoId();
        if (!StringUtil.isInteger(infoId)) {
          document.add(tblHeader);
          PublicationTemplateImpl pubTemplate =
              (PublicationTemplateImpl) PublicationTemplateManager.getInstance().
              getPublicationTemplate(componentId + ":" + infoId);
          Form formView = pubTemplate.getViewForm();
          RecordSet recordSet = pubTemplate.getRecordSet();
          DataRecord data = recordSet.getRecord(objectId, languageToDisplay);
          if (data == null) {
            data = recordSet.getEmptyRecord();
            data.setId(objectId);
          }
          PagesContext context = new PagesContext();
          context.setLanguage(language);
          context.setComponentId(componentId);
          context.setObjectId(objectId);
          context.setBorderPrinted(false);
          context.setContentLanguage(languageToDisplay);
          context.setUserId(kmelia.getUserId());
          context.setNodeId(kmelia.getSessionTopic().getNodeDetail().getNodePK().
              getId());
          String htmlResult = formView.toString(context, data);
          parseHTML(document, htmlResult);
        }
      }
    }
  }
}
