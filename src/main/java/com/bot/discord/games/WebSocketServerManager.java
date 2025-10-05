package com.bot.discord.games;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia o servidor WebSocket para os mini-jogos interativos.
 */
public class WebSocketServerManager extends WebSocketServer {

    private static final Map<String, String> pendingTokens = new ConcurrentHashMap<>();
    private static final Map<String, WebSocket> activeConnections = new ConcurrentHashMap<>();
    private static final Map<WebSocket, String> reverseConnections = new ConcurrentHashMap<>();

    private final JDA jda;

    public WebSocketServerManager(int port, JDA jda) {
        super(new InetSocketAddress(port));
        this.jda = jda;
    }

    public static String generateToken(String userId) {
        String token = UUID.randomUUID().toString();
        pendingTokens.put(token, userId);
        return token;
    }

    public static boolean sendMessageToUser(String userId, String message) {
        WebSocket conn = activeConnections.get(userId);
        if (conn != null && conn.isOpen()) {
            conn.send(message);
            return true;
        }
        return false;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Nova conexão WebSocket aberta de: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String userId = reverseConnections.remove(conn);
        if (userId != null) {
            activeConnections.remove(userId);
            System.out.println("Usuário " + userId + " desconectado.");
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Mensagem recebida de " + conn.getRemoteSocketAddress() + ": " + message);

        try {
            if (message.startsWith("AUTH:")) {
                String token = message.substring(5);
                String userId = pendingTokens.remove(token);
                if (userId != null) {
                    activeConnections.put(userId, conn);
                    reverseConnections.put(conn, userId);
                    System.out.println("Usuário " + userId + " autenticado com sucesso!");
                    conn.send("{\"status\": \"authenticated\"}");
                } else {
                    conn.send("{\"status\": \"auth_failed\"}");
                    conn.close();
                }
                return;
            }

            if (message.contains("\"action\":\"GAME_RESULT\"")) {
                // Encontra qual usuário enviou esta mensagem
                String userId = reverseConnections.get(conn);
                if (userId == null) return; // Mensagem de uma conexão não autenticada

                // Pega a sessão do jogo para saber em qual canal postar o resultado
                GameManager.ReflexGameSession session = GameManager.activeReflexGames.remove(userId);
                if (session == null) return; // Jogo já finalizado ou não encontrado

                // Encontra o canal do Discord onde o comando /reflexo foi originalmente usado
                MessageChannel channel = jda.getTextChannelById(session.channelId);
                if (channel == null) {
                    System.err.println("Não foi possível encontrar o canal com ID: " + session.channelId);
                    return;
                }

                // Pega o objeto User para poder mencioná-lo no chat
                User user = jda.retrieveUserById(userId).complete();
                if (user == null) return;

                // Determina a mensagem de resultado
                String resultText = message.contains("\"result\":\"success\"")
                        ? "venceu o desafio de reflexo! 🎉"
                        : "falhou no desafio de reflexo. 💥";

                String finalMessage = String.format("%s %s", user.getAsMention(), resultText);

                // Envia a mensagem pública no canal onde o jogo começou
                channel.sendMessage(finalMessage).queue();
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar mensagem WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Ocorreu um erro na conexão WebSocket " + (conn != null ? conn.getRemoteSocketAddress() : ""));
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Servidor WebSocket iniciado na porta: " + getPort());
    }
}