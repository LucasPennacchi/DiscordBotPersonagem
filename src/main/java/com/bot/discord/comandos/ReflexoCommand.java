package com.bot.discord.comandos;

import com.bot.discord.games.GameManager;
import com.bot.discord.games.WebSocketServerManager;
import com.bot.model.Personagem;
import com.bot.service.CalculadoraAtributos;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /reflexo, que é restrito a administradores.
 * <p>
 * Este comando inicia um desafio de reflexo para um jogador conectado. Ele busca
 * os dados do personagem do jogador alvo, calcula sua defesa, e envia todos os
 * parâmetros do desafio via WebSocket para a aplicação web cliente do jogador.
 */
public class ReflexoCommand implements ICommand {

    @Override
    public String getName() {
        return "reflexo";
    }

    @Override
    public String getDescription() {
        return "(Admin) Inicia um desafio de reflexo para um jogador conectado.";
    }

    /**
     * Define os parâmetros do comando: o usuário alvo, a pontuação necessária e os erros permitidos.
     */
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "usuario", "O jogador a ser desafiado.", true),
                new OptionData(OptionType.INTEGER, "pontuacao_necessaria", "A pontuação necessária para vencer.", true)
                        .setMinValue(1),
                new OptionData(OptionType.INTEGER, "erros_permitidos", "A quantidade de erros permitidos.", true)
                        .setMinValue(1)
        );
    }

    @Override
    public boolean isAdminCommand() {
        return true;
    }

    /**
     * Executa o comando, buscando o personagem, calculando sua defesa,
     * construindo uma mensagem JSON e enviando-a para o cliente do jogador
     * alvo através do WebSocketServerManager.
     *
     * @param event O evento de interação do slash command.
     * @param service A instância do PersonagemService para buscar dados do personagem.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        User targetUser = event.getOption("usuario").getAsUser();
        int pontuacao = event.getOption("pontuacao_necessaria").getAsInt();
        int erros = event.getOption("erros_permitidos").getAsInt();

        // 1. Busca o personagem do jogador alvo para obter seus status.
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(targetUser.getId());

        if (personagemOpt.isEmpty()) {
            event.reply("O usuário " + targetUser.getAsMention() + " não possui um personagem para o desafio.").setEphemeral(true).queue();
            return;
        }

        Personagem personagem = personagemOpt.get();

        // 2. Calcula os sub-atributos para obter o valor da Defesa.
        Map<String, Integer> subAtributos = CalculadoraAtributos.calcularSubAtributos(personagem);
        int defesa = subAtributos.get("Defesa");

        // 3. Cria e registra a sessão do jogo com o ID do canal atual.
        String channelId = event.getChannel().getId();
        GameManager.ReflexGameSession session = new GameManager.ReflexGameSession(channelId);
        GameManager.activeReflexGames.put(targetUser.getId(), session);

        // 4. Monta a mensagem JSON que o cliente de jogo (p5.js) espera, agora incluindo a defesa.
        String startGameMessage = String.format(
                "{\"action\":\"START_GAME\", \"pontuacaoNecessaria\":%d, \"errosPermitidos\":%d, \"defesa\":%d}",
                pontuacao, erros, defesa
        );

        // 5. Envia a mensagem para o usuário alvo através do WebSocket.
        boolean sent = WebSocketServerManager.sendMessageToUser(targetUser.getId(), startGameMessage);

        // 6. Envia uma confirmação (privada) para quem usou o comando.
        if (sent) {
            event.reply("Desafio de reflexo enviado para " + targetUser.getAsMention() + "!").setEphemeral(true).queue();
        } else {
            // Se não foi possível enviar, remove a sessão do jogo que acabamos de criar.
            GameManager.activeReflexGames.remove(targetUser.getId());
            event.reply("Falha ao enviar desafio: o usuário " + targetUser.getAsMention() + " não está com o cliente de jogo conectado.").setEphemeral(true).queue();
        }
    }
}