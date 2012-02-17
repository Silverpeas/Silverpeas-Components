CREATE TABLE SC_Contact_GroupFather
(
	groupId			int		NOT NULL,
	fatherId		int		NOT NULL,
	instanceId		varchar(50)     NOT NULL
) 
;

CREATE TABLE SC_Contact_Company_GroupFather
(
	groupId			int		NOT NULL,
	fatherId		int		NOT NULL,
	instanceId		varchar(50)     NOT NULL
) 
;

CREATE TABLE SC_Contact_Company 
(
	companyId			int			NOT NULL,
	companyName			varchar (1000)	NULL,
	companyEmail		varchar (1000)	NULL,
	companyPhone		varchar (20)	NULL,
	companyFax			varchar (20)	NULL,
	instanceId			varchar (50)	NOT NULL
)
;

CREATE TABLE SC_Contact_GenericContact 
(
	genericContactId	int		NOT NULL,
	contactType			int		NOT NULL,
	contactId			int		NULL,
	companyId			int	    NULL
)
;

CREATE TABLE SC_Contact_GenericContact_Rel
(
    relationId          int     NOT NULL,
	genericContactId	int		NOT NULL,
	genericCompanyId    int		NOT NULL,
	relationType	    int		NOT NULL,
	enabled 	    	int 	DEFAULT(1) NOT NULL
)
;

CREATE TABLE SC_Contact_GenericContact_Topic
(
    relationId          int     NOT NULL,
    genericContactId    int     NOT NULL,
    nodeId              int     NOT NULL
);

