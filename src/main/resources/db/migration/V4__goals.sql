CREATE TABLE IF NOT EXISTS goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    valor_alvo NUMERIC(15,2) NOT NULL,
    valor_atual NUMERIC(15,2) NOT NULL DEFAULT 0,
    data_inicio DATE NOT NULL,
    data_prazo DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO' CHECK (status IN ('EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA', 'EXPIRADA')),
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_goals_user_id ON goals(user_id);
CREATE INDEX IF NOT EXISTS idx_goals_user_id_status ON goals(user_id, status);
