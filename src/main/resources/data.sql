delete from employees;
delete from report_management;

INSERT INTO report_management(report_name, query_type, query, params) VALUES
('employee', 'select',
 'SELECT id, name, position, salary FROM employees WHERE id=? LIMIT ? OFFSET ?',
 'empId,limit,offset'),
('employee', 'count',
 'SELECT COUNT(*) FROM employees WHERE id=?',
 'empId');


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
('Freddy Merc', 'Salesperson', 6900.00),
('Michael Shaddows', 'Manager', 10500.00),
('Liza Smart', 'Salesperson', 250.00),
('Jim Halpert', 'Salesperson', 7100.00),
('Patty Reis', 'Receptionist', 6800.00),
('Stanley', 'Salesperson', 6900.00),
('Angela Rodriguez', 'Accountant', 7500.00),
('Kevin Malone', 'Accountant', 7300.00),
('Oscar Martinez', 'Accountant', 7400.00),
('Creed Bratton', 'Quality Assurance', 7000.00),
('Meredith Palmer', 'Supplier Relations', 7100.00);
