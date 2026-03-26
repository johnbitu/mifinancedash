CREATE TABLE IF NOT EXISTS cards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
    nome VARCHAR(100) NOT NULL,
    bandeira VARCHAR(30) NOT NULL CHECK (bandeira IN ('VISA', 'MASTERCARD', 'ELO', 'AMEX', 'HIPERCARD', 'OUTRO')),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('CREDITO', 'DEBITO')),
    limite NUMERIC(15,2),
    limite_disponivel NUMERIC(15,2),
    dia_fechamento INTEGER CHECK (dia_fechamento BETWEEN 1 AND 31),
    dia_vencimento INTEGER CHECK (dia_vencimento BETWEEN 1 AND 31),
    ativo BOOLEAN NOT NULL DEFAULT true,
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP
);

CREATE TABLE IF NOT EXISTS card_invoices (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mes_referencia INTEGER NOT NULL CHECK (mes_referencia BETWEEN 1 AND 12),
    ano_referencia INTEGER NOT NULL,
    valor_total NUMERIC(15,2) NOT NULL DEFAULT 0,
    data_vencimento DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA' CHECK (status IN ('ABERTA', 'FECHADA', 'PAGA')),
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP,
    UNIQUE (card_id, mes_referencia, ano_referencia)
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'transactions'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'transactions' AND column_name = 'card_id'
    ) THEN
        ALTER TABLE transactions
            ADD COLUMN card_id BIGINT;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_transactions_card'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'transactions' AND column_name = 'card_id'
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_card
                FOREIGN KEY (card_id)
                    REFERENCES cards(id)
                    ON DELETE SET NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cards' AND column_name = 'user_id'
    ) THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_cards_user_id ON cards(user_id)';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cards' AND column_name = 'account_id'
    ) THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_cards_account_id ON cards(account_id)';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cards' AND column_name = 'user_id'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'cards' AND column_name = 'ativo'
    ) THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_cards_user_id_ativo ON cards(user_id, ativo)';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'card_invoices' AND column_name = 'card_id'
    ) THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_card_invoices_card_id ON card_invoices(card_id)';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'card_invoices' AND column_name = 'user_id'
    ) THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_card_invoices_user_id ON card_invoices(user_id)';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'card_invoices' AND column_name = 'card_id'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'card_invoices' AND column_name = 'mes_referencia'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'card_invoices' AND column_name = 'ano_referencia'
    ) THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_card_invoices_card_mes_ano ON card_invoices(card_id, mes_referencia, ano_referencia)';
    END IF;
END $$;
