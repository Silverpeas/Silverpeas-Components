<%!

GraphicElementFactory gef ;

String iconsPath ;

//Icons
String belpou;
String online;
String mandatoryField;

NewsEditoSessionController news;

String action ;
String flag;
ResourceLocator settings ;

int detailLevel=0; // 0 -> nothing is selected, 1 -> an archive is selected, 2 -> a title is selected, 3-> a publication is selected
Collection archives = null;
NodeDetail archiveDetail = null;
NodeDetail titleDetail = null;
PublicationDetail pubDetail =null;
String navigationString ; // detail level
		
// label variables 
String consultationTP="Consultation";
String organisationTP="Organisation";
String inLineSettingTP="Mise en ligne";
String deleteArchivesOP = "Effacer les archives sélectionnées" ;
String deletePublicationsOP = "Effacer les publications sélectionnées";
String onlineAL = "En ligne" ;
String archivesAP ="Archives";
String titreAP ="Titres";
String creationDateAP ="Date de création";
String organiseBB = "Organiser";
String setInLineBB = "Mettre en ligne";
String ajoutEditionPublicationBB = "Ajout/Edition d'une publication";
String compilationPdfBB = "Compilation PDF";
String emptyEditorialME= "Selectionnez le titre puis la publication à ajouter dans l'éditorial";
String stateRBT = "Etat :";
String onLineRB = "En ligne";
String notOnLineRB = "En cours de rédaction";
String modelRBT = "Modèle éditorial :";
String model1RB = "modèle 1";
String model2RB = "modèle 2";
String choisirImageEditorialBB ="Choisir une image pour l'éditorial";
String choisirImageEditorialOP ="Choisir une image pour l'éditorial";

// (les vraies init sont faites plus tard)
%>
<%

//response.setHeader("Cache-Control","no-store"); //HTTP 1.1
//response.setHeader("Pragma","no-cache"); //HTTP 1.0
//response.setDateHeader ("Expires",-1); //prevents caching at the proxy server



gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

news = (NewsEditoSessionController) request.getAttribute("newsEdito");

ResourceLocator 	generalMessage 	= GeneralPropertiesManager.getGeneralMultilang(news.getLanguage());
ResourcesWrapper 	resources 		= (ResourcesWrapper)request.getAttribute("resources");

if (news == null)
{
    // No news session controller in the request -> security exception
    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}

action = (String) request.getParameter("Action");
flag = (String) request.getParameter("flag");

	consultationTP = news.getString("consultationTP");
	organisationTP = news.getString("organisationTP");
	inLineSettingTP = news.getString("inLineSettingTP");
	deleteArchivesOP = news.getString("deleteArchivesOP");
	deletePublicationsOP = news.getString("deletePublicationsOP");
	onlineAL = news.getString("onlineAL");
	archivesAP = news.getString("archivesAP");
	titreAP = news.getString("titreAP");
	creationDateAP = news.getString("creationDateAP");
	organiseBB = news.getString("organiseBB");
	setInLineBB = news.getString("setInLineBB");
	ajoutEditionPublicationBB = news.getString("ajoutEditionPublicationBB");
	compilationPdfBB = news.getString("compilationPdfBB");
	emptyEditorialME = news.getString("emptyEditorialME");
	stateRBT  = news.getString("stateRBT");
	onLineRB  = news.getString("onLineRB");
	notOnLineRB  = news.getString("notOnLineRB");
	modelRBT  = news.getString("modelRBT");
	model1RB  = news.getString("model1RB");
	model2RB  = news.getString("model2RB");
	choisirImageEditorialBB =  news.getString("choisirImageEditorialBB");
	choisirImageEditorialOP =  news.getString("choisirImageEditorialOP");
//}
if (settings == null)
{
	settings = news.getSettings();
	belpou=settings.getString("belpou");
	online=settings.getString("online");
	mandatoryField = settings.getString("mandatoryField");
	
}
%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
