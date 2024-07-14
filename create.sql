create sequence log_seq start with 1 increment by 50;
create table log (id bigint not null, timestamp timestamp(6) not null, callsign varchar(255), primary key (id));
