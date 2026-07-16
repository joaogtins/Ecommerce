ALTER TABLE products ADD COLUMN image_url VARCHAR(512);
ALTER TABLE products ADD COLUMN featured BOOLEAN DEFAULT false;
ALTER TABLE products ADD COLUMN new_collection BOOLEAN DEFAULT false;
CREATE INDEX idx_products_featured ON products(featured);
CREATE INDEX idx_products_new_collection ON products(new_collection);
