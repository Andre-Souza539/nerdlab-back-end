# 📮 NerdLab API - Postman Collection & Environment

Este diretório contém os arquivos prontos para importação no Postman:

1. **[`NerdLab_API.postman_collection.json`](./NerdLab_API.postman_collection.json)**: Coleção com todos os endpoints organizados por domínio, contendo payloads de exemplo e testes automáticos de resposta.
2. **[`NerdLab_Local.postman_environment.json`](./NerdLab_Local.postman_environment.json)**: Variáveis de ambiente apontando para `http://localhost:8080` e gerenciamento do token JWT.

---

## 🚀 Como Importar e Usar no Postman

1. Abra o Postman.
2. Clique no botão **Import** (canto superior esquerdo).
3. Selecione os dois arquivos JSON desta pasta (`NerdLab_API.postman_collection.json` e `NerdLab_Local.postman_environment.json`).
4. No seletor de ambientes (canto superior direito do Postman), selecione:  
   👉 **`NerdLab - Local (H2 / Dev)`**.

---

## ⚡ Fluxo Recomendado de Execução

1. **Login (Admin Padrão)** (`POST /api/v1/auth/login`):
   - Envia as credenciais `admin@nerdlab.com` / `admin123456`.
   - **Mágica do Script**: A aba *Tests* da requisição captura o token retornado na resposta e grava automaticamente na variável `jwt_token`.
2. **Ping Protegido** (`GET /api/v1/ping`):
   - Já vem configurado com `Bearer {{jwt_token}}`.
   - Retorna `200 OK` com dados do usuário e timestamp.
3. **Consultar Perfil** (`GET /api/v1/auth/me`):
   - Retorna as informações do usuário atual autenticado.
4. **Cadastrar Novo Usuário** (`POST /api/v1/auth/register`):
   - Cria uma nova conta (`ROLE_USER`).
5. **Alterar Senha** (`PATCH /api/v1/auth/change-password`):
   - Atualiza a senha da conta atualmente logada.
