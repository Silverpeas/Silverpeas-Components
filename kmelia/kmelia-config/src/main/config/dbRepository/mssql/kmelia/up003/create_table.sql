create table sc_kmelia_search (
    id numeric(19,0) not null,
    instanceId varchar(50),
    topicId integer not null,
    userId integer not null,
    date datetime not null,
    language varchar(50),
    query varchar(255)
);
