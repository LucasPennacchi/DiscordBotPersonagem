package com.bot.discord.comandos;

import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /foto.
 * <p>
 * Este comando permite que um usuário altere a imagem de perfil (avatar) do seu
 * personagem, enviando um arquivo de imagem como anexo. O comando valida se o
 * anexo é uma imagem antes de salvar a nova URL no banco de dados.
 */
public class FotoCommand implements ICommand {

    /**
     * Retorna o nome do comando.
     *
     * @return O nome "foto".
     */
    @Override
    public String getName() {
        return "foto";
    }

    /**
     * Retorna a descrição do comando.
     *
     * @return A descrição "Altera a foto do seu personagem.".
     */
    @Override
    public String getDescription() {
        return "Altera a foto do seu personagem.";
    }

    /**
     * Define as opções (argumentos) para o comando /foto.
     *
     * @return Uma lista contendo a opção "imagem" do tipo ATTACHMENT,
     * que é obrigatória.
     */
    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.ATTACHMENT, "imagem", "O novo arquivo de imagem para o personagem.", true)
        );
    }

    /**
     * Executa a lógica do comando /foto.
     * <p>
     * O fluxo de execução é o seguinte:
     * 1. Adia a resposta para evitar timeouts.
     * 2. Verifica se o usuário possui um personagem.
     * 3. Obtém o anexo da interação.
     * 4. Valida se o anexo é um tipo de imagem suportado pela JDA.
     * 5. Se for válido, atualiza a URL da foto do personagem no banco de dados via serviço.
     * 6. Envia uma mensagem Embed de confirmação exibindo a nova imagem.
     *
     * @param event   O objeto do evento de interação, contendo todas as informações da invocação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        event.deferReply(true).queue();

        String userId = event.getUser().getId();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(userId);

        // 1. Verifica se o personagem existe.
        if (personagemOpt.isEmpty()) {
            event.getHook().sendMessage("Você precisa ter um personagem para alterar a foto. Use `/criar`.")
                    .setEphemeral(true).queue();
            return;
        }

        // 2. Obtém o anexo. Como a opção é obrigatória, não precisamos verificar se é nulo.
        Message.Attachment attachment = event.getOption("imagem").getAsAttachment();

        // 3. Valida se o anexo é uma imagem.
        if (!attachment.isImage()) {
            event.getHook().sendMessage("O arquivo enviado precisa ser uma imagem (ex: .png, .jpg, .gif).")
                    .setEphemeral(true).queue();
            return;
        }

        Personagem personagem = personagemOpt.get();
        // 4. Atualiza a URL da foto e salva.
        personagem.setFotoUrl(attachment.getUrl());
        service.salvar(personagem);

        // 5. Envia uma confirmação visual.
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Foto do personagem atualizada!")
                .setColor(Color.GREEN)
                .setDescription("A nova foto de **" + personagem.getNome() + "** foi salva.")
                .setImage(attachment.getUrl()); // Exibe a nova imagem diretamente no embed.

        event.getHook().sendMessageEmbeds(eb.build()).queue();
    }
}