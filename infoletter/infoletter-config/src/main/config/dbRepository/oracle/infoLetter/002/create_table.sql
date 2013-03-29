CREATE TABLE SC_IL_Letter 
(
	id			int		NOT NULL ,
	name			varchar (1000)	NOT NULL ,
	description		varchar (2000)	NULL ,
	periode			varchar (255)	NULL ,
	instanceId		varchar (50)	NOT NULL
);

CREATE TABLE SC_IL_Publication
(
	id			int		NOT NULL ,
	title			varchar (1000)	NOT NULL ,
	description		varchar (2000)	NULL ,
	parutionDate		varchar (255)	NULL ,
	publicationState	int		NOT NULL ,
	letterId		int		NOT NULL ,
	instanceId		varchar (50)	NOT NULL
);

CREATE TABLE SC_IL_ExtSus
(
	letter			int		NOT NULL ,
	email			varchar (1000)	NOT NULL ,
	instanceId		varchar (50)	NOT NULL
);