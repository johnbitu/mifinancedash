CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_user_id_ativo ON accounts(user_id, ativo);

CREATE INDEX IF NOT EXISTS idx_categories_user_id ON categories(user_id);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category_id ON transactions(category_id);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'transactions' AND column_name = 'card_id'
    ) THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_transactions_card_id ON transactions(card_id)';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'transactions' AND column_name = 'recurrence_id'
    ) THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_transactions_recurrence_id ON transactions(recurrence_id)';
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_transactions_user_id_data_transacao_desc ON transactions(user_id, data_transacao DESC);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_ocorreu_em_desc ON audit_logs(ocorreu_em DESC);
