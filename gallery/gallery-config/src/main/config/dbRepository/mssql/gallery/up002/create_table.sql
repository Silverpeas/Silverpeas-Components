ALTER TABLE SC_Gallery_Photo
ADD		keyWord			varchar (1000)	NULL
;

ALTER TABLE SC_Gallery_Photo
ADD		beginDownloadDate	varchar(10)	NULL
;

ALTER TABLE SC_Gallery_Photo
ADD		endDownloadDate		varchar(10)	NULL
;

UPDATE SC_Gallery_Photo 
SET beginDate='0000/00/00'
;

UPDATE SC_Gallery_Photo 
SET endDate='9999/99/99'
;

CREATE TABLE SC_Gallery_Path 
(
	photoId			int		NOT NULL,
	nodeId			int		NOT NULL,
	instanceId		varchar(50)     NOT NULL
);

INSERT INTO SC_Gallery_Path (photoId, nodeId, instanceId)
SELECT photoId, CAST(albumId as int), instanceId FROM SC_Gallery_Photo
;