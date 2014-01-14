    create table sc_delegatednews_news (
        pubId		int		not null ,
        instanceId		varchar(50)     not null,
        status varchar(100) not null,
        contributorId varchar(50) not null,
        validatorId varchar(50) null,
        validationDate datetime null,
        beginDate datetime null,
        endDate datetime null,
        newsOrder int not null DEFAULT (0)
    );

    
