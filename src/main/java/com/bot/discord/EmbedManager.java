package com.bot.discord;

import com.bot.model.Personagem;
import com.bot.service.CalculadoraAtributos;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.util.Map;

/**
 * Classe utilitária e sem estado (stateless) para construir e padronizar todas as
 * mensagens {@link MessageEmbed} do bot.
 * <p>
 * O propósito desta classe é separar a lógica de apresentação (a aparência das
 * respostas) da lógica de execução dos comandos. Isso promove um código mais limpo
 * e facilita a manutenção do estilo visual do bot de forma centralizada.
 * <p>
 * Por ser uma classe utilitária, todos os seus métodos são estáticos e ela não
 * deve ser instanciada.
 */
public final class EmbedManager {

    /**
     * Construtor privado para prevenir a instanciação da classe utilitária.
     */
    private EmbedManager() {
        // Esta classe não deve ser instanciada.
    }

    /**
     * Constrói a mensagem Embed padrão para exibir a ficha completa de um personagem.
     * Utilizado pelos comandos /personagem e /ver.
     *
     * @param p    O objeto {@link Personagem} contendo todos os dados a serem exibidos.
     * @param user O objeto {@link User} do Discord, usado para exibir o nome e o avatar
     * do dono do personagem no cabeçalho do embed.
     * @return Um objeto {@link MessageEmbed} completamente construído e pronto para ser enviado.
     */
    public static MessageEmbed buildPersonagemEmbed(Personagem p, User user) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setAuthor("Ficha de " + user.getName(), null, user.getEffectiveAvatarUrl());
        eb.setTitle(p.getNome());
        eb.setColor(Color.CYAN);
        eb.setThumbnail(p.getFotoUrl());
        eb.setFooter("ID do Personagem (Usuário): " + p.getUserId());

        eb.addField("Nível", String.valueOf(p.getNivel()), true);
        eb.addField("Pontos Disponíveis", String.valueOf(p.getPontosDisponíveis()), true);
        eb.addBlankField(false); // Adiciona um espaço para melhor formatação

        // Formata e adiciona os atributos principais
        String atributosPrincipais = String.format(
                "**Corpo:** %d\n**Destreza:** %d\n**Mente:** %d\n**Vontade:** %d",
                p.getCorpo(), p.getDestreza(), p.getMente(), p.getVontade()
        );
        eb.addField("Atributos Principais", atributosPrincipais, true);

        // Calcula e adiciona os sub-atributos
        Map<String, Integer> subAtributos = CalculadoraAtributos.calcularSubAtributos(p);
        StringBuilder subAtributosStr = new StringBuilder();
        for (Map.Entry<String, Integer> entry : subAtributos.entrySet()) {
            subAtributosStr.append(String.format("**%s:** %d\n", entry.getKey(), entry.getValue()));
        }
        eb.addField("Atributos Secundários", subAtributosStr.toString(), true);

        return eb.build();
    }

    /**
     * Constrói a mensagem Embed específica para a interface de gerenciamento de atributos.
     * Utilizado pelo comando /atributos, instrui o usuário a usar reações para
     * distribuir seus pontos.
     *
     * @param p O objeto {@link Personagem} para exibir os pontos e atributos atuais.
     * @return Um objeto {@link MessageEmbed} construído para a interface de atributos.
     */
    public static MessageEmbed buildAtributosEmbed(Personagem p) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Distribuição de Pontos de Atributo");
        eb.setColor(Color.YELLOW);
        eb.setDescription("Reaja com o emoji correspondente para aumentar um atributo.\n\nVocê tem **"
                + p.getPontosDisponíveis() + "** pontos disponíveis.");

        // Adiciona os campos para cada atributo com seu emoji correspondente
        eb.addField("💪 Corpo", String.valueOf(p.getCorpo()), true);
        eb.addField("🏃 Destreza", String.valueOf(p.getDestreza()), true);
        eb.addField("🧠 Mente", String.valueOf(p.getMente()), true);
        eb.addField("✨ Vontade", String.valueOf(p.getVontade()), true);

        eb.setFooter("Lembre-se: um atributo não pode ter mais de 3 pontos de diferença dos outros.");

        return eb.build();
    }
}