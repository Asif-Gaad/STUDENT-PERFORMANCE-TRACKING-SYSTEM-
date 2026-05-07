-- ============================================================
-- Student Performance Tracking System - Database Schema v2
-- Compatible with MySQL 5.7+ / XAMPP
-- ============================================================

CREATE DATABASE IF NOT EXISTS student_performance_db;
USE student_performance_db;

-- ============================================================
-- TABLE: users  (role now includes 'student')
-- student_id links a student-role user to their students record
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('admin','teacher','student') NOT NULL DEFAULT 'student',
    student_id  INT NULL DEFAULT NULL,   -- only set when role = 'student'
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: students
-- ============================================================
CREATE TABLE IF NOT EXISTS students (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(20),
    dob             DATE,
    address         TEXT,
    enrollment_date DATE NOT NULL DEFAULT (CURDATE()),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add the FK after both tables exist
ALTER TABLE users
    ADD CONSTRAINT fk_users_student
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL;

-- ============================================================
-- TABLE: courses
-- ============================================================
CREATE TABLE IF NOT EXISTS courses (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    course_name  VARCHAR(100) NOT NULL,
    course_code  VARCHAR(20)  NOT NULL UNIQUE,
    credits      INT NOT NULL DEFAULT 3,
    teacher_name VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: enrollments
-- ============================================================
CREATE TABLE IF NOT EXISTS enrollments (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    student_id      INT NOT NULL,
    course_id       INT NOT NULL,
    enrollment_date DATE NOT NULL DEFAULT (CURDATE()),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(id)  ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment (student_id, course_id)
);

-- ============================================================
-- TABLE: grades
-- ============================================================
CREATE TABLE IF NOT EXISTS grades (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL UNIQUE,
    grade_value   DECIMAL(5,2) NOT NULL CHECK (grade_value >= 0 AND grade_value <= 100),
    grade_letter  CHAR(2) NOT NULL,
    semester      VARCHAR(20) NOT NULL,
    academic_year VARCHAR(10) NOT NULL,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Staff users
INSERT INTO users (username, password, role, student_id) VALUES
('admin',   'admin123',   'admin',   NULL),
('teacher', 'teacher123', 'teacher', NULL),
('dr_khan', 'khan123',    'teacher', NULL),
('ms_ali',  'ali123',     'teacher', NULL);

-- Students
INSERT INTO students (name, email, phone, dob, address, enrollment_date) VALUES
('Ali Hassan',     'ali.hassan@example.com',    '0300-1234567', '2002-03-15', 'House 12, Street 4, Khairpur',           '2024-01-10'),
('Sara Ahmed',     'sara.ahmed@example.com',    '0301-2345678', '2003-06-22', 'Block B, Model Town, Sukkur',            '2024-01-10'),
('Usman Raza',     'usman.raza@example.com',    '0302-3456789', '2002-11-01', 'Flat 5, Garden Apartments, Larkana',     '2024-01-15'),
('Fatima Malik',   'fatima.malik@example.com',  '0303-4567890', '2003-01-30', 'Street 7, Old City, Nawabshah',          '2024-01-15'),
('Bilal Shah',     'bilal.shah@example.com',    '0304-5678901', '2002-08-12', 'House 99, Qasimabad, Hyderabad',         '2024-02-01'),
('Zainab Mirza',   'zainab.mirza@example.com',  '0305-6789012', '2003-04-18', 'Block C, Latifabad, Hyderabad',          '2024-02-01'),
('Hassan Khan',    'hassan.khan@example.com',   '0306-7890123', '2002-09-25', 'House 45, Sindhi Colony, Karachi',       '2024-02-10'),
('Ayesha Qureshi', 'ayesha.qureshi@example.com','0307-8901234', '2003-12-05', 'Flat 10, Gulshan-e-Iqbal, Karachi',     '2024-02-10');

-- Student portal logins  (student_id matches the inserted IDs above: 1-8)
INSERT INTO users (username, password, role, student_id) VALUES
('ali.hassan',     'ali123',     'student', 1),
('sara.ahmed',     'sara123',    'student', 2),
('usman.raza',     'usman123',   'student', 3),
('fatima.malik',   'fatima123',  'student', 4),
('bilal.shah',     'bilal123',   'student', 5),
('zainab.mirza',   'zainab123',  'student', 6),
('hassan.khan',    'hassan123',  'student', 7),
('ayesha.qureshi', 'ayesha123',  'student', 8);

-- Courses
INSERT INTO courses (course_name, course_code, credits, teacher_name) VALUES
('Object Oriented Programming',  'CS101', 3, 'Engr. Asmatullah Zubair'),
('Data Structures & Algorithms', 'CS102', 3, 'Dr. Imran Khan'),
('Database Management Systems',  'CS201', 3, 'Ms. Sana Ali'),
('Software Engineering',         'CS301', 3, 'Engr. Asmatullah Zubair'),
('Computer Networks',            'CS401', 3, 'Dr. Imran Khan'),
('Web Development',              'CS202', 2, 'Ms. Sana Ali'),
('Artificial Intelligence',      'CS501', 3, 'Dr. Imran Khan'),
('Operating Systems',            'CS302', 3, 'Engr. Asmatullah Zubair');

-- Enrollments
INSERT INTO enrollments (student_id, course_id, enrollment_date) VALUES
(1,1,'2024-01-20'),(1,2,'2024-01-20'),(1,3,'2024-01-20'),
(2,1,'2024-01-20'),(2,4,'2024-01-20'),(2,6,'2024-01-20'),
(3,1,'2024-01-25'),(3,2,'2024-01-25'),(3,5,'2024-01-25'),
(4,1,'2024-01-25'),(4,3,'2024-01-25'),(4,7,'2024-01-25'),
(5,2,'2024-02-05'),(5,4,'2024-02-05'),(5,6,'2024-02-05'),
(6,2,'2024-02-05'),(6,5,'2024-02-05'),(6,8,'2024-02-05'),
(7,3,'2024-02-15'),(7,6,'2024-02-15'),(7,7,'2024-02-15'),
(8,3,'2024-02-15'),(8,4,'2024-02-15'),(8,8,'2024-02-15');

-- Grades
INSERT INTO grades (enrollment_id, grade_value, grade_letter, semester, academic_year) VALUES
(1,  88.5, 'B+', 'Spring', '2024'),
(2,  92.0, 'A',  'Spring', '2024'),
(3,  76.0, 'B',  'Spring', '2024'),
(4,  95.0, 'A+', 'Spring', '2024'),
(5,  61.0, 'C',  'Spring', '2024'),
(7,  45.0, 'F',  'Spring', '2024'),
(8,  78.5, 'B',  'Spring', '2024'),
(10, 82.0, 'B+', 'Spring', '2024'),
(13, 90.0, 'A',  'Spring', '2024'),
(16, 55.0, 'D',  'Spring', '2024'),
(19, 70.0, 'B-', 'Spring', '2024'),
(22, 88.0, 'B+', 'Spring', '2024');
