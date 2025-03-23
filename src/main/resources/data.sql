delete from employees;
delete from report_management;

INSERT INTO report_management VALUES
('employee', 'select', 'SELECT id, name, position FROM employees WHERE id = ${empId}$ LIMIT ${limit}$ OFFSET ${offset}$'),
('employee', 'count', 'SELECT COUNT(1) FROM employees WHERE id = ${empId}$');


INSERT INTO employees (name, position, salary) VALUES
('John Doe', 'Developer', 8500.00),
('Jane Smith', 'Team Lead', 9500.00),
('Alice Johnson', 'Developer', 8000.00),
('Robert Brown', 'Tester', 7000.00),
('Emily Davis', 'Developer', 7800.00),

('Michael Scott', 'Manager', 10500.00),
('Dwight Schrute', 'Salesperson', 7200.00),
('Jim Halpert', 'Salesperson', 7100.00),
('Pam Beesly', 'Receptionist', 6800.00),
('Stanley Hudson', 'Salesperson', 6900.00),

('Angela Martin', 'Accountant', 7500.00),
('Kevin Malone', 'Accountant', 7300.00),
('Oscar Martinez', 'Accountant', 7400.00),
('Creed Bratton', 'Quality Assurance', 7000.00),
('Meredith Palmer', 'Supplier Relations', 7100.00);
