package st.project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.GameGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GameGUIEstruturaTest {

    private GameGUI gui;

    @BeforeEach
    void setUp() {
        System.setProperty("java.awt.headless", "false");
        gui = new GameGUI();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        if (gui != null) {
            gui.dispose();
        }
    }

    @Test
    @DisplayName("Estrutura: componentes principais devem existir e estar adicionados ao JFrame")
    void testComponentesPrincipaisExistem() {
        Container contentPane = gui.getContentPane();
        assertInstanceOf(BorderLayout.class, contentPane.getLayout());

        Component north = ((BorderLayout) contentPane.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        assertNotNull(north);
        assertInstanceOf(JPanel.class, north);

        Component center = ((BorderLayout) contentPane.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        assertNotNull(center);
        assertInstanceOf(JPanel.class, center);
        JPanel centerWrapper = (JPanel) center;
        assertEquals(1, centerWrapper.getComponentCount());
        Component mapPanel = centerWrapper.getComponent(0);
        assertInstanceOf(JPanel.class, mapPanel);

        Component east = ((BorderLayout) contentPane.getLayout()).getLayoutComponent(BorderLayout.EAST);
        assertNotNull(east);
        assertInstanceOf(JPanel.class, east);
    }


    @Test
    @DisplayName("Estrutura: labels de tempo, status e movimentos estão configurados")
    void testLabelsConfigurados() throws Exception {
        Field timeLabelField = GameGUI.class.getDeclaredField("timeLabel");
        timeLabelField.setAccessible(true);
        JLabel timeLabel = (JLabel) timeLabelField.get(gui);
        assertNotNull(timeLabel);
        assertTrue(timeLabel.getText().startsWith("Tempo:"));

        Field statusLabelField = GameGUI.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(gui);
        assertNotNull(statusLabel);
        assertTrue(statusLabel.getText().contains("Explorando..."));

        Field movesLabelField = GameGUI.class.getDeclaredField("movesLabel");
        movesLabelField.setAccessible(true);
        JLabel movesLabel = (JLabel) movesLabelField.get(gui);
        assertNotNull(movesLabel);
        assertTrue(movesLabel.getText().startsWith("Mov:"));
    }

    @Test
    @DisplayName("Estrutura: área de log (JTextArea) está configurada dentro de JScrollPane")
    void testLogAreaConfigurada() throws Exception {
        Field logAreaField = GameGUI.class.getDeclaredField("logArea");
        logAreaField.setAccessible(true);
        JTextArea logArea = (JTextArea) logAreaField.get(gui);
        assertNotNull(logArea);
        assertFalse(logArea.isEditable());

        Container parent = logArea.getParent();
        assertNotNull(parent);
        assertInstanceOf(JViewport.class, parent);

        Container grandParent = parent.getParent();
        assertNotNull(grandParent);
        assertInstanceOf(JScrollPane.class, grandParent);
    }

    @Test
    @DisplayName("Estrutura: botões direcionais do D-Pad são criados e adicionados")
    void testDPadBotoesCriados() throws Exception {
        Method buildDPadMethod = GameGUI.class.getDeclaredMethod("buildDPad");
        buildDPadMethod.setAccessible(true);
        JPanel dpadPanel = (JPanel) buildDPadMethod.invoke(gui);

        boolean foundNorth = false, foundSouth = false, foundWest = false, foundEast = false;
        for (Component comp : dpadPanel.getComponents()) {
            if (comp instanceof JPanel inner) {
                for (Component innerComp : inner.getComponents()) {
                    if (innerComp instanceof JButton btn) {
                        String text = btn.getText();
                        if (text.contains("Norte")) foundNorth = true;
                        if (text.contains("Sul")) foundSouth = true;
                        if (text.contains("Oeste")) foundWest = true;
                        if (text.contains("Leste")) foundEast = true;
                    }
                }
            }
        }
        assertTrue(foundNorth && foundSouth && foundWest && foundEast,
                "Todos os botões direcionais devem existir");
    }

    @Test
    @DisplayName("Estrutura: key bindings estão registrados para WASD e setas")
    void testKeyBindingsRegistrados() {
        InputMap inputMap = gui.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = gui.getRootPane().getActionMap();

        assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));
        assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0)));
        assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
        assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)));
        assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)));
        assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0)));
        assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)));
        assertNotNull(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0)));

        assertNotNull(actionMap.get("move_" + KeyEvent.VK_UP));
        assertNotNull(actionMap.get("move_" + KeyEvent.VK_W));
    }

    @Test
    @DisplayName("Estrutura: carregamento de fontes não lança exceção e fontes são definidas")
    void testFontesCarregadas() throws Exception {
        Field fontTitleField = GameGUI.class.getDeclaredField("fontTitle");
        fontTitleField.setAccessible(true);
        Font fontTitle = (Font) fontTitleField.get(gui);
        assertNotNull(fontTitle);

        Field fontMonoField = GameGUI.class.getDeclaredField("fontMono");
        fontMonoField.setAccessible(true);
        Font fontMono = (Font) fontMonoField.get(gui);
        assertNotNull(fontMono);

        Field fontBodyField = GameGUI.class.getDeclaredField("fontBody");
        fontBodyField.setAccessible(true);
        Font fontBody = (Font) fontBodyField.get(gui);
        assertNotNull(fontBody);
    }

    @Test
    @DisplayName("Estrutura: mapPanel tem tamanho esperado e está dentro de wrapper")
    void testMapPanelConfigurado() throws Exception {
        Field mapPanelField = GameGUI.class.getDeclaredField("mapPanel");
        mapPanelField.setAccessible(true);
        JPanel mapPanel = (JPanel) mapPanelField.get(gui);
        assertNotNull(mapPanel);
        assertEquals(new Dimension(5 * 100 + 40, 5 * 100 + 40), mapPanel.getPreferredSize());

        Container parent = mapPanel.getParent();
        assertNotNull(parent);
        assertInstanceOf(GridBagLayout.class, parent.getLayout());
    }
}