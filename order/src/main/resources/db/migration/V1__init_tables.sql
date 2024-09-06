CREATE TABLE bill
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    user         VARCHAR(255)          NULL,
    date_time    datetime              NULL,
    order_number BIGINT                NULL,
    CONSTRAINT pk_bill PRIMARY KEY (id)
);

CREATE TABLE bill_items
(
    bill_id  BIGINT NOT NULL,
    items_id BIGINT NOT NULL
);

CREATE TABLE item
(
    id    BIGINT AUTO_INCREMENT NOT NULL,
    name  VARCHAR(255)          NULL,
    price FLOAT                 NULL,
    CONSTRAINT pk_item PRIMARY KEY (id)
);

ALTER TABLE bill_items
    ADD CONSTRAINT uc_bill_items_items UNIQUE (items_id);

ALTER TABLE bill_items
    ADD CONSTRAINT fk_bilite_on_bill FOREIGN KEY (bill_id) REFERENCES bill (id);

ALTER TABLE bill_items
    ADD CONSTRAINT fk_bilite_on_item FOREIGN KEY (items_id) REFERENCES item (id);