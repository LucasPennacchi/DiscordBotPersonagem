package com.bot.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitária e sem estado (stateless) responsável por inicializar e configurar
 * a camada de persistência JPA (Hibernate).
 * <p>
 * O propósito desta classe é centralizar a criação da {@link EntityManagerFactory},
 * que é um objeto caro de ser criado e deve ser instanciado apenas uma vez
 * durante o ciclo de vida da aplicação.
 * <p>
 * A principal característica desta implementação é a externalização da configuração
 * do banco de dados. As informações sensíveis (URL, usuário e senha) são lidas
 * a partir de variáveis de ambiente, tornando a aplicação portátil e pronta para
 * ser implantada em diferentes ambientes (desenvolvimento, testes, produção na nuvem)
 * sem a necessidade de alterar o código ou arquivos de configuração versionados.
 */
public final class PersistenceManager {

    /**
     * Construtor privado para prevenir a instanciação da classe utilitária.
     */
    private PersistenceManager() {
        // Esta classe não deve ser instanciada.
    }

    /**
     * Cria e configura a instância da {@link EntityManagerFactory}.
     * <p>
     * Este método lê as seguintes variáveis de ambiente obrigatórias:
     * <ul>
     * <li>{@code DB_URL}: A URL de conexão JDBC com o banco de dados.</li>
     * <li>{@code DB_USER}: O nome de usuário para a conexão com o banco.</li>
     * <li>{@code DB_PASS}: A senha para a conexão com o banco.</li>
     * </ul>
     * Se qualquer uma dessas variáveis não estiver definida, a aplicação irá registrar
     * um erro fatal no console e encerrar sua execução. Esta abordagem "fail-fast"
     * garante que a aplicação não inicie em um estado inválido.
     * <p>
     * As propriedades lidas são então usadas para sobrescrever as configurações da
     * unidade de persistência "rpg-pu" definida no arquivo {@code persistence.xml}.
     *
     * @return Uma instância de {@link EntityManagerFactory} pronta para uso,
     * que será injetada no {@code PersonagemService}.
     */
    public static EntityManagerFactory createEntityManagerFactory() {
        // Lê as variáveis de ambiente.
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        // Validação "Fail-Fast": Se as variáveis não estiverem configuradas, encerra a aplicação.
        if (dbUrl == null || dbUser == null || dbPass == null || dbUrl.isBlank() || dbUser.isBlank()) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("!! ERRO FATAL: Variáveis de ambiente do banco de dados    !!");
            System.err.println("!! não foram configuradas corretamente.                  !!");
            System.err.println("!!                                                        !!");
            System.err.println("!! Verifique se as seguintes variáveis estão definidas:   !!");
            System.err.println("!!   - DB_URL                                             !!");
            System.err.println("!!   - DB_USER                                            !!");
            System.err.println("!!   - DB_PASS                                            !!");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.exit(1); // Encerra a JVM com um código de erro.
        }

        // Cria um mapa de propriedades para sobrescrever as configurações do persistence.xml
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", dbUrl);
        properties.put("jakarta.persistence.jdbc.user", dbUser);
        properties.put("jakarta.persistence.jdbc.password", dbPass);

        // Adiciona outras propriedades do Hibernate que são consistentes entre ambientes.
        // O dialeto pode, opcionalmente, ser lido de uma variável de ambiente também.
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update"); // 'update' é seguro para produção.
        properties.put("hibernate.show_sql", "true"); // Útil para debug, pode ser desativado em produção.
        properties.put("hibernate.format_sql", "true");


        // Cria a EntityManagerFactory usando o nome da unidade de persistência do
        // persistence.xml e o mapa de propriedades customizadas.
        return Persistence.createEntityManagerFactory("rpg-pu", properties);
    }
}