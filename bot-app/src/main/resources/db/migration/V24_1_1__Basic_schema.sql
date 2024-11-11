create schema if not exists bot_app;

create table bot_app.t_player
(
    id    serial primary key,
    name  varchar(50) not null check (length(trim(name)) >= 3),
    tg_id int4
);