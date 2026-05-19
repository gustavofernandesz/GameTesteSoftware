package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import st.project.game.model.User;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE FRONTEIRA: User ──────────────────────────────────────────────
 *
 * Fronteiras testadas:
 *   SC1. Score = 0 (limite inferior): updateScore(0) do zero
 *   SC2. Score = 1 (fronteira inferior+1)
 *   SC3. Score atual = 0, novo = 0 → não altera
 *   SE1. totalSessions = 0 (antes do primeiro incremento)
 *   SE2. totalSessions = 1 (exatamente após primeiro incremento)
 *   P1.  Senha vazia ("") — autenticação deve funcionar
 *   P2.  Senha com espaços
 *   E1.  equals com a própria instância
 *   E2.  equals com null
 *   E3.  equals com objeto de tipo diferente
 *
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("User – Testes de Fronteira")
class UserFronteiraTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("jogador", "pw", "av.png");
    }

    // ── SC1-3. Score nas fronteiras ───────────────────────────────────────

    @Test
    @DisplayName("Fronteira (SC1): updateScore(0) não altera bestScore quando já é 0")
    void testeFronteiraScoreZeroNaoAltera() {
        user.updateScore(0);
        assertThat(user.getBestScore()).isZero();
    }

    @Test
    @DisplayName("Fronteira (SC2): updateScore(1) no zero → bestScore = 1")
    void testeFronteiraScoreUmAtualizaDeZero() {
        user.updateScore(1);
        assertThat(user.getBestScore()).isEqualTo(1);
    }

    @ParameterizedTest(name = "updateScore({0}) não supera bestScore=500")
    @ValueSource(ints = {0, 100, 499, 500})
    @DisplayName("Fronteira: scores <= bestScore não atualizam")
    void testeFronteiraScoresMenoresOuIguaisNaoAtualizam(int score) {
        user.updateScore(500); // baseline
        user.updateScore(score);
        assertThat(user.getBestScore()).isEqualTo(500);
    }

    @ParameterizedTest(name = "updateScore({0}) supera bestScore=500")
    @ValueSource(ints = {501, 1000, Integer.MAX_VALUE})
    @DisplayName("Fronteira: scores > bestScore atualizam")
    void testeFronteiraMaioresAtualizam(int score) {
        user.updateScore(500);
        user.updateScore(score);
        assertThat(user.getBestScore()).isEqualTo(score);
    }

    // ── SE1-2. Sessions ───────────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (SE1): totalSessions = 0 antes do primeiro incremento")
    void testeFronteiraSessionsInicial() {
        assertThat(user.getTotalSessions()).isZero();
    }

    @Test
    @DisplayName("Fronteira (SE2): totalSessions = 1 exatamente após primeiro incremento")
    void testeFronteiraSessionsUmAposIncremento() {
        user.incrementSessions();
        assertThat(user.getTotalSessions()).isEqualTo(1);
    }

    // ── P1-2. Senhas especiais ─────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (P1): senha vazia é autenticada corretamente")
    void testeFronteiraSenhaVazia() {
        User u = new User("x", "", "av.png");
        assertThat(u.checkPassword("")).isTrue();
        assertThat(u.checkPassword("a")).isFalse();
    }

    @Test
    @DisplayName("Fronteira (P2): senha com espaços é autenticada corretamente")
    void testeFronteiraSenhaComEspacos() {
        User u = new User("x", "a b c", "av.png");
        assertThat(u.checkPassword("a b c")).isTrue();
        assertThat(u.checkPassword("abc")).isFalse();
    }

    // ── E1-3. equals nos extremos ─────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (E1): equals com a própria instância retorna true")
    void testeFronteiraEqualsComSiMesmo() {
        assertThat(user.equals(user)).isTrue();
    }

    @Test
    @DisplayName("Fronteira (E2): equals com null retorna false")
    void testeFronteiraEqualsComNull() {
        assertThat(user.equals(null)).isFalse();
    }

    @Test
    @DisplayName("Fronteira (E3): equals com objeto de tipo diferente retorna false")
    void testeFronteiraEqualsComTipoDiferente() {
        assertThat(user.equals("heroi")).isFalse();
    }
}