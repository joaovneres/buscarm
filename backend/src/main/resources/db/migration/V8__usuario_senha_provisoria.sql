-- Reset de senha pelo admin: marca a senha como provisória até o usuário trocá-la.
ALTER TABLE usuario
    ADD COLUMN senha_provisoria BOOLEAN NOT NULL DEFAULT FALSE;
