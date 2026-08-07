-- Auditoria de autoria no nível do banco (ADR-0006).
--
-- A aplicação define, por transação, o usuário da sessão do banco na GUC
-- 'app.current_username' (ver PostgresUsuarioSessaoBanco). Este trigger garante que
-- criado_por / atualizado_por sejam SEMPRE gravados a partir desse usuário da sessão,
-- bloqueando qualquer gravação que não tenha o usuário definido.

CREATE OR REPLACE FUNCTION registro_set_auditoria_usuario() RETURNS trigger AS $$
DECLARE
    usuario_sessao text := current_setting('app.current_username', true);
BEGIN
    IF usuario_sessao IS NULL OR btrim(usuario_sessao) = '' THEN
        RAISE EXCEPTION 'Usuario da sessao nao definido (app.current_username); gravacao bloqueada';
    END IF;

    IF TG_OP = 'INSERT' THEN
        NEW.criado_por := usuario_sessao;
        NEW.atualizado_por := usuario_sessao;
    ELSIF TG_OP = 'UPDATE' THEN
        NEW.criado_por := OLD.criado_por;        -- preserva o criador original
        NEW.atualizado_por := usuario_sessao;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_registro_auditoria
    BEFORE INSERT OR UPDATE ON registro_matricula
    FOR EACH ROW EXECUTE FUNCTION registro_set_auditoria_usuario();
