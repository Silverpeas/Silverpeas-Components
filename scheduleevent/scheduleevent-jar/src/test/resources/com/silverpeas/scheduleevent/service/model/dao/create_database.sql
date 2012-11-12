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

alter table sc_scheduleevent_list
        add constraint pk_scheduleevent_list
        primary key (id);

alter table sc_scheduleevent_options
        add constraint pk_scheduleevent_options
        primary key (id);

alter table sc_scheduleevent_contributor
        add constraint pk_scheduleevent_contrib
        primary key (id);

alter table sc_scheduleevent_response
        add constraint pk_scheduleevent_response
        primary key (id);
                
alter table sc_scheduleevent_options
        add constraint fk_options_eventid
         foreign key (scheduleeventid)
        references sc_scheduleevent_list (id);

alter table sc_scheduleevent_contributor
        add constraint fk_contributor_scheduleeventid
         foreign key (scheduleeventid)
        references sc_scheduleevent_list (id); 

alter table sc_scheduleevent_response
        add constraint fk_response_scheduleeventid
         foreign key (scheduleeventid)
        references sc_scheduleevent_list (id);        
alter table sc_scheduleevent_response
        add constraint fk_response_optionid
        foreign key (optionid)
        references sc_scheduleevent_options (id);

CREATE INDEX ind_sc_scheduleevent_contributor_1
   ON sc_scheduleevent_contributor (scheduleeventid);
   
CREATE INDEX ind_sc_scheduleevent_contributor_2
   ON sc_scheduleevent_contributor (userid);
      
CREATE INDEX ind_sc_scheduleevent_response_1
   ON sc_scheduleevent_response (scheduleeventid, userid);