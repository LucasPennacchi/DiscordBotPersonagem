package com.bot.model;

/**
 * Representa o modelo de dados de um personagem no jogo de RPG.
 * <p>
 * Esta classe é um POJO (Plain Old Java Object), atuando como um contêiner de dados
 * para as informações de um personagem. Ela não possui nenhuma lógica de persistência
 * ou acoplamento com o banco de dados.
 * <p>
 * A responsabilidade de preencher (carregar) e persistir (salvar) os dados
 * de um objeto Personagem é totalmente da camada de serviço ({@code PersonagemService}).
 */
public class Personagem {

    /**
     * O ID único do usuário do Discord, que serve como identificador do personagem.
     */
    private String userId;

    /**
     * O nome do personagem.
     */
    private String nome;

    /**
     * O nível atual do personagem.
     */
    private int nivel;

    /**
     * A URL para a imagem (avatar) do personagem.
     */
    private String fotoUrl;

    /**
     * Atributo principal que representa a força física e vitalidade do personagem.
     */
    private int corpo;

    /**
     * Atributo principal que representa a agilidade, reflexos e coordenação do personagem.
     */
    private int destreza;

    /**
     * Atributo principal que representa a inteligência, raciocínio e conhecimento do personagem.
     */
    private int mente;

    /**
     * Atributo principal que representa a força de vontade, determinação e resistência mental.
     */
    private int vontade;

    /**
     * A quantidade de pontos que o jogador pode distribuir para aumentar os atributos principais.
     */
    private int pontosDisponiveis;

    /**
     * Construtor padrão. Útil para criar uma instância vazia que será preenchida
     * manualmente (por exemplo, ao carregar dados do banco de dados).
     */
    public Personagem() {
    }

    /**
     * Construtor para criar um novo personagem com valores iniciais padrão.
     * Utilizado pelo serviço ao criar um personagem pela primeira vez.
     *
     * @param userId O ID do usuário do Discord.
     * @param nome   O nome inicial para o personagem.
     * @param nivel  O nível inicial do personagem.
     */
    public Personagem(String userId, String nome, int nivel) {
        this.userId = userId;
        this.nome = nome;
        this.nivel = nivel;

        this.corpo = 1;
        this.destreza = 1;
        this.mente = 1;
        this.vontade = 1;
        this.pontosDisponiveis = nivel * 3;
        this.fotoUrl = "https://i.imgur.com/8f12b7j.png";
    }

    // --- Getters e Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public int getCorpo() {
        return corpo;
    }

    public void setCorpo(int corpo) {
        this.corpo = corpo;
    }

    public int getDestreza() {
        return destreza;
    }

    public void setDestreza(int destreza) {
        this.destreza = destreza;
    }



    public int getMente() {
        return mente;
    }

    public void setMente(int mente) {
        this.mente = mente;
    }

    public int getVontade() {
        return vontade;
    }

    public void setVontade(int vontade) {
        this.vontade = vontade;
    }

    public int getPontosDisponiveis() {
        return pontosDisponiveis;
    }

    public void setPontosDisponiveis(int pontosDisponiveis) {
        this.pontosDisponiveis = pontosDisponiveis;
    }
}