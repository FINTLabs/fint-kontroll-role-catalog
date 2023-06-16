create table members (
                         id int8 not null,
                         first_name varchar(255),
                         last_name varchar(255),
                         resource_id varchar(255),
                         user_id varchar(255),
                         user_name varchar(255),
                         user_type varchar(255),
                         primary key (id));

create table role_memberships (
                                  role_id int8 not null,
                                  member_id int8 not null,
                                  primary key (role_id, member_id));

create table roles (
                       id  bigserial not null,
                       aggregated_role boolean not null,
                       organisation_unit_id varchar(255),
                       organisation_unit_name varchar(255),
                       resource_id varchar(255),
                       role_id varchar(255) not null,
                       role_name varchar(255),
                       role_source varchar(255),
                       role_sub_type varchar(255),
                       role_type varchar(255),
                       primary key (id));

create index resource_id_index on members (resource_id);

create index role_id_index on roles (role_id);

alter table roles add constraint UK_pnm8e0ibh0d93wnn7a3sm7ksh unique (role_id);
alter table role_memberships add constraint FKioer1to1ge9lqga8qo04setpq foreign key (member_id) references members;
alter table role_memberships add constraint FKcp0ftu77ofxmipxtnxnwc58ah foreign key (role_id) references roles;
