INSERT INTO customers (name, email, password, phone, role, created_at)
VALUES ('Administrador', 'admin@trie.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        '11988888888', 'ADMIN', NOW())
ON CONFLICT (email) DO NOTHING;
