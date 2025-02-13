create schema if not exists game_app;

create table game_app.t_player
(
    id    serial primary key,
    name  varchar(50) not null unique,
    tg_id int8 unique
);