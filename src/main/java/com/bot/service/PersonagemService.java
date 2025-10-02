package com.bot.service;

import com.bot.model.Personagem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Camada de serviço responsável por toda a lógica de negócio e regras
 * relacionadas aos personagens.
 * <p>
 * Esta classe é o cérebro da aplicação, agindo como um intermediário entre a camada de
 * interface (comandos do Discord) e a camada de persistência de dados (JPA/Banco de Dados).
 * Ela garante que a lógica de negócio permaneça desacoplada de detalhes de implementação
 * externos, como a API do Discord ou o acesso direto ao banco de dados.
 */
public class PersonagemService {

    /**
     * A fábrica de EntityManagers, injetada no construtor.
     * É utilizada para criar instâncias de EntityManager para cada operação de banco de dados.
     * É uma dependência essencial e imutável para o serviço.
     */
    private final EntityManagerFactory emf;

    /**
     * Constrói uma nova instância de PersonagemService.
     *
     * @param emf A instância de {@link EntityManagerFactory} necessária para a comunicação
     * com o banco de dados. Não deve ser nula.
     */
    public PersonagemService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Busca um personagem no banco de dados pelo ID do usuário do Discord.
     *
     * @param userId O ID único do usuário do Discord a ser pesquisado.
     * @return um {@link Optional} contendo o {@link Personagem} se encontrado,
     * ou um Optional vazio caso contrário.
     */
    public Optional<Personagem> buscarPorUsuario(String userId) {
        EntityManager em = emf.createEntityManager();
        try {
            // O método find é a forma mais direta de buscar uma entidade pela sua chave primária.
            // Ele retorna null se a entidade não for encontrada.
            Personagem personagem = em.find(Personagem.class, userId);
            return Optional.ofNullable(personagem);
        } finally {
            // É crucial fechar o EntityManager após cada operação para liberar recursos.
            em.close();
        }
    }

    /**
     * Cria e persiste um novo personagem com valores padrão.
     *
     * @param userId O ID do usuário do Discord.
     * @param nome   O nome do personagem.
     * @param nivel  O nível inicial do personagem.
     * @return O objeto {@link Personagem} recém-criado e salvo no banco.
     */
    public Personagem criarPersonagem(String userId, String nome, int nivel) {
        Personagem novoPersonagem = new Personagem(userId, nome, nivel);
        salvar(novoPersonagem);
        return novoPersonagem;
    }

    /**
     * Salva (insere ou atualiza) uma entidade Personagem no banco de dados.
     * Esta operação é transacional para garantir a integridade dos dados.
     *
     * @param personagem O objeto {@link Personagem} a ser salvo.
     */
    public void salvar(Personagem personagem) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            // O método merge é seguro tanto para inserir novas entidades quanto para
            // atualizar entidades existentes.
            em.merge(personagem);
            transaction.commit();
        } catch (Exception e) {
            // Se qualquer erro ocorrer, a transação é revertida para não corromper os dados.
            if (transaction.isActive()) {
                transaction.rollback();
            }
            // Logar ou relançar a exceção é uma boa prática.
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Deleta um personagem do banco de dados com base no ID do usuário do Discord.
     * A operação é transacional.
     *
     * @param userId O ID do usuário do Discord cujo personagem será deletado.
     */
    public void deletar(String userId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            // Para remover, primeiro precisamos encontrar a entidade no contexto da persistência atual.
            buscarPorUsuario(userId).ifPresent(personagem -> {
                Personagem managedPersonagem = em.merge(personagem);
                em.remove(managedPersonagem);
            });
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Aumenta o nível de um personagem e adiciona os pontos de atributo correspondentes.
     *
     * @param personagem          O personagem que subirá de nível.
     * @param niveisParaAdicionar A quantidade de níveis a serem adicionados.
     */
    public void uparNivel(Personagem personagem, int niveisParaAdicionar) {
        if (niveisParaAdicionar <= 0) {
            return; // Não faz nada se o número de níveis for zero ou negativo.
        }
        int nivelAtual = personagem.getNivel();
        int pontosAtuais = personagem.getPontosDisponiveis();

        int novosPontos = niveisParaAdicionar * 3;

        personagem.setNivel(nivelAtual + niveisParaAdicionar);
        personagem.setPontosDisponiveis(pontosAtuais + novosPontos);

        salvar(personagem);
    }


    /**
     * Valida a regra de negócio que impede um atributo de ser mais de 3 pontos
     * maior que qualquer outro atributo.
     *
     * @param p                O personagem cujos atributos serão verificados.
     * @param atributoAumentar O nome do atributo que se deseja aumentar (ex: "corpo", "destreza").
     * @return {@code true} se o aumento for permitido, {@code false} caso contrário.
     */
    public boolean podeAumentarAtributo(Personagem p, String atributoAumentar) {
        int valorAlvo;
        List<Integer> outrosValores = new ArrayList<>();

        // Determina o valor do atributo alvo após o aumento e popula a lista com os outros.
        switch (atributoAumentar.toLowerCase()) {
            case "corpo":
                valorAlvo = p.getCorpo() + 1;
                outrosValores.add(p.getDestreza());
                outrosValores.add(p.getMente());
                outrosValores.add(p.getVontade());
                break;
            case "destreza":
                valorAlvo = p.getDestreza() + 1;
                outrosValores.add(p.getCorpo());
                outrosValores.add(p.getMente());
                outrosValores.add(p.getVontade());
                break;
            case "mente":
                valorAlvo = p.getMente() + 1;
                outrosValores.add(p.getCorpo());
                outrosValores.add(p.getDestreza());
                outrosValores.add(p.getVontade());
                break;
            case "vontade":
                valorAlvo = p.getVontade() + 1;
                outrosValores.add(p.getCorpo());
                outrosValores.add(p.getDestreza());
                outrosValores.add(p.getMente());
                break;
            default:
                return false; // Retorna falso se o nome do atributo for inválido.
        }

        // Verifica a regra contra cada um dos outros atributos.
        for (int outroValor : outrosValores) {
            if (valorAlvo - outroValor > 3) {
                return false; // A regra foi violada.
            }
        }

        return true; // A regra foi respeitada para todos os atributos.
    }
}