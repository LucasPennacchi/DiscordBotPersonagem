package com.bot.service;

import com.bot.model.Personagem;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Classe utilitária e sem estado (stateless) para realizar cálculos relacionados
 * aos atributos de um personagem.
 * <p>
 * O propósito desta classe é centralizar a lógica de cálculo dos sub-atributos,
 * facilitando futuras modificações nas fórmulas sem a necessidade de alterar
 * outras partes do sistema, como a camada de apresentação (Discord).
 * <p>
 * Por ser uma classe utilitária, todos os seus métodos são estáticos e ela não
 * deve ser instanciada.
 */
public final class CalculadoraAtributos {

    /**
     * Construtor privado para prevenir a instanciação da classe utilitária.
     */
    private CalculadoraAtributos() {
        // Esta classe não deve ser instanciada.
    }

    /**
     * Calcula os sub-atributos de um personagem com base em seus atributos principais.
     * Os sub-atributos são valores derivados que representam as capacidades
     * específicas do personagem em combate ou outras situações de jogo.
     *
     * @param personagem O objeto {@link Personagem} cujos sub-atributos serão calculados.
     * Este objeto deve ser válido e não-nulo.
     * @return Um {@code Map<String, Integer>} contendo o nome de cada sub-atributo
     * como chave e seu valor calculado como valor. A ordem de inserção dos
     * atributos no mapa é preservada para uma exibição consistente.
     */
    public static Map<String, Integer> calcularSubAtributos(Personagem personagem) {
        // Usamos LinkedHashMap para garantir que a ordem dos atributos seja mantida
        // ao exibi-los para o usuário.
        Map<String, Integer> subAtributos = new LinkedHashMap<>();

        // --- Atributos derivados de CORPO ---
        // Força: Influencia o dano físico. Proporção 1:1 com Corpo.
        subAtributos.put("Força", personagem.getCorpo() * 1);
        // Vida: Pontos de vida totais do personagem. Proporção 5:1 com Corpo.
        subAtributos.put("Vida", personagem.getCorpo() * 5);

        // --- Atributos derivados de DESTREZA ---
        // Defesa: Capacidade de evitar ou reduzir dano. Proporção 1:1 com Destreza.
        subAtributos.put("Defesa", personagem.getDestreza() * 1);
        // Locomoção: Distância que o personagem pode se mover. Proporção 2:1 com Destreza.
        subAtributos.put("Locomoção", personagem.getDestreza() * 2);

        // --- Atributos derivados de MENTE ---
        // Intelecto: Capacidade de raciocínio e conhecimento. Proporção 1:1 com Mente.
        subAtributos.put("Intelecto", personagem.getMente() * 1);
        // Percepção: Capacidade de notar detalhes no ambiente. Proporção 1:1 com Mente.
        subAtributos.put("Percepção", personagem.getMente() * 1);

        // --- Atributos derivados de VONTADE ---
        // Foco: Recurso para usar habilidades especiais. Proporção 3:1 com Vontade.
        subAtributos.put("Foco", personagem.getVontade() * 3);
        // Resistência Mental (MR): Defesa contra ataques mentais. Proporção 1:1 com Vontade.
        subAtributos.put("Resistência Mental (MR)", personagem.getVontade() * 1);

        return subAtributos;
    }
}