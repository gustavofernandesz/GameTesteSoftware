package st.project.game.view;

import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import st.project.game.controller.SaveManager;
import st.project.game.controller.UserManager;
import st.project.game.model.GameModel;
import st.project.game.model.User;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE DOMÍNIO: MainMenu ────────────────────────────────────────────
 *
 * Escopo:
 *   Regras funcionais do menu principal:
 *   - iniciar novo jogo
 *   - continuar jogo salvo
 *   - gerenciamento de usuários
 *   - logout
 *   - renderização condicional do botão admin
 *
 * Dublês:
 *   - SaveManager
 *   - UserManager
 *   - JOptionPane
 *   - MockedConstruction para GameGUI/LoginScreen
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("MainMenu – Testes de Domínio")
class MainMenuDominioTest {

    private User user;
    private UserManager userManager;
    private SaveManager saveManager;
    private MainMenu menu;

    @BeforeEach
    void setUp() throws Exception {
        user = mock(User.class);
        when(user.getLogin()).thenReturn("player");

        userManager = mock(UserManager.class);

        menu = new MainMenu(user, userManager);

        saveManager = mock(SaveManager.class);

        var field = MainMenu.class.getDeclaredField("saveManager");
        field.setAccessible(true);
        field.set(menu, saveManager);
    }

    @AfterEach
    void tearDown() {
        if (menu != null) {
            menu.dispose();
    }
    }

    private void invoke(String methodName) throws Exception {
        Method method = MainMenu.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(menu);
    }

    @Test
    @DisplayName("Domínio: startNewGame cria GameGUI quando existe slot livre")
    void testeDominioNovoJogoComSlotLivre() throws Exception {
        when(saveManager.getFreeSlot(user)).thenReturn(1);

        try (MockedConstruction<GameGUI> ignored =
                     mockConstruction(GameGUI.class)) {

            invoke("startNewGame");

            verify(saveManager).getFreeSlot(user);
        }
    }

    @Test
    @DisplayName("Domínio: continueGame carrega save válido")
    void testeDominioContinueGameValido() throws Exception {
        when(saveManager.listSlots(user)).thenReturn(List.of(0));

        GameModel model = mock(GameModel.class);

        when(saveManager.loadGame(user, 0)).thenReturn(model);

        try (MockedConstruction<GameGUI> ignored =
                     mockConstruction(GameGUI.class)) {

            invoke("continueGame");

            verify(saveManager).loadGame(user, 0);
        }
    }

    @Test
    @DisplayName("Domínio: botão Gerenciar Usuários existe para superusuário")
    void testeDominioBotaoGerenciarUsuariosSuperUser() {
        menu.dispose();

        when(userManager.isSuperUser("player")).thenReturn(true);

        MainMenu adminMenu = new MainMenu(user, userManager);

        boolean encontrado = false;

        for (Component c : adminMenu.getContentPane().getComponents()) {
            if (c instanceof JButton btn &&
                    btn.getText().equals("Gerenciar Usuários")) {
                encontrado = true;
            }
        }

        assertThat(encontrado).isTrue();

        adminMenu.dispose();
    }

    @Test
    @DisplayName("Domínio: botão Gerenciar Usuários não existe para usuário comum")
    void testeDominioBotaoGerenciarUsuariosUsuarioComum() {
        menu.dispose();

        when(userManager.isSuperUser("player")).thenReturn(false);

        MainMenu normalMenu = new MainMenu(user, userManager);

        boolean encontrado = false;

        for (Component c : normalMenu.getContentPane().getComponents()) {
            if (c instanceof JButton btn &&
                    btn.getText().equals("Gerenciar Usuários")) {
                encontrado = true;
            }
        }

        assertThat(encontrado).isFalse();

        normalMenu.dispose();
    }

    @Test
    @DisplayName("Domínio: logout fecha menu e abre LoginScreen")
    void testeDominioLogout() {

        try (MockedConstruction<LoginScreen> ignored =
                     mockConstruction(LoginScreen.class)) {

            for (Component c : menu.getContentPane().getComponents()) {
                if (c instanceof JButton btn &&
                        btn.getText().equals("Sair")) {

                    btn.doClick();
                }
            }

            assertThat(menu.isDisplayable()).isFalse();
        }
    }
}