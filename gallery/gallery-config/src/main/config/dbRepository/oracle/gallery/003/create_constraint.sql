ALTER TABLE SC_Gallery_Photo
ADD CONSTRAINT PK_SC_Gallery_Photo PRIMARY KEY
	(
		photoId
	)   
;

ALTER TABLE SC_Gallery_Path
ADD CONSTRAINT PK_SC_Gallery_Path PRIMARY KEY
	(
		photoId, nodeId
	)   
;
