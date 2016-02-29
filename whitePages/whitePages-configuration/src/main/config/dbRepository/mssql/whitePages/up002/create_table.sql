ALTER TABLE SC_WhitePages_Card
ADD creationDate char(10) NOT NULL DEFAULT ('2003/01/01')
;
ALTER TABLE SC_WhitePages_Card
ADD creatorId	int NOT NULL DEFAULT (0)
;
