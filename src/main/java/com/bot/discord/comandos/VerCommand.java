package com.bot.discord.comandos;

import com.bot.discord.EmbedManager;
import com.bot.discord.ImageGenerator;
import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.List;
import java.util.Optional;

/**
 * Implementa a lógica para o comando /ver, que é restrito a administradores.
 * Permite visualizar a ficha de personagem de qualquer outro usuário no servidor.
 */
public class VerCommand implements ICommand {

    @Override
    public String getName() {
        return "ver";
    }

    @Override
    public String getDescription() {
        return "(Admin) Vê a ficha de outro jogador.";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(new OptionData(OptionType.USER, "usuario", "O usuário que você quer ver.", true));
    }

    @Override
    public boolean isAdminCommand() {
        return true;
    }

    /**
     * Executa a lógica do comando /ver.
     * Busca o personagem do usuário alvo. Se encontrado, gera a imagem e o embed
     * da ficha e os envia como resposta.
     *
     * @param event   O objeto do evento de interação.
     * @param service A instância do serviço de personagem para a lógica de negócio.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event, PersonagemService service) {
        // Não usamos o fluxo assíncrono aqui para manter o comando de admin simples e direto.
        event.deferReply().queue();

        User targetUser = event.getOption("usuario").getAsUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(targetUser.getId());

        personagemOpt.ifPresentOrElse(
                personagem -> {
                    try {
                        byte[] imageBytes = ImageGenerator.generatePersonagemAttributesImage(personagem);


                        // Chamamos o método com o nome correto: buildPersonagemEmbedWithImage
                        // Ele não precisa mais dos bytes da imagem como parâmetro.
                        MessageEmbed embed = EmbedManager.buildPersonagemEmbedWithImage(personagem, targetUser);

                        event.getHook().sendFiles(FileUpload.fromData(imageBytes, "ficha_atributos.png"))
                                .addEmbeds(embed)
                                .queue();

                    } catch (Exception e) {
                        System.err.println("Erro ao gerar ou enviar a imagem da ficha (comando /ver): " + e.getMessage());
                        e.printStackTrace();
                        event.getHook().sendMessage("Ocorreu um erro ao gerar a imagem da ficha.").setEphemeral(true).queue();
                    }
                },
                () -> {
                    String mensagem = "O usuário " + targetUser.getAsMention() + " não possui um personagem.";
                    event.getHook().sendMessage(mensagem).setEphemeral(true).queue();
                }
        );
    }
}