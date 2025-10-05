package com.bot;

import com.bot.discord.ComandosListener;
import com.bot.discord.ComandosRegister;
import com.bot.discord.games.WebSocketServerManager;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.net.URI; // <-- NOVO IMPORT
import java.net.URISyntaxException; // <-- NOVO IMPORT
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Bot {

    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static WebSocketServerManager wsServer;
    public static String APP_URL;

    public static void main(String[] args) throws InterruptedException {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EXECUTOR.shutdown();
            try {
                if (wsServer != null) {
                    wsServer.stop();
                    System.out.println("Servidor WebSocket parado.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }));

        String discordToken = System.getenv("DISCORD_TOKEN");
        String databaseUrl = System.getenv("DB_URL");
        String appUrl = System.getenv("APP_URL");

        if (discordToken == null || databaseUrl == null) {
            System.err.println("ERRO FATAL: As variáveis de ambiente DISCORD_TOKEN e/ou DB_URL não foram encontradas.");
            return;
        }

        APP_URL = appUrl;

        PersonagemService personagemService;
        try {
            // --- INÍCIO DA CORREÇÃO DEFINITIVA ---
            // Usamos a classe URI para extrair as partes da URL de forma segura.
            URI dbUri = new URI(databaseUrl);

            String dbUser = dbUri.getUserInfo().split(":")[0];
            String dbPass = dbUri.getUserInfo().split(":")[1];
            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

            System.out.println("Conectando ao banco de dados com o usuário: " + dbUser);
            personagemService = new PersonagemService(jdbcUrl, dbUser, dbPass);
            // --- FIM DA CORREÇÃO DEFINITIVA ---

        } catch (URISyntaxException e) {
            System.err.println("ERRO FATAL: A URL do banco de dados (DB_URL) está mal formatada.");
            e.printStackTrace();
            return;
        }

        JDA jda = JDABuilder.createDefault(discordToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.playing("RPG de Mesa"))
                .addEventListeners(new ComandosListener(personagemService))
                .build()
                .awaitReady();

        int wsPort = Integer.parseInt(System.getenv("PORT")); // O Render define a porta dinamicamente
        wsServer = new WebSocketServerManager(wsPort, jda);
        wsServer.start();

        ComandosRegister.register(jda);

        System.out.println("Bot iniciado e pronto para receber comandos!");
    }
}