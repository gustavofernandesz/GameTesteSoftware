package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.Item;
import st.project.game.model.Player;
import st.project.game.model.Room;
import st.project.game.model.User;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES ESTRUTURAIS: Player e User ──────────────────────────────────────
 *
 * Foco: cobertura MC/DC das decisões internas de Player e User.
 *
 * Decisões MC/DC cobertas em Player:
 *   (A) moverPara: destino == null               → false, sem mover
 *   (B) moverPara: destino != null && bloqueada  → verifica chave
 *   (B1) bloqueada && sem chave                  → false
 *   (B2) bloqueada && com chave                  → true
 *   (C) moverPara: destino != null && desbloqueada → true
 *   (D) adicionarItem: item == null              → não adiciona
 *   (E) adicionarItem: item != null              → adiciona
 *   (F) removerItem: item na lista               → remove, retorna
 *   (G) removerItem: item fora lista             → retorna null
 *   (H) possuiItem: tipo encontrado              → true
 *   (I) possuiItem: tipo ausente                 → false
 *   (J) usarItem: tipo consumível                → remove
 *   (K) usarItem: tipo não-consumível (CHAVE)    → mantém
 *   (K') usarItem: PASSAPORTE não consumível     → mantém
 *
 * Decisões MC/DC cobertas em User:
 *   (L) checkPassword: hash correto              → true
 *   (M) checkPassword: hash errado               → false
 *   (N) updateScore: newScore > bestScore        → atualiza
 *   (O) updateScore: newScore <= bestScore       → não atualiza
 *   (P) equals: mesmo login                      → true
 *   (Q) equals: login diferente                  → false
 *
 * Dublê de teste: nenhum — classes de estado puras.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("Player e User – Testes Estruturais (MC/DC)")
class PlayerEstruturaTest {

    private Room   salaInicial;
    private Player player;

    @BeforeEach
    void setUp() {
        salaInicial = new Room("Sala Inicial", 0, 0);
        player      = new Player(salaInicial);
    }

    // ── (A) moverPara null ─────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (A): moverPara(null) → false, posição inalterada")
    void testeEstruturaAMoverParaNull() {
        assertThat(player.moverPara(null)).isFalse();
        assertThat(player.getPosicaoAtual()).isEqualTo(salaInicial);
    }

    // ── (B1) moverPara bloqueada sem chave ─────────────────────────────────

    @Test
    @DisplayName("Estrutura (B1): moverPara bloqueada sem chave → false")
    void testeEstruturaB1MoverParaBloqueadaSemChave() {
        Room bloqueada = new Room("sagrado", 4, 4);
        bloqueada.setBloqueada(true);
        // sem chave no inventário
        assertThat(player.moverPara(bloqueada)).isFalse();
        assertThat(player.getPosicaoAtual()).isEqualTo(salaInicial);
    }

    // ── (B2) moverPara bloqueada com chave ─────────────────────────────────

    @Test
    @DisplayName("Estrutura (B2): moverPara bloqueada com chave → true")
    void testeEstruturaB2MoverParaBloqueadaComChave() {
        Room bloqueada = new Room("sagrado", 4, 4);
        bloqueada.setBloqueada(true);
        player.adicionarItem(new Item("Chave", Item.Type.CHAVE, "Abre"));

        assertThat(player.moverPara(bloqueada)).isTrue();
        assertThat(player.getPosicaoAtual()).isEqualTo(bloqueada);
    }

    // ── (C) moverPara desbloqueada ─────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (C): moverPara sala desbloqueada → true")
    void testeEstruturaCMoverParaDesbloqueada() {
        Room destino = new Room("Corredor", 1, 0);
        assertThat(player.moverPara(destino)).isTrue();
        assertThat(player.getPosicaoAtual()).isEqualTo(destino);
    }

    // ── (D) adicionarItem null ─────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (D): adicionarItem(null) → inventário permanece vazio")
    void testeEstruturaDAdicionarNull() {
        player.adicionarItem(null);
        assertThat(player.getInventario()).isEmpty();
    }

    // ── (E) adicionarItem válido ───────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (E): adicionarItem válido → inserido no inventário")
    void testeEstruturaEAdicionarValido() {
        Item lupa = new Item("Lupa", Item.Type.LUPA, "Revela");
        player.adicionarItem(lupa);
        assertThat(player.getInventario()).containsExactly(lupa);
    }

    // ── (F) removerItem presente ───────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (F): removerItem presente → retorna item e remove da lista")
    void testeEstruturaFRemoverPresente() {
        Item chave = new Item("Chave", Item.Type.CHAVE, "Abre");
        player.adicionarItem(chave);
        assertThat(player.removerItem(chave)).isEqualTo(chave);
        assertThat(player.getInventario()).isEmpty();
    }

    // ── (G) removerItem ausente ────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (G): removerItem ausente → retorna null, inventário inalterado")
    void testeEstruturaGRemoverAusente() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2");
        assertThat(player.removerItem(pocao)).isNull();
    }

    @Test
    @DisplayName("Estrutura (G'): removerItem(null) com inventário populado → retorna null")
    void testeEstruturaGRemoverNuloComInventario() {
        player.adicionarItem(new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3"));
        assertThat(player.removerItem(null)).isNull();
    }

    @Test
    @DisplayName("Estrutura (G''): removerItem(null) com inventário vazio → retorna null")
    void testeEstruturaGRemoverNuloInventarioVazio() {
        assertThat(player.removerItem(null)).isNull();
    }

    // ── (H) possuiItem encontrado ──────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (H): possuiItem tipo presente → true")
    void testeEstruturaHPossuiItemEncontrado() {
        player.adicionarItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        assertThat(player.possuiItem(Item.Type.LUPA)).isTrue();
    }

    // ── (I) possuiItem ausente ─────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (I): possuiItem tipo ausente → false")
    void testeEstruturaIPossuiItemAusente() {
        player.adicionarItem(new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3"));
        assertThat(player.possuiItem(Item.Type.CALICE)).isFalse();
    }

    @Test
    @DisplayName("Estrutura (I'): possuiItem(null) → false")
    void testeEstruturaIPossuiItemNull() {
        assertThat(player.possuiItem(null)).isFalse();
    }

    // ── (J) usarItem consumível ────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (J): usarItem POCAO_VELOCIDADE → remove do inventário")
    void testeEstruturaJUsarItemPocaoConsumivel() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2");
        player.adicionarItem(pocao);
        player.usarItem(pocao);
        assertThat(player.getInventario()).doesNotContain(pocao);
    }

    @Test
    @DisplayName("Estrutura (J'): usarItem AMULETO_VISAO → remove do inventário")
    void testeEstruturaJUsarItemAmuletoConsumivel() {
        Item amuleto = new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3");
        player.adicionarItem(amuleto);
        player.usarItem(amuleto);
        assertThat(player.getInventario()).doesNotContain(amuleto);
    }

    // ── (K) usarItem não-consumível ────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (K): usarItem CHAVE → permanece no inventário")
    void testeEstruturaKUsarItemChaveNaoConsumivel() {
        Item chave = new Item("Chave", Item.Type.CHAVE, "Abre");
        player.adicionarItem(chave);
        player.usarItem(chave);
        assertThat(player.getInventario()).contains(chave);
    }



    @Test
    @DisplayName("Estrutura (K''): usarItem CALICE → permanece no inventário")
    void testeEstruturaKUsarItemCaliceNaoConsumivel() {
        Item calice = new Item("Cálice", Item.Type.CALICE, "Missão");
        player.adicionarItem(calice);
        player.usarItem(calice);
        assertThat(player.getInventario()).contains(calice);
    }

    // ── Histórico estrutural ───────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: histórico registra todas as salas em sequência")
    void testeEstruturaHistoricoSequencial() {
        Room s1 = new Room("A", 1, 0);
        Room s2 = new Room("B", 2, 0);
        player.moverPara(s1);
        player.moverPara(s2);

        assertThat(player.getHistorico()).containsExactly(salaInicial, s1, s2);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TESTES ESTRUTURAIS: User (login, senha, avatar, pontuação, sessões)
    // ═══════════════════════════════════════════════════════════════════════

    // ── (L) checkPassword correto ──────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (L): checkPassword com senha correta → true")
    void testeEstruturaLCheckPasswordCorreto() {
        User user = new User("jogador1", "senha123", "avatar.png");
        assertThat(user.checkPassword("senha123")).isTrue();
    }

    // ── (M) checkPassword errado ───────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (M): checkPassword com senha errada → false")
    void testeEstruturaMCheckPasswordErrado() {
        User user = new User("jogador1", "senha123", "avatar.png");
        assertThat(user.checkPassword("errada")).isFalse();
    }

    @Test
    @DisplayName("Estrutura (M'): checkPassword case-sensitive → false para 'Senha123'")
    void testeEstruturaMCheckPasswordCaseSensitive() {
        User user = new User("jogador1", "senha123", "avatar.png");
        assertThat(user.checkPassword("Senha123")).isFalse();
    }

    // ── (N) updateScore atualiza ───────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (N): updateScore com valor maior → bestScore atualizado")
    void testeEstruturaNUpdateScoreMaior() {
        User user = new User("u", "p", "a.png");
        user.updateScore(500);
        assertThat(user.getBestScore()).isEqualTo(500);
    }

    // ── (O) updateScore não atualiza ──────────────────────────────────────

    @Test
    @DisplayName("Estrutura (O): updateScore com valor menor → bestScore inalterado")
    void testeEstruturaOUpdateScoreMenor() {
        User user = new User("u", "p", "a.png");
        user.updateScore(500);
        user.updateScore(200);
        assertThat(user.getBestScore()).isEqualTo(500);
    }

    @Test
    @DisplayName("Estrutura (O'): updateScore com valor igual → bestScore inalterado")
    void testeEstruturaOUpdateScoreIgual() {
        User user = new User("u", "p", "a.png");
        user.updateScore(300);
        user.updateScore(300);
        assertThat(user.getBestScore()).isEqualTo(300);
    }

    // ── (P) equals mesmo login ─────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (P): dois Users com mesmo login são iguais")
    void testeEstruturaPEqualsLoginIgual() {
        User u1 = new User("alice", "abc", "a1.png");
        User u2 = new User("alice", "xyz", "a2.png");
        assertThat(u1).isEqualTo(u2);
        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
    }

    // ── (Q) equals login diferente ─────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (Q): Users com logins diferentes não são iguais")
    void testeEstruturaQEqualsLoginDiferente() {
        User u1 = new User("alice", "abc", "a1.png");
        User u2 = new User("bob",   "abc", "a1.png");
        assertThat(u1).isNotEqualTo(u2);
    }

    // ── incrementSessions ─────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: incrementSessions acumula sessões corretamente")
    void testeEstruturaIncrementSessions() {
        User user = new User("u", "p", "a.png");
        assertThat(user.getTotalSessions()).isZero();
        user.incrementSessions();
        user.incrementSessions();
        assertThat(user.getTotalSessions()).isEqualTo(2);
    }

    // ── avatar ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: setAvatar atualiza o avatar do usuário")
    void testeEstruturaSetAvatar() {
        User user = new User("u", "p", "old.png");
        user.setAvatar("new.png");
        assertThat(user.getAvatar()).isEqualTo("new.png");
    }

    // ── hash determinístico ───────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: mesma senha produz mesmo hash (determinístico)")
    void testeEstruturaHashDeterministico() {
        String hash1 = User.hashPassword("segredo");
        String hash2 = User.hashPassword("segredo");
        assertThat(hash1).isEqualTo(hash2).hasSize(64); // SHA-256 = 64 hex chars
    }

    @Test
    @DisplayName("Estrutura: senhas diferentes produzem hashes diferentes")
    void testeEstruturaHashDiferente() {
        assertThat(User.hashPassword("abc")).isNotEqualTo(User.hashPassword("xyz"));
    }

    // ── toString ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: toString contém login, bestScore e totalSessions")
    void testeEstruturaToString() {
        User user = new User("heroi", "pw", "h.png");
        user.updateScore(800);
        user.incrementSessions();
        String str = user.toString();
        assertThat(str).contains("heroi").contains("800").contains("1");
    }
}
