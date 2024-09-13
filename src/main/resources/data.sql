-- Вставка организаций с явным указанием UUID
INSERT INTO organization (id, name, description, type) VALUES
                                                           ('5f06dd7b-2e66-4f12-8cda-b5dd642d9b8c', 'Tech Innovations', 'A leading tech company', 'LLC'),
                                                           ('f523ac31-6f0e-4403-bd99-989153e77855', 'Green Solutions', 'Eco-friendly products and services', 'IE');

-- Вставка пользователей с явным указанием UUID
INSERT INTO employee (id, username, first_name, last_name) VALUES
                                                               ('92f376e2-43d5-467b-85fa-1d2342f8f3f6', 'user1', 'John', 'Doe'),
                                                               ('72082be9-f84f-467e-ac46-7a67117b8ec7', 'user2', 'Jane', 'Smith'),
                                                               ('a142543d-b4ea-4ab2-9889-bb696cff8c0d', 'user3', 'Michael', 'Brown'),
                                                               ('9a1ad33a-22bc-445a-8162-43694d253250', 'user4', 'Emily', 'Jones'),
                                                               ('d9ef13ba-6eb8-4743-bccf-24118b2cd1b2', 'user5', 'Chris', 'Taylor');


INSERT INTO organization_responsible (organization_id, user_id) VALUES
                                                                    ('5f06dd7b-2e66-4f12-8cda-b5dd642d9b8c', '92f376e2-43d5-467b-85fa-1d2342f8f3f6'),  -- Tech Innovations - John Doe
                                                                    ('5f06dd7b-2e66-4f12-8cda-b5dd642d9b8c', '72082be9-f84f-467e-ac46-7a67117b8ec7'),  -- Tech Innovations - Jane Smith
                                                                    ('f523ac31-6f0e-4403-bd99-989153e77855', 'a142543d-b4ea-4ab2-9889-bb696cff8c0d'),  -- Green Solutions - Michael Brown
                                                                    ('f523ac31-6f0e-4403-bd99-989153e77855', '9a1ad33a-22bc-445a-8162-43694d253250'),  -- Green Solutions - Emily Jones
                                                                    ('f523ac31-6f0e-4403-bd99-989153e77855', 'd9ef13ba-6eb8-4743-bccf-24118b2cd1b2');  -- Green Solutions - Chris Taylor
INSERT INTO tender (tender_id, name, service_type, description, status, organization_id, creator_id) VALUES
    ('e4a1e3c5-9a2b-4c3d-a1b5-6e789f0b1c2d', 'Construction Project Alpha', 'CONSTRUCTION', 'A construction project for a new tech facility.', 'CREATED', '5f06dd7b-2e66-4f12-8cda-b5dd642d9b8c', '92f376e2-43d5-467b-85fa-1d2342f8f3f6'); -- Tech Innovations - user1

-- Вставка тендера для второй организации
INSERT INTO tender (tender_id, name, service_type, description, status, organization_id, creator_id) VALUES
    ('a5b1c8d9-7e3f-4b2a-8c5d-9f1e2d3c4b5a', 'Eco Delivery Service', 'DELIVERY', 'Delivery service for eco-friendly products.', 'PUBLISHED', 'f523ac31-6f0e-4403-bd99-989153e77855', 'a142543d-b4ea-4ab2-9889-bb696cff8c0d'); -- Green Solutions - user3