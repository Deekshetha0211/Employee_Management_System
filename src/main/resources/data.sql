insert into departments (code, name) values
('ENG', 'Engineering'),
('HR', 'Human Resources'),
('FIN', 'Finance'),
('SALES', 'Sales'),
('MKT', 'Marketing'),
('OPS', 'Operations'),
('IT', 'Information Technology'),
('QA', 'Quality Assurance'),
('SUP', 'Customer Support'),
('ADMIN', 'Administration')
on conflict do nothing;

insert into employees (full_name, email, hire_date, status, department_id)
values
('Alice Johnson', 'alice.johnson@ems.local', '2021-03-15', 'ACTIVE',
 (select id from departments where code = 'ENG')),

('Bob Singh', 'bob.singh@ems.local', '2020-07-01', 'ACTIVE',
 (select id from departments where code = 'HR')),

('Charlie Brown', 'charlie.brown@ems.local', '2019-11-20', 'INACTIVE',
 (select id from departments where code = 'FIN')),

('Diana Prince', 'diana.prince@ems.local', '2022-01-10', 'ACTIVE',
 (select id from departments where code = 'SALES')),

('Ethan Hunt', 'ethan.hunt@ems.local', '2018-06-05', 'ACTIVE',
 (select id from departments where code = 'OPS')),

('Fatima Khan', 'fatima.khan@ems.local', '2023-04-18', 'ACTIVE',
 (select id from departments where code = 'IT')),

('George Miller', 'george.miller@ems.local', '2020-09-30', 'ACTIVE',
 (select id from departments where code = 'QA')),

('Hema Reddy', 'hema.reddy@ems.local', '2021-12-12', 'INACTIVE',
 (select id from departments where code = 'MKT')),

('Ivan Petrov', 'ivan.petrov@ems.local', '2019-02-25', 'ACTIVE',
 (select id from departments where code = 'SUP')),

('Julia Fernandez', 'julia.fernandez@ems.local', '2022-08-08', 'ACTIVE',
 (select id from departments where code = 'ADMIN'))
on conflict (email) do nothing;
