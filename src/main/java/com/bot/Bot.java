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

        // Lemos as variáveis diretamente do ambiente do servidor (configurado no Render).
        // Não usamos mais a biblioteca Dotenv.
        String discordToken = System.getenv("DISCORD_TOKEN");
        String dbUrl = System.getenv("DB_URL");
        // O Render fornece usuário e senha dentro da URL, então não precisamos de variáveis separadas para eles.
        String appUrl = System.getenv("APP_URL");

        if (discordToken == null || dbUrl == null) {
            System.err.println("ERRO FATAL: As variáveis de ambiente DISCORD_TOKEN e/ou DB_URL não foram encontradas.");
            return;
        }

        APP_URL = appUrl;

        // O Render configura o DB_URL com usuário e senha, mas nosso service espera separado.
        // Vamos extrair as credenciais da URL do Render.
        // Formato esperado: postgres://user:password@host:port/database
        String dbUser = dbUrl.substring(dbUrl.indexOf("//") + 2, dbUrl.indexOf(":", dbUrl.indexOf("//") + 2));
        String dbPass = dbUrl.substring(dbUrl.indexOf(":", dbUrl.indexOf(dbUser)) + 1, dbUrl.indexOf("@"));
        String jdbcUrl = "jdbc:" + dbUrl.substring(0, dbUrl.indexOf("//")) + dbUrl.substring(dbUrl.indexOf("@") + 1);

        PersonagemService personagemService = new PersonagemService(jdbcUrl, dbUser, dbPass);

        JDA jda = JDABuilder.createDefault(discordToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.playing("RPG de Mesa"))
                .addEventListeners(new ComandosListener(personagemService))
                .build()
                .awaitReady();

        // A porta 10000 é frequentemente usada por padrão em serviços de hospedagem como o Render.
        int wsPort = 10000;
        wsServer = new WebSocketServerManager(wsPort, jda);
        wsServer.start();

        ComandosRegister.register(jda);

        System.out.println("Bot iniciado e pronto para receber comandos!");
    }
}