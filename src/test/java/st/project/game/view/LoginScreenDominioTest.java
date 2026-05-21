package st.project.game.view;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import st.project.game.controller.UserManager;
import st.project.game.model.User;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**

 ─────────────────────────────────────────────────────────────
 TESTES DE DOMÍNIO — LoginScreen

 ─────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginScreen — Testes de Domínio")
class LoginScreenDominioTest {

    private LoginScreen screen;
    private UserManager userMock;

    @BeforeEach
    void setUp() throws Exception {

        userMock = mock(UserManager.class);

        SwingUtilities.invokeAndWait(() -> {
            screen = spy(new LoginScreen(userMock));
        });

    }

    @AfterEach
    void tearDown() throws Exception {

        if (screen != null) {
            SwingUtilities.invokeAndWait(() -> screen.dispose());
        }

    }

// ── Helpers ─────────────────────────────────────────────

    private JTextField loginField() throws Exception {
        Field f = LoginScreen.class.getDeclaredField("loginField");
        f.setAccessible(true);
        return (JTextField) f.get(screen);
    }

    private JPasswordField passwordField() throws Exception {
        Field f = LoginScreen.class.getDeclaredField("passwordField");
        f.setAccessible(true);
        return (JPasswordField) f.get(screen);
    }

// ── Login ───────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: login válido → abre MainMenu")
    void testeDominioLoginValido() throws Exception {

        User user = new User("hero", "123", "a.png");

        when(userMock.authenticate("hero", "123"))
                .thenReturn(user);

        doNothing().when(screen).openMainMenu(any());

        loginField().setText("hero");
        passwordField().setText("123");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen).openMainMenu(user);

    }

    @Test
    @DisplayName("Domínio: login inválido → mensagem de erro")
    void testeDominioLoginInvalido() throws Exception {

        when(userMock.authenticate(anyString(), anyString()))
                .thenReturn(null);

        doNothing().when(screen).showMessage(anyString());

        loginField().setText("x");
        passwordField().setText("y");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen)
                .showMessage("Login ou senha incorretos.");

    }

    @Test
    @DisplayName("Domínio: login vazio → validação")
    void testeDominioLoginVazio() throws Exception {

        doNothing().when(screen).showMessage(anyString());

        loginField().setText("");
        passwordField().setText("123");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen)
                .showMessage("Preencha todos os campos.");

        verify(userMock, never())
                .authenticate(any(), any());

    }

// ── Cadastro ────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: ranking → abre RankingDialog")
    void testeDominioRanking() throws Exception {

        doNothing().when(screen).openRanking(anyList());

        SwingUtilities.invokeAndWait(() -> screen.showRanking());

        verify(userMock).getAllUsers();

        verify(screen).openRanking(anyList());

    }
}