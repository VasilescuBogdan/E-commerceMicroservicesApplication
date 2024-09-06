CREATE TABLE `order`
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    user         VARCHAR(255)          NULL,
    address      VARCHAR(255)          NULL,
    order_status VARCHAR(255)          NULL,
    CONSTRAINT pk_order PRIMARY KEY (id)
);

CREATE TABLE order_product
(
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL
);

CREATE TABLE product
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255)          NULL,
    price         FLOAT                 NULL,
    `description` VARCHAR(255)          NULL,
    CONSTRAINT pk_product PRIMARY KEY (id)
);

CREATE TABLE review
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    sender          VARCHAR(255)          NULL,
    message         VARCHAR(255)          NULL,
    number_of_stars INT                   NULL,
    product         BIGINT                NULL,
    CONSTRAINT pk_review PRIMARY KEY (id)
);

ALTER TABLE review
    ADD CONSTRAINT FK_REVIEW_ON_PRODUCT FOREIGN KEY (product) REFERENCES product (id);

ALTER TABLE order_product
    ADD CONSTRAINT fk_ordpro_on_order FOREIGN KEY (order_id) REFERENCES `order` (id);

ALTER TABLE order_product
    ADD CONSTRAINT fk_ordpro_on_product FOREIGN KEY (product_id) REFERENCES product (id);