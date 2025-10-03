package com.bot.service;

import com.bot.model.Personagem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Camada de serviço responsável por toda a lógica de negócio dos personagens.
 * ESTA VERSÃO UTILIZA JDBC PURO para comunicação com o banco de dados.
 */
public class PersonagemService {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    /**
     * Constrói uma nova instância de PersonagemService com as credenciais do banco.
     */
    public PersonagemService(String dbUrl, String dbUser, String dbPass) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    /**
     * Busca um personagem no banco de dados pelo ID do usuário do Discord.
     */
    public Optional<Personagem> buscarPorUsuario(String userId) {
        final String sql = "SELECT * FROM personagens WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPersonagem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar personagem por usuário: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Salva (insere ou atualiza) uma entidade Personagem no banco de dados.
     */
    public void salvar(Personagem personagem) {
        final String sql = """
            INSERT INTO personagens (user_id, nome, nivel, foto_url, corpo, destreza, mente, vontade, pontos_disponiveis)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (user_id) DO UPDATE SET
                nome = EXCLUDED.nome,
                nivel = EXCLUDED.nivel,
                foto_url = EXCLUDED.foto_url,
                corpo = EXCLUDED.corpo,
                destreza = EXCLUDED.destreza,
                mente = EXCLUDED.mente,
                vontade = EXCLUDED.vontade,
                pontos_disponiveis = EXCLUDED.pontos_disponiveis
            """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, personagem.getUserId());
            pstmt.setString(2, personagem.getNome());
            pstmt.setInt(3, personagem.getNivel());
            pstmt.setString(4, personagem.getFotoUrl());
            pstmt.setInt(5, personagem.getCorpo());
            pstmt.setInt(6, personagem.getDestreza());
            pstmt.setInt(7, personagem.getMente());
            pstmt.setInt(8, personagem.getVontade());
            pstmt.setInt(9, personagem.getPontosDisponiveis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar personagem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cria um novo personagem com valores padrão e o persiste no banco de dados.
     */
    public Personagem criarPersonagem(String userId, String nome, int nivel) {
        Personagem novoPersonagem = new Personagem(userId, nome, nivel);
        salvar(novoPersonagem);
        return novoPersonagem;
    }

    /**
     * Deleta um personagem do banco de dados com base no ID do usuário do Discord.
     */
    public void deletar(String userId) {
        final String sql = "DELETE FROM personagens WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao deletar personagem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Aumenta o nível de um personagem e adiciona os pontos de atributo correspondentes.
     */
    public void uparNivel(Personagem personagem, int niveisParaAdicionar) {
        if (niveisParaAdicionar <= 0) return;

        personagem.setNivel(personagem.getNivel() + niveisParaAdicionar);
        personagem.setPontosDisponiveis(personagem.getPontosDisponiveis() + (niveisParaAdicionar * 3));
        salvar(personagem);
    }

    /**
     * Valida a regra de negócio que impede um atributo de ser 3 ou mais pontos
     * maior que qualquer outro atributo.
     *
     * @param p                O personagem cujos atributos serão verificados.
     * @param atributoAumentar O nome do atributo que se deseja aumentar (ex: "corpo").
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
            // Se a diferença for 3 ou mais, o aumento é negado.
            if (valorAlvo - outroValor >= 3) {
                return false; // A regra foi violada.
            }
        }

        return true; // A regra foi respeitada para todos os atributos.
    }

    private Personagem mapRowToPersonagem(ResultSet rs) throws SQLException {
        Personagem p = new Personagem();
        p.setUserId(rs.getString("user_id"));
        p.setNome(rs.getString("nome"));
        p.setNivel(rs.getInt("nivel"));
        p.setFotoUrl(rs.getString("foto_url"));
        p.setCorpo(rs.getInt("corpo"));
        p.setDestreza(rs.getInt("destreza"));
        p.setMente(rs.getInt("mente"));
        p.setVontade(rs.getInt("vontade"));
        p.setPontosDisponiveis(rs.getInt("pontos_disponiveis"));
        return p;
    }
}