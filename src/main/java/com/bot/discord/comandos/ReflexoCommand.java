package com.bot.discord.comandos;

import com.bot.discord.games.GameManager;
import com.bot.discord.games.WebSocketServerManager;
import com.bot.model.Personagem;
import com.bot.service.CalculadoraAtributos;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /reflexo, que é restrito a administradores.
 * Inicia um desafio de reflexo para um jogador conectado com múltiplos parâmetros customizáveis.
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
     * Define os parâmetros do comando, incluindo os novos parâmetros opcionais.
     */
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "usuario", "O jogador a ser desafiado.", true),
                new OptionData(OptionType.INTEGER, "pontuacao_necessaria", "A pontuação necessária para vencer.", true)
                        .setMinValue(1),
                new OptionData(OptionType.INTEGER, "erros_permitidos", "A quantidade de erros permitidos.", true)
                        .setMinValue(1),
                new OptionData(OptionType.STRING, "modo", "O modo de jogo (padrão: normal).", false)
                        .addChoice("Normal (progressivo)", "normal")
                        .addChoice("Apenas Barras Brancas", "branco"),
                new OptionData(OptionType.NUMBER, "velocidade_inicial", "Velocidade inicial do ponteiro (padrão: 2.5).", false),
                new OptionData(OptionType.NUMBER, "tempo_limite", "Tempo limite em segundos (-1 para infinito).", false)
        );
    }

    @Override
    public boolean isAdminCommand() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        User targetUser = event.getOption("usuario").getAsUser();
        int pontuacao = event.getOption("pontuacao_necessaria").getAsInt();
        int erros = event.getOption("erros_permitidos").getAsInt();

        String modo = event.getOption("modo", "normal", OptionMapping::getAsString);
        double velocidadeInicial = event.getOption("velocidade_inicial", 2.5, OptionMapping::getAsDouble);
        double tempoLimite = event.getOption("tempo_limite", -1.0, OptionMapping::getAsDouble);

        Optional<Personagem> personagemOpt = service.buscarPorUsuario(targetUser.getId());
        if (personagemOpt.isEmpty()) {
            event.reply("O usuário " + targetUser.getAsMention() + " não possui um personagem para o desafio.").setEphemeral(true).queue();
            return;
        }

        Personagem personagem = personagemOpt.get();
        Map<String, Integer> subAtributos = CalculadoraAtributos.calcularSubAtributos(personagem);
        int defesa = subAtributos.get("Defesa");

        String channelId = event.getChannel().getId();
        GameManager.ReflexGameSession session = new GameManager.ReflexGameSession(channelId);
        GameManager.activeReflexGames.put(targetUser.getId(), session);

        // Usamos Locale.US para garantir que números flutuantes usem '.' como separador decimal.
        String startGameMessage = String.format(java.util.Locale.US,
                "{\"action\":\"START_GAME\",\"pontuacaoNecessaria\":%d,\"errosPermitidos\":%d,\"defesa\":%d,\"modo\":\"%s\",\"velocidadeInicial\":%.2f,\"tempoLimite\":%.2f}",
                pontuacao, erros, defesa, modo, velocidadeInicial, tempoLimite
        );

        boolean sent = WebSocketServerManager.sendMessageToUser(targetUser.getId(), startGameMessage);

        if (sent) {
            event.reply("Desafio de reflexo enviado para " + targetUser.getAsMention() + "!").setEphemeral(true).queue();
        } else {
            GameManager.activeReflexGames.remove(targetUser.getId());
            event.reply("Falha ao enviar desafio: o usuário " + targetUser.getAsMention() + " não está com o cliente de jogo conectado.").setEphemeral(true).queue();
        }
    }
}