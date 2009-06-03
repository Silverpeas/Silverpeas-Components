ALTER TABLE SC_WhitePages_Card
ADD creationDate char(10) DEFAULT ('2003/01/01') NOT NULL 
;
ALTER TABLE SC_WhitePages_Card
ADD creatorId	int DEFAULT (0) NOT NULL 
;
