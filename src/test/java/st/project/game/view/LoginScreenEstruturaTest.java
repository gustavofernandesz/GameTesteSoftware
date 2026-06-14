package st.project.game.view;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import st.project.game.controller.UserManager;
import st.project.game.model.User;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**

 ─────────────────────────────────────────────────────────────
 TESTES ESTRUTURAIS (MC/DC) — LoginScreen

 ─────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginScreen — Testes Estruturais")
class LoginScreenEstruturaTest {

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

// ── MC/DC login.isEmpty() || password.isEmpty() ────────

    @Test
    @DisplayName("Estrutura (A): login vazio → validação")
    void testeEstruturaLoginVazio() throws Exception {

        doNothing().when(screen).showMessage(anyString());

        screen.loginField.setText("");
        screen.passwordField.setText("123");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen)
                .showMessage("Preencha todos os campos.");

    }

    @Test
    @DisplayName("Estrutura (A'): senha vazia → validação")
    void testeEstruturaSenhaVazia() throws Exception {

        doNothing().when(screen).showMessage(anyString());

        screen.loginField.setText("hero");
        screen.passwordField.setText("");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen)
                .showMessage("Preencha todos os campos.");

    }

    @Test
    @DisplayName("Estrutura (A''): login e senha preenchidos → autentica")
    void testeEstruturaCamposValidos() throws Exception {

        User user = new User("hero", "123", "a.png");

        when(userMock.authenticate("hero", "123"))
                .thenReturn(user);

        doNothing().when(screen).openMainMenu(any());

        screen.loginField.setText("hero");
        screen.passwordField.setText("123");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(userMock)
                .authenticate("hero", "123");

    }

// ── user != null ───────────────────────────────────────

    @Test
    @DisplayName("Estrutura (B): usuário autenticado → abre menu")
    void testeEstruturaUsuarioAutenticado() throws Exception {

        User user = new User("hero", "123", "a.png");

        when(userMock.authenticate(any(), any()))
                .thenReturn(user);

        doNothing().when(screen).openMainMenu(any());

        screen.loginField.setText("hero");
        screen.passwordField.setText("123");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen).openMainMenu(user);

    }

    @Test
    @DisplayName("Estrutura (B'): usuário nulo → erro")
    void testeEstruturaUsuarioNulo() throws Exception {

        when(userMock.authenticate(any(), any()))
                .thenReturn(null);

        doNothing().when(screen).showMessage(anyString());

        screen.loginField.setText("hero");
        screen.passwordField.setText("123");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen)
                .showMessage("Login ou senha incorretos.");

    }

// ── createButton ───────────────────────────────────────

    @Test
    @DisplayName("Estrutura: createButton → botão branco sem focus")
    void testeEstruturaCreateButton() {

        JButton btn = screen.createButton("Teste", "testBtn");

        assertThat(btn.getForeground())
                .isEqualTo(java.awt.Color.WHITE);

        assertThat(btn.isFocusPainted())
                .isFalse();

    }

// ── createLabel ────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: createLabel → texto correto")
    void testeEstruturaCreateLabel() {

        JLabel lbl = screen.createLabel("ABC");

        assertThat(lbl.getText()).isEqualTo("ABC");

    }
}