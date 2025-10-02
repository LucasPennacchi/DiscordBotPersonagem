# Bot de Personagem de RPG para Discord

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![Maven](https://img.shields.io/badge/Maven-4.0.0-red?style=for-the-badge&logo=apache-maven)
![JDA](https://img.shields.io/badge/JDA-5.0.0-blue?style=for-the-badge)
![JPA/Hibernate](https://img.shields.io/badge/JPA-Hibernate-lightgrey?style=for-the-badge&logo=hibernate)

Um bot para Discord focado na criação e gerenciamento de fichas de personagem para um sistema de RPG customizado. O projeto foi desenvolvido em Java, utilizando uma arquitetura robusta e preparada para ambientes de nuvem.

## 📜 Propósito

O objetivo deste bot é facilitar a experiência de jogadores de RPG de mesa, permitindo que eles interajam com suas fichas de personagem diretamente pelo Discord. Ele automatiza a criação, atualização de atributos, cálculos de sub-atributos e visualização da ficha, servindo como uma ferramenta central durante as sessões de jogo.

## ✨ Funcionalidades

* `/criar [nome] [nivel]`: Cria um novo personagem para o usuário.
* `/personagem`: Exibe a ficha completa do personagem do usuário em uma mensagem embed.
* `/nome <novo_nome>`: Altera o nome do personagem.
* `/foto <imagem>`: Altera a imagem de perfil do personagem.
* `/deletar`: Inicia um processo de confirmação para deletar o personagem.
* `/atributos`: Permite ao usuário gastar pontos para aumentar os atributos principais através de reações na mensagem.
* `/ver <@usuario>`: (Admin) Permite que um administrador veja a ficha de qualquer usuário.
* `/upar <@usuario> <niveis>`: (Admin) Aumenta o nível e concede pontos de atributo a um personagem.

## 🏛️ Arquitetura

O projeto foi construído seguindo os princípios de uma **Arquitetura Orientada a Serviços (SOA)** para garantir alta manutenibilidade, baixo acoplamento e reusabilidade do código. A estrutura é dividida em três camadas principais:

1.  **Camada de Interface (`discord`):** Responsável exclusivamente por interagir com a API do Discord (receber comandos e enviar respostas). Utiliza a biblioteca JDA.
2.  **Camada de Serviço (`service`):** O cérebro da aplicação. Contém toda a lógica de negócio (regras de criação, validação de atributos, cálculos, etc.) de forma totalmente independente do Discord.
3.  **Camada de Persistência (`model`, `config`):** Responsável por interagir com o banco de dados através do JPA (Hibernate). A classe `Personagem` é a entidade que mapeia a tabela, e o `PersistenceManager` gerencia a conexão.

Essa arquitetura torna o projeto **Cloud-Ready**, pois toda a configuração sensível é externalizada, permitindo o deploy em qualquer provedor de nuvem (como a Oracle Cloud) sem alteração no código.

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 17+
* **Build Tool:** Apache Maven
* **API Discord:** JDA (Java Discord API)
* **Persistência:** JPA 3.0 (com implementação do Hibernate)
* **Banco de Dados:** Projetado para PostgreSQL (facilmente adaptável a outros bancos SQL)

## 🚀 Como Executar o Projeto

### Pré-requisitos

* JDK 17 ou superior
* Apache Maven
* Um servidor de banco de dados PostgreSQL
* Uma conta de Bot do Portal de Desenvolvedores do Discord

### 1. Configuração do Ambiente

**a. Clone o repositório:**
```bash
git clone [https://github.com/seu-usuario/seu-repositorio.git](https://github.com/seu-usuario/seu-repositorio.git)
cd seu-repositorio
```

**b. Configure o Token do Bot:**
Na pasta `src/main/resources/`, copie o arquivo `config.properties.example` e renomeie a cópia para `config.properties`.
```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```
Abra o novo `config.properties` e insira o token do seu bot do Discord.

**c. Configure o Banco de Dados:**
Execute os seguintes comandos no seu servidor PostgreSQL para criar o banco de dados e um usuário dedicado.
```sql
CREATE DATABASE rpg_bot_db;
CREATE USER rpg_user WITH ENCRYPTed PASSWORD 'sua_senha_segura';
GRANT ALL PRIVILEGES ON DATABASE rpg_bot_db TO rpg_user;
```

**d. Configure as Variáveis de Ambiente do Banco de Dados:**
A aplicação requer as seguintes variáveis de ambiente **apenas para a conexão com o banco de dados**. Para desenvolvimento local, você pode configurá-las diretamente na sua IDE (Run/Debug Configurations no IntelliJ).

* `DB_URL`: A URL de conexão JDBC para o seu banco. Ex: `jdbc:postgresql://localhost:5432/rpg_bot_db`
* `DB_USER`: O usuário do banco de dados. Ex: `rpg_user`
* `DB_PASS`: A senha do usuário do banco. Ex: `sua_senha_segura`

### 2. Executando Localmente

Após configurar o ambiente, abra o projeto na sua IDE. O Maven irá baixar todas as dependências automaticamente. Em seguida, execute o método `main` da classe `Bot.java`.

### 3. Empacotando para Produção

Para criar um arquivo `.jar` executável contendo todas as dependências ("fat JAR"), utilize o Maven:

```bash
mvn clean package
```

O arquivo final estará em `target/rpg-discord-bot-1.0-jar-with-dependencies.jar`.

### 4. Executando em Produção

Em um servidor com as variáveis de ambiente devidamente configuradas, execute o bot com o seguinte comando:

```bash
java -jar target/rpg-discord-bot-1.0-jar-with-dependencies.jar
```

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.