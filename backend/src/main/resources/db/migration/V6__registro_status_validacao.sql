-- Status de validação do registro (conferência humana), independente do 'ativo'.
-- Registros já existentes eram confirmados no cadastro/importação, então nascem VALIDADO.
ALTER TABLE registro_matricula
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'VALIDADO';

-- Índice para o filtro por status na listagem.
CREATE INDEX idx_registro_status ON registro_matricula (status);
