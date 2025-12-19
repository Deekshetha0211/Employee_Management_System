CREATE EXTENSION IF NOT EXISTS pgcrypto;

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
  emp_role varchar(60) not null,
  hire_date date not null,
  status varchar(20) not null default 'ACTIVE',
  department_id bigint not null references departments(id),
  created_at timestamp not null default now(),
  updated_at timestamp not null default now()
);

create table if not exists app_users (
  id bigserial primary key,
  email varchar(180) not null unique,
  password_hash varchar(255) not null,
  role varchar(30) not null, -- ADMIN, HR_MANAGER, EMPLOYEE
  employee_id bigint not null,
  enabled boolean not null default true,
  created_at timestamp not null default now(),
  updated_at timestamp not null default now(),
  CONSTRAINT fk_users_employee
      FOREIGN KEY (employee_id)
      REFERENCES employees(id)
      ON DELETE CASCADE
);

