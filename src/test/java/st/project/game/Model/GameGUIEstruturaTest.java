package st.project.game.Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.controller.SaveManager;
import st.project.game.controller.UserManager;
import st.project.game.model.GameModel;
import st.project.game.model.User;
import st.project.game.view.GameGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * ─── TESTES ESTRUTURAIS: GameGUI ────────────────────────────────────────────
 *
 * Foco: verificar que a estrutura dos componentes Swing está corretamente
 * montada — hierarquia de painéis, labels, keybindings e fontes.
 *
 * Dublês de teste (Mockito):
 *   • UserManager (mock) — isola I/O sem tela.
 *   • SaveManager (mock) — isola I/O sem tela.
 *
 * Headless: java.awt.headless=true para execução em CI.
 * ────────────────────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameGUI – Testes Estruturais")
class GameGUIEstruturaTest {

    private static final long SEED = 42L;

    private GameGUI gui;

    @BeforeAll
    static void configurarHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() {
        GameModel   model   = new GameModel(SEED);
        UserManager um      = mock(UserManager.class);
        SaveManager sm      = mock(SaveManager.class);
        User        user    = new User("teste", "pw", "av.png");

        gui = new GameGUI(model, user, um, sm, 0);
        gui.setVisible(false);

        // Para o timer
        try {
            Field ef = GameGUI.class.getDeclaredField("engine");
            ef.setAccessible(true);
            ((st.project.game.controller.GameEngine) ef.get(gui)).pausar();
        } catch (Exception ignored) {}
    }

    @AfterEach
    void tearDown() {
        if (gui != null) gui.dispose();
    }

    // ── Layout geral ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: contentPane usa BorderLayout")
    void testeEstruturaBorderLayout() {
        assertThat(gui.getContentPane().getLayout()).isInstanceOf(BorderLayout.class);
    }

    @Test
    @DisplayName("Estrutura: painel NORTH (topPanel) existe")
    void testeEstruturaTopPanelExiste() {
        Component north = ((BorderLayout) gui.getContentPane().getLayout())
                .getLayoutComponent(BorderLayout.NORTH);
        assertThat(north).isInstanceOf(JPanel.class);
    }

