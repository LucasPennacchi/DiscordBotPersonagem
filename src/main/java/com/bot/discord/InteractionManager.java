package com.bot.discord;

import com.bot.model.Personagem;
import com.bot.service.PersonagemService;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Classe utilitária e sem estado (stateless) para tratar eventos de interação
 * que não são slash commands, como cliques em botões e adições de reação.
 * <p>
 * O propósito desta classe é remover a complexidade de lidar com interações
 * contínuas do {@link ComandosListener}, centralizando essa lógica em um único local.
 */
public final class InteractionManager {

    /**
     * Construtor privado para prevenir a instanciação da classe utilitária.
     */
    private InteractionManager() {}

    /**
     * Processa todos os eventos de clique em botão ({@link ButtonInteractionEvent}).
     * <p>
     * Atualmente utilizado para o fluxo de confirmação do comando /deletar.
     * Inclui uma verificação de segurança para garantir que apenas o usuário que
     * invocou o comando original possa clicar nos botões correspondentes.
     *
     * @param event O objeto do evento de interação de botão fornecido pela JDA.
     * @param service A instância do {@link PersonagemService} para executar ações de negócio.
     */
    public static void handleButtonInteraction(ButtonInteractionEvent event, PersonagemService service) {
        String[] parts = event.getComponentId().split(":");
        String action = parts[0];
        String targetUserId = parts[1];

        // Medida de segurança crucial: Apenas o usuário que iniciou o comando pode interagir.
        if (!event.getUser().getId().equals(targetUserId)) {
            event.reply("Você não pode interagir com os botões de outro usuário.").setEphemeral(true).queue();
            return;
        }

        // Acknowledge da interação para evitar o erro "This interaction failed".
        event.deferEdit().queue();

        // Roteia a ação com base no ID do botão.
        switch (action) {
            case "delete-confirm":
                service.deletar(targetUserId);
                // Edita a mensagem original para dar feedback e remove os botões.
                event.getHook().editOriginal("Personagem deletado com sucesso.")
                        .setComponents(Collections.emptyList()).queue();
                break;

            case "delete-cancel":
                event.getHook().editOriginal("Ação cancelada.")
                        .setComponents(Collections.emptyList()).queue();
                break;
        }
    }

    /**
     * Processa eventos de adição de reação ({@link MessageReactionAddEvent}).
     * <p>
     * Atualmente utilizado para a interface de distribuição de pontos do comando /atributos.
     * O método verifica se a reação ocorreu em uma mensagem "rastreada", se o autor da
     * reação é o dono daquela sessão e então aplica a lógica de negócio correspondente.
     *
     * @param event O objeto do evento de adição de reação fornecido pela JDA.
     * @param service A instância do {@link PersonagemService} para executar a lógica de negócio.
     * @param tracker O Mapa que rastreia as mensagens de atributos ativas (ID da Mensagem -> ID do Usuário).
     */
    public static void handleReactionAdd(MessageReactionAddEvent event, PersonagemService service, Map<Long, String> tracker) {
        // Ignora reações do próprio bot para evitar loops.
        if (event.getUserId().equals(event.getJDA().getSelfUser().getId())) {
            return;
        }

        // Verifica se a reação foi em uma mensagem que estamos rastreando.
        String targetUserId = tracker.get(event.getMessageIdLong());

        // Medida de segurança: Verifica se a mensagem é rastreada e se quem reagiu é o "dono".
        if (targetUserId == null || !targetUserId.equals(event.getUserId())) {
            return;
        }

        // Remove a reação do usuário para que ele possa clicar novamente (feedback instantâneo).
        event.getReaction().removeReaction(event.getUser()).queue();

        Optional<Personagem> personagemOpt = service.buscarPorUsuario(targetUserId);
        if (personagemOpt.isEmpty()) {
            tracker.remove(event.getMessageIdLong()); // Limpa o tracker se o personagem não existe mais.
            return;
        }

        Personagem p = personagemOpt.get();
        if (p.getPontosDisponiveis() <= 0) {
            // Se não há pontos, não faz nada.
            return;
        }

        // Determina qual atributo modificar com base no emoji.
        String atributo = null;
        String emoji = event.getEmoji().getName();
        switch (emoji) {
            case "💪" -> atributo = "corpo";
            case "🏃" -> atributo = "destreza";
            case "🧠" -> atributo = "mente";
            case "✨" -> atributo = "vontade";
        }

        // Se for um emoji válido e a regra de negócio permitir, atualiza o personagem.
        if (atributo != null && service.podeAumentarAtributo(p, atributo)) {
            p.setPontosDisponiveis(p.getPontosDisponiveis() - 1);
            switch (atributo) {
                case "corpo" -> p.setCorpo(p.getCorpo() + 1);
                case "destreza" -> p.setDestreza(p.getDestreza() + 1);
                case "mente" -> p.setMente(p.getMente() + 1);
                case "vontade" -> p.setVontade(p.getVontade() + 1);
            }
            service.salvar(p);

            // Atualiza a mensagem original com os novos valores usando o EmbedManager.
            event.getChannel().editMessageEmbedsById(event.getMessageId(), EmbedManager.buildAtributosEmbed(p)).queue();
        }
    }
}