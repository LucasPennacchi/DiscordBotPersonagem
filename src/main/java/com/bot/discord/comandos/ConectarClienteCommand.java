package com.bot.discord.comandos;

import com.bot.Bot;
import com.bot.discord.games.WebSocketServerManager;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Implementa a lógica para o comando /conectar-cliente.
 * Gera um token de uso único para que o cliente de jogo (.jar) possa se autenticar
 * com o servidor WebSocket do bot.
 */
public class ConectarClienteCommand implements ICommand {
    @Override
    public String getName() {
        return "conectar-cliente";
    }

    @Override
    public String getDescription() {
        return "Gera um token temporário para conectar seu cliente de jogo.";
    }

    /**
     * Executa o comando, gerando um token e enviando-o por mensagem privada (efêmera).
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        String userId = event.getUser().getId();

        // Gera um novo token e o associa a este usuário
        String token = WebSocketServerManager.generateToken(userId);

        String response = String.format(
                "**Passo 1: Acesse a Aplicação Web**\n" +
                    "Clique no [aqui](https://%s) para abrir o jogo:\n\n" +
                "**Passo 2: Use seu Token de Conexão**\n" +
                    "Copie o token abaixo e cole no campo indicado na página do jogo:\n" +
                "```\n" +
                    "%s\n" +
                "```\n" +
                "Este token é de uso único e garante que o jogo se conecte à sua conta.",
                Bot.APP_URL, token // Usa a URL carregada do .env
        );

        // Envia a resposta de forma privada para que apenas o jogador veja seu token
        event.reply(response).setEphemeral(true).queue();
    }
}