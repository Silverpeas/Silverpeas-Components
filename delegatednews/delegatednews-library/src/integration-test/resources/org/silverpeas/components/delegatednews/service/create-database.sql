CREATE TABLE ST_ComponentInstance
(
        id            	int           PRIMARY KEY,
        spaceId       	int           NOT NULL,
        name          	varchar(100)  NOT NULL,
        componentName 	varchar(100)  NOT NULL,
        description   	varchar(400),
        createdBy     	int,
        orderNum 		int DEFAULT (0) NOT NULL,
        createTime 		varchar(20),
        updateTime 		varchar(20),
        removeTime 		varchar(20),
        componentStatus char(1),
        updatedBy 		int,
        removedBy 		int,
        isPublic		int	DEFAULT(0)	NOT NULL,
        isHidden		int	DEFAULT(0)	NOT NULL,
        lang		char(2),
        isInheritanceBlocked	int	default(0) NOT NULL
);

CREATE TABLE sc_delegatednews_news (
        pubId		int		not null ,
        instanceId		varchar(50)     not null,
        status varchar(100) not null,
        contributorId varchar(50) not null,
        validatorId varchar(50) null,
        validationDate timestamp(0) null,
        beginDate timestamp(0) null,
        endDate timestamp(0) null,
        newsOrder int not null DEFAULT (0)
);

ALTER TABLE sc_delegatednews_news
        add constraint pk_delegatednews_news
        primary key (pubId);
