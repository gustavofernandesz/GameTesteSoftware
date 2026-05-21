package st.project.game.controller;

import org.junit.jupiter.api.*;

import st.project.game.model.User;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE DOMÍNIO: UserManager ─────────────────────────────────────────
 *
 * Escopo: regras de negócio — autenticação, cadastro, exclusão e atualização.
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("UserManager – Testes de Domínio")
class UserManagerDominioTest {

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

    @Test
    @DisplayName("Domínio: construtor cria usuário admin padrão")
    void testeDominioConstrutorCriaAdmin() {
        List<User> users = manager.getAllUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getLogin()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Domínio: registerUser adiciona novo usuário")
    void testeDominioRegisterUser() {
        boolean resultado = manager.registerUser("alice", "123", "a.png");

        assertThat(resultado).isTrue();
        assertThat(manager.getUser("alice")).isNotNull();
    }

    @Test
    @DisplayName("Domínio: authenticate retorna usuário válido")
    void testeDominioAuthenticateValido() {
        manager.registerUser("bob", "senha", "b.png");

        User user = manager.authenticate("bob", "senha");

        assertThat(user).isNotNull();
        assertThat(user.getLogin()).isEqualTo("bob");
    }

    @Test
    @DisplayName("Domínio: deleteUser remove usuário existente")
    void testeDominioDeleteUser() {
        manager.registerUser("maria", "123", "m.png");

        boolean removido = manager.deleteUser("maria");

        assertThat(removido).isTrue();
        assertThat(manager.getUser("maria")).isNull();
    }

    @Test
    @DisplayName("Domínio: updateUserScoreAndSession atualiza score e sessões")
    void testeDominioUpdateUserScoreAndSession() {
        manager.registerUser("hero", "123", "h.png");

        User user = manager.getUser("hero");

        manager.updateUserScoreAndSession(user, 900);

        User atualizado = manager.getUser("hero");

        assertThat(atualizado.getBestScore()).isEqualTo(900);
        assertThat(atualizado.getTotalSessions()).isEqualTo(1);
    }

    @Test
    @DisplayName("Domínio: isSuperUser retorna true para admin")
    void testeDominioIsSuperUser() {
        assertThat(manager.isSuperUser("admin")).isTrue();
    }

    @Test
    @DisplayName("Domínio: getAllUsers retorna lista imutável")
    void testeDominioGetAllUsersImutavel() {
        List<User> users = manager.getAllUsers();

        Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> users.add(new User("x", "y", "z.png"))
        );
    }
}