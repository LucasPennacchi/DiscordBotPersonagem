package com.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa a entidade de um personagem no jogo de RPG.
 * <p>
 * Esta classe é uma entidade JPA, mapeada para a tabela "personagens" no banco de dados.
 * Cada instância desta classe corresponde a uma linha na tabela, contendo todos os
 * dados essenciais de um personagem, como seus atributos, nível e informações de identificação
 * vinculadas a um usuário do Discord.
 */
@Entity
@Table(name = "personagens")
public class Personagem {

    /**
     * O ID único do usuário do Discord, utilizado como chave primária.
     * Garante que cada usuário do Discord possa ter apenas um personagem.
     */
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * O nome do personagem. Não pode ser nulo.
     */
    @Column(nullable = false)
    private String nome;

    /**
     * O nível atual do personagem. Influencia a quantidade de pontos de atributo disponíveis.
     */
    @Column(nullable = false)
    private int nivel;

    /**
     * A URL para a imagem (avatar) do personagem.
     */
    @Column(name = "foto_url", length = 512) // Aumentando o tamanho para URLs longas
    private String fotoUrl;

    /**
     * Atributo principal que representa a força física e vitalidade do personagem.
     */
    @Column(name = "corpo")
    private int corpo;

    /**
     * Atributo principal que representa a agilidade, reflexos e coordenação do personagem.
     */
    @Column(name = "destreza")
    private int destreza;

    /**
     * Atributo principal que representa a inteligência, raciocínio e conhecimento do personagem.
     */
    @Column(name = "mente")
    private int mente;

    /**
     * Atributo principal que representa a força de vontade, determinação e resistência mental.
     */
    @Column(name = "vontade")
    private int vontade;

    /**
     * A quantidade de pontos que o jogador pode distribuir para aumentar os atributos principais.
     */
    @Column(name = "pontos_disponiveis")
    private int pontosDisponiveis;


    /**
     * Construtor padrão sem argumentos.
     * É obrigatório para o funcionamento do framework JPA (Hibernate).
     */
    public Personagem() {
    }

    /**
     * Construtor para criar um novo personagem com valores iniciais padrão.
     * Define todos os atributos como 1, calcula os pontos disponíveis com base no nível
     * e atribui uma foto padrão.
     *
     * @param userId O ID do usuário do Discord, que será a chave primária.
     * @param nome   O nome inicial para o personagem.
     * @param nivel  O nível inicial do personagem.
     */
    public Personagem(String userId, String nome, int nivel) {
        this.userId = userId;
        this.nome = nome;
        this.nivel = nivel;

        // Valores iniciais padrão para um novo personagem
        this.corpo = 1;
        this.destreza = 1;
        this.mente = 1;
        this.vontade = 1;
        this.pontosDisponiveis = nivel * 3;
        this.fotoUrl = "https://i.imgur.com/8f12b7j.png"; // URL de uma imagem padrão genérica
    }

    // --- Getters e Setters ---

    /**
     * Retorna o ID do usuário do Discord associado a este personagem.
     * @return O ID do usuário do Discord.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Define o ID do usuário do Discord para este personagem.
     * @param userId O ID do usuário do Discord.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retorna o nome do personagem.
     * @return O nome atual do personagem.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o novo nome para o personagem.
     * @param nome O novo nome a ser definido.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Retorna o nível do personagem.
     * @return O nível atual do personagem.
     */
    public int getNivel() {
        return nivel;
    }

    /**
     * Define o nível do personagem.
     * @param nivel O novo nível do personagem.
     */
    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    /**
     * Retorna a URL da foto do personagem.
     * @return A URL da imagem do personagem.
     */
    public String getFotoUrl() {
        return fotoUrl;
    }

    /**
     * Define a URL da foto do personagem.
     * @param fotoUrl A nova URL da imagem para o personagem.
     */
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    /**
     * Retorna o valor do atributo Corpo.
     * @return O valor de Corpo.
     */
    public int getCorpo() {
        return corpo;
    }

    /**
     * Define o valor do atributo Corpo.
     * @param corpo O novo valor de Corpo.
     */
    public void setCorpo(int corpo) {
        this.corpo = corpo;
    }

    /**
     * Retorna o valor do atributo Destreza.
     * @return O valor de Destreza.
     */
    public int getDestreza() {
        return destreza;
    }

    /**
     * Define o valor do atributo Destreza.
     * @param destreza O novo valor de Destreza.
     */
    public void setDestreza(int destreza) {
        this.destreza = destreza;
    }

    /**
     * Retorna o valor do atributo Mente.
     * @return O valor de Mente.
     */
    public int getMente() {
        return mente;
    }

    /**
     * Define o valor do atributo Mente.
     * @param mente O novo valor de Mente.
     */
    public void setMente(int mente) {
        this.mente = mente;
    }

    /**
     * Retorna o valor do atributo Vontade.
     * @return O valor de Vontade.
     */
    public int getVontade() {
        return vontade;
    }

    /**
     * Define o valor do atributo Vontade.
     * @param vontade O novo valor de Vontade.
     */
    public void setVontade(int vontade) {
        this.vontade = vontade;
    }

    /**
     * Retorna a quantidade de pontos disponíveis para distribuição.
     * @return O total de pontos de atributo não gastos.
     */
    public int getPontosDisponiveis() {
        return pontosDisponiveis;
    }

    /**
     * Define a quantidade de pontos disponíveis para distribuição.
     * @param pontosDisponiveis O novo total de pontos de atributo não gastos.
     */
    public void setPontosDisponiveis(int pontosDisponiveis) {
        this.pontosDisponiveis = pontosDisponiveis;
    }
}