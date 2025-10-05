package com.bot;

import com.bot.discord.ComandosListener;
import com.bot.discord.ComandosRegister;
import com.bot.discord.games.WebSocketServerManager;
import com.bot.service.PersonagemService;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe principal e ponto de entrada da aplicação do bot.
 */
public class Bot {

    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static WebSocketServerManager wsServer;
    public static String APP_URL;

    private Bot() {}

    /**
     * O método principal que inicia a aplicação.
     * @param args Argumentos de linha de comando (não utilizados).
     * @throws InterruptedException Se a JDA não conseguir iniciar.
     */
    public static void main(String[] args) throws InterruptedException {

        // Hook para garantir o desligamento limpo dos nossos serviços.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EXECUTOR.shutdown();
            try {
                if (wsServer != null) {
                    wsServer.stop();
                    System.out.println("Servidor WebSocket parado.");
                }
            } catch (InterruptedException e) { // <-- CORREÇÃO: Removemos o IOException daqui
                System.err.println("Erro ao parar o servidor WebSocket:");
                e.printStackTrace();
                // É uma boa prática restaurar o status de interrupção da thread
                Thread.currentThread().interrupt();
            }
        }));

        Dotenv dotenv = Dotenv.configure().load();
        APP_URL = dotenv.get("APP_URL");

        String discordToken = dotenv.get("DISCORD_TOKEN");
        String dbUrl = dotenv.get("DB_URL");
        String dbUser = dotenv.get("DB_USER");
        String dbPass = dotenv.get("DB_PASS");

        if (discordToken == null || dbUrl == null || dbUser == null || dbPass == null) {
            System.err.println("ERRO FATAL: Uma ou mais variáveis essenciais não foram encontradas no arquivo .env.");
            return;
        }

        PersonagemService personagemService = new PersonagemService(dbUrl, dbUser, dbPass);

        JDA jda = JDABuilder.createDefault(discordToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.playing("RPG de Mesa"))
                .addEventListeners(new ComandosListener(personagemService))
                .build()
                .awaitReady();

        int wsPort = 8080;
        wsServer = new WebSocketServerManager(wsPort, jda);
        wsServer.start();

        ComandosRegister.register(jda);

        System.out.println("Bot iniciado e pronto para receber comandos!");
    }
}