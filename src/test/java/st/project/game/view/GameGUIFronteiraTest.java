package st.project.game.view;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import st.project.game.controller.SaveManager;
import st.project.game.controller.UserManager;
import st.project.game.model.GameModel;
import st.project.game.model.Item;
import st.project.game.model.Room;
import st.project.game.model.User;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE FRONTEIRA: GameGUI ───────────────────────────────────────────
 *
 * Cobre os valores exatos nos limites das decisões de coloração e as
 * situações extremas de entrada (tempo = 0, movimentos = 0, tempo = 1,
 * movimentos = 1, log vazio, evento de propriedade desconhecido).
 *
 * Fronteiras mapeadas:
 *   (FB-A) tempo:       11 → dourado  |  10 → vermelho
 *                       21 → dourado  |  20 → laranja
 *   (FB-B) movimentos:   4 → laranja  |   3 → vermelho
 *                        6 → roxo     |   5 → laranja
 *   (FB-C) tempo = 0     → vermelho (caso extremo mínimo)
 *   (FB-D) movimentos = 0 → vermelho (caso extremo mínimo)
 *   (FB-E) tempo = 1     → vermelho (um acima do zero)
 *   (FB-F) movimentos = 1 → vermelho (um acima do zero)
 *   (FB-G) evento desconhecido → nenhum label alterado, sem exceção
 *   (FB-H) log com string vazia → aceita sem exceção
 *   (FB-I) log com string muito longa → aceita sem exceção
 *   (FB-J) mover() com string de direção inválida → log contém "Bloqueado"
 *   (FB-K) mover() com string vazia → log contém "Bloqueado"
 *   (FB-L) atualizarMapa() sem exceção após construção
 *
 * Dublê de teste: SaveManager (mock), UserManager (mock).
 * ────────────────────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameGUI – Testes de Fronteira")
class GameGUIFronteiraTest {

    private GameModel   model;
    private GameGUI     gui;
    private SaveManager saveMock;
    private UserManager userMock;
    private User        user;

