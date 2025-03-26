drop table if exists report_management;
drop table if exists employees;


CREATE TABLE if not exists report_management (
    report_name VARCHAR(100) NOT NULL,
    report_description TEXT NOT NULL,
    query_type VARCHAR(20) NOT NULL, -- 'select' or 'count'
    query TEXT NOT NULL,
    params TEXT NOT NULL,
    PRIMARY KEY (report_name, query_type)
);

;



CREATE TABLE if not exists employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    position VARCHAR(50) NOT NULL,
    salary DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);