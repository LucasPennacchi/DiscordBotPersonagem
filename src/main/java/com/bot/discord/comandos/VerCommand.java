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
        event.deferReply().queue();
        User targetUser = event.getOption("usuario").getAsUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(targetUser.getId());

        personagemOpt.ifPresentOrElse(
                personagem -> {
                    try {
                        // 1. Gera a imagem dinâmica com os atributos do personagem alvo
                        byte[] imageBytes = ImageGenerator.generatePersonagemAttributesImage(personagem);

                        // 2. Constrói o embed, passando o personagem, o usuário alvo e a imagem gerada
                        MessageEmbed embed = EmbedManager.buildPersonagemEmbed(personagem, targetUser, imageBytes);

                        // 3. Envia a imagem como arquivo e o embed juntos
                        event.getHook().sendFiles(FileUpload.fromData(imageBytes, "ficha_atributos.png"))
                                .addEmbeds(embed)
                                .queue();

                    } catch (Exception e) { // Captura Exception genérica por causa do Batik
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