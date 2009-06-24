package com.stratelia.webactiv.kmelia.control;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.w3c.tidy.Tidy;

import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.List;
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
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.stratelia.webactiv.util.DateUtil;
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
import com.stratelia.silverpeas.comment.control.CommentController;
import com.stratelia.silverpeas.comment.model.Comment;
import com.stratelia.silverpeas.comment.model.CommentPK;
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
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoLinkDetail;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class PdfGenerator extends PdfPageEventHelper
{
    /** Current KmeliaSessionController */
    static private KmeliaSessionController kmeliaSessionController = null;
    /** Language */
    static private String language;
    /** ResourceBundle kmelia */
    static private ResourceLocator message = null; 
    /** ResourceBundle genral multilang */
    static private ResourceLocator generalMessage = null;   
    /** Current Complete publication */
    static private CompletePublication completePublicationDetail;
    /** Current publication */
    static private PublicationDetail publicationDetail;
    /** Content language */
    static private String publiContentLanguage;
    /** Image Full Star */
    static private Image fullStar;
    /** Image Empty Star */
    static private Image emptyStar;
    /** The headertable. */
    static private PdfPTable pdfTableHeader;
    /** A template that will hold the total number of pages. */
    static private PdfTemplate pdfTemplate;
    /** The font that will be used. */
    static private BaseFont baseFontHelv;
    
    static private String serverURL; 

    public static void generate(String namePdf, CompletePublication currentPublication, KmeliaSessionController scc) throws KmeliaRuntimeException
    {
        SilverTrace.info("kmelia", "PdfGenerator.generatePubList", "root.MSG_ENTRY_METHOD", "Pdf name = " + namePdf);
        try
        {
        	kmeliaSessionController = scc;
        	language = kmeliaSessionController.getLanguage();
        	message = new ResourceLocator("com.stratelia.webactiv.kmelia.multilang.kmeliaBundle", language);
        	generalMessage = new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", language);
        	
        	completePublicationDetail = currentPublication;
        	publicationDetail = currentPublication.getPublicationDetail();
            String fileName = FileRepositoryManager.getTemporaryPath(publicationDetail.getPK().getSpace(), publicationDetail.getPK().getComponentName()) + namePdf;
            publiContentLanguage = kmeliaSessionController.getCurrentLanguage();
            try {
            	serverURL = scc.getOrganizationController().getDomain(scc.getUserDetail().getDomainId()).getSilverpeasServerURL();
            	fullStar = Image.getInstance(serverURL + URLManager.getApplicationURL() + "/util/icons/starFilled.gif");
            	emptyStar = Image.getInstance(serverURL + URLManager.getApplicationURL() + "/util/icons/starEmpty.gif");
            } catch (Exception e) {
				SilverTrace.warn("kmelia", "PdfGenerator.generate", "root.EX_REMOTE_EXCEPTION", e);
				fullStar = null;
				emptyStar = null;
            }
            
            Document document = new com.lowagie.text.Document(PageSize.A4, 50, 50, 115, 50);
            document.addAuthor(message.getString("GeneratorPdf"));
            document.addSubject(message.getString("SubjectPdf"));
            document.addCreationDate();

            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            pdfWriter.setPageEvent(new PdfGenerator());
            document.open();
              
            //En-Tete
		    generateHeaderPage(document);
		    
		    //Fichiers joints
			generateAttachments(document);
			
			//Voir aussi
			generateSeeAlso(document);
		    
			//Plan de classement
			generateCategorization(document);
			
			//Commentaires
			generateComments(document);
			
			//Contenu
		    generateContent(document);
           
            document.close();
        }
        catch (Exception e)
        {
            throw new KmeliaRuntimeException("PdfGenerator.generate", KmeliaRuntimeException.WARNING, "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", e);
        } 
    }
    
    /**
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onOpenDocument(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     */
    public void onOpenDocument(PdfWriter writer, Document document) {
    	// initialization of the header table
    	//Paramétrage global logo entete pdf
    	String logo = kmeliaSessionController.getSettings().getString("logoPdf");
    	if(! logo.startsWith("http")) {//logo du type /weblib/image.jpg
    		logo = serverURL + logo; 
    	}
    	Image headerImage = null;
		try {
			headerImage = Image.getInstance(new URL(logo));
		} catch (Exception e) {
			 SilverTrace.error( "kmelia", "PdfGenerator.onOpenDocument", "root.EX_REMOTE_EXCEPTION", e );
		} 
			
        pdfTableHeader = new PdfPTable(2);
        Phrase p = new Phrase();
        Chunk ck = new Chunk(publicationDetail.getName(publiContentLanguage)+"\n", new Font(Font.HELVETICA, 12, Font.BOLD));
        p.add(ck);
        
        if (kmeliaSessionController.isPublicationIdDisplayed())
        {
	        String contentHeaderText = message.getString("Codification")+" : "+publicationDetail.getPK().getId();
	        //Version : optionnel
	        if(kmeliaSessionController.isFieldVersionVisible()) {
	        	if(publicationDetail.getVersion() != null && publicationDetail.getVersion().length()>0) {
	        		contentHeaderText += "\n"+message.getString("PubVersion")+" : "+publicationDetail.getVersion();
	        	}
	        }
	        ck = new Chunk(contentHeaderText, new Font(Font.HELVETICA, 10, Font.NORMAL));
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
        if(headerImage != null) {
        	pdfTableHeader.addCell(new Phrase(new Chunk(headerImage, 0, 0)));
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
			SilverTrace.error( "kmelia", "PdfGenerator.onOpenDocument", "root.EX_REMOTE_EXCEPTION", e );
		} 
    }    
    
    /**
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onEndPage(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     */
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        // write the headertable
    	pdfTableHeader.setTotalWidth(document.right() - document.left());
    	pdfTableHeader.writeSelectedRows(0, -1, document.left(), document.getPageSize().getHeight() - 50, cb);
    
        String text = message.getString("editeLe")+" "+DateUtil.dateToString(new Date(), language)+" "+ message.getString("For")+" "+kmeliaSessionController.getUserDetail().getDisplayedName()+" - "+message.getString("Page")+" " + writer.getPageNumber() + "/";
        float textSize = 0;
        if(baseFontHelv != null) {
        	textSize = baseFontHelv.getWidthPoint(text, 8);
        }
        float textBase = document.bottom() - 20;
        cb.beginText();
        float adjust = 0;
        if(baseFontHelv != null) {
        	cb.setFontAndSize(baseFontHelv, 8);
        	adjust = baseFontHelv.getWidthPoint("0", 8);
        }
        cb.setTextMatrix(document.right() - textSize - adjust, textBase);
        cb.showText(text);
        cb.endText();
        cb.addTemplate(pdfTemplate, document.right() - adjust, textBase);
		cb.saveState();
    }
   
    
    /**
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onCloseDocument(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     */
    public void onCloseDocument(PdfWriter writer, Document document) {
	   pdfTemplate.beginText();
	   if(baseFontHelv != null) {
		   pdfTemplate.setFontAndSize(baseFontHelv, 8);
	   }
	   pdfTemplate.setTextMatrix(0, 0);
	   pdfTemplate.showText("" + (writer.getPageNumber() - 1));
	   pdfTemplate.endText();
    }
    
    private static void addRowToTable( Table tbl, Font fnt, String[] cells, Color bg_colour, boolean isBorder, boolean first_column_bold )
    {
        Cell cl = null;
        Chunk chnk;
        Font font = null;
        if ( fnt != null )
        {
          font = fnt;
        }
        for ( int i=0; i<cells.length; i++ )
        {
            if ( i==0 && first_column_bold && fnt == null)
            {
                font = new Font(Font.HELVETICA, 10, Font.BOLD);
            }
            else if ( fnt == null )
            {
                font = new Font(Font.HELVETICA, 10, Font.NORMAL);
            }
            
            try
            {
            	chnk = new Chunk( cells[i], font );
            	cl = new Cell(chnk);
            }
            catch ( Exception ex )
            {
            	cl = new Cell();
            }
            if ( !isBorder )
            {
              cl.setBorderWidth(0);
            }
            if ( bg_colour != null )
            {
              cl.setBackgroundColor(bg_colour);
            }
            tbl.addCell(cl);
        }
    }
    
    private static void addRowToTable( Table tbl, Font fnt, String[] cells, boolean isBorder, boolean first_column_bold )
    {
        addRowToTable( tbl, fnt, cells, null, isBorder, first_column_bold );
    }
    
    private static void addRowImagesToTable( Table tbl, String cell, Image[] tabImage, boolean isBorder)
    {
        Cell cl = null;
        //cell 1 
        Chunk chnk = new Chunk( cell, new Font(Font.HELVETICA, 10, Font.BOLD) );
        try
        {
          cl = new Cell(chnk);
        }
        catch ( Exception ex )
        {
        }
        if ( !isBorder )
        {
          cl.setBorderWidth(0);
        }
        tbl.addCell(cl);
        
        
        //cell 2
        Phrase images = new Phrase();
        for(int i=0; i<tabImage.length;i++) {
        	chnk = new Chunk(tabImage[i], 0, -5);
        	images.add(chnk);
        }
        try
        {
          cl = new Cell(images);
        }
        catch ( Exception ex )
        {
        }
        if ( !isBorder )
        {
          cl.setBorderWidth(0);
        }
        tbl.addCell(cl);
        
    }
    
    private static void addRowLinkToTable( Table tbl, String[] cells, Color[] colours, boolean isBorder, boolean first_column_bold )
    {
        Cell cl = null;
        Chunk chnk;
        Font font = null;
        for ( int i=0; i<cells.length; i++ )
        {
            if ( i==0 && first_column_bold)
            {
                font = new Font(Font.HELVETICA, 10, Font.BOLD);
            }
            else 
            {
                font = new Font(Font.HELVETICA, 10, Font.NORMAL);
                font.setStyle(Font.UNDERLINE);
            }
            
            if ( colours != null )
            {
            	font.setColor( colours[i]);
            }
            
            chnk = new Chunk( cells[i], font );
            try {
	            if (i>0) {
	            	chnk.setAnchor(new URL(cells[i]));
	            }
            }catch ( Exception ex )
            {
            }
            try
            {
              cl = new Cell(chnk);
            }
            catch ( Exception ex )
            {
            }
            if ( !isBorder )
            {
              cl.setBorderWidth(0);
            }
            tbl.addCell(cl);
        }
    }
    
    private static void addRowImageToTable( Table tbl, String cell, Image image, boolean isBorder)
    {
        Cell cl = null;
        //cell 1 
        Chunk chnk = new Chunk( cell, new Font(Font.HELVETICA, 10, Font.BOLD) );
        try
        {
          cl = new Cell(chnk);
        }
        catch ( Exception ex )
        {
        }
        if ( !isBorder )
        {
          cl.setBorderWidth(0);
        }
        tbl.addCell(cl);
        
        
        //cell 2
        chnk = new Chunk(image, 0, 0);
        try
        {
          cl = new Cell(chnk);
        }
        catch ( Exception ex )
        {
        }
        if ( !isBorder )
        {
          cl.setBorderWidth(0);
        }
        tbl.addCell(cl);
        
    }
    
    private static void addRowToTable( Table tbl, String[] cells)
    {
        Cell cl = null;
        Chunk chnk;
        Font font = null;
     
        for ( int i=0; i<cells.length; i++ )
        {
            if ( i==0 || i==2)
            {
                font = new Font(Font.HELVETICA, 10, Font.BOLD);
            }
            else 
            {
                font = new Font(Font.HELVETICA, 10, Font.NORMAL);
            }
            
            chnk = new Chunk( cells[i], font );
            try
            {
              cl = new Cell(chnk);
            }
            catch ( Exception ex )
            {
            }
            cl.setBorderWidth(0);
            tbl.addCell(cl);
        }
    }
    
    private static String getTopicPath() throws RemoteException
    {
        String out = "";
        Collection pathList = kmeliaSessionController.getPathList(publicationDetail.getPK().getId());
        Iterator i = pathList.iterator();
        Collection path;
        Iterator j;
        String pathString = "";
        int nbItemInPath;
        boolean alreadyCut = false;
        int nb = 0;
        NodeDetail nodeInPath;
        while (i.hasNext())
        {
        	path = (Collection) i.next();
        	j = path.iterator();
        	pathString = "";
        	nbItemInPath = path.size();
        	alreadyCut = false;
            nb = 0;
            while (j.hasNext()) {
            	nodeInPath = (NodeDetail) j.next();
            	if ((nb <= 3) || (nb + 3 >= nbItemInPath - 1))
            	{
            		pathString = nodeInPath.getName() + " " + pathString;
                    if (j.hasNext()) {
                    	pathString = " > " + pathString;
                    }
               }
               else
               {
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
    
    private static Table addHearderToSection(String title) throws DocumentException {
    	Table tblHeader = new Table(1);
    	int headerwidthsHeader[] = {100};
    	tblHeader.setWidths(headerwidthsHeader);
    	tblHeader.setWidth(100);
    	tblHeader.setBorderWidth(0);
    	tblHeader.getDefaultCell().setHorizontalAlignment(Cell.ALIGN_CENTER);
    	tblHeader.getDefaultCell().setBorderWidthTop(0);
    	tblHeader.getDefaultCell().setBorderWidthBottom(1);
    	tblHeader.getDefaultCell().setBorderWidthLeft(0);
    	tblHeader.getDefaultCell().setBorderWidthRight(0);
    	tblHeader.setPadding(2);
    	Font header_font = new Font(Font.HELVETICA, 10, Font.BOLD);
        Chunk chnk = new Chunk(title, header_font );
        Cell cl = new Cell(chnk);
        tblHeader.addCell(cl);
    	tblHeader.endHeaders();
    	return tblHeader;
    }
    
    private static void generateHeaderPage(Document document) throws DocumentException, RemoteException 
    {
    	Table tblHeader = addHearderToSection(message.getString("Header").toUpperCase());
        document.add(tblHeader);
         
        Table tbl = new Table(2);
        tbl.setBorderWidth(0);
        tbl.setAlignment(Table.ALIGN_LEFT);
        tbl.setWidth(100);
        int headerwidths[] = {25, 75};
    	tbl.setWidths(headerwidths);
       
    	//Emplacement
        addRowToTable( tbl, null, new String[] {message.getString("TopicPath") + " :", getTopicPath()}, false, true );
        
        //Permalien
        boolean displayLinks = URLManager.displayUniversalLinks();
        if(displayLinks) {
        	addRowLinkToTable( tbl, new String[] {message.getString("Link") + " :", serverURL + URLManager.getSimpleURL(URLManager.URL_PUBLI, publicationDetail.getPK().getId())}, new Color[] {Color.BLACK, Color.BLUE} , false, true);
        }
        
        //Vignette : optionnel
        ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", language);
        if (new Boolean(settings.getString("isVignetteVisible")).booleanValue()) {
        	if (publicationDetail.getImage() != null) {
        		ResourceLocator publicationSettings = new ResourceLocator("com.stratelia.webactiv.util.publication.publicationSettings", language);
                String vignette_url = serverURL + FileServerUtils.getUrl("useless", publicationDetail.getPK().getComponentName(), "vignette", publicationDetail.getImage(), publicationDetail.getImageMimeType(), publicationSettings.getString("imagesSubDirectory")); 
                vignette_url = vignette_url.replaceAll("/FileServer", "/OnlineFileServer");     
                try {
					Image image = Image.getInstance(new URL(vignette_url));
					addRowImageToTable( tbl, message.getString("Vignette") + " :", image, false);
				} catch (Exception e) {
					SilverTrace.error( "kmelia", "PdfGenerator.onOpenDocument", "root.EX_REMOTE_EXCEPTION", e );
				} 
			}
        }
        
        //Description : optionnel
        if(kmeliaSessionController.isFieldDescriptionVisible()) {
        	String sDescription = publicationDetail.getDescription(publiContentLanguage);
            if (sDescription != null && sDescription.length()>0) {
            	addRowToTable( tbl, null, new String[] {message.getString("Description") + " :", sDescription}, false, true);
            }
        }
        
        //Mots clés : optionnel
        if (kmeliaSessionController.isFieldKeywordsVisible()) {
        	String sKeywords = publicationDetail.getKeywords(publiContentLanguage);
        	if (sKeywords != null && sKeywords.length()>0) {
        		addRowToTable( tbl, null, new String[] {message.getString("PubMotsCles") + " :", sKeywords}, false, true);
        	}
        }
        
        //Auteur : optionnel
        if(kmeliaSessionController.isAuthorUsed()) {
        	 String sAuthor = publicationDetail.getAuthor();
        	 if (sAuthor != null && sAuthor.length()>0) {
        		 addRowToTable( tbl, null, new String[] {generalMessage.getString("GML.author", language) + " :", sAuthor}, false, true);
        	 }
        }
        
        //Importance : optionnel
        if(kmeliaSessionController.isFieldImportanceVisible() && fullStar != null && emptyStar != null) {
        	String importance = new Integer(publicationDetail.getImportance()).toString();
        	if (importance.equals("")) {
	          	importance = "1";
        	}
      		int sImportance = new Integer(importance).intValue();
        	Image[] tabImage = new Image[5];
	        //display full Stars
        	for(int i=0; i<sImportance; i++) {
	        	tabImage[i] = fullStar;
	        }
	        //display empty stars
	        for (int i=sImportance; i<5; i++) {
	        	tabImage[i] = emptyStar;
	        }
	        addRowImagesToTable( tbl, message.getString("PubImportance") + " :", tabImage, false);
        }
        
        //Valideurs publication
        if(publicationDetail.getValidatorId() != null) {
        	String validatorDate = kmeliaSessionController.getUserDetail(publicationDetail.getValidatorId()).getDisplayedName();
        	if(publicationDetail.getValidateDate() != null) {
        		validatorDate += " ("+DateUtil.getOutputDate(publicationDetail.getValidateDate(), language)+")";
        	}
        	addRowToTable( tbl, null, new String[] {message.getString("kmelia.Valideur") + " :", validatorDate}, false, true);
        }
        
        document.add(tbl);
        
        Table tbl2 = new Table(4);
        tbl2.setBorderWidth(0);
        tbl2.setAlignment(Table.ALIGN_LEFT);
        tbl2.setWidth(100);
        int headerwidths2[] = {25, 30, 15, 30};
        tbl2.setWidths(headerwidths2);
        
        
        String creatorName = "";
        UserDetail ownerDetail = kmeliaSessionController.getUserCompletePublication(publicationDetail.getPK().getId()).getOwner();
        if (ownerDetail != null) {
            creatorName = ownerDetail.getDisplayedName();
        }
        else {
            creatorName = message.getString("UnknownAuthor");
        }
        
        String messageUpdatedDate = "";
        String sUpdated = "";
        String updateDate = null;
        if(publicationDetail.getUpdateDate() != null) {
        	updateDate = DateUtil.getOutputDate(publicationDetail.getUpdateDate(), language);
        }
        String updaterName = null;
        UserDetail updater = kmeliaSessionController.getUserDetail(publicationDetail.getUpdaterId());
        if (updater != null) {
        	updaterName = updater.getDisplayedName();
        }
        
        if (updateDate != null && updateDate.length()>0 && updaterName != null && updaterName.length()>0) {
        	messageUpdatedDate = message.getString("PubDateUpdate")+" :";
        	sUpdated = updateDate + " "+ message.getString("kmelia.By")+" "+updaterName;
        }
        //Créé par, Modifiée par
        addRowToTable( tbl2, new String[] {message.getString("PubDateCreation") + " :", DateUtil.getOutputDate(publicationDetail.getCreationDate(), language) + " "+message.getString("kmelia.By")+ " "+creatorName, messageUpdatedDate, sUpdated});
        
        String beginDate = "";
        if (publicationDetail.getBeginDate() != null) {
            beginDate = DateUtil.getInputDate(publicationDetail.getBeginDate(), language);
        }
        String beginHour = "";
        if (beginDate.length()>0) {
    		beginHour	= publicationDetail.getBeginHour();
        }
        
        String endDate = "";
        if (publicationDetail.getEndDate() != null) {
            if (DateUtil.date2SQLDate(publicationDetail.getEndDate()).equals("1000/01/01")) {
                endDate = "";
            }
            else {
                endDate = DateUtil.getInputDate(publicationDetail.getEndDate(), language);
            }
        }
        String endHour = "";
        if (endDate.length()>0) {
    		endHour		= publicationDetail.getEndHour();
        }    		
    	
    	if(beginDate.length()>0 && endDate.length()>0) {
    		//Consultable du 'Date' à 'Heure' au 'Date' à 'Heure' 
    		addRowToTable( tbl2, new String[] {message.getString("PubDateDebut")+ " :", beginDate + " "+message.getString("ToHour")+" "+beginHour, message.getString("PubDateFin") + " :", endDate+ " "+message.getString("ToHour")+" "+endHour, "", ""});
    	} else if(beginDate.length()>0) {
    		//Consultable à partir du 'Date' à 'Heure' 
    		addRowToTable( tbl2, new String[] {message.getString("PubDateDebutPdf")+ " :", beginDate + " "+message.getString("ToHour")+" "+beginHour, "", "", "", ""});
    	} else if(endDate.length()>0) {
    		//Consultable jusqu'au 'Date' à 'Heure' 
    		addRowToTable( tbl2, new String[] {message.getString("PubDateFinPdf")+ " :", endDate + " "+message.getString("ToHour")+" "+endHour, "", "", "", ""});
    	}
        
        document.add(tbl2);
        document.add(new Paragraph("\n"));
    }

    private static void addRowToTable( Table tbl, Font fnt, String[] cells, Color bg_colour, boolean isBorder )
    {
        addRowToTable( tbl, fnt, cells, bg_colour, isBorder, false );
    }
    
    private static Table addTableAttachments(String mode) throws DocumentException {
    	Table tbl = null;

    	Font header_font = new Font(Font.HELVETICA, 10, Font.BOLD);
    	header_font.setColor( new Color(255, 255, 255) );
    	
    	ResourceLocator messageAttachment = new ResourceLocator("com.stratelia.webactiv.util.attachment.multilang.attachment", language);
        
    	if("A".equals(mode)) {
    		
	    	//Attachments links
    		tbl = new Table(5);
    		int headerwidths[] = {25, 25, 30, 10, 10};
        	tbl.setWidths(headerwidths);
        	tbl.setWidth(100);
        	tbl.setBorderWidth(0);
        	tbl.setPadding(2);
	    	addRowToTable( tbl, header_font, new String[] {
	    						messageAttachment.getString("fichier"),
	    						messageAttachment.getString("Title"),
	    						messageAttachment.getString("Description"),
	    						messageAttachment.getString("taille"),
	    						messageAttachment.getString("uploadDate")},
	    						Color.LIGHT_GRAY, true );
    	} else if("V".equals(mode)) {
    		
    		ResourceLocator messageVersioning = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", language);
            
    		//Versioning links
    		tbl = new Table(6);
    		int headerwidths[] = {20, 15, 20, 10, 15, 20};
        	tbl.setWidths(headerwidths);
        	tbl.setWidth(100);
        	tbl.setBorderWidth(0);
        	tbl.setPadding(2);
        	addRowToTable( tbl, header_font, new String[] {
	    			messageAttachment.getString("fichier"), //pas affiché dans l'interface onglet fichiers joints
	    			messageVersioning.getString("name"),
	    			messageAttachment.getString("Description"), //pas affiché dans l'interface onglet fichiers joints
					messageVersioning.getString("version"),
					messageVersioning.getString("date"),
					messageVersioning.getString("validator")}, //pas affiché dans l'interface onglet fichiers joints mais besoin INRA -> si Ordonné avec approbation : "Valideurs" = liste des valideurs, sinon "Créateur" = créateur version
					Color.LIGHT_GRAY, true );
    	}
    	
    	tbl.endHeaders();
    	return tbl;
    }
    
    
    private static boolean isUserReader(com.stratelia.silverpeas.versioning.model.Document document, int user_id, VersioningUtil versioningUtil) /*throws RemoteException*/
    {
        try
        {
            ArrayList readers = document.getReadList();
            ArrayList writers = versioningUtil.getAllNoReader(document);
            com.stratelia.silverpeas.versioning.model.Reader user;

            for ( int i=0; readers != null  && i<readers.size(); i++ )
            {
                user = (com.stratelia.silverpeas.versioning.model.Reader) readers.get(i);
                if ( user.getUserId() == user_id )
                {
                    return true;
                }
            }

            for ( int i=0; writers != null && i<writers.size(); i++ )
            {
                user = (com.stratelia.silverpeas.versioning.model.Reader) writers.get(i);
                if ( user.getUserId() == user_id )
                {
                    return true;
                }
            }
        } catch (Exception e)
        {
            SilverTrace.error( "kmelia", "PdfGenerator.isUserReader", "root.EX_REMOTE_EXCEPTION", e );
        }

        return false;
    }

    /**
     * 
     * @param documentPdf
     * @throws DocumentException
     * @throws RemoteException
     */
    private static void generateAttachments(Document documentPdf) throws DocumentException, RemoteException
    {
        Table tblHeader = addHearderToSection(message.getString("Attachments").toUpperCase());
       
    	Table tbl;
    	String idPubli = publicationDetail.getPK().getId();
    	String instanceId = kmeliaSessionController.getComponentId();
    	
    	if (kmeliaSessionController.isVersionControlled()) 
    	{
    		//Versioning links
    		//liste des fichiers attachés 
    		VersioningUtil versioningUtil = new VersioningUtil();
    		ForeignPK foreignKey = new ForeignPK(idPubli, instanceId);
    		ArrayList documents = versioningUtil.getDocuments(foreignKey);
    		com.stratelia.silverpeas.versioning.model.Document document;

    		if (documents != null && documents.size()>0) {
    			//Titre
    			documentPdf.add(tblHeader);
    			
    			//En-tete tableau des fichiers attachés
        		tbl = addTableAttachments("V");
        		
	    	    Iterator documents_iterator = documents.iterator();
	    	    boolean is_reader = false;
	    	    int user_id = new Integer(kmeliaSessionController.getUserId()).intValue();
	    	    ArrayList versions;
	    	    DocumentVersion document_version = null;
	    	    String creation_date;
	    	    String creatorOrValidators = "";
	    	    ArrayList users;
	    	    Worker user;
	    	    while(documents_iterator.hasNext())
	    	    {
	    	    	document = (com.stratelia.silverpeas.versioning.model.Document) documents_iterator.next();
	    	    	 
	    	    	/* Solution 1 : affichage de la dernière version (publique ou privée) */
	    	        is_reader = isUserReader( document, user_id, versioningUtil );
		            if ( versioningUtil.isWriter(document, user_id) || is_reader || "admin".equals(kmeliaSessionController.getUserRoleLevel()) )
		            {
		                versions = versioningUtil.getDocumentFilteredVersions(document.getPk(), user_id);
		                if ( versions.size() > 0 )
		                {
		                    document_version = (DocumentVersion)(versions.get(versions.size()-1)); //current version
		                    creation_date = DateUtil.dateToString(document.getLastCheckOutDate(), language);
	
		                    if ( document_version.getSize() != 0 || !"dummy".equals(document_version.getLogicalName()) )
		                    {
		                    	if (2 == document.getTypeWorkList()) {//Ordonné avec Approbation
		                    		users = document.getWorkList();
		                    		for ( int i=0; i<users.size(); i++ )
		                    	    {
		                    	        user = (Worker) users.get(i);
		                    	        if(user.isApproval()) {
		                    	        	creatorOrValidators += kmeliaSessionController.getOrganizationController().getUserDetail(new Integer(user.getUserId()).toString()).getDisplayedName()+", ";
		                    	        }
		                    	    }
		                    		if(creatorOrValidators.length()>0) {
		                    			creatorOrValidators = creatorOrValidators.substring(0, creatorOrValidators.length() - 2);
		                    		}
		                    	} else {//Autres cas
		                    		creatorOrValidators = kmeliaSessionController.getOrganizationController().getUserDetail(new Integer(document_version.getAuthorId()).toString()).getDisplayedName();
		                    	}
		                    	addRowToTable( tbl, null, new String[] {document_version.getLogicalName(), 
		                    										document.getName(), document.getDescription(), 
		                    										document_version.getMajorNumber() + "." + document_version.getMinorNumber(), 
		                    										creation_date, creatorOrValidators}, true, false );
		                    }
		                    else
		                    {
		                    	addRowToTable( tbl, null, new String[] {document_version.getLogicalName(), 
		                    									document.getName(), document.getDescription(), 
		                    									"", creation_date, 
		                    									kmeliaSessionController.getOrganizationController().getUserDetail(new Integer(document.getOwnerId()).toString()).getDisplayedName()}, true, false );
		                    }
						}
		            }
	    	    }
	        	documentPdf.add(tbl);
    		}
    	} else 
    	{
    		//Attachments links
    		
    		//liste des fichiers attachés
    		//récupération des fichiers attachés à un événement
    		//create foreignKey with componentId and customer id
    		//use AttachmentPK to build the foreign key of customer object.
    		AttachmentPK foreignKey = new AttachmentPK(idPubli, instanceId);
    		Vector vectAttachment = AttachmentController.searchAttachmentByPKAndContext(foreignKey, "Images");
    		if(vectAttachment != null && vectAttachment.size()>0) {
    			//Titre
    			documentPdf.add(tblHeader);
    			
    			//En-tete tableau des fichiers attachés
        		tbl = addTableAttachments("A");  
        		
	    		Iterator itAttachment = vectAttachment.iterator();
	    		AttachmentDetail attachmentDetail = null;
	    		while (itAttachment.hasNext()) {
	                attachmentDetail = (AttachmentDetail) (itAttachment.next());
	                addRowToTable( tbl, null, new String[] {attachmentDetail.getLogicalName(publiContentLanguage), 
	                									attachmentDetail.getTitle(publiContentLanguage), 
	                									attachmentDetail.getInfo(publiContentLanguage), 
	                									attachmentDetail.getAttachmentFileSize(publiContentLanguage), 
	                									DateUtil.getOutputDate(attachmentDetail.getCreationDate(publiContentLanguage), language)}, true, false );
	    		}
	    		
	        	documentPdf.add(tbl);
    		}
    	}
    }
    
    private static String getUserName(UserPublication userPub)
    {
    	UserDetail			user		= userPub.getOwner(); //contains creator
    	PublicationDetail	pub			= userPub.getPublication();
    	String 				updaterId	= pub.getUpdaterId();
    	UserDetail			updater		= null;
    	if (updaterId != null && updaterId.length()>0) {
    		updater = kmeliaSessionController.getUserDetail(updaterId);
    	}
    	if (updater == null) {
    		updater = user;
    	}
    	
    	String userName = "";
    	if (updater != null && (updater.getFirstName().length() > 0 || updater.getLastName().length() > 0)) {
    		userName = updater.getFirstName() + " " + updater.getLastName();
    	}
    	else {
    		userName = message.getString("kmelia.UnknownUser");
    	}
    	
    	return userName;
    }
    
    private static void generateSeeAlso(Document document) throws IOException, DocumentException
    {
    	Collection targets = completePublicationDetail.getInfoDetail().getInfoLinkList();
    	if(targets != null && targets.size()>0) {
    		/*Paragraph title = new Paragraph("\n"+message.getString("PubReferenceeParAuteur")+" : ", new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 0, 0)));
        	document.add(title);*/
    		Table tblHeader = addHearderToSection(message.getString("PubReferenceeParAuteur").toUpperCase());
    		document.add(tblHeader);
        	
        	List list = new List(false, 20);
        	list.setListSymbol(new Chunk("\u2022", new Font(Font.HELVETICA, 20, Font.BOLD, new Color(0, 0, 0))));
        	
	        Iterator targetIterator = targets.iterator();
	        ArrayList targetIds = new ArrayList();
	        String targetId;
	        while (targetIterator.hasNext()) {
	        	targetId = ((InfoLinkDetail) targetIterator.next()).getTargetId();
	        	targetIds.add(targetId);
	        }
	        Collection linkedPublications = kmeliaSessionController.getPublications(targetIds);
	        Iterator iterator = linkedPublications.iterator();
	        UserPublication userPub;
	        PublicationDetail pub;
	        Chunk permalinkPubli;
	        String importance;
	        int sImportance;
	        Image[] tabImage;
	        Phrase permalinkStar;
	        Chunk chnk;
	        ListItem listItem;
	        List sublist;
	        ListItem subListItem;
	        if (iterator.hasNext()) {  
	           	while (iterator.hasNext()) {
	                userPub = (UserPublication) iterator.next();
	                pub = userPub.getPublication();
	                
	                if ( pub.getStatus() != null && pub.getStatus().equals("Valid") && !pub.getPK().getId().equals(publicationDetail.getPK().getId())) {
	                	permalinkPubli = new Chunk(pub.getPK().getId()+" - "+pub.getName(publiContentLanguage), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.UNDERLINE, Color.BLUE));
	                	permalinkPubli.setAnchor(new URL(serverURL + URLManager.getSimpleURL(URLManager.URL_PUBLI, pub.getPK().getId())));
	                	permalinkStar = new Phrase();
	        	        permalinkStar.add(permalinkPubli);
	        	        
	                	//Importance : optionnel
	                	if(kmeliaSessionController.isFieldImportanceVisible() && fullStar != null && emptyStar != null) {
	                		importance = new Integer(pub.getImportance()).toString();
	                    	if (importance.equals("")) {
	            	          	importance = "1";
	                    	}
	                  		sImportance = new Integer(importance).intValue();
	                    	tabImage = new Image[5];
	            	        //display full Stars
	                    	for(int i=0; i<sImportance; i++) {
	            	        	tabImage[i] = fullStar;
	            	        }
	            	        //display empty stars
	            	        for (int i=sImportance; i<5; i++) {
	            	        	tabImage[i] = emptyStar;
	            	        }
	            	        for(int i=0; i<tabImage.length;i++) {
	            	        	chnk = new Chunk(tabImage[i], 0, -5);
	            	        	permalinkStar.add(chnk);
	            	        }
	                	}
	                	
	                	listItem = new ListItem(permalinkStar);
	                    list.add(listItem);  		
							
	                    sublist = new List(false, false, 8);
	                    sublist.setListSymbol(new Chunk("", FontFactory.getFont(FontFactory.HELVETICA, 8)));
	                    chnk = new Chunk(getUserName(userPub)+ 
	                    					" - "+
	                    				DateUtil.getOutputDate(pub.getUpdateDate(), language)+
	                    				"\n"+
	                    				pub.getDescription(publiContentLanguage), 
	                    				FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL));
	                    subListItem = new ListItem(chnk);
	                    sublist.add(subListItem);
	                    list.add(sublist);	
	                }
	           	} // End while
	        } // End if   
	        
	        document.add(list);
    	}
    }
    
    private static void generateComments(Document document) throws DocumentException, RemoteException, ParseException
    {
    	Vector comments = CommentController.getAllComments(new CommentPK(publicationDetail.getPK().getId(), null, kmeliaSessionController.getComponentId()));
    	if(comments != null && comments.size()>0) {
    		/*Paragraph title = new Paragraph("\n"+message.getString("Comments")+" : ", new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 0, 0)));
        	document.add(title);*/
    		
    		Table tblHeader = addHearderToSection(message.getString("Comments").toUpperCase());
    		document.add(tblHeader);
        	
        	ResourceLocator messageComment = new ResourceLocator("com.stratelia.webactiv.util.comment.multilang.comment", language);
            
        	Font header_font = new Font(Font.HELVETICA, 10, Font.BOLD);
        	header_font.setColor( new Color(255, 255, 255) );
        	
        	Table tbl = new Table(4);
        	int headerwidths[] = {25, 55, 20, 20};
        	tbl.setWidths(headerwidths);
        	tbl.setWidth(100);
        	tbl.setBorderWidth(0);
        	tbl.setPadding(2);
        	addRowToTable( tbl, header_font, new String[] {
        						messageComment.getString("author"),
        						messageComment.getString("c_comment"),
        						messageComment.getString("created"),
        						messageComment.getString("modified")},
        						Color.LIGHT_GRAY, true );
        	tbl.endHeaders();
    		
	        Comment comment;
	        for ( int i=0; i< comments.size(); i++ )
	        {
	            comment = (Comment)comments.get(i);
	            
	            addRowToTable( tbl, null, new String[] {comment.getOwner(), 
	            										comment.getMessage(), 
	            										DateUtil.getOutputDate(comment.getCreationDate(), language), 
	            										DateUtil.getOutputDate(comment.getModificationDate(), language)}, true, false );
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
    private static String linkedNode(Value unit, boolean isLinked){
        String node = "";
        
        // Attention la partie hyperlink est a faire !!!!
        if (isLinked){
        	node = "<a href="+(String)unit.getPath()+">"+(String)unit.getName(language)+"</a>";
        } else {
            node = (String)unit.getName(language);
        }

        return node;
    }
    
    /**
    * Cette methode construit le chemin complet tronque pour acceder a une valeur
    * @param list - un objet contenant une liste de liste(nom+url). Cette valeur ne doit pas etre nulle
    * @param completPath - le chemin que l'on veut tronquer
    * @param withLastValue - on garde ou non la valeur selectionnee
    * @return completPath - le chemin fabrique
    */
    private static String troncatePath(String completPath, java.util.List list, boolean isLinked, int withLastValue){
    	int nbShowedEltAuthorized = 2 ; // nombre de noeud que l'on veut afficher avant les ...
    	String separatorPath		= " / "; // separateur pour le chemin complet
    	String troncateSeparator	= " ... ";
    	
		Value value = null;
	    // prend les nbShowedEltAuthorized 1er elements
	    for (int nb=0; nb < nbShowedEltAuthorized; nb++){
			value = (Value) list.get(nb);
	        completPath +=  linkedNode(value, isLinked)+separatorPath;
	    }
	
	    // colle ici les points de suspension
	    completPath += troncateSeparator+separatorPath;
	
	    // prend les nbShowedEltAuthorized derniers elements
	    for (int nb=nbShowedEltAuthorized+withLastValue ; nb>withLastValue ; nb--){
			value = (Value) list.get(list.size() - nb);
	        completPath +=  linkedNode(value, isLinked)+separatorPath;
	    }
	
	    return completPath;
    }
    
    /**
    * Cette methode construit le chemin complet pour acceder a une valeur
    * @param list - un objet contenant une liste de liste(nom+url). Cette valeur ne doit pas etre nulle
    * @param isLinked - vrai si l'on souhaite un hyperlien faux si l'on ne veut que du texte
    * @param withLastNode - 0 si l'on veut afficher le chemin complet de la valeur selectionnee. 
    *                                               1 si l'on ne souhaite afficher que le chemin complet sans la valeur selectionnee
    * @return completPath - le chemin fabrique
    */
    private static String buildCompletPath(java.util.List list, boolean isLinked, int withLastValue) {
    	int maxEltAuthorized = 5; // nombre min d'elements avant la troncature du chemin 
    	String separatorPath		= " / "; // separateur pour le chemin complet
    	
    	String completPath = "";
        // on regarde d'en un 1er temps le nombre d'element de la liste que l'on recoit.
        // si ce nombre est strictement superieur a maxEltAuthorized alors on doit tronquer le chemin complet
        // et l'afficher comme suit : noeud1 / noeud2 / ... / noeudn-1 / noeudn
		Value value = null;
        if (list.size() > maxEltAuthorized){
        	completPath = troncatePath(completPath,list,isLinked,withLastValue);
        } else {
        	for (int nb=0; nb<list.size()-withLastValue;nb++ ){
        		value = (Value) list.get(nb);
        		completPath += linkedNode(value,isLinked)+separatorPath;
        	}
        }

        if ( (completPath == "") || (completPath.equals("/")) ){
        	completPath = null;
        } else {
        	completPath = completPath.substring(0,completPath.length()-separatorPath.length()); // retire le dernier separateur
        }

        return completPath;
    }
    
    private static String buildCompletPath(java.util.List list, boolean isLinked){
        return buildCompletPath(list, isLinked, 0);
    }

    private static void generateCategorization(Document document) throws DocumentException, PdcException
    {
    	PdcBm pdcBm = (PdcBm) new PdcBmImpl();
    	java.util.List listPositions = pdcBm.getPositions(kmeliaSessionController.getSilverObjectId(publicationDetail.getPK().getId()), kmeliaSessionController.getComponentId());
    	if(listPositions != null && listPositions.size()>0) {
    		/*Paragraph title = new Paragraph("\n"+generalMessage.getString("GML.PDC")+" : ", new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 0, 0)));
        	document.add(title);*/
    		Table tblHeader = addHearderToSection(generalMessage.getString("GML.PDC").toUpperCase());
    		document.add(tblHeader);
        	
	    	ResourceLocator messagePdc = new ResourceLocator("com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle", language);
	    	String separatorPath = " / "; // separateur pour le chemin complet
	    	ClassifyPosition position = null;
	    	String nI;
	    	java.util.List values = null;
	    	Paragraph paragraph;
	    	List list;
	    	ClassifyValue value = null;
	    	java.util.List pathValues = null;
	    	String path = null;
	    	Chunk chnk;
	    	ListItem listItem;
	    	for (int i = 0; i<listPositions.size(); i++) { 
	    		position = (ClassifyPosition) listPositions.get(i);
	    		nI = new Integer(i+1).toString(); 
	    			
	    		paragraph = new Paragraph(messagePdc.getString("pdcPeas.position")+" "+ nI, new Font(Font.HELVETICA, 10, Font.NORMAL));
	        	document.add(paragraph);
	        	
	        	list = new List(false, 20);
	        	list.setListSymbol(new Chunk("\u2022", new Font(Font.HELVETICA, 20, Font.BOLD, new Color(0, 0, 0))));
	    	
	        	values = position.getValues();
	        	for (int v = 0; v < values.size(); v++) {
	        		value = (ClassifyValue) values.get(v);
	        		pathValues	= value.getFullPath();
	        		path = buildCompletPath(pathValues, false);
	        		if (path == null) {
	        			path = separatorPath;
	        		}
	        		
	        		chnk = new Chunk(path, FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL));
	        		listItem = new ListItem(chnk);
	                list.add(listItem);
	                
	        	} //fin du for termes
	        	
	        	document.add(list);
	    	} //fin du for position
    	}
    }
    
    private static void parseHTML(Document document, String text) throws KmeliaRuntimeException
    {
		SilverTrace.info("kmelia", "PdfGenerator.parseHTML", "root.MSG_ENTRY_METHOD");
	    
    	//1 - Parsing texte html -> replace src="/silverpeas/FileServer par src="/silverpeas/OnlineFileServer
    	String textTransformPathFile = text.replaceAll("src=\""+URLManager.getApplicationURL()+"/FileServer", "src=\""+URLManager.getApplicationURL()+"/OnlineFileServer");
    	
    	//2 - Parsing texte html -> replace src="/silverpeas par l'URL absolu : src="http://localhost:8000/silverpeas
    	textTransformPathFile = textTransformPathFile.replaceAll("src=\""+URLManager.getApplicationURL(), "src=\""+serverURL+ URLManager.getApplicationURL());
 
        	//Fix : Images from galleries does not appears !
    	textTransformPathFile = textTransformPathFile.replaceAll("&amp;ComponentId=", "&ComponentId=");

    	//relative font-size do not supported by HtmlWorker !
    	textTransformPathFile = textTransformPathFile.replaceAll("font-size: smaller", "font-size: 6pt");
    	textTransformPathFile = textTransformPathFile.replaceAll("font-size: larger", "font-size: 6.9pt");
    	textTransformPathFile = textTransformPathFile.replaceAll("font-size: xx-small", "font-size: 6.9pt");
    	textTransformPathFile = textTransformPathFile.replaceAll("font-size: x-small", "font-size: 8.3pt");
    	textTransformPathFile = textTransformPathFile.replaceAll("font-size: small", "font-size: 10pt");
    	textTransformPathFile = textTransformPathFile.replaceAll("font-size: medium", "font-size: 12pt");
    	textTransformPathFile = textTransformPathFile.replaceAll("font-size: large", "font-size: 14.4pt");
    	textTransformPathFile = textTransformPathFile.replaceAll("font-size: x-large", "font-size: 17.28pt");
    	textTransformPathFile = textTransformPathFile.replaceAll("font-size: xx-large", "font-size: 20.7pt");
    	
    	ByteArrayInputStream inputHtml = new ByteArrayInputStream(textTransformPathFile.getBytes());
    	
    	//3 - Transformation HTML en XHTML
    	ByteArrayOutputStream xhtml = new ByteArrayOutputStream();
    	Tidy tidy = new Tidy();
    	tidy.setXHTML(true);
    	tidy.setDocType("strict");//omit
    	tidy.setMakeClean(true);
    	tidy.setQuiet(false);
    	tidy.setIndentContent(true);
    	tidy.setSmartIndent(true);
    	tidy.setIndentAttributes(true);
    	tidy.setWord2000(true);
    	tidy.setShowWarnings(false);
    	tidy.parseDOM(inputHtml, xhtml);
    	
    	InputStream inputXhtml = new ByteArrayInputStream(xhtml.toByteArray());
    	
    	//4 - Transformation XHTML en PDF
    	HTMLWorker htmlWorker = new HTMLWorker(document);
    	Reader reader = new InputStreamReader(inputXhtml);
    	try {
			htmlWorker.parse(reader);
		} catch (Exception e) {
			SilverTrace.error("kmelia", "PdfGenerator.parseHTML", "kmelia.CANT_PARSE_HTML", e);
		}
    }
    
    private static void parseModelHTML(Document document, String text, Iterator textIterator, Iterator imageIterator ) throws KmeliaRuntimeException
    {
		SilverTrace.info("kmelia", "PdfGenerator.parseModelHTML", "root.MSG_ENTRY_METHOD");
	    
        try
        {
            CharArrayReader html = new CharArrayReader(text.toCharArray());
            ParserDelegator parser = new ParserDelegator();
            HTMLEditorKit.ParserCallback callbacktablecolumncounter = new CallbackInfoCollector();
            parser.parse(html, callbacktablecolumncounter, true);
            Vector columns = ((CallbackInfoCollector)callbacktablecolumncounter).getTableColumnCount();

            html = new CharArrayReader(text.toCharArray());
            HTMLEditorKit.ParserCallback callback = new Callback( document, columns, textIterator, imageIterator);
            parser.parse(html, callback, true);
          }
          catch ( Exception ex )
          {
              throw new KmeliaRuntimeException("PdfGenerator.parseModelHTML", KmeliaRuntimeException.WARNING, "kmelia.EX_CANNOT_SHOW_PDF_GENERATION", ex);
          }
    }
    
    private static boolean isInteger(String id)
	{
		try
		{
			Integer.parseInt(id);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
    
    private static void generateContent(Document document) throws DocumentException, WysiwygException, PublicationTemplateException, FormException 
    {
        //Paragraph title = new Paragraph("\n"+message.getString("Model")+" : ", new Font(Font.HELVETICA, 10, Font.BOLD, new Color(0, 0, 0)));
    	Table tblHeader = addHearderToSection(message.getString("Model").toUpperCase());
		
    	String componentId = kmeliaSessionController.getComponentId();
        String objectId = publicationDetail.getPK().getId();
        
        //get displayed language
        String languageToDisplay = publicationDetail.getLanguageToDisplay(publiContentLanguage); 
        
        String sWysiwyg = WysiwygController.load(componentId, objectId, languageToDisplay);
        if (StringUtil.isDefined(sWysiwyg))
        {//Wysiwyg
        	document.add(tblHeader);
        	
        	parseHTML(document, sWysiwyg);
        }
        else
        {
            if ((completePublicationDetail.getInfoDetail() != null) && (completePublicationDetail.getModelDetail() != null))
            {//Modèles Base de données
            	document.add(tblHeader);
            	
                String toParse = completePublicationDetail.getModelDetail().getHtmlDisplayer();
                Iterator textIterator = completePublicationDetail.getInfoDetail().getInfoTextList().iterator();
                Iterator imageIterator = completePublicationDetail.getInfoDetail().getInfoImageList().iterator();
                parseModelHTML(document, toParse, textIterator, imageIterator );
            } 
            else 
            {//Modèles XML
            	
                String infoId = publicationDetail.getInfoId();
        		if (!isInteger(infoId))
        		{
        			document.add(tblHeader);
        			
        			PublicationTemplateImpl pubTemplate = (PublicationTemplateImpl) PublicationTemplateManager.getPublicationTemplate(componentId+":"+infoId);
        			
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
        			context.setUserId(kmeliaSessionController.getUserId());
        			context.setNodeId(kmeliaSessionController.getSessionTopic().getNodeDetail().getNodePK().getId());
    				
        			String htmlResult = formView.toString(context, data);
        			parseHTML(document, htmlResult);
        		}
            }
        }
    }
}