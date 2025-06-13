create table users (
    id int primary key auto_increment,
    name varchar(50) not null,
    email varchar(50) unique not null,
    password varchar(50) not null
);

create table emails (
    id int primary key auto_increment,
    code varchar(6) unique not null,
    sender_id int not null,
    subject varchar(80) not null,
    body text not null,
    sent_at datetime not null,

    foreign key (sender_id) references users(id)
);

create table email_recipients (
    id int primary key auto_increment,
    email_id int not null,
    recipient_id int not null,
    is_read boolean default false not null,

    foreign key (email_id) references emails(id),
    foreign key (recipient_id) references users(id)
);