INSERT INTO TEACHERS (first_name, last_name)
VALUES ('Margot', 'DELAHAYE'), ('Hélène', 'THIERCELIN');

INSERT INTO USERS (first_name, last_name, admin, email, password) VALUES
('Admin', 'Admin', true, 'yoga@studio.com', '$2a$10$.Hsa/ZjUVaHqi0tp9xieMeewrnZxrZ5pQRzddUXE/WjDu2ZThe6Iq'),
('Hugo','Lebolide', false,'hugo@studio.com','$2a$10$Un2hnRoAkhu9YVDzFgWeI.zPv1K/DsrfWLitTJ.2gIYxWVaUOniIG'),
('Bob', 'Léponge', false,'bob@studio.com','$2a$10$3JCu52xBcML0LsoaCYnrD.PdQBdbNPtXy6hywf2sfIkB/dapcIJmu');

INSERT INTO SESSIONS (name, description, date, teacher_id) VALUES
('Yoga pour la forme', 'Yoga pour la forme', '2026-01-03 12:00:00', 1),
('Yoga débutant', 'Yoga pour les débutants', '2026-01-03 12:00:00', 2),
('Yoga confirmé', 'Yoga pour les confirmés', '2026-01-03 13:00:00', 2),
('Yoga détente', 'Yoga détente', '2026-01-03 13:00:00', 1);
