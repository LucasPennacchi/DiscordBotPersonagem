package com.bot.discord.comandos;

import com.bot.discord.EmbedManager;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Optional;

/**
 * Implementa a lógica para o comando /atributos.
 * <p>
 * Este comando inicia uma interface interativa com botões para que o usuário possa gastar
 * seus pontos de atributo disponíveis. Ele não possui opções e sua principal função
 * é enviar uma mensagem Embed especializada e anexar os botões de interação.
 * <p>
 * A lógica de resposta aos cliques nos botões é tratada pelo {@code InteractionManager}.
 */
public class AtributosCommand implements ICommand {

    /**
     * Retorna o nome do comando.
     *
     * @return O nome "atributos".
     */
    @Override
    public String getName() {
        return "atributos";
    }

    /**
     * Retorna a descrição do comando.
     *
     * @return A descrição "Gerencia os pontos de atributos do seu personagem.".
     */
    @Override
    public String getDescription() {
        return "Gerencia os pontos de atributos do seu personagem.";
    }

    /**
     * Executa a lógica do comando /atributos.
     * <p>
     * O fluxo de execução é o seguinte:
     * 1. Adia a resposta para evitar timeouts.
     * 2. Busca o personagem do usuário. Se não existir, envia uma mensagem de erro.
     * 3. Utiliza o {@link EmbedManager} para construir a interface visual de atributos.
     * 4. Cria os botões para cada atributo, desabilitando-os se o personagem não tiver pontos.
     * 5. Envia a mensagem embed com os botões anexados.
     *
     * @param event   O objeto do evento de interação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply().queue();

        String userId = event.getUser().getId();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(userId);

        if (personagemOpt.isEmpty()) {
            event.getHook().sendMessage("Você precisa ter um personagem para gerenciar atributos. Use `/criar`.")
                    .setEphemeral(true).queue();
            return;
        }

        Personagem personagem = personagemOpt.get();
        MessageEmbed embed = EmbedManager.buildAtributosEmbed(personagem);

        // Define se os botões devem estar habilitados ou não (apenas se houver pontos)
        boolean hasPoints = personagem.getPontosDisponiveis() > 0;

        // Cria os botões com IDs customizados no formato "ação:userId:alvo"
        Button corpoBtn = Button.secondary("attr-add:" + userId + ":corpo", "💪 Corpo")
                .withDisabled(!hasPoints);
        Button destrezaBtn = Button.secondary("attr-add:" + userId + ":destreza", "🏃 Destreza")
                .withDisabled(!hasPoints);
        Button menteBtn = Button.secondary("attr-add:" + userId + ":mente", "🧠 Mente")
                .withDisabled(!hasPoints);
        Button vontadeBtn = Button.secondary("attr-add:" + userId + ":vontade", "✨ Vontade")
                .withDisabled(!hasPoints);

        // Envia a mensagem com o embed e uma "ActionRow" contendo os botões
        event.getHook().sendMessageEmbeds(embed)
                .addComponents(ActionRow.of(corpoBtn, destrezaBtn, menteBtn, vontadeBtn))
                .queue();
    }
}