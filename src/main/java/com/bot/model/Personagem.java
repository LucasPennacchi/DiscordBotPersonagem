package com.bot.model;
import java.util.List;
import java.util.Random;

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

    // --- NOVO: Lista de URLs para as fotos padrão ---
    // Você pode adicionar, remover ou trocar os links nesta lista como quiser.
    private static final List<String> FOTOS_PADRAO = List.of(
            "https://i.pinimg.com/1200x/c4/50/95/c450955e6e747e421656edb5d306e01c.jpg",
            "https://i.pinimg.com/736x/88/ea/2b/88ea2b38a91acc8af25125fac52f63b0.jpg",
            "https://i.pinimg.com/1200x/99/8b/8f/998b8f31eb5055055d5ae2ea6c5b091b.jpg",
            "https://i.pinimg.com/736x/55/ae/e2/55aee2c2eb758f8a383efa0b2b12a35f.jpg",
            "https://i.pinimg.com/736x/6e/a0/53/6ea053926804d6554117e9263e6b69ce.jpg",
            "https://i.pinimg.com/736x/6f/84/f3/6f84f342183d86bf25eeb1c35263ca6f.jpg",
            "https://i.pinimg.com/1200x/56/01/2d/56012d7260d30bec97f8eea089d08dcc.jpg",
            "https://i.pinimg.com/1200x/de/69/7e/de697e1176cc8d17e648dcd8130e9e2e.jpg",
            "https://i.pinimg.com/1200x/bc/ca/ee/bccaeea91dbb2ded1ce9f890b639f044.jpg",
            "https://i.pinimg.com/1200x/66/2d/9b/662d9b0b8906550fed686d0d0608440e.jpg",
            "https://i.pinimg.com/736x/82/15/56/82155685c31df04b7911d54f9a409ca9.jpg",
            "https://i.pinimg.com/736x/45/ea/46/45ea46d7360ef4615fc5e301886a4009.jpg",
            "https://i.pinimg.com/736x/fe/cc/ca/feccca09069edbc17117bc4e0578ce12.jpg",
            "https://i.pinimg.com/736x/c9/6f/7b/c96f7ba6b15d3012bcf5315b034626cf.jpg",
            "https://i.pinimg.com/736x/94/32/a7/9432a7b14813ad83ae40f7b08c4acee5.jpg",
            "https://i.pinimg.com/736x/87/48/d1/8748d1cf24a8675d3d533efbdd4ccc73.jpg",
            "https://i.pinimg.com/736x/0b/ca/08/0bca085741dac18d73ad554c6997d341.jpg",
            "https://i.pinimg.com/736x/20/47/0f/20470fbc7887a9fde4527243c9a6a209.jpg"
    );

    /**
     * O ID único do usuário do Discord, que serve como identificador do personagem.
     */
    private String userId;
    private String nome;
    private int nivel;
    private String fotoUrl;
    private int corpo;
    private int destreza;
    private int mente;
    private int vontade;
    private int pontosDisponiveis;
    private static final Random random = new Random();

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

        this.corpo = 0;
        this.destreza = 0;
        this.mente = 0;
        this.vontade = 0;
        this.pontosDisponiveis = nivel * 3;
        int indexSorteado = random.nextInt(FOTOS_PADRAO.size());
        this.fotoUrl = FOTOS_PADRAO.get(indexSorteado);
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