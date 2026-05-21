package st.project.game.view;

import org.junit.jupiter.api.*;
import st.project.game.controller.SaveManager;
import st.project.game.controller.UserManager;
import st.project.game.model.User;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE FRONTEIRA: MainMenu ──────────────────────────────────────────
 *
 * Cobre:
 *   - ausência de slots
 *   - slots totalmente ocupados
 *   - cancelamento do chooser
 *   - loadGame retornando null
 *   - lista vazia de usuários
 *
 * Dublês:
 *   - SaveManager
 *   - JOptionPane
 *   - UserManager
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("MainMenu – Testes de Fronteira")
class MainMenuFronteiraTest {

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
    @DisplayName("Fronteira: startNewGame sem slot livre")
    void testeFronteiraNovoJogoSemSlotLivre() throws Exception {

        when(saveManager.getFreeSlot(user)).thenReturn(-1);

        invoke("startNewGame");

        verify(saveManager).getFreeSlot(user);
    }

    @Test
    @DisplayName("Fronteira: continueGame sem saves")
    void testeFronteiraContinueGameSemSaves() throws Exception {

        when(saveManager.listSlots(user))
                .thenReturn(Collections.emptyList());

        invoke("continueGame");

        verify(saveManager).listSlots(user);
    }

    @Test
    @DisplayName("Fronteira: continueGame com múltiplos slots e cancelamento")
    void testeFronteiraContinueGameCancelado() throws Exception {

        when(saveManager.listSlots(user))
                .thenReturn(List.of(0, 1));

        try (var mocked = mockStatic(JOptionPane.class)) {

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

            verify(saveManager, never()).loadGame(any(), anyInt());
        }
    }

    @Test
    @DisplayName("Fronteira: continueGame com loadGame retornando null")
    void testeFronteiraContinueGameLoadNull() throws Exception {

        when(saveManager.listSlots(user))
                .thenReturn(List.of(0));

        when(saveManager.loadGame(user, 0))
                .thenReturn(null);

        invoke("continueGame");

        verify(saveManager).loadGame(user, 0);
    }

    @Test
    @DisplayName("Fronteira: manageUsers com lista vazia")
    void testeFronteiraManageUsersSemUsuarios() throws Exception {

        when(userManager.getAllUsers())
                .thenReturn(Collections.emptyList());

        invoke("manageUsers");

        verify(userManager).getAllUsers();
    }
}