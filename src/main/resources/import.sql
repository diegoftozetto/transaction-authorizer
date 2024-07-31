INSERT INTO tb_category (id, "name") VALUES(1, 'FOOD');
INSERT INTO tb_category (id, "name") VALUES(2, 'MEAL');
INSERT INTO tb_category (id, "name") VALUES(3, 'CASH');

INSERT INTO tb_mcc (id, code, category_id) VALUES(1, '5411', 1);
INSERT INTO tb_mcc (id, code, category_id) VALUES(2, '5412', 1);
INSERT INTO tb_mcc (id, code, category_id) VALUES(3, '5811', 2);
INSERT INTO tb_mcc (id, code, category_id) VALUES(4, '5812', 2);

INSERT INTO tb_account (id, account) VALUES(1, '000078956984567');

INSERT INTO tb_balance (id, total_amount, category_id, account_id) VALUES(1, 10, 1, 1);
INSERT INTO tb_balance (id, total_amount, category_id, account_id) VALUES(2, 10, 2, 1);
INSERT INTO tb_balance (id, total_amount, category_id, account_id) VALUES(3, 100, 3, 1);

INSERT INTO tb_merchant (id, "name", mcc_id) VALUES(1, 'UBER TRIP                   SAO PAULO BR', 2);
INSERT INTO tb_merchant (id, "name", mcc_id) VALUES(2, 'UBER EATS                   SAO PAULO BR', 1);
INSERT INTO tb_merchant (id, "name", mcc_id) VALUES(3, 'PAG*JoseDaSilva          RIO DE JANEI BR', 3);
INSERT INTO tb_merchant (id, "name", mcc_id) VALUES(4, 'PICPAY*BILHETEUNICO           GOIANIA BR', 4);
