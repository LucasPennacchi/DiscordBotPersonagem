package com.bot.discord;

import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Collections;
import java.util.Optional;

/**
 * Classe auxiliar para tratar eventos de interação de componentes, como botões.
 * Esta classe centraliza a lógica de resposta a interações contínuas.
 */
public final class InteractionManager {

    private InteractionManager() {}

    /**
     * Processa todos os eventos de clique em botão ({@link ButtonInteractionEvent}).
     * Roteia a ação com base no ID customizado do botão para a lógica apropriada.
     *
     * @param event O objeto do evento de interação de botão.
     * @param service A instância do {@link PersonagemService} para executar ações de negócio.
     */
    public static void handleButtonInteraction(ButtonInteractionEvent event, PersonagemService service) {
        String[] parts = event.getComponentId().split(":");
        String action = parts[0];
        String targetUserId = parts[1];

        if (!event.getUser().getId().equals(targetUserId)) {
            event.reply("Você não pode interagir com os botões de outro usuário.").setEphemeral(true).queue();
            return;
        }

        event.deferEdit().queue();

        switch (action) {
            case "delete-confirm":
                service.deletar(targetUserId);
                event.getHook().editOriginal("Personagem deletado com sucesso.")
                        .setComponents(Collections.emptyList()).queue();
                break;

            case "delete-cancel":
                event.getHook().editOriginal("Ação cancelada.")
                        .setComponents(Collections.emptyList()).queue();
                break;

            case "attr-add":
                String attributeToUpgrade = parts[2];
                handleAttributeUpgrade(event, service, targetUserId, attributeToUpgrade);
                break;
        }
    }

    /**
     * Lida especificamente com a lógica de upgrade de atributos após um clique no botão.
     * Atualiza o personagem, gera uma nova imagem e edita a mensagem original com a ficha completa.
     */
    private static void handleAttributeUpgrade(ButtonInteractionEvent event, PersonagemService service, String userId, String attributeToUpgrade) {
        User user = event.getUser();
        Optional<Personagem> personagemOpt = service.buscarPorUsuario(userId);

        if (personagemOpt.isEmpty()) {
            event.getHook().editOriginal("Erro: Personagem não encontrado.")
                    .setComponents(Collections.emptyList()).queue();
            return;
        }

        Personagem p = personagemOpt.get();

        if (p.getPontosDisponiveis() > 0 && service.podeAumentarAtributo(p, attributeToUpgrade)) {
            p.setPontosDisponiveis(p.getPontosDisponiveis() - 1);
            switch (attributeToUpgrade) {
                case "corpo" -> p.setCorpo(p.getCorpo() + 1);
                case "destreza" -> p.setDestreza(p.getDestreza() + 1);
                case "mente" -> p.setMente(p.getMente() + 1);
                case "vontade" -> p.setVontade(p.getVontade() + 1);
            }
            service.salvar(p);
        }

        try {
            byte[] newImageBytes = ImageGenerator.generatePersonagemAttributesImage(p);

            // --- INÍCIO DA CORREÇÃO ---
            // Chamamos o método com o nome correto: buildPersonagemEmbedWithImage
            MessageEmbed newEmbed = EmbedManager.buildPersonagemEmbedWithImage(p, user);
            // --- FIM DA CORREÇÃO ---

            WebhookMessageEditAction editAction = event.getHook()
                    .editOriginalAttachments(FileUpload.fromData(newImageBytes, "ficha_atributos.png"))
                    .setEmbeds(newEmbed);

            if (p.getPontosDisponiveis() <= 0) {
                editAction.setComponents(Collections.emptyList());
            }

            editAction.queue();

        } catch (Exception e) {
            System.err.println("Erro ao re-gerar imagem de atributos: " + e.getMessage());
            e.printStackTrace();
            event.getHook().sendMessage("Ocorreu um erro ao atualizar a ficha.").setEphemeral(true).queue();
        }
    }
}