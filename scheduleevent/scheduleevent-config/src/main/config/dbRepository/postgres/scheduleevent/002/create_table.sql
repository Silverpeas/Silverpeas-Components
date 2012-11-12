    create table sc_scheduleevent_list (
        id varchar(255) not null,
        title varchar(255) not null,
        description varchar(4000),
        creationdate timestamp not null,
        status int4 not null,
        creatorid integer not null
    );

    create table sc_scheduleevent_options (
        id varchar(255) not null,
        scheduleeventid varchar(255) not null,
        optionday timestamp not null,
        optionhour int4 not null
    );

    create table sc_scheduleevent_contributor (
        id varchar(255) not null,
        scheduleeventid varchar(255) not null,
        userid integer not null,
		lastvalidation timestamp,
        lastvisit timestamp
    );
    
    create table sc_scheduleevent_response (
        id varchar(255) not null,
        scheduleeventid varchar(255) not null,
        userid integer not null,
        optionid varchar(255) not null
    );