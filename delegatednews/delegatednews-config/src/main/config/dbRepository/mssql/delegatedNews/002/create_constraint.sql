
alter table sc_delegatednews_news with nocheck 
        add constraint pk_delegatednews_news
        primary key clustered (pubId);
