package st.project.game.controller;

import org.junit.jupiter.api.*;
import st.project.game.model.GameModel;
import st.project.game.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE FRONTEIRA: SaveManager ───────────────────────────────────────
 *
 * Cobre:
 *  - ausência de saves
 *  - limite máximo de slots
 *  - deleção inexistente
 *  - lista vazia
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("SaveManager – Testes de Fronteira")
class SaveManagerFronteiraTest {

    private SaveManager saveManager;
    private User user;

    @BeforeEach
    void setUp() {
        saveManager = new SaveManager();
        user = new User("fronteiraUser", "123", "avatar.png");

        saveManager.deleteAllSaves(user.getLogin());
    }

    @AfterEach
    void tearDown() {
        saveManager.deleteAllSaves(user.getLogin());
    }

    @Test
    @DisplayName("Fronteira: listSlots sem saves retorna lista vazia")
    void testeFronteiraListSlotsVazio() {
        assertThat(saveManager.listSlots(user)).isEmpty();
    }

    @Test
    @DisplayName("Fronteira: loadGame sem save retorna null")
    void testeFronteiraLoadGameSemSave() {
        assertThat(saveManager.loadGame(user, 0)).isNull();
    }

    @Test
    @DisplayName("Fronteira: deleteSave inexistente retorna false")
    void testeFronteiraDeleteInexistente() {
        assertThat(saveManager.deleteSave(user, 0)).isFalse();
    }

    @Test
    @DisplayName("Fronteira: getFreeSlot retorna 0 quando nenhum slot ocupado")
    void testeFronteiraGetFreeSlotInicial() {
        assertThat(saveManager.getFreeSlot(user)).isEqualTo(0);
    }

    @Test
    @DisplayName("Fronteira: todos slots ocupados retorna -1")
    void testeFronteiraTodosSlotsOcupados() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 0);
        saveManager.saveGame(model, user, 1);
        saveManager.saveGame(model, user, 2);

        assertThat(saveManager.getFreeSlot(user)).isEqualTo(-1);
    }

    @Test
    @DisplayName("Fronteira: listSlots com 3 saves retorna tamanho máximo")
    void testeFronteiraListSlotsMaximo() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 0);
        saveManager.saveGame(model, user, 1);
        saveManager.saveGame(model, user, 2);

        List<Integer> slots = saveManager.listSlots(user);

        assertThat(slots).hasSize(3)
                .containsExactly(0, 1, 2);
    }
}