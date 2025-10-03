# Bot de Personagem de RPG para Discord

![Java](https://img.shields.io/badge/Java-21+-orange?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-4.0.0-red?style=for-the-badge&logo=apache-maven)
![JDA](https://img.shields.io/badge/JDA-5.0.0-blue?style=for-the-badge)
![JDBC](https://img.shields.io/badge/Persistence-JDBC-blue?style=for-the-badge&logo=java)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue?style=for-the-badge&logo=postgresql)

Um bot para Discord focado na criação e gerenciamento de fichas de personagem para um sistema de RPG customizado. O projeto foi desenvolvido em Java, utilizando uma arquitetura de serviços robusta e preparada para ambientes de nuvem.

## 📜 Propósito

O objetivo deste bot é facilitar a experiência de jogadores de RPG de mesa, permitindo que eles interajam com suas fichas de personagem diretamente pelo Discord. Ele automatiza a criação, atualização de atributos, cálculos de sub-atributos e visualização da ficha, servindo como uma ferramenta central durante as sessões de jogo.

## ✨ Funcionalidades

* `/criar [nome] [nivel]`: Cria um novo personagem para o usuário.
* `/personagem`: Exibe a ficha completa do personagem do usuário.
* `/nome <novo_nome>`: Altera o nome do personagem.
* `/foto <imagem>`: Altera a imagem de perfil do personagem.
* `/deletar`: Inicia um processo de confirmação para deletar o personagem.
* `/atributos`: Permite ao usuário gastar pontos para aumentar os atributos através de botões interativos.
* `/ver <@usuario>`: (Admin) Permite que um administrador veja a ficha de qualquer usuário.
* `/upar <@usuario> <niveis>`: (Admin) Aumenta o nível e concede pontos de atributo a um personagem.

## 🏛️ Arquitetura

O projeto foi construído seguindo os princípios de uma **Arquitetura Orientada a Serviços (SOA)** para garantir alta manutenibilidade e baixo acoplamento. A estrutura é dividida em três camadas principais:

1.  **Camada de Interface (`discord`):** Responsável exclusivamente por interagir com a API do Discord (receber comandos, botões e enviar respostas). Utiliza a biblioteca JDA.
2.  **Camada de Serviço (`service`):** O cérebro da aplicação. Contém toda a lógica de negócio (regras de criação, validação de atributos, cálculos, etc.) de forma independente do Discord.
3.  **Camada de Persistência (`model`):** Responsável por interagir com o banco de dados. Esta camada é implementada usando **JDBC (Java Database Connectivity)** puro.
    * A classe `PersonagemService` contém toda a lógica de acesso ao banco, escrevendo as queries SQL diretamente e gerenciando as conexões.
    * A classe `Personagem` atua como um POJO (Plain Old Java Object), um simples contêiner de dados, desacoplado da lógica de persistência.

A grande vantagem desta arquitetura é que a camada de persistência pôde ser completamente trocada (de Hibernate/JPA para JDBC) sem que **nenhuma alteração** fosse necessária na Camada de Interface ou nas classes de Comando.

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 21+
* **Build Tool:** Apache Maven
* **API Discord:** JDA (Java Discord API)
* **Persistência:** **JDBC (Java Database Connectivity)**
* **Banco de Dados:** PostgreSQL
* **Configuração:** Dotenv-java (para ler arquivos `.env`)
* **Logging:** Logback

## 🚀 Como Executar o Projeto

### Pré-requisitos

* JDK 17 ou superior
* Apache Maven
* Um servidor de banco de dados PostgreSQL
* Um token de Bot do Portal de Desenvolvedores do Discord

### 1. Configuração do Ambiente

**a. Clone o repositório:**
```bash
git clone [https://github.com/seu-usuario/seu-repositorio.git](https://github.com/seu-usuario/seu-repositorio.git)
cd seu-repositorio
```

**b. Crie o arquivo `.env`:**
Na raiz do projeto, crie uma cópia do arquivo `.env.example` com o nome `.env`. Preencha as variáveis com seus dados:
```env
DISCORD_TOKEN=seu_token_aqui
DB_URL=jdbc:postgresql://localhost:5432/rpg_bot_db
DB_USER=rpg_user
DB_PASS=sua_senha_segura
```

**c. Configure o Banco de Dados:**
Execute os scripts SQL necessários no pgAdmin (ou outra ferramenta) para criar o banco de dados `rpg_bot_db`, o usuário `rpg_user` e a tabela `personagens`.

### 2. Executando Localmente

Após configurar o ambiente, abra o projeto na sua IDE. O Maven irá baixar todas as dependências automaticamente. Em seguida, execute o método `main` da classe `Bot.java`.

### 3. Empacotando para Produção

Para criar um arquivo `.jar` executável contendo todas as dependências ("fat JAR"), utilize o Maven:

```bash
mvn clean package
```

O arquivo final estará em `target/rpg-discord-bot-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## 📄 Licença

Este projeto está sob a licença MIT.