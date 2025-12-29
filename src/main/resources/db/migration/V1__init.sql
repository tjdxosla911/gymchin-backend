create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    nickname varchar(255) not null,
    gender varchar(50),
    age integer,
    city varchar(100),
    district varchar(100),
    fitness_level varchar(50),
    coach_option varchar(50),
    gym_name varchar(255),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table user_goals (
    user_id bigint not null,
    goal varchar(50) not null,
    constraint fk_user_goals_user foreign key (user_id) references users(id) on delete cascade
);

create table user_preferred_days (
    user_id bigint not null,
    preferred_day varchar(50) not null,
    constraint fk_user_days_user foreign key (user_id) references users(id) on delete cascade
);

create table user_preferred_time_slots (
    user_id bigint not null,
    time_slot varchar(50) not null,
    constraint fk_user_time_slots_user foreign key (user_id) references users(id) on delete cascade
);

create table matchings (
    id bigserial primary key,
    requester_user_id bigint not null,
    target_user_id bigint not null,
    status varchar(50) not null,
    message varchar(500),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_matchings_requester foreign key (requester_user_id) references users(id),
    constraint fk_matchings_target foreign key (target_user_id) references users(id)
);

create index idx_matchings_requester on matchings(requester_user_id);
create index idx_matchings_target on matchings(target_user_id);
create index idx_matchings_status on matchings(status);
