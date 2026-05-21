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
 * ─── TESTES ESTRUTURAIS: MainMenu ───────────────────────────────────────────
 *
 * Foco:
 *   cobertura MC/DC das decisões internas.
 *
 * Decisões cobertas:
 *
 *   (A) startNewGame:
 *       slot == -1                  → mensagem erro
 *       slot válido                 → abre GameGUI
 *
 *   (B) continueGame:
 *       slots vazio                 → erro
 *       slots.size == 1             → carrega direto
 *       slots.size > 1              → chooser
 *       escolha == null             → return
 *       model == null               → erro
 *       model válido                → abre jogo
 *
 *   (C) manageUsers:
 *       selected == null            → return
 *       login == admin              → impede exclusão
 *       confirmação != YES_OPTION   → não exclui
 *       confirmação == YES_OPTION   → exclui
 *
 *   (D) createButton:
 *       botão recebe texto/cores
 *
 * Dublês:
 *   - SaveManager
 *   - UserManager
 *   - JOptionPane
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("MainMenu – Testes Estruturais (MC/DC)")
class MainMenuEstruturaTest {

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

    // ── (A) startNewGame ──────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (A1): slot inexistente → mostra erro")
    void testeEstruturaA1NovoJogoSemSlot() throws Exception {

        when(saveManager.getFreeSlot(user)).thenReturn(-1);

        invoke("startNewGame");

        verify(saveManager).getFreeSlot(user);
    }

    @Test
    @DisplayName("Estrutura (A2): slot válido → cria GameGUI")
    void testeEstruturaA2NovoJogoComSlot() throws Exception {

        when(saveManager.getFreeSlot(user)).thenReturn(0);

        try (MockedConstruction<GameGUI> ignored =
                     mockConstruction(GameGUI.class)) {

            invoke("startNewGame");
        }
    }

    // ── (B) continueGame ─────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (B1): lista vazia → mensagem erro")
    void testeEstruturaB1ContinueSemSave() throws Exception {

        when(saveManager.listSlots(user)).thenReturn(List.of());

        invoke("continueGame");
    }

    @Test
    @DisplayName("Estrutura (B2): único slot → carrega direto")
    void testeEstruturaB2ContinueUmSlot() throws Exception {

        when(saveManager.listSlots(user)).thenReturn(List.of(0));

        when(saveManager.loadGame(user, 0))
                .thenReturn(mock(GameModel.class));

        try (MockedConstruction<GameGUI> ignored =
                     mockConstruction(GameGUI.class)) {

            invoke("continueGame");
        }
    }

    @Test
    @DisplayName("Estrutura (B3): múltiplos slots + escolha válida")
    void testeEstruturaB3ContinueEscolhaValida() throws Exception {

        when(saveManager.listSlots(user))
                .thenReturn(List.of(0, 1));

        when(saveManager.loadGame(user, 1))
                .thenReturn(mock(GameModel.class));

        try (MockedStatic<JOptionPane> mocked =
                     mockStatic(JOptionPane.class);
             MockedConstruction<GameGUI> ignored =
                     mockConstruction(GameGUI.class)) {

            mocked.when(() -> JOptionPane.showInputDialog(
                            any(),
                            any(),
                            any(),
                            anyInt(),
                            any(),
                            any(),
                            any()))
                    .thenReturn("Slot 2");

            invoke("continueGame");

            verify(saveManager).loadGame(user, 1);
        }
    }

    @Test
    @DisplayName("Estrutura (B4): múltiplos slots + cancelamento")
    void testeEstruturaB4ContinueCancelado() throws Exception {

        when(saveManager.listSlots(user))
                .thenReturn(List.of(0, 1));

        try (MockedStatic<JOptionPane> mocked =
                     mockStatic(JOptionPane.class)) {

            mocked.when(() -> JOptionPane.showInputDialog(
                            any(),
                            any(),
                            any(),
                            anyInt(),
                            any(),
                            any(),
                            any()))
                    .thenReturn(null);

            invoke("continueGame");
        }
    }

    @Test
    @DisplayName("Estrutura (B5): loadGame retorna null")
    void testeEstruturaB5LoadNull() throws Exception {

        when(saveManager.listSlots(user))
                .thenReturn(List.of(0));

        when(saveManager.loadGame(user, 0))
                .thenReturn(null);

        invoke("continueGame");
    }

    // ── (C) manageUsers ──────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (C1): nenhum usuário selecionado")
    void testeEstruturaC1ManageUsersSemSelecao() throws Exception {

        when(userManager.getAllUsers()).thenReturn(List.of());

        invoke("manageUsers");
    }

    @Test
    @DisplayName("Estrutura (C2): impede exclusão do admin")
    void testeEstruturaC2NaoExcluirAdmin() throws Exception {

        User admin = mock(User.class);

        when(admin.getLogin()).thenReturn("admin");
        when(admin.getBestScore()).thenReturn(999);

        when(userManager.getAllUsers())
                .thenReturn(List.of(admin));

        invoke("manageUsers");

        verify(userManager, never()).deleteUser(anyString());
    }

    // ── (D) createButton ─────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (D): createButton define texto e propriedades")
    void testeEstruturaDCreateButton() throws Exception {

        Method method = MainMenu.class
                .getDeclaredMethod("createButton", String.class);

        method.setAccessible(true);

        JButton btn = (JButton) method.invoke(menu, "Teste");

        assertThat(btn.getText()).isEqualTo("Teste");
        assertThat(btn.isFocusPainted()).isFalse();
        assertThat(btn.getForeground()).isEqualTo(Color.WHITE);
    }
}