-- Login passa a ser por username (definido pelo admin), não mais por e-mail.
-- Renomeia a coluna e a constraint preservando os valores existentes
-- (o admin atual continua logando com o valor que estava em 'email').
ALTER TABLE usuario RENAME COLUMN email TO username;
ALTER TABLE usuario RENAME CONSTRAINT uq_usuario_email TO uq_usuario_username;
