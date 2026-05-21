package st.project.game.controller;

import org.junit.jupiter.api.*;

import st.project.game.model.User;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE FRONTEIRA: UserManager ──────────────────────────────────────
 *
 * Cobre: usuários inexistentes, duplicados, admin protegido e arquivos inválidos.
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("UserManager – Testes de Fronteira")
class UserManagerFronteiraTest {

    private static final String USERS_FILE = "users.dat";

    private UserManager manager;

    @BeforeEach
    void setUp() {
        apagarArquivoUsuarios();
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
    @DisplayName("Fronteira: registerUser com login duplicado retorna false")
    void testeFronteiraRegisterDuplicado() {
        manager = new UserManager();

        manager.registerUser("ana", "123", "a.png");

        boolean resultado = manager.registerUser("ana", "456", "b.png");

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Fronteira: authenticate com senha errada retorna null")
    void testeFronteiraAuthenticateSenhaErrada() {
        manager = new UserManager();

        manager.registerUser("leo", "123", "l.png");

        assertThat(manager.authenticate("leo", "errada")).isNull();
    }

    @Test
    @DisplayName("Fronteira: authenticate usuário inexistente retorna null")
    void testeFronteiraAuthenticateInexistente() {
        manager = new UserManager();

        assertThat(manager.authenticate("fantasma", "123")).isNull();
    }

    @Test
    @DisplayName("Fronteira: deleteUser usuário inexistente retorna false")
    void testeFronteiraDeleteUserInexistente() {
        manager = new UserManager();

        assertThat(manager.deleteUser("naoExiste")).isFalse();
    }

    @Test
    @DisplayName("Fronteira: deleteUser admin retorna false")
    void testeFronteiraDeleteAdmin() {
        manager = new UserManager();

        assertThat(manager.deleteUser("admin")).isFalse();
    }

    @Test
    @DisplayName("Fronteira: getUser inexistente retorna null")
    void testeFronteiraGetUserInexistente() {
        manager = new UserManager();

        assertThat(manager.getUser("xyz")).isNull();
    }

    @Test
    @DisplayName("Fronteira: isSuperUser com login comum retorna false")
    void testeFronteiraIsSuperUserFalse() {
        manager = new UserManager();

        assertThat(manager.isSuperUser("player")).isFalse();
    }

    @Test
    @DisplayName("Fronteira: updateUserScoreAndSession com usuário inexistente não falha")
    void testeFronteiraUpdateUserInexistente() {
        manager = new UserManager();

        User fake = new User("ghost", "123", "g.png");

        manager.updateUserScoreAndSession(fake, 1000);

        assertThat(manager.getUser("ghost")).isNull();
    }

    @Test
    @DisplayName("Fronteira: arquivo users.dat corrompido recria admin")
    void testeFronteiraArquivoCorrompido() throws Exception {
        try (FileWriter fw = new FileWriter(USERS_FILE)) {
            fw.write("arquivo invalido");
        }

        manager = new UserManager();

        assertThat(manager.getUser("admin")).isNotNull();
    }
}