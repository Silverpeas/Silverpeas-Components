CREATE TABLE SC_WhitePages_Card  
(
   id			int					NOT NULL,
   userId		varchar(50)				NOT NULL,
   hideStatus		int					NOT NULL,
   instanceId		varchar(50)				NOT NULL,
   creationDate		char(10)	DEFAULT ('2003/01/01')	NOT NULL,
   creatorId		int		DEFAULT (0)		NOT NULL 
) 
;

CREATE TABLE SC_WhitePages_SearchFields  
(
   id			varchar(255)					NOT NULL,
   instanceId		varchar(50)				NOT NULL,
   fieldId		varchar(50)				NOT NULL
) 
;
