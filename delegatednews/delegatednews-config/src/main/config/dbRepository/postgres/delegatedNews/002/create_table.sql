    create table sc_delegatednews_new (
        pubId		int		not null ,
        instanceId		varchar(50)     not null,
        status varchar(100) not null,
        contributorId varchar(50) not null,
        validatorId varchar(50) null,
        beginDate timestamp(0) null,
        endDate timestamp(0) null
    );