    @Test
    @DisplayName("Estrutura: painel CENTER (mapWrapper) existe")
    void testeEstruturaCenterExiste() {
        Component center = ((BorderLayout) gui.getContentPane().getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        assertThat(center).isInstanceOf(JPanel.class);
    }

    @Test
    @DisplayName("Estrutura: painel EAST (rightPanel) existe")
    void testeEstruturaEastPanelExiste() {
        Component east = ((BorderLayout) gui.getContentPane().getLayout())
                .getLayoutComponent(BorderLayout.EAST);
        assertThat(east).isInstanceOf(JPanel.class);
    }

    // ── Labels do painel topo ─────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: timeLabel existe e inicia com 'Tempo:'")
    void testeEstruturaTimeLabelExiste() {
        JLabel l = getLabel("timeLabel");
        assertThat(l).isNotNull();
        assertThat(l.getText()).startsWith("Tempo:");
    }

    @Test
    @DisplayName("Estrutura: movesLabel existe e inicia com 'Mov:'")
    void testeEstruturaMovesLabelExiste() {
        JLabel l = getLabel("movesLabel");
        assertThat(l).isNotNull();
        assertThat(l.getText()).startsWith("Mov:");
    }

    @Test
    @DisplayName("Estrutura: levelLabel existe e inicia com 'Nível:'")
    void testeEstruturaLevelLabelExiste() {
        JLabel l = getLabel("levelLabel");
        assertThat(l).isNotNull();
        assertThat(l.getText()).startsWith("Nível:");
    }

    @Test
    @DisplayName("Estrutura: scoreLabel existe e inicia com 'Score:'")
    void testeEstruturaScoreLabelExiste() {
        JLabel l = getLabel("scoreLabel");
        assertThat(l).isNotNull();
        assertThat(l.getText()).startsWith("Score:");
    }

    @Test
    @DisplayName("Estrutura: andarLabel existe e inicia com 'Andar:'")
    void testeEstruturaAndarLabelExiste() {
        JLabel l = getLabel("andarLabel");
        assertThat(l).isNotNull();
        assertThat(l.getText()).startsWith("Andar:");
    }

    @Test
    @DisplayName("Estrutura: statusLabel existe e contém 'Explorando'")
    void testeEstruturaStatusLabelExiste() {
        JLabel l = getLabel("statusLabel");
        assertThat(l).isNotNull();
        assertThat(l.getText()).contains("Explorando");
    }

    // ── LogArea ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: logArea existe, não é editável e está dentro de JScrollPane")
    void testeEstruturaLogAreaConfigurada() {
        try {
            Field f = GameGUI.class.getDeclaredField("logArea");
            f.setAccessible(true);
            JTextArea logArea = (JTextArea) f.get(gui);

            assertThat(logArea).isNotNull();
            assertThat(logArea.isEditable()).isFalse();
            assertThat(logArea.getParent()).isInstanceOf(JViewport.class);
            assertThat(logArea.getParent().getParent()).isInstanceOf(JScrollPane.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ── MapPanel ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: mapPanel tem tamanho preferido 5×100+40 × 5×100+40")
    void testeEstruturaMapPanelTamanho() {
        try {
            Field f = GameGUI.class.getDeclaredField("mapPanel");
            f.setAccessible(true);
            JPanel mapPanel = (JPanel) f.get(gui);

            assertThat(mapPanel).isNotNull();
            assertThat(mapPanel.getPreferredSize())
                    .isEqualTo(new Dimension(5 * 100 + 40, 5 * 100 + 40));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ── Key bindings ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: keybindings WASD registrados no rootPane")
    void testeEstruturaKeyBindingsWASD() {
        InputMap im = gui.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        assertThat(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0))).isNotNull();
        assertThat(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0))).isNotNull();
        assertThat(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0))).isNotNull();
        assertThat(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0))).isNotNull();
    }

    @Test
    @DisplayName("Estrutura: keybindings de setas registrados no rootPane")
    void testeEstruturaKeyBindingsSetas() {
        InputMap im = gui.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        assertThat(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP,    0))).isNotNull();
        assertThat(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,  0))).isNotNull();
        assertThat(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,  0))).isNotNull();
        assertThat(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0))).isNotNull();
    }

    @Test
    @DisplayName("Estrutura: actionMap tem ações para todas as 8 teclas de movimento")
    void testeEstruturaActionMapCompleto() {
        ActionMap am = gui.getRootPane().getActionMap();
        for (int key : new int[]{
                KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                KeyEvent.VK_W,  KeyEvent.VK_S,    KeyEvent.VK_A,    KeyEvent.VK_D
        }) {
            assertThat(am.get("move_" + key))
                    .as("Faltando action para key=" + key)
                    .isNotNull();
        }
    }

    // ── Fontes ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: fontTitle, fontMono e fontBody estão inicializadas")
    void testeEstruturaFontesInicializadas() {
        for (String nome : new String[]{"fontTitle", "fontMono", "fontBody"}) {
            try {
                Field f = GameGUI.class.getDeclaredField(nome);
                f.setAccessible(true);
                assertThat(f.get(gui)).as(nome + " deve estar definida").isNotNull();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ── Organização MVC ───────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (MVC): GUI implementa PropertyChangeListener")
    void testeEstruturaGuiImplementaPropertyChangeListener() {
        assertThat(gui).isInstanceOf(java.beans.PropertyChangeListener.class);
    }

    @Test
    @DisplayName("Estrutura (MVC): GameEngine está injetado na GUI")
    void testeEstruturaEngineInjetado() {
        try {
            Field f = GameGUI.class.getDeclaredField("engine");
            f.setAccessible(true);
            assertThat(f.get(gui)).isNotNull()
                    .isInstanceOf(st.project.game.controller.GameEngine.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Estrutura (MVC): GameModel está injetado na GUI")
    void testeEstruturaModelInjetado() {
        try {
            Field f = GameGUI.class.getDeclaredField("model");
            f.setAccessible(true);
            assertThat(f.get(gui)).isNotNull()
                    .isInstanceOf(GameModel.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Estrutura (MVC): UserManager e SaveManager estão injetados na GUI")
    void testeEstruturaUserManagerSaveManagerInjetados() {
        try {
            Field uf = GameGUI.class.getDeclaredField("userManager");
            uf.setAccessible(true);
            assertThat(uf.get(gui)).isNotNull();

            Field sf = GameGUI.class.getDeclaredField("saveManager");
            sf.setAccessible(true);
            assertThat(sf.get(gui)).isNotNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utilitários
    // ═══════════════════════════════════════════════════════════════════════

    private JLabel getLabel(String fieldName) {
        try {
            Field f = GameGUI.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (JLabel) f.get(gui);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
