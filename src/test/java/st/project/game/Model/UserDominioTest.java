package st.project.game.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.User;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE DOMÍNIO: User ────────────────────────────────────────────────
 *
 * Escopo: regras de negócio de User — autenticação, score, sessões, equals.
 *
 * SUBSTITUIÇÃO de mocks de UserManager nos testes de domínio do GameEngine:
 * Os testes anteriores em GameEngineDominioTest mockavam UserManager para
 * testar comportamentos que pertencem à User diretamente (checkPassword,
 * updateScore). Mocks de UserManager para testar lógica de User são
 * desnecessários e testam implementação em vez de comportamento.
 * Aqui testamos User diretamente — classe de estado pura, sem mock.
 *
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("User – Testes de Domínio")
class UserDominioTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("heroi", "senha123", "avatar.png");
    }

    // ── Construção ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: login é preservado exatamente como fornecido")
    void testeDominioLoginPreservado() {
        assertThat(user.getLogin()).isEqualTo("heroi");
    }

    @Test
    @DisplayName("Domínio: avatar é preservado exatamente como fornecido")
    void testeDominioAvatarPreservado() {
        assertThat(user.getAvatar()).isEqualTo("avatar.png");
    }

    @Test
    @DisplayName("Domínio: bestScore inicial é 0")
    void testeDominioBestScoreInicial() {
        assertThat(user.getBestScore()).isZero();
    }

    @Test
    @DisplayName("Domínio: totalSessions inicial é 0")
    void testeDominioTotalSessionsInicial() {
        assertThat(user.getTotalSessions()).isZero();
    }

    @Test
    @DisplayName("Domínio: senha é armazenada como hash SHA-256, não em claro")
    void testeDominioSenhaArmazenadaComoHash() {
        // O hash não deve ser igual à senha em texto claro
        assertThat(user.getPasswordHash()).isNotEqualTo("senha123");
        // O hash deve ter 64 caracteres (SHA-256 → 256 bits → 32 bytes → 64 hex)
        assertThat(user.getPasswordHash()).hasSize(64);
    }

    // ── checkPassword ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: checkPassword retorna true para senha correta")
    void testeDominioCheckPasswordCorreto() {
        assertThat(user.checkPassword("senha123")).isTrue();
    }

    @Test
    @DisplayName("Domínio: checkPassword retorna false para senha incorreta")
    void testeDominioCheckPasswordIncorreto() {
        assertThat(user.checkPassword("senhaErrada")).isFalse();
    }

    @Test
    @DisplayName("Domínio: checkPassword é case-sensitive")
    void testeDominioCheckPasswordCaseSensitive() {
        assertThat(user.checkPassword("Senha123")).isFalse();
        assertThat(user.checkPassword("SENHA123")).isFalse();
    }

    // ── updateScore ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: updateScore atualiza bestScore quando novo score é maior")
    void testeDominioUpdateScoreMaiorAtualiza() {
        user.updateScore(500);
        assertThat(user.getBestScore()).isEqualTo(500);
    }

    @Test
    @DisplayName("Domínio: updateScore não altera bestScore quando novo score é menor")
    void testeDominioUpdateScoreMenorNaoAltera() {
        user.updateScore(500);
        user.updateScore(300);
        assertThat(user.getBestScore()).isEqualTo(500);
    }

    @Test
    @DisplayName("Domínio: updateScore não altera bestScore quando novo score é igual")
    void testeDominioUpdateScoreIgualNaoAltera() {
        user.updateScore(500);
        user.updateScore(500);
        assertThat(user.getBestScore()).isEqualTo(500);
    }

    @Test
    @DisplayName("Domínio: múltiplos updateScore preservam o máximo absoluto")
    void testeDominioUpdateScoreMultiplasChamadas() {
        user.updateScore(100);
        user.updateScore(800);
        user.updateScore(400);
        assertThat(user.getBestScore()).isEqualTo(800);
    }

    // ── incrementSessions ─────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: incrementSessions aumenta totalSessions em 1")
    void testeDominioIncrementSessions() {
        user.incrementSessions();
        assertThat(user.getTotalSessions()).isEqualTo(1);
    }

    @Test
    @DisplayName("Domínio: múltiplos incrementSessions acumulam corretamente")
    void testeDominioIncrementSessionsMultiplos() {
        user.incrementSessions();
        user.incrementSessions();
        user.incrementSessions();
        assertThat(user.getTotalSessions()).isEqualTo(3);
    }

    // ── setAvatar ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: setAvatar substitui o avatar existente")
    void testeDominioSetAvatar() {
        user.setAvatar("novo_avatar.png");
        assertThat(user.getAvatar()).isEqualTo("novo_avatar.png");
    }

    // ── equals e hashCode ─────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: dois usuários com mesmo login são iguais (equals por login)")
    void testeDominioEqualsLoginIgual() {
        User outro = new User("heroi", "outraSenha", "outro.png");
        assertThat(user).isEqualTo(outro);
    }

    @Test
    @DisplayName("Domínio: dois usuários com logins diferentes não são iguais")
    void testeDominioEqualsLoginDiferente() {
        User outro = new User("mago", "pw", "mago.png");
        assertThat(user).isNotEqualTo(outro);
    }

    @Test
    @DisplayName("Domínio: hashCode é consistente com equals (mesmo login → mesmo hash)")
    void testeDominioHashCodeConsistente() {
        User outro = new User("heroi", "senhaX", "avatar2.png");
        assertThat(user.hashCode()).isEqualTo(outro.hashCode());
    }

    // ── toString ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: toString contém login, bestScore e totalSessions")
    void testeDominioToStringContemInfos() {
        user.updateScore(1500);
        user.incrementSessions();
        String s = user.toString();
        assertThat(s).contains("heroi").contains("1500").contains("1");
    }

    // ── hashPassword (método estático) ────────────────────────────────────

    @Test
    @DisplayName("Domínio: hashPassword é determinístico para mesma entrada")
    void testeDominioHashPasswordDeterministico() {
        String hash1 = User.hashPassword("abc");
        String hash2 = User.hashPassword("abc");
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("Domínio: hashPassword produz hashes diferentes para entradas diferentes")
    void testeDominioHashPasswordSenhasDiferentes() {
        assertThat(User.hashPassword("abc")).isNotEqualTo(User.hashPassword("abd"));
    }
}