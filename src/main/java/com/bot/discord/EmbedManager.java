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
     * Constrói o Embed para a ficha de personagem com o layout refinado,
     * espelhando o exemplo do Paint.
     *
     * @param p O objeto Personagem com os dados.
     * @param user O usuário do Discord associado.
     * @param attributeImageBytes Os bytes da imagem gerada de atributos.
     * @return Um objeto MessageEmbed completo.
     */
    public static MessageEmbed buildPersonagemEmbed(Personagem p, User user, byte[] attributeImageBytes) {
        EmbedBuilder eb = new EmbedBuilder();

        // <Personagem de usuario x>
        eb.setAuthor("Ficha de " + user.getName(), null, user.getEffectiveAvatarUrl());

        // <Nome> <Nível>
        eb.setTitle(p.getNome() + "  |  Nível " + p.getNivel());
        eb.setColor(Color.CYAN);

        // --- MUDANÇA 1: Foto do personagem no canto superior direito (thumbnail) ---
        // Se o personagem tiver uma foto_url definida, use-a. Caso contrário, não define thumbnail.
        if (p.getFotoUrl() != null && !p.getFotoUrl().trim().isEmpty()) {
            eb.setThumbnail(p.getFotoUrl());
        }

        // <Atributos Secundários> (posição: campo normal)
        Map<String, Integer> subAtributos = CalculadoraAtributos.calcularSubAtributos(p);
        StringBuilder subAtributosStr = new StringBuilder();
        for (Map.Entry<String, Integer> entry : subAtributos.entrySet()) {
            subAtributosStr.append(String.format("**%s:** %d\n", entry.getKey(), entry.getValue()));
        }
        // Adiciona um título mais descritivo para os sub-atributos, ou você pode deixar vazio se preferir.
        eb.addField("Atributos Secundários", subAtributosStr.toString(), false);

        // --- MUDANÇA 2: Imagem da ficha_template como imagem principal (centro do embed) ---
        // Isso coloca a imagem grande na parte inferior do embed, como no seu exemplo do Paint.
        eb.setImage("attachment://ficha_atributos.png");

        // <Pontos disponíveis, se tiver algum, aumenta a fonte>
        if (p.getPontosDisponiveis() > 0) {
            String pontosTexto = String.format("✨ **Você tem %d pontos disponíveis para gastar!** ✨", p.getPontosDisponiveis());
            eb.addField("", pontosTexto, false); // Campo vazio para um texto grande e centralizado.
        }

        // <footnote de id>
        eb.setFooter("ID do Personagem (Usuário): " + p.getUserId());

        return eb.build();
    }
}