ALTER TABLE SC_Classifieds_Classifieds
ADD CONSTRAINT PK_SC_Classifieds_Classifieds PRIMARY KEY
	(
		classifiedId
	)   
;

ALTER TABLE SC_Classifieds_Images
ADD CONSTRAINT PK_SC_Classifieds_Images PRIMARY KEY
	(
		imageId
	)   
;

ALTER TABLE SC_Classifieds_Subscribes
ADD CONSTRAINT PK_SC_Classifieds_Subscribes PRIMARY KEY
	(
		userId,
		instanceId,
		field1,
		field2
	)   
;
