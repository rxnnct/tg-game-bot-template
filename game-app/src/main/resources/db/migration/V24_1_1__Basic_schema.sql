create schema if not exists game_app;

create table game_app.t_app_user
(
    id            uuid primary key,
    tg_id         int8 unique,
    name          varchar(25) not null unique,
    is_registered boolean,
    balance       int8,
    created_at    timestamptz
);

create table game_app.t_game_character
(
    id               uuid primary key,
    max_health       int8,
    strength         int8,
    currency         int8,
    --is_pvp_available boolean,
    created_at       timestamptz,
    app_user_id      uuid references game_app.t_app_user (id) on delete cascade
);

CREATE TABLE game_app.t_player_rating
(
    id           uuid primary key,
    app_user_id  uuid references game_app.t_app_user (id) on delete cascade,
    mmr          int8,
    games_played int8, --deliberately denormalized
    wins         int8, --deliberately denormalized
    losses       int8, --deliberately denormalized
    updated_at   timestamptz,
    unique (app_user_id)
);

create table game_app.t_match
(
    id            uuid primary key,
    app_user_id_1 uuid references game_app.t_app_user (id) on delete cascade,
    app_user_id_2 uuid references game_app.t_app_user (id) on delete cascade,
    winner        uuid references game_app.t_app_user (id) on delete cascade,
    created_at    timestamptz
);

CREATE TABLE game_app.t_last_match_battle_log
(
    match_id   uuid primary key references game_app.t_match (id) on delete cascade,
    battle_log text,
    updated_at timestamptz
);

CREATE INDEX idx_pvp_available ON game_app.t_game_character (is_pvp_available)
    WHERE is_pvp_available = TRUE;