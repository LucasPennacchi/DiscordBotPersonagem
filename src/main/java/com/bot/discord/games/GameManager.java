package com.bot.discord.games;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe central para gerenciar o estado e o contexto dos mini-jogos ativos.
 */
public class GameManager {

    /**
     * Classe interna para guardar o contexto de uma sessão de jogo de reflexo.
     */
    public static class ReflexGameSession {
        public final String channelId; // O ID do canal onde o jogo foi iniciado.

        public ReflexGameSession(String channelId) {
            this.channelId = channelId;
        }
    }

    /**
     * Mapa estático que armazena as sessões de jogos de reflexo em andamento.
     * A chave é o ID do usuário do Discord que está jogando.
     */
    public static final Map<String, ReflexGameSession> activeReflexGames = new ConcurrentHashMap<>();
}