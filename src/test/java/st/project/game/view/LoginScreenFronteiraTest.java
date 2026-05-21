package st.project.game.view;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import st.project.game.controller.UserManager;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**

 ─────────────────────────────────────────────────────────────
 TESTES DE FRONTEIRA — LoginScreen

 ─────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginScreen — Testes de Fronteira")
class LoginScreenFronteiraTest {

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

    @Test
    @DisplayName("Fronteira: login vazio")
    void testeFronteiraLoginVazio() throws Exception {

        doNothing().when(screen).showMessage(anyString());

        screen.loginField.setText("");
        screen.passwordField.setText("123");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen)
                .showMessage("Preencha todos os campos.");

    }

    @Test
    @DisplayName("Fronteira: senha vazia")
    void testeFronteiraSenhaVazia() throws Exception {

        doNothing().when(screen).showMessage(anyString());

        screen.loginField.setText("hero");
        screen.passwordField.setText("");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen)
                .showMessage("Preencha todos os campos.");

    }

    @Test
    @DisplayName("Fronteira: login com espaços → trim")
    void testeFronteiraTrimLogin() throws Exception {

        doNothing().when(screen).showMessage(anyString());

        screen.loginField.setText("   ");
        screen.passwordField.setText("123");

        SwingUtilities.invokeAndWait(() -> screen.login());

        verify(screen)
                .showMessage("Preencha todos os campos.");

    }

    @Test
    @DisplayName("Fronteira: login tamanho 1")
    void testeFronteiraLoginUmCaractere() throws Exception {

        screen.loginField.setText("a");

        assertThat(screen.loginField.getText())
                .hasSize(1);

    }

    @Test
    @DisplayName("Fronteira: senha tamanho 1")
    void testeFronteiraSenhaUmCaractere() throws Exception {

        screen.passwordField.setText("x");

        assertThat(
                new String(screen.passwordField.getPassword())
        ).hasSize(1);

    }

    @Test
    @DisplayName("Fronteira: botão texto vazio")
    void testeFronteiraBotaoTextoVazio() {

        JButton btn = screen.createButton("");

        assertThat(btn.getText()).isEmpty();

    }

    @Test
    @DisplayName("Fronteira: label texto vazio")
    void testeFronteiraLabelTextoVazio() {

        JLabel lbl = screen.createLabel("");

        assertThat(lbl.getText()).isEmpty();

    }
}