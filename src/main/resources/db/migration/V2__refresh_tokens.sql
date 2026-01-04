create table refresh_tokens (
    id bigserial primary key,
    user_id bigint not null unique,
    token varchar(512) not null,
    expires_at timestamp with time zone not null,
    created_at timestamp with time zone not null,
    constraint fk_refresh_tokens_user foreign key (user_id) references users(id) on delete cascade
);