    @BeforeEach
    void setUp() throws Exception {

        model    = new GameModel(42L);
        saveMock = mock(SaveManager.class);
        userMock = mock(UserManager.class);
        user     = new User("heroi", "pw", "avatar.png");

        SwingUtilities.invokeAndWait(() ->
                gui = new GameGUI(model, user, userMock, saveMock, 0));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (gui != null) {
            SwingUtilities.invokeAndWait(() -> gui.dispose());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JLabel labelField(String name) throws Exception {
        Field f = GameGUI.class.getDeclaredField(name);
        f.setAccessible(true);
        return (JLabel) f.get(gui);
    }

    private JTextArea logAreaField() throws Exception {
        Field f = GameGUI.class.getDeclaredField("logArea");
        f.setAccessible(true);
        return (JTextArea) f.get(gui);
    }

    private void fireEvent(String prop, Object oldVal, Object newVal) {
        PropertyChangeEvent evt = new PropertyChangeEvent(model, prop, oldVal, newVal);
        SwingUtilities.invokeLater(() -> gui.propertyChange(evt));
        try { SwingUtilities.invokeAndWait(() -> {}); } catch (Exception ignored) {}
    }

    // ── (FB-A) Fronteiras de tempo ─────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (FB-A1): tempo = 11 → laranja")
    void testeFronteiraTempoOnze() throws Exception {
        fireEvent("tempo", 12, 11);

        assertThat(labelField("timeLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFFAA00));
    }

    @Test
    @DisplayName("Fronteira (FB-A2): tempo = 10 → vermelho (no limiar vermelho)")
    void testeFronteiraTempoDez() throws Exception {
        fireEvent("tempo", 11, 10);
        assertThat(labelField("timeLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFF4444));
    }

    @Test
    @DisplayName("Fronteira (FB-A3): tempo = 21 → dourado (acima do limiar laranja)")
    void testeFronteiraTempoVinteEUm() throws Exception {
        fireEvent("tempo", 22, 21);
        assertThat(labelField("timeLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xF0C040));
    }

    @Test
    @DisplayName("Fronteira (FB-A4): tempo = 20 → laranja (no limiar laranja)")
    void testeFronteiraTempoVinte() throws Exception {
        fireEvent("tempo", 21, 20);
        assertThat(labelField("timeLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFFAA00));
    }

    // ── (FB-B) Fronteiras de movimentos ────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (FB-B1): movimentos = 4 → laranja")
    void testeFronteiraMovimentosQuatro() throws Exception {
        fireEvent("movimentos", 5, 4);
        assertThat(labelField("movesLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFFAA00));
    }

    @Test
    @DisplayName("Fronteira (FB-B2): movimentos = 3 → vermelho")
    void testeFronteiraMovimentosTres() throws Exception {
        fireEvent("movimentos", 4, 3);
        assertThat(labelField("movesLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFF4444));
    }

    @Test
    @DisplayName("Fronteira (FB-B3): movimentos = 6 → roxo")
    void testeFronteiraMovimentosSeis() throws Exception {
        fireEvent("movimentos", 7, 6);
        assertThat(labelField("movesLabel").getForeground())
                .isEqualTo(new java.awt.Color(0x8B5CF6));
    }

    @Test
    @DisplayName("Fronteira (FB-B4): movimentos = 5 → laranja (no limiar)")
    void testeFronteiraMovimentosCinco() throws Exception {
        fireEvent("movimentos", 6, 5);
        assertThat(labelField("movesLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFFAA00));
    }

    // ── (FB-C/D/E/F) Casos extremos mínimos ────────────────────────────────────

    @Test
    @DisplayName("Fronteira (FB-C): tempo = 0 → vermelho e texto 'Tempo: 0s'")
    void testeFronteiraTempoZero() throws Exception {
        fireEvent("tempo", 1, 0);
        JLabel lbl = labelField("timeLabel");
        assertThat(lbl.getText()).isEqualTo("Tempo: 0s");
        assertThat(lbl.getForeground()).isEqualTo(new java.awt.Color(0xFF4444));
    }

    @Test
    @DisplayName("Fronteira (FB-D): movimentos = 0 → vermelho e texto 'Mov: 0'")
    void testeFronteiraMovimentosZero() throws Exception {
        fireEvent("movimentos", 1, 0);
        JLabel lbl = labelField("movesLabel");
        assertThat(lbl.getText()).isEqualTo("Mov: 0");
        assertThat(lbl.getForeground()).isEqualTo(new java.awt.Color(0xFF4444));
    }

    @Test
    @DisplayName("Fronteira (FB-E): tempo = 1 → vermelho (um acima do zero)")
    void testeFronteiraTempoUm() throws Exception {
        fireEvent("tempo", 2, 1);
        assertThat(labelField("timeLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFF4444));
    }

    @Test
    @DisplayName("Fronteira (FB-F): movimentos = 1 → vermelho (um acima do zero)")
    void testeFronteiraMovimentosUm() throws Exception {
        fireEvent("movimentos", 2, 1);
        assertThat(labelField("movesLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFF4444));
    }

    // ── (FB-G) Evento desconhecido ──────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (FB-G): evento desconhecido → sem exceção, labels inalterados")
    void testeFronteiraEventoDesconhecido() throws Exception {
        String textoAntes = labelField("statusLabel").getText();
        // Não deve lançar exceção
        fireEvent("propriedadeInexistente", null, "qualquerCoisa");
        assertThat(labelField("statusLabel").getText()).isEqualTo(textoAntes);
    }

    // ── (FB-H/I) Log com entradas extremas ─────────────────────────────────────

    @Test
    @DisplayName("Fronteira (FB-H): log() com string vazia → aceita sem exceção")
    void testeFronteiraLogStringVazia() throws Exception {
        SwingUtilities.invokeAndWait(() -> gui.log(""));
        // Apenas verificamos que não lança e a logArea ainda existe
        assertThat(logAreaField()).isNotNull();
    }

    @Test
    @DisplayName("Fronteira (FB-I): log() com string de 500 caracteres → aceita sem exceção")
    void testeFronteiraLogStringLonga() throws Exception {
        String longa = "X".repeat(500);
        SwingUtilities.invokeAndWait(() -> gui.log(longa));
        assertThat(logAreaField().getText()).contains(longa);
    }

    // ── (FB-J/K) Direções inválidas ─────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (FB-J): mover() com direção inválida → log contém 'Bloqueado'")
    void testeFronteiraMoverDirecaoInvalida() throws Exception {
        SwingUtilities.invokeAndWait(() -> gui.mover("cima"));
        assertThat(logAreaField().getText()).contains("Bloqueado");
    }

    @Test
    @DisplayName("Fronteira (FB-K): mover() com string vazia → log contém 'Bloqueado'")
    void testeFronteiraMoverStringVazia() throws Exception {
        SwingUtilities.invokeAndWait(() -> gui.mover(""));
        assertThat(logAreaField().getText()).contains("Bloqueado");
    }

    // ── (FB-L) atualizarMapa ────────────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (FB-L): atualizarMapa() logo após construção → sem exceção")
    void testeFronteiraAtualizarMapa() throws Exception {
        // Apenas verificamos que não lança exceção
        SwingUtilities.invokeAndWait(() -> gui.atualizarMapa());
    }

    // ── Score e Nível com valores extremos ──────────────────────────────────────

    @Test
    @DisplayName("Fronteira: 'score' = 0 → scoreLabel exibe 'Score: 0'")
    void testeFronteiraScoreZero() throws Exception {
        fireEvent("score", 100, 0);
        assertThat(labelField("scoreLabel").getText()).isEqualTo("Score: 0");
    }

    @Test
    @DisplayName("Fronteira: 'nivel' = 1 → levelLabel exibe 'Nível: 1' (mínimo)")
    void testeFronteiraNivelMinimo() throws Exception {
        fireEvent("nivel", 2, 1);
        assertThat(labelField("levelLabel").getText()).isEqualTo("Nível: 1");
    }

    @Test
    @DisplayName("Fronteira: 'andar' = 1 → andarLabel exibe 'Andar: 1/4'")
    void testeFronteiraAndarMinimo() throws Exception {
        fireEvent("andar", 2, 1);
        assertThat(labelField("andarLabel").getText()).isEqualTo("Andar: 1/4");
    }

    @Test
    @DisplayName("Fronteira: 'andar' = 4 → andarLabel exibe 'Andar: 4/4' (máximo)")
    void testeFronteiraAndarMaximo() throws Exception {
        fireEvent("andar", 3, 4);
        assertThat(labelField("andarLabel").getText()).isEqualTo("Andar: 4/4");
    }

    // ── gameOver nos dois estados de borda ──────────────────────────────────────

    @Test
    @DisplayName("Fronteira: 'gameOver' false seguido de mover() → jogo encerrado, mover ignorado")
    void testeFronteiraGameOverDepoisMover() throws Exception {

        model.finalizarJogo(false);

        SwingUtilities.invokeAndWait(() -> {});

        String logDepoisGameOver = logAreaField().getText();

        SwingUtilities.invokeAndWait(() -> gui.mover("leste"));

        assertThat(logAreaField().getText())
                .isEqualTo(logDepoisGameOver);
    }
}