
create table account (
    id bigint primary key auto_increment,
    owner varchar(255) not null,
    balance double
);

create table transaction (
    id bigint primary key auto_increment,
    amount double,
    type varchar(255) not null,
    timestamp TIMESTAMP,
    account_id bigint
);