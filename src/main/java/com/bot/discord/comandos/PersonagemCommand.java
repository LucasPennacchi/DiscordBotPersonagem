package com.bot.discord.comandos;

import com.bot.discord.EmbedManager;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Optional;

/**
 * Implementa a lógica para o comando /personagem.
 * <p>
 * Este comando é responsável por exibir a ficha de personagem do usuário que o invocou.
 * Ele não possui opções e sua função principal é buscar os dados do personagem e
 * utilizar o {@link EmbedManager} para formatar e exibir essas informações de
 * forma visualmente agradável em uma mensagem Embed.
 */
public class PersonagemCommand implements ICommand {

    /**
     * Retorna o nome do comando.
     *
     * @return O nome "personagem".
     */
    @Override
    public String getName() {
        return "personagem";
    }

    /**
     * Retorna a descrição do comando.
     *
     * @return A descrição "Mostra a ficha do seu personagem.".
     */
    @Override
    public String getDescription() {
        return "Mostra a ficha do seu personagem.";
    }

    /**
     * Executa a lógica do comando /personagem.
     * <p>
     * O fluxo de execução é o seguinte:
     * 1. Adia a resposta para evitar timeouts.
     * 2. Obtém o usuário que invocou o comando.
     * 3. Busca o personagem associado a esse usuário no banco de dados através do serviço.
     * 4. Se o personagem for encontrado, delega a criação do Embed para o {@link EmbedManager}
     * e envia a ficha como resposta.
     * 5. Se o personagem não for encontrado, envia uma mensagem de ajuda privada (efêmera)
     * instruindo o usuário a usar o comando /criar.
     *
     * @param event   O objeto do evento de interação, contendo todas as informações da invocação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply().queue();

        User user = event.getUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(user.getId());

        // A abordagem funcional com ifPresentOrElse deixa o código mais limpo.
        personagemOpt.ifPresentOrElse(
                // Ação a ser executada se o personagem EXISTIR
                personagem -> {
                    MessageEmbed embed = EmbedManager.buildPersonagemEmbed(personagem, user);
                    event.getHook().sendMessageEmbeds(embed).queue();
                },
                // Ação a ser executada se o personagem NÃO EXISTIR
                () -> {
                    event.getHook().sendMessage("Você não possui um personagem. Use `/criar` para começar sua jornada!")
                            .setEphemeral(true).queue();
                }
        );
    }
}