    create table sc_scheduleevent_list (
        id varchar2(255) not null,
        title varchar2(255) not null,
        description varchar2(4000),
        creationdate DATE not null,
        status number(10,0) not null,
        creatorid int not null
    );

    create table sc_scheduleevent_options (
        id varchar2(255) not null,
        scheduleeventid varchar2(255) not null,
        optionday DATE not null,
        optionhour number(10,0) not null
    );

    create table sc_scheduleevent_contributor (
        id varchar2(255) not null,
        scheduleeventid varchar2(255) not null,
        userid int not null,
        lastvisit DATE not null
    );
    
    create table sc_scheduleevent_response (
        id varchar2(255) not null,
        scheduleeventid varchar2(255) not null,
        userid int not null,
        optionid varchar2(255) not null
    );