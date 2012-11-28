ALTER TABLE SC_Classifieds_Classifieds
ADD description	varchar(2000)	NULL,
ADD price		int 			NULL
;

CREATE TABLE SC_Classifieds_Images
(
	imageId		int		NOT NULL,
	classifiedId int	NOT NULL,
	imageName		varchar(255)	NOT NULL,
	mimeType 	 varchar(100)	NOT NULL
) 
;
