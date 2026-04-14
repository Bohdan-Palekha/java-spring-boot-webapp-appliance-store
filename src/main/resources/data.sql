INSERT INTO manufacturers (id, name)
VALUES (1, 'Samsung'),
       (2, 'LG'),
       (3, 'Bosch'),
       (4, 'Siemens'),
       (5, 'Whirlpool'),
       (6, 'Philips'),
       (7, 'Dyson');
INSERT INTO appliances (id, name, category, model, manufacturer_id, power_type, characteristic, description, power,
                        price)
VALUES (1, 'Samsung Washer', 'BIG', 'WW90T', 1, 'AC220', '9kg Energy A+++', 'Front-loading washing machine', 2000,
        799.99),
       (2, 'LG TurboWash', 'BIG', 'F4WV908', 2, 'AC220', '8kg Direct Drive', 'Efficient top-rated washer', 1800,
        649.99),
       (3, 'Bosch Fridge', 'BIG', 'KGN56HI30', 3, 'AC220', '500L No Frost', 'Large double-door fridge', 150, 1299.99),
       (4, 'Siemens Dishwasher', 'BIG', 'SN256I01', 4, 'AC220', '13 sets A++ 42dB', 'Built-in ultra-quiet dishwasher',
        1800, 599.99),
       (5, 'Whirlpool Microwave', 'SMALL', 'MWF422BL', 5, 'AC220', '25L 800W Grill', 'Compact microwave with grill',
        700, 149.99),
       (6, 'Samsung Blender', 'SMALL', 'MX-SM1075', 1, 'AC220', '1L 1000W 6 blades', 'High-performance blender', 1000,
        89.99),
       (7, 'Bosch Cordless Drill', 'SMALL', 'UniversalDrill18', 3, 'ACCUMULATOR', '18V 2.0Ah 40Nm',
        'Versatile cordless drill', 18, 119.99),
       (8, 'Philips Air Fryer', 'SMALL', 'HD9252/70', 6, 'AC220', '4.1L 1400W Rapid Air',
        'XL air fryer healthy cooking', 1400, 179.99),
       (9, 'Dyson V15 Vacuum', 'SMALL', 'V15 Detect', 7, 'ACCUMULATOR', '240AW Laser HEPA',
        'Cordless vacuum laser detection', 240, 699.99),
       (10, 'LG Cordless Vacuum', 'SMALL', 'A9N-CORE', 2, 'ACCUMULATOR', '200W 80min', 'Cordless stick vacuum', 200,
        349.99);
INSERT INTO users (id, user_type, name, email, password, department, card)
VALUES (1, 'EMPLOYEE', 'Admin User', 'admin@store.com', '$2a$12$85vog6zlsVjbiylqDiwkwe39FNIDWj/76k3Gall68F7r0jPqu7Aam',
        'ADMIN', NULL),
       (2, 'EMPLOYEE', 'Jane Staff', 'jane@store.com', '$2a$12$GeXzXIoC34PIwZ9kNydqMeVJFi8kzMkOQ1rm2FdTavsQFSbilHAGy',
        'Sales', NULL),
       (3, 'CLIENT', 'Oleksiy Petrenko', 'oleksiy@example.com',
        '$2a$12$GeXzXIoC34PIwZ9kNydqMeVJFi8kzMkOQ1rm2FdTavsQFSbilHAGy', NULL, 'CARD-001'),
       (4, 'CLIENT', 'Iryna Shevchenko', 'iryna@example.com',
        '$2a$12$GeXzXIoC34PIwZ9kNydqMeVJFi8kzMkOQ1rm2FdTavsQFSbilHAGy', NULL, 'CARD-002');
INSERT INTO orders (id, approved, client_id, employee_id)
VALUES (1, NULL, 3, NULL),
       (2, TRUE, 4, 2),
       (3, FALSE, 3, 1);
INSERT INTO order_rows (id, appliance_id, amount, number, orders_id)
VALUES (1, 1, 799.99, 1, 1),
       (2, 5, 299.98, 2, 1),
       (3, 3, 1299.99, 1, 2),
       (4, 6, 269.97, 3, 3);
ALTER TABLE manufacturers
    ALTER COLUMN id RESTART WITH 8;
ALTER TABLE appliances
    ALTER COLUMN id RESTART WITH 11;
ALTER TABLE users
    ALTER COLUMN id RESTART WITH 5;
ALTER TABLE orders
    ALTER COLUMN id RESTART WITH 4;
ALTER TABLE order_rows
    ALTER COLUMN id RESTART WITH 5;
