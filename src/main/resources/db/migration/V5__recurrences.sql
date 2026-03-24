CREATE TABLE IF NOT EXISTS recurrences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    card_id BIGINT REFERENCES cards(id) ON DELETE SET NULL,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('RECEITA', 'DESPESA')),
    valor NUMERIC(15,2) NOT NULL,
    frequencia VARCHAR(20) NOT NULL CHECK (frequencia IN ('DIARIA', 'SEMANAL', 'QUINZENAL', 'MENSAL', 'BIMESTRAL', 'TRIMESTRAL', 'SEMESTRAL', 'ANUAL')),
    dia_cobranca INTEGER CHECK (dia_cobranca BETWEEN 1 AND 31),
    data_inicio DATE NOT NULL,
    data_fim DATE,
    proxima_execucao DATE NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT true,
    ultima_falha TIMESTAMP,
    motivo_falha TEXT,
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_transactions_recurrence'
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT fk_transactions_recurrence
                FOREIGN KEY (recurrence_id)
                    REFERENCES recurrences(id)
                    ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_recurrences_user_id ON recurrences(user_id);
CREATE INDEX IF NOT EXISTS idx_recurrences_account_id ON recurrences(account_id);
CREATE INDEX IF NOT EXISTS idx_recurrences_user_id_ativo ON recurrences(user_id, ativo);
CREATE INDEX IF NOT EXISTS idx_recurrences_proxima_execucao ON recurrences(proxima_execucao);
