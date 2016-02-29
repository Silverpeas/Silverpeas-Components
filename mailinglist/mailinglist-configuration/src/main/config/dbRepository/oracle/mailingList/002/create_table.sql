    create table sc_mailinglist_attachment (
        id varchar2(255) not null,
        version number(10,0) not null,
        attachmentSize number(19,0),
        attachmentPath varchar2(255),
        fileName varchar2(255),
        contentType varchar2(255),
        md5Signature varchar2(255),
        messageId varchar2(255)
    );


    create table sc_mailinglist_external_user (
        id varchar2(255) not null,
        version number(10,0) not null,
        componentId varchar2(255),
        email varchar2(255) not null,
        listId varchar2(255)
    );

    create table sc_mailinglist_list (
        id varchar2(255) not null,
        version number(10,0) not null,
        componentId varchar2(255)
    );

    create table sc_mailinglist_message (
        id varchar2(255) not null,
        version number(10,0) not null,
        mailId varchar2(255) not null,
        componentId varchar2(255) not null,
        title varchar2(255),
        summary varchar2(255),
        sender varchar2(255),
        sentDate DATE,
        referenceId varchar2(255),
        moderated NUMBER(1),
        contentType varchar2(255),
        attachmentsSize number(19,0),
        messageYear number(10,0),
        messageMonth number(10,0),
        body CLOB
    );

    create table sc_mailinglist_internal_sub (
        id varchar2(255) not null,
        version number(10,0) not null,
        subscriber_type varchar2(255) not null,
        externalid varchar2(255) not null,
        mailinglistid varchar2(255) not null
    );
