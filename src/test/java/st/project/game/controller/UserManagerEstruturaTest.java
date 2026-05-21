package st.project.game.controller;

import org.junit.jupiter.api.*;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import st.project.game.model.User;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES ESTRUTURAIS: UserManager ───────────────────────────────────────
 *
 * Foco: cobertura MC/DC das decisões internas.
 *
 * Decisões cobertas:
 *
 * (A) users.dat inexistente → cria admin + saveUsers
 * (B) users.dat existente e válido → carrega lista
 * (C) exceção no loadUsers → recria admin
 * (D) registerUser login já existe → false
 * (E) registerUser login novo → true
 * (F) authenticate válido → usuário
 * (G) authenticate inválido → null
 * (H) deleteUser inexistente → false
 * (I) deleteUser admin → false
 * (J) deleteUser válido → true
 * (K) getUser encontrado → user
 * (L) getUser ausente → null
 * (M) isSuperUser admin → true
 * (N) isSuperUser comum → false
 * (O) updateUserScoreAndSession encontrado → atualiza
 * (P) updateUserScoreAndSession ausente → não atualiza
 *
 * ────────────────────────────────────────────────────────────────────────────
 */
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("UserManager – Testes Estruturais (MC/DC)")
class UserManagerEstruturaTest {

    private static final String USERS_FILE = "users.dat";

    private UserManager manager;

    @BeforeEach
    void setUp() {
        apagarArquivoUsuarios();
        manager = new UserManager();
    }

    @AfterEach
    void tearDown() {
        apagarArquivoUsuarios();
    }

    private void apagarArquivoUsuarios() {
        File f = new File(USERS_FILE);

        if (f.exists()) {

            if (f.isDirectory()) {
                File[] children = f.listFiles();

                if (children != null) {
                    for (File c : children) {
                        c.delete();
                    }
                }
            }

            f.delete();
        }
    }

    // ── (A) arquivo inexistente ───────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (A): sem users.dat cria admin automaticamente")
    void testeEstruturaASemArquivoCriaAdmin() {

        apagarArquivoUsuarios();

        UserManager manager = new UserManager();

        User admin = manager.getUser("admin");

        assertThat(admin).isNotNull();
        assertThat(admin.getLogin()).isEqualTo("admin");
    }

    // ── (B) arquivo válido ────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (B): users.dat existente carrega usuários")
    void testeEstruturaBCarregaArquivoExistente() {
        manager = new UserManager();
        manager.registerUser("joao", "123", "j.png");

        UserManager novo = new UserManager();

        assertThat(novo.getUser("joao")).isNotNull();
    }

    // ── (C) exceção loadUsers ─────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (C): arquivo inválido cai no catch e recria admin")
    void testeEstruturaCLoadUsersException() throws Exception {
        File file = new File(USERS_FILE);
        java.nio.file.Files.writeString(file.toPath(), "invalido");

        manager = new UserManager();

        assertThat(manager.getUser("admin")).isNotNull();
    }

    // ── (D) register duplicado ────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (D): registerUser duplicado retorna false")
    void testeEstruturaDRegisterDuplicado() {
        manager = new UserManager();

        manager.registerUser("ana", "1", "a.png");

        assertThat(manager.registerUser("ana", "2", "b.png")).isFalse();
    }

    // ── (E) register válido ───────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (E): registerUser válido adiciona usuário")
    void testeEstruturaERegisterValido() {
        manager = new UserManager();

        assertThat(manager.registerUser("novo", "123", "n.png")).isTrue();
    }

    // ── (F) authenticate válido ───────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (F): authenticate correto retorna usuário")
    void testeEstruturaFAuthenticateCorreto() {
        manager = new UserManager();

        manager.registerUser("user", "senha", "u.png");

        assertThat(manager.authenticate("user", "senha")).isNotNull();
    }

    // ── (G) authenticate inválido ────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (G): authenticate inválido retorna null")
    void testeEstruturaGAuthenticateErrado() {
        manager = new UserManager();

        manager.registerUser("user", "senha", "u.png");

        assertThat(manager.authenticate("user", "x")).isNull();
    }

    // ── (H) delete inexistente ────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (H): deleteUser inexistente retorna false")
    void testeEstruturaHDeleteInexistente() {
        manager = new UserManager();

        assertThat(manager.deleteUser("ghost")).isFalse();
    }

    // ── (I) delete admin ──────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (I): deleteUser admin retorna false")
    void testeEstruturaIDeleteAdmin() {
        manager = new UserManager();

        assertThat(manager.deleteUser("admin")).isFalse();
    }

    // ── (J) delete válido ─────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (J): deleteUser válido remove usuário")
    void testeEstruturaJDeleteValido() {
        manager = new UserManager();

        manager.registerUser("mike", "123", "m.png");

        assertThat(manager.deleteUser("mike")).isTrue();
        assertThat(manager.getUser("mike")).isNull();
    }

    // ── (K) getUser encontrado ────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (K): getUser existente retorna usuário")
    void testeEstruturaKGetUserEncontrado() {
        manager = new UserManager();

        manager.registerUser("neo", "123", "n.png");

        assertThat(manager.getUser("neo")).isNotNull();
    }

    // ── (L) getUser ausente ───────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (L): getUser inexistente retorna null")
    void testeEstruturaLGetUserAusente() {
        manager = new UserManager();

        assertThat(manager.getUser("abc")).isNull();
    }

    // ── (M) superuser true ────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (M): isSuperUser(admin) → true")
    void testeEstruturaMSuperUserTrue() {
        manager = new UserManager();

        assertThat(manager.isSuperUser("admin")).isTrue();
    }

    // ── (N) superuser false ───────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (N): isSuperUser(player) → false")
    void testeEstruturaNSuperUserFalse() {
        manager = new UserManager();

        assertThat(manager.isSuperUser("player")).isFalse();
    }

    // ── (O) update existente ──────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (O): updateUserScoreAndSession atualiza usuário")
    void testeEstruturaOUpdateExistente() {
        manager = new UserManager();

        manager.registerUser("hero", "123", "h.png");

        User user = manager.getUser("hero");

        manager.updateUserScoreAndSession(user, 700);

        User atualizado = manager.getUser("hero");

        assertThat(atualizado.getBestScore()).isEqualTo(700);
        assertThat(atualizado.getTotalSessions()).isEqualTo(1);
    }

    // ── (P) update inexistente ────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (P): updateUserScoreAndSession usuário ausente não altera nada")
    void testeEstruturaPUpdateAusente() {
        manager = new UserManager();

        User fake = new User("ghost", "123", "g.png");

        manager.updateUserScoreAndSession(fake, 500);

        assertThat(manager.getUser("ghost")).isNull();
    }

    // ── saveUsers catch IOException ───────────────────────────────────────

    @Test
    @DisplayName("Estrutura: saveUsers captura IOException")
    void testeEstruturaSaveUsersIOException() throws Exception {

        apagarArquivoUsuarios();

        UserManager manager = new UserManager();

        File file = new File(USERS_FILE);

        // força falha de escrita
        assertThat(file.setReadOnly()).isTrue();

        Method metodo = UserManager.class.getDeclaredMethod("saveUsers");
        metodo.setAccessible(true);

        // não deve lançar exceção
        metodo.invoke(manager);

        assertThat(file.exists()).isTrue();

        // restaura permissão para cleanup
        file.setWritable(true);

        apagarArquivoUsuarios();
    }
}