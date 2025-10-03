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
     * Constrói o Embed padrão para exibir a ficha de um personagem.
     */
    public static MessageEmbed buildPersonagemEmbed(Personagem p, User user) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor("Ficha de " + user.getName(), null, user.getEffectiveAvatarUrl());
        eb.setTitle(p.getNome());
        eb.setColor(Color.CYAN);
        eb.setThumbnail(p.getFotoUrl());
        eb.setFooter("ID do Personagem (Usuário): " + p.getUserId());

        eb.addField("Nível", String.valueOf(p.getNivel()), true);
        eb.addField("Pontos Disponíveis", String.valueOf(p.getPontosDisponiveis()), true);
        eb.addBlankField(false);

        String atributosPrincipais = String.format(
                "**Corpo:** %d\n**Destreza:** %d\n**Mente:** %d\n**Vontade:** %d",
                p.getCorpo(), p.getDestreza(), p.getMente(), p.getVontade()
        );
        eb.addField("Atributos Principais", atributosPrincipais, true);

        Map<String, Integer> subAtributos = CalculadoraAtributos.calcularSubAtributos(p);
        StringBuilder subAtributosStr = new StringBuilder();
        for (Map.Entry<String, Integer> entry : subAtributos.entrySet()) {
            subAtributosStr.append(String.format("**%s:** %d\n", entry.getKey(), entry.getValue()));
        }
        eb.addField("Atributos Secundários", subAtributosStr.toString(), true);

        return eb.build();
    }

    /**
     * Constrói o Embed específico para a interface de gerenciamento de atributos.
     *
     * @param p O objeto {@link Personagem} para exibir os pontos e atributos atuais.
     * @return Um objeto {@link MessageEmbed} construído para a interface de atributos.
     */
    public static MessageEmbed buildAtributosEmbed(Personagem p) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Distribuição de Pontos de Atributo");
        eb.setColor(Color.YELLOW);

        // --- TEXTO ATUALIZADO ---
        eb.setDescription("Clique no botão correspondente para aumentar um atributo.\n\nVocê tem **"
                + p.getPontosDisponiveis() + "** pontos disponíveis.");

        // Adiciona os campos para cada atributo.
        eb.addField("Corpo", String.valueOf(p.getCorpo()), true);
        eb.addField("Destreza", String.valueOf(p.getDestreza()), true);
        eb.addField("Mente", String.valueOf(p.getMente()), true);
        eb.addField("Vontade", String.valueOf(p.getVontade()), true);

        eb.setFooter("Lembre-se: um atributo não pode ter de 3 pontos de diferença dos outros.");

        return eb.build();
    }
}