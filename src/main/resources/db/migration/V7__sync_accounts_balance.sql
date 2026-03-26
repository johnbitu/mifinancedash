ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS saldo_atual NUMERIC(15,2);

UPDATE accounts
SET saldo_atual = saldo_inicial
WHERE saldo_atual IS NULL;

ALTER TABLE accounts
    ALTER COLUMN saldo_atual SET NOT NULL;
