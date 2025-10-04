package com.bot.discord.comandos;

import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

/**
 * Implementa a lógica para o comando /deletar.
 * <p>
 * Este comando inicia um processo de confirmação para a exclusão permanente
 * do personagem de um usuário. Ele não deleta o personagem diretamente, mas
 * envia uma mensagem privada (efêmera) com botões de confirmação e cancelamento.
 * <p>
 * A lógica de resposta aos cliques nos botões é tratada pelo {@code InteractionManager}.
 * Esta classe apenas inicia o fluxo, garantindo uma etapa de segurança para
 * uma ação destrutiva.
 */
public class DeletarCommand implements ICommand {

    /**
     * Retorna o nome do comando.
     *
     * @return O nome "deletar".
     */
    @Override
    public String getName() {
        return "deletar";
    }

    /**
     * Retorna a descrição do comando.
     *
     * @return A descrição "Deleta seu personagem permanentemente.".
     */
    @Override
    public String getDescription() {
        return "Deleta seu personagem permanentemente.";
    }

    /**
     * Executa a lógica do comando /deletar.
     * <p>
     * O fluxo de execução é o seguinte:
     * 1. Adia a resposta de forma efêmera (privada para o usuário).
     * 2. Verifica se o usuário possui um personagem para deletar.
     * 3. Se possuir, cria dois botões: um de confirmação (vermelho) e um de cancelamento (cinza).
     * 4. O ID de cada botão inclui o ID do usuário que invocou o comando, uma medida de
     * segurança para garantir que apenas o usuário original possa clicar.
     * 5. Envia a mensagem de confirmação com os botões.
     *
     * @param event   O objeto do evento de interação, contendo todas as informações da invocação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        // A resposta é efêmera, pois a confirmação deve ser privada para o usuário.
        event.deferReply(true).queue();

        String userId = event.getUser().getId();

        // Verifica se o personagem existe antes de prosseguir.
        if (service.buscarPorUsuario(userId).isEmpty()) {
            event.getHook().sendMessage("Você não possui um personagem para deletar.").queue();
            return;
        }

        // Cria os botões de interação. Os IDs são estruturados como "ação:id_do_alvo"
        // para serem processados de forma segura pelo InteractionManager.
        Button confirmButton = Button.danger("delete-confirm:" + userId, "Sim, deletar permanentemente");
        Button cancelButton = Button.secondary("delete-cancel:" + userId, "Cancelar");

        // Envia a mensagem de confirmação com os botões em uma ActionRow.
        event.getHook().sendMessage("Você tem certeza que deseja deletar seu personagem? **Esta ação é irreversível.**")
                .addActionRow(confirmButton, cancelButton)
                .queue();
    }
}