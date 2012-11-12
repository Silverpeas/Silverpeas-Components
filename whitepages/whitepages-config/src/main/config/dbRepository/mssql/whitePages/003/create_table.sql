CREATE TABLE SC_WhitePages_Card  
(
   id int                   NOT NULL,
   userId varchar(50)       NOT NULL,
   hideStatus int           NOT NULL,
   instanceId varchar(50)   NOT NULL,
   creationDate char(10)	NOT NULL DEFAULT ('2003/01/01'),
   creatorId	int		    NOT NULL DEFAULT (0)
) 
;
