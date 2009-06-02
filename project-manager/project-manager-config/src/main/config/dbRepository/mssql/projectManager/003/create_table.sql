CREATE TABLE SC_ProjectManager_Tasks 
(
	id			int		NOT NULL ,
	mereId			int		NULL ,
	chrono			int		NOT NULL ,
	nom			varchar (100)	NOT NULL ,
	description		varchar (500)	NULL ,
	organisateurId		int		NOT NULL ,
	responsableId		int		NOT NULL ,
	charge			float		NULL ,
	consomme		float		NULL ,
	raf			float		NULL ,
	avancement		int		NULL ,
	statut			int		NULL ,
	dateDebut		varchar (10)	NOT NULL ,
	dateFin			varchar (10)	NOT NULL ,
	codeProjet		varchar (50)	NULL ,
	descriptionProjet	varchar (100)	NULL ,
	estDecomposee		int		NULL ,
	instanceId		varchar (50)	NOT NULL,
	path			varchar (50)	NOT NULL,
	previousId		int		NULL
) 
;
CREATE TABLE SC_ProjectManager_Calendar 
(
	holidayDate		varchar (10)	NOT NULL,
	fatherId		int		NOT NULL,
	instanceId		varchar (50)	NOT NULL
) 
;
CREATE TABLE SC_ProjectManager_Resources
(
	id			int		NOT NULL,
	taskId			int		NOT NULL,
	resourceId		int		NOT NULL,
	charge	 		int		NOT NULL,
	instanceId		varchar(50)	NOT NULL
)
;