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

    /**
     * Construtor privado para prevenir a instanciação da classe utilitária.
     */
    private EmbedManager() {}

    /**
     * Constrói o Embed padrão para exibir a ficha de um personagem.
     * Aceita os bytes da imagem dos atributos para incluí-la como imagem principal (abaixo do texto).
     *
     * @param p O objeto Personagem com os dados.
     * @param user O usuário do Discord associado.
     * @param attributeImageBytes Os bytes da imagem gerada de atributos.
     * @return Um objeto MessageEmbed completo.
     */
    public static MessageEmbed buildPersonagemEmbed(Personagem p, User user, byte[] attributeImageBytes) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor("Ficha de " + user.getName(), null, user.getEffectiveAvatarUrl());
        eb.setTitle(p.getNome());
        eb.setColor(Color.CYAN);
        eb.setThumbnail(p.getFotoUrl()); // Foto customizada do personagem (canto superior direito)
        eb.setFooter("ID do Personagem (Usuário): " + p.getUserId());

        eb.addField("Nível", String.valueOf(p.getNivel()), true);
        eb.addField("Pontos Disponíveis", String.valueOf(p.getPontosDisponiveis()), true);
        eb.addBlankField(false); // Adiciona um espaço para melhor layout

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

        // Adiciona a imagem de atributos na parte inferior do embed
        // "attachment://ficha_atributos.png" diz ao Discord para usar o anexo com este nome.
        eb.setImage("attachment://ficha_atributos.png");

        return eb.build();
    }

    /**
     * Constrói o Embed específico para a interface de gerenciamento de atributos.
     * Aceita os bytes da imagem dos atributos para incluí-la como miniatura (thumbnail).
     *
     * @param p O objeto {@link Personagem} para exibir os pontos e atributos atuais.
     * @param attributeImageBytes Os bytes da imagem gerada de atributos.
     * @return Um objeto {@link MessageEmbed} construído para a interface de atributos.
     */
    public static MessageEmbed buildAtributosEmbed(Personagem p, byte[] attributeImageBytes) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Distribuição de Pontos de Atributo");
        eb.setColor(Color.YELLOW);

        eb.setDescription("Clique no botão correspondente para aumentar um atributo.\n\nVocê tem **"
                + p.getPontosDisponiveis() + "** pontos disponíveis.");

        // Adiciona a imagem de atributos como miniatura (thumbnail) do embed (canto superior direito)
        // "attachment://atributos.png" diz ao Discord para usar o anexo com este nome.
        eb.setThumbnail("attachment://atributos.png");

        eb.addField("Corpo", String.valueOf(p.getCorpo()), true);
        eb.addField("Destreza", String.valueOf(p.getDestreza()), true);
        eb.addField("Mente", String.valueOf(p.getMente()), true);
        eb.addField("Vontade", String.valueOf(p.getVontade()), true);

        eb.setFooter("Lembre-se: um atributo não pode ter 3 ou mais pontos de diferença dos outros.");

        return eb.build();
    }
}