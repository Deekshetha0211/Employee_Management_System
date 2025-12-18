create table if not exists departments (
  id bigserial primary key,
  code varchar(40) not null unique,
  name varchar(120) not null unique,
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);

create table if not exists employees (
  id bigserial primary key,
  full_name varchar(120) not null,
  email varchar(180) not null unique,
  hire_date date not null,
  status varchar(20) not null default 'ACTIVE',
  department_id bigint not null references departments(id),
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);
