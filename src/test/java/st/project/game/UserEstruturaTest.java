package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ─── TESTES ESTRUTURAIS: User ───────────────────────────────────────────────
 *
 * Foco: cobertura MC/DC das decisões internas de User.
 *
 * Decisões cobertas:
 *   (A) updateScore: newScore > bestScore         → atualiza   (true)
 *   (A) updateScore: newScore > bestScore         → não atualiza (false)
 *       Subcasos de fronteira (menor, igual, maior+1) já estão em
 *       UserFronteiraTest; aqui cobrimos o caminho negativo com bestScore > 0.
 *   (B) checkPassword: hash(input).equals(hash)  → true / false
 *       (requer que hashPassword seja chamado duas vezes — cobre o try do catch)
 *   (C) hashWithAlgorithm: algoritmo inválido     → RuntimeException (catch)
 *   (D) equals: this == o                         → true  (identidade)
 *       equals: !(o instanceof User)              → false (null e tipo errado)
 *       equals: login.equals                      → true / false
 *   (E) incrementSessions: acumulação sequencial  → +1 por chamada
 *   (F) setAvatar: substituição de valor não-nulo → reflete imediatamente
 *
 * Lacunas cobertas em relação a Domínio/Fronteira:
 *   - updateScore com bestScore já positivo e novo valor menor (branch false
 *     com bestScore != 0, diferente do SC1 que parte do zero)
 *   - checkPassword com senha que difere apenas em um caractere (hash distinto)
 *   - catch de NoSuchAlgorithmException via hashWithAlgorithm
 *   - equals entre duas instâncias distintas com login idêntico
 *   - hashCode igual para logins iguais, diferente para logins diferentes
 *
 * Dublê de teste: nenhum — User é classe de estado pura.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("User – Testes Estruturais (MC/DC)")
class UserEstruturaTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("heroi", "senha123", "avatar.png");
    }

    // ── (A) updateScore ────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (A-true): updateScore com bestScore positivo e novo maior → atualiza")
    void testeEstruturaUpdateScoreMaiorQueBestScorePositivo() {
        // Arrange
        user.updateScore(300);
        // Act
        user.updateScore(301);
        // Assert — branch newScore > bestScore = true
        assertThat(user.getBestScore()).isEqualTo(301);
    }

    @Test
    @DisplayName("Estrutura (A-false): updateScore com novo menor que bestScore positivo → não atualiza")
    void testeEstruturaUpdateScoreMenorQueBestScorePositivo() {
        // Arrange
        user.updateScore(300);
        // Act
        user.updateScore(299);
        // Assert — branch newScore > bestScore = false, bestScore != 0
        assertThat(user.getBestScore()).isEqualTo(300);
    }

    @Test
    @DisplayName("Estrutura (A): updateScore negativo nunca supera bestScore=0")
    void testeEstruturaUpdateScoreNegativoNaoAltera() {
        // Arrange — bestScore = 0 (estado inicial)
        // Act
        user.updateScore(-1);
        // Assert — branch false com valor abaixo do limite inferior
        assertThat(user.getBestScore()).isZero();
    }

    // ── (B) checkPassword ──────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (B-true): checkPassword com senha correta → hash igual → true")
    void testeEstruturaCheckPasswordHashIgual() {
        // Arrange / Act / Assert — cobre o caminho try completo do hashWithAlgorithm
        assertThat(user.checkPassword("senha123")).isTrue();
    }

    @Test
    @DisplayName("Estrutura (B-false): checkPassword com senha diferindo em 1 char → hash diferente → false")
    void testeEstruturaCheckPasswordHashDiferente() {
        // Arrange — "senha124" difere de "senha123" em apenas um caractere
        // Act / Assert — garante que a comparação equals retorna false
        assertThat(user.checkPassword("senha124")).isFalse();
    }

    // ── (C) hashWithAlgorithm – catch de NoSuchAlgorithmException ──────────

    @Test
    @DisplayName("Estrutura (C): algoritmo inexistente lança RuntimeException com mensagem")
    void testeEstruturaHashAlgoritmoInvalido() {
        // Arrange — algoritmo inválido força o catch que normalmente é inatingível
        // Act / Assert
        assertThatThrownBy(() -> User.hashWithAlgorithm("qualquer", "ALGORITMO-INEXISTENTE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não disponível");
    }

    // ── (D) equals / hashCode ──────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (D-identidade): equals com a própria referência → true sem comparar login")
    void testeEstruturaEqualsIdentidade() {
        // Cobre o branch `if (this == o) return true`
        User mesmo = user;
        assertThat(user.equals(mesmo)).isTrue();
    }

    @Test
    @DisplayName("Estrutura (D-instanceof false): equals com null → false")
    void testeEstruturaEqualsNull() {
        // Cobre `if (!(o instanceof User)) return false` com null
        assertThat(user.equals(null)).isFalse();
    }

    @Test
    @DisplayName("Estrutura (D-instanceof false): equals com tipo diferente → false")
    void testeEstruturaEqualsOutroTipo() {
        // Cobre `if (!(o instanceof User)) return false` com tipo incompatível
        assertThat(user.equals(42)).isFalse();
    }

    @Test
    @DisplayName("Estrutura (D-login igual): dois objetos distintos com mesmo login → equals true")
    void testeEstruturaEqualsLoginIgualObjetoDistinto() {
        // Cobre o caminho final `login.equals(user.login)` retornando true
        User outro = new User("heroi", "senhaCompletamenteDiferente", "outro.png");
        assertThat(user.equals(outro)).isTrue();
    }

    @Test
    @DisplayName("Estrutura (D-login diferente): objetos com login diferente → equals false")
    void testeEstruturaEqualsLoginDiferente() {
        // Cobre o caminho final `login.equals(user.login)` retornando false
        User outro = new User("vilao", "senha123", "avatar.png");
        assertThat(user.equals(outro)).isFalse();
    }

    @Test
    @DisplayName("Estrutura (D-hashCode): logins iguais → hashCode igual; logins diferentes → hashCode diferente")
    void testeEstruturaHashCodeConsistencia() {
        User mesmoLogin  = new User("heroi", "outraSenha", "x.png");
        User loginDiferente = new User("npc", "senha123", "avatar.png");

        assertThat(user.hashCode()).isEqualTo(mesmoLogin.hashCode());
        assertThat(user.hashCode()).isNotEqualTo(loginDiferente.hashCode());
    }

    // ── (E) incrementSessions ─────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (E): cada incrementSessions adiciona exatamente 1")
    void testeEstruturaIncrementSessionsAcumulacaoExata() {
        // Cobre o corpo do método para N chamadas sequenciais
        for (int i = 1; i <= 5; i++) {
            user.incrementSessions();
            assertThat(user.getTotalSessions()).isEqualTo(i);
        }
    }

    // ── (F) setAvatar ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (F): setAvatar substitui valor não-nulo por outro não-nulo")
    void testeEstruturaSetAvatarSubstituicao() {
        // Arrange
        assertThat(user.getAvatar()).isEqualTo("avatar.png");
        // Act
        user.setAvatar("novo.png");
        // Assert
        assertThat(user.getAvatar()).isEqualTo("novo.png");
    }

    @Test
    @DisplayName("Estrutura (F): setAvatar aceita string vazia")
    void testeEstruturaSetAvatarStringVazia() {
        user.setAvatar("");
        assertThat(user.getAvatar()).isEmpty();
    }
}