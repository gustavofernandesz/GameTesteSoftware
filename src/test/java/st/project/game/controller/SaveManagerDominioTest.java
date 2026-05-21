package st.project.game.controller;

import org.junit.jupiter.api.*;
import st.project.game.model.GameModel;
import st.project.game.model.User;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE DOMÍNIO: SaveManager ─────────────────────────────────────────
 *
 * Escopo:
 *  - salvar e carregar jogos
 *  - listar slots
 *  - deletar saves
 *  - encontrar slots livres
 *
 * Dublês:
 *  - Mockito para GameModel
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("SaveManager – Testes de Domínio")
class SaveManagerDominioTest {

    private SaveManager saveManager;
    private User user;

    @BeforeEach
    void setUp() {
        saveManager = new SaveManager();
        user = new User("dominioUser", "123", "avatar.png");

        saveManager.deleteAllSaves(user.getLogin());
    }

    @AfterEach
    void tearDown() {
        saveManager.deleteAllSaves(user.getLogin());
    }

    @Test
    @DisplayName("Domínio: saveGame salva jogo corretamente")
    void testeDominioSaveGame() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 0);

        List<Integer> slots = saveManager.listSlots(user);

        assertThat(slots).contains(0);
    }

    @Test
    @DisplayName("Domínio: loadGame carrega jogo salvo")
    void testeDominioLoadGame() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 1);

        GameModel loaded = saveManager.loadGame(user, 1);

        assertThat(loaded).isNotNull();
    }

    @Test
    @DisplayName("Domínio: deleteSave remove save existente")
    void testeDominioDeleteSave() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 2);

        assertThat(saveManager.deleteSave(user, 2)).isTrue();
    }

    @Test
    @DisplayName("Domínio: listSlots retorna slots ocupados")
    void testeDominioListSlots() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 0);
        saveManager.saveGame(model, user, 2);

        List<Integer> slots = saveManager.listSlots(user);

        assertThat(slots).containsExactly(0, 2);
    }

    @Test
    @DisplayName("Domínio: getFreeSlot retorna primeiro slot livre")
    void testeDominioGetFreeSlot() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 0);

        assertThat(saveManager.getFreeSlot(user)).isEqualTo(1);
    }

    @Test
    @DisplayName("Domínio: deleteAllSaves remove todos os saves")
    void testeDominioDeleteAllSaves() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 0);
        saveManager.saveGame(model, user, 1);

        saveManager.deleteAllSaves(user.getLogin());

        assertThat(saveManager.listSlots(user)).isEmpty();
    }
}