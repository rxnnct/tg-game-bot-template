create schema if not exists game_app;

create table game_app.t_app_user
(
    id         uuid primary key,
    tg_id      bigint unique,
    name       varchar(25)              not null unique,
    balance    bigint                            default 0,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table game_app.t_game_character
(
    id               uuid primary key,
    max_health       bigint,
    strength         bigint,
    currency         bigint                            default 0,
    is_pvp_available boolean                           default false,
    created_at       timestamp with time zone not null default now(),
    updated_at       timestamp with time zone not null default now(),
    app_user_id      uuid references game_app.t_app_user (id) on delete cascade
);

CREATE TABLE game_app.t_player_rating
(
    id           uuid primary key,
    app_user_id  uuid references game_app.t_app_user (id) on delete cascade,
    mmr          bigint,
    games_played bigint                            default 0, --deliberately denormalized
    wins         bigint                            default 0, --deliberately denormalized
    losses       bigint                            default 0, --deliberately denormalized
    created_at   timestamp with time zone not null default now(),
    updated_at   timestamp with time zone not null default now(),
    unique (app_user_id)
);

create table game_app.t_match
(
    id            uuid primary key,
    app_user_id_1 uuid references game_app.t_app_user (id) on delete cascade,
    app_user_id_2 uuid references game_app.t_app_user (id) on delete cascade,
    winner        uuid references game_app.t_app_user (id) on delete cascade,
    created_at    timestamp with time zone not null default now()
);

CREATE TABLE game_app.t_last_match_battle_log
(
    match_id   uuid primary key references game_app.t_match (id) on delete cascade,
    battle_log text,
    updated_at timestamp with time zone not null default now()
);

CREATE INDEX idx_pvp_available ON game_app.t_game_character (is_pvp_available)
    WHERE is_pvp_available = TRUE;