package com.bot.discord.comandos;

import com.bot.discord.EmbedManager;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /ver, que é restrito a administradores.
 * <p>
 * Este comando permite que um administrador visualize a ficha de personagem de qualquer
 * outro usuário no servidor. Ele reutiliza o mesmo {@link EmbedManager} que o comando
 * /personagem para garantir uma exibição consistente da ficha.
 */
public class VerCommand implements ICommand {

    /**
     * Retorna o nome do comando.
     *
     * @return O nome "ver".
     */
    @Override
    public String getName() {
        return "ver";
    }

    /**
     * Retorna a descrição do comando.
     *
     * @return A descrição "(Admin) Vê a ficha de outro jogador.".
     */
    @Override
    public String getDescription() {
        return "(Admin) Vê a ficha de outro jogador.";
    }

    /**
     * Define as opções (argumentos) para o comando /ver.
     *
     * @return Uma lista contendo a opção "usuario" do tipo USER, que é obrigatória.
     */
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "usuario", "O usuário que você quer ver.", true)
        );
    }

    /**
     * Sobrescreve o método padrão para marcar este comando como exclusivo para administradores.
     * O {@code ComandosRegister} usará esta informação para restringir o uso do comando
     * apenas para membros com a permissão de Administrador.
     *
     * @return {@code true} para indicar que é um comando de administrador.
     */
    @Override
    public boolean isAdminCommand() {
        return true;
    }

    /**
     * Executa a lógica do comando /ver.
     * <p>
     * O fluxo de execução é o seguinte:
     * 1. Adia a resposta para evitar timeouts.
     * 2. Obtém o usuário alvo a partir das opções do comando.
     * 3. Busca o personagem associado a esse usuário.
     * 4. Se o personagem for encontrado, utiliza o {@link EmbedManager} para construir
     * a ficha e a envia como resposta.
     * 5. Se o personagem não for encontrado, envia uma mensagem de erro efêmera para
     * o administrador.
     *
     * @param event   O objeto do evento de interação, contendo todas as informações da invocação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply().queue();

        // Obtém o usuário-alvo da opção do comando.
        User targetUser = event.getOption("usuario").getAsUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(targetUser.getId());

        personagemOpt.ifPresentOrElse(
                // Ação se o personagem do usuário-alvo EXISTIR
                personagem -> {
                    MessageEmbed embed = EmbedManager.buildPersonagemEmbed(personagem, targetUser);
                    event.getHook().sendMessageEmbeds(embed).queue();
                },
                // Ação se o personagem NÃO EXISTIR
                () -> {
                    String mensagem = "O usuário " + targetUser.getAsMention() + " não possui um personagem.";
                    event.getHook().sendMessage(mensagem).setEphemeral(true).queue();
                }
        );
    }
}