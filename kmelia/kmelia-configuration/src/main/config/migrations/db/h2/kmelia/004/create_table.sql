create table sc_kmelia_search (
    id bigint not null,
    instanceId varchar(50),
    topicId integer not null,
    userId integer not null,
    searchDate timestamp not null,
    language varchar(50),
    query varchar(255)
);
