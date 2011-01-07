CREATE TABLE SC_WhitePages_SearchFields  
(
   id			varchar(255)					NOT NULL,
   instanceId		varchar(50)			NOT NULL,
   fieldId		varchar(50)				NOT NULL
) 
;

ALTER TABLE SC_WhitePages_SearchFields WITH NOCHECK ADD 
   CONSTRAINT PK_WhitePages_SearchFields PRIMARY KEY CLUSTERED 
   ( 
   		id 
   )   
;

CREATE INDEX WhitePages_SearchFieldsInstId
ON SC_WhitePages_SearchFields (instanceId);
