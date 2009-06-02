CREATE TABLE SC_ProjectManager_Resources
(
	id			int		NOT NULL,
	taskId			int		NOT NULL,
	resourceId		int		NOT NULL,
	charge	 		int		NOT NULL,
	instanceId		varchar(50)	NOT NULL
)
;