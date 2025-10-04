package com.bot.discord;

import com.bot.model.Personagem;
import com.bot.service.CalculadoraAtributos;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.util.Map;

/**
 * Classe utilitária para construir e padronizar todas as mensagens Embed do bot.
 */
public final class EmbedManager {

    private EmbedManager() {}

    /**
     * Método base privado que constrói o corpo principal (apenas texto) do embed da ficha.
     * É reutilizado pelos métodos públicos para garantir consistência.
     *
     * @param p    O objeto Personagem com os dados.
     * @param user O usuário do Discord associado.
     * @return Um {@link EmbedBuilder} pré-configurado com todas as informações textuais.
     */
    private static EmbedBuilder buildPersonagemEmbedBase(Personagem p, User user) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor("Ficha de " + user.getName(), null, user.getEffectiveAvatarUrl());
        eb.setTitle(p.getNome() + "  |  Nível " + p.getNivel());
        eb.setColor(Color.CYAN);
        eb.setFooter("ID do Personagem (Usuário): " + p.getUserId());

        // A foto customizada do personagem continua como thumbnail.
        if (p.getFotoUrl() != null && !p.getFotoUrl().trim().isEmpty()) {
            eb.setThumbnail(p.getFotoUrl());
        }

        Map<String, Integer> subAtributos = CalculadoraAtributos.calcularSubAtributos(p);
        StringBuilder subAtributosStr = new StringBuilder();
        for (Map.Entry<String, Integer> entry : subAtributos.entrySet()) {
            subAtributosStr.append(String.format("**%s:** %d\n", entry.getKey(), entry.getValue()));
        }
        eb.addField("Atributos Secundários", subAtributosStr.toString(), false);

        if (p.getPontosDisponiveis() > 0) {
            String pontosTexto = String.format("✨ **Você tem %d pontos disponíveis para gastar!** ✨", p.getPontosDisponiveis());
            eb.addField("", pontosTexto, false);
        }
        return eb;
    }

    /**
     * Constrói o embed da ficha de personagem contendo apenas o texto.
     * Usado para a resposta imediata e rápida do comando /personagem.
     *
     * @param p    O objeto Personagem.
     * @param user O usuário do Discord.
     * @return Um {@link MessageEmbed} contendo apenas as informações textuais da ficha.
     */
    public static MessageEmbed buildPersonagemEmbedTextOnly(Personagem p, User user) {
        return buildPersonagemEmbedBase(p, user).build();
    }

    /**
     * Constrói o embed da ficha de personagem completo, com o texto e a imagem de atributos.
     * Usado para a edição final da mensagem, após a imagem ter sido gerada.
     *
     * @param p    O objeto Personagem.
     * @param user O usuário do Discord.
     * @return Um {@link MessageEmbed} completo, com a referência para a imagem de atributos.
     */
    public static MessageEmbed buildPersonagemEmbedWithImage(Personagem p, User user) {
        EmbedBuilder eb = buildPersonagemEmbedBase(p, user);
        // Adiciona a referência à imagem que será enviada como anexo.
        eb.setImage("attachment://ficha_atributos.png");
        return eb.build();
    }

    /**
     * Constrói o Embed para a interface de gerenciamento de atributos.
     */
    public static MessageEmbed buildAtributosEmbed(Personagem p, byte[] attributeImageBytes) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Distribuição de Pontos de Atributo");
        eb.setColor(Color.YELLOW);
        eb.setDescription("Clique no botão correspondente para aumentar um atributo.\n\nVocê tem **"
                + p.getPontosDisponiveis() + "** pontos disponíveis.");
        eb.setImage("attachment://atributos.png");
        eb.setFooter("Lembre-se: um atributo não pode ter 3 ou mais pontos de diferença dos outros.");
        return eb.build();
    }
}