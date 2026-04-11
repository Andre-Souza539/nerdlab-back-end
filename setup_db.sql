-- 🛡️ PROTOCOLO NERDLAB: CRIAÇÃO DE BANCO DE DADOS ISOLADO
-- Execute este script como superusuário (postgres) na sua instância

-- 1. Criar o Banco de Dados
CREATE DATABASE nerdlab_db;

-- 2. Criar o Usuário Dedicado
CREATE USER nerdlab_user WITH PASSWORD 'DEFINA_UMA_SENHA_FORTE_AQUI';

-- 3. Conceder Permissões
GRANT ALL PRIVILEGES ON DATABASE nerdlab_db TO nerdlab_user;

-- 4. Ajustar permissões de Schema (necessário para o PostgreSQL 15+)
\c nerdlab_db
GRANT ALL ON SCHEMA public TO nerdlab_user;

-- ✅ Banco de dados 'nerdlab_db' pronto para a Singularidade.
