package com.bot;

import com.bot.discord.ComandosListener;
import com.bot.discord.ComandosRegister;
import com.bot.service.PersonagemService;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe principal e ponto de entrada (entrypoint) da aplicação do bot de Discord.
 * <p>
 * As responsabilidades desta classe são:
 * <ul>
 * <li>Carregar configurações do ambiente a partir do arquivo {@code .env}.</li>
 * <li>Instanciar a camada de serviço ({@link PersonagemService}) com as credenciais do banco de dados.</li>
 * <li>Construir e iniciar a instância do JDA (Java Discord API) com o token do bot.</li>
 * <li>Registrar o listener de eventos ({@link ComandosListener}).</li>
 * <li>Delegar o registro dos slash commands para a classe {@link ComandosRegister}.</li>
 * </ul>
 */
public class Bot {

    // Cria um pool de threads que cresce conforme a necessidade.
    // Usaremos isso para todas as nossas tarefas assíncronas pesadas.
    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

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

        // Hook de Desligamento
        // Garante que, ao fechar o bot (ex: com Ctrl+C), o pool de threads
        // seja desligado de forma organizada.
        Runtime.getRuntime().addShutdownHook(new Thread(EXECUTOR::shutdown));

        // --- 1. Carregar Configurações do arquivo .env ---
        // A biblioteca Dotenv procura por um arquivo .env na raiz do projeto e o carrega.
        Dotenv dotenv = Dotenv.configure().load();

        String discordToken = dotenv.get("DISCORD_TOKEN");
        String dbUrl = dotenv.get("DB_URL");
        String dbUser = dotenv.get("DB_USER");
        String dbPass = dotenv.get("DB_PASS");

        // Validação básica para garantir que as variáveis essenciais foram carregadas.
        if (discordToken == null || dbUrl == null || dbUser == null || dbPass == null) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("!! ERRO FATAL: Uma ou mais variáveis essenciais não        !!");
            System.err.println("!! foram encontradas no arquivo .env.                     !!");
            System.err.println("!! Verifique se DISCORD_TOKEN, DB_URL, DB_USER, e DB_PASS !!");
            System.err.println("!! estão definidos.                                       !!");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return; // Encerra a aplicação
        }

        // --- 2. Inicializar a Camada de Serviço ---
        // Instancia o serviço de personagem, passando as credenciais do banco lidas do .env.
        PersonagemService personagemService = new PersonagemService(dbUrl, dbUser, dbPass);

        // --- 3. Construir e Iniciar a Instância do JDA ---
        // Cria a instância do JDA, injetando o serviço de personagem no listener de comandos.
        JDA jda = JDABuilder.createDefault(discordToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.playing("RPG de Mesa"))
                .addEventListeners(new ComandosListener(personagemService))
                .build()
                .awaitReady();

        // --- 4. Registrar os Slash Commands ---
        // A responsabilidade de registro é delegada para a classe ComandosRegister.
        ComandosRegister.register(jda);

        System.out.println("Bot iniciado e pronto para receber comandos!");
    }
}