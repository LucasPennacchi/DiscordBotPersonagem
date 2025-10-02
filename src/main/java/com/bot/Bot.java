package com.bot;

import com.bot.config.PersistenceManager;
import com.bot.discord.ComandosListener;
import com.bot.discord.ComandosRegister;
import com.bot.service.PersonagemService;
import jakarta.persistence.EntityManagerFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe principal e ponto de entrada (entrypoint) da aplicação do bot de Discord.
 * <p>
 * As responsabilidades desta classe são:
 * <ul>
 * <li>Carregar configurações essenciais, como o token do bot.</li>
 * <li>Inicializar a camada de persistência (JPA/Hibernate) através do {@link PersistenceManager}.</li>
 * <li>Instanciar a camada de serviço ({@link PersonagemService}).</li>
 * <li>Construir e iniciar a instância do JDA (Java Discord API).</li>
 * <li>Registrar o listener de eventos ({@link ComandosListener}).</li>
 * <li>Delegar o registro dos slash commands para a classe {@link ComandosRegister}.</li>
 * </ul>
 */
public class Bot {

    /**
     * Construtor privado para prevenir a instanciação desta classe utilitária.
     */
    private Bot() {
        // Esta classe não deve ser instanciada.
    }

    /**
     * O método principal que inicia a aplicação do bot.
     *
     * @param args Argumentos de linha de comando (não utilizados).
     * @throws InterruptedException Se a thread for interrompida enquanto aguarda a JDA
     * ficar pronta (lançado por {@code awaitReady()}).
     */
    public static void main(String[] args) throws InterruptedException {

        // --- 1. Carregar Configurações ---
        String discordToken = loadToken();
        if (discordToken == null) {
            System.err.println("Inicialização do bot abortada devido à falha ao carregar o token.");
            return;
        }

        // --- 2. Inicializar a Camada de Persistência e Serviços ---
        // O PersistenceManager lê as variáveis de ambiente do banco de dados (DB_URL, DB_USER, DB_PASS)
        EntityManagerFactory emf = PersistenceManager.createEntityManagerFactory();
        PersonagemService personagemService = new PersonagemService(emf);

        // --- 3. Construir e Iniciar a Instância do JDA ---
        // Cria a instância do JDA, injetando o serviço de personagem no novo listener simplificado.
        JDA jda = JDABuilder.createDefault(discordToken)
                // Habilita as "Intents" necessárias para o bot funcionar corretamente.
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                // Define a atividade do bot (ex: "Jogando RPG de Mesa").
                .setActivity(Activity.playing("RPG de Mesa"))
                // Adiciona nossa classe que ouve e delega todos os eventos de interação.
                .addEventListeners(new ComandosListener(personagemService))
                // Constrói a instância e aguarda até que a conexão com o Discord seja estabelecida.
                .build()
                .awaitReady();

        // --- 4. Registrar os Slash Commands ---
        // A responsabilidade de registro agora é delegada para a classe ComandosRegister.
        ComandosRegister.register(jda);

        System.out.println("Bot iniciado e pronto para receber comandos!");
    }

    /**
     * Carrega o token do bot a partir do arquivo {@code config.properties}.
     * <p>
     * Este método procura por um arquivo {@code config.properties} na pasta de recursos
     * e lê a propriedade "DISCORD_TOKEN".
     *
     * @return O token como {@code String}, ou {@code null} se o arquivo ou a propriedade
     * não forem encontrados, ou se ocorrer um erro de leitura.
     */
    private static String loadToken() {
        Properties prop = new Properties();
        // O caminho pode precisar ser ajustado dependendo do seu diretório de execução.
        // Usar ClassLoader é uma abordagem mais robusta para encontrar recursos.
        try (InputStream input = Bot.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("FATAL: Não foi possível encontrar o arquivo 'config.properties' no classpath.");
                System.err.println("Certifique-se de que ele está em 'src/main/resources'.");
                return null;
            }
            prop.load(input);
            String token = prop.getProperty("DISCORD_TOKEN");
            if (token == null || token.isEmpty()) {
                System.err.println("FATAL: A propriedade 'DISCORD_TOKEN' está vazia ou não foi encontrada no arquivo config.properties.");
                return null;
            }
            return token;
        } catch (IOException ex) {
            System.err.println("FATAL: Ocorreu um erro ao ler o arquivo 'config.properties'.");
            ex.printStackTrace();
            return null;
        }
    }
}