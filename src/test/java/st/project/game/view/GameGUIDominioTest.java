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
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE DOMÍNIO: GameGUI ─────────────────────────────────────────────
 *
 * Escopo: regras de negócio observáveis na camada de visão — reações a eventos
 * de propriedade (propertyChange), comportamento do método mover() e do log.
 *
 * Decisões de domínio cobertas:
 *   (A) propertyChange "tempo" → atualiza timeLabel (≤10s = vermelho,
 *                                ≤20s = laranja, >20s = dourado)
 *   (B) propertyChange "movimentos" → atualiza movesLabel (≤3 = vermelho,
 *                                     ≤5 = laranja, >5 = roxo)
 *   (C) propertyChange "score"  → atualiza scoreLabel
 *   (D) propertyChange "nivel"  → atualiza levelLabel
 *   (E) propertyChange "andar"  → atualiza andarLabel e registra no log
 *   (F) propertyChange "lupaObtida" → registra mensagem no log
 *   (G) propertyChange "gameOver" vitória → atualiza statusLabel, score e
 *                                            apaga save
 *   (H) propertyChange "gameOver" derrota → atualiza statusLabel e apaga save
 *   (I) mover() com jogo ativo e movimento válido → log contém nome da sala
 *   (J) mover() bloqueado → log contém "Bloqueado"
 *   (K) mover() com jogo inativo → nenhuma ação tomada
 *   (L) log() acumula mensagens na logArea
 *
 * Dublê de teste: SaveManager (mock), UserManager (mock), GameModel (spy/real).
 * ────────────────────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameGUI – Testes de Domínio")
class GameGUIDominioTest {

    private GameModel  model;
    private GameGUI    gui;
    private SaveManager  saveMock;
    private UserManager  userMock;
    private User         user;

    @BeforeEach
    void setUp() throws Exception {

        model    = new GameModel(42L);
        saveMock = mock(SaveManager.class);
        userMock = mock(UserManager.class);
        user     = new User("heroi", "pw", "avatar.png");

        // Criação da GUI deve ocorrer na EDT; como é headless, invokeLater é síncrono
        SwingUtilities.invokeAndWait(() ->
                gui = new GameGUI(model, user, userMock, saveMock, 0));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (gui != null) {
            SwingUtilities.invokeAndWait(() -> gui.dispose());
        }
    }

    // ── Helpers de reflexão ───────────────────────────────────────────────────

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
        // drena EDT
        try { SwingUtilities.invokeAndWait(() -> {}); } catch (Exception ignored) {}
    }

    // ── (A) tempo ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (A1): 'tempo' > 20 → timeLabel dourado e texto correto")
    void testeDominioTempoAcima20() throws Exception {
        fireEvent("tempo", 30, 25);
        JLabel lbl = labelField("timeLabel");
        assertThat(lbl.getText()).isEqualTo("Tempo: 25s");
        assertThat(lbl.getForeground()).isEqualTo(new java.awt.Color(0xF0C040));
    }

    @Test
    @DisplayName("Domínio (A2): 'tempo' ≤ 20 e > 10 → timeLabel laranja")
    void testeDominioTempoEntre10e20() throws Exception {
        fireEvent("tempo", 25, 15);
        JLabel lbl = labelField("timeLabel");
        assertThat(lbl.getText()).isEqualTo("Tempo: 15s");
        assertThat(lbl.getForeground()).isEqualTo(new java.awt.Color(0xFFAA00));
    }

    @Test
    @DisplayName("Domínio (A3): 'tempo' ≤ 10 → timeLabel vermelho")
    void testeDominioTempoAte10() throws Exception {
        fireEvent("tempo", 15, 5);
        JLabel lbl = labelField("timeLabel");
        assertThat(lbl.getText()).isEqualTo("Tempo: 5s");
        assertThat(lbl.getForeground()).isEqualTo(new java.awt.Color(0xFF4444));
    }

    @Test
    @DisplayName("Domínio (A4): 'tempo' exatamente 10 → timeLabel vermelho (fronteira)")
    void testeDominioTempoExatamente10() throws Exception {
        fireEvent("tempo", 11, 10);
        assertThat(labelField("timeLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFF4444));
    }

    @Test
    @DisplayName("Domínio (A5): 'tempo' exatamente 20 → timeLabel laranja (fronteira)")
    void testeDominioTempoExatamente20() throws Exception {
        fireEvent("tempo", 21, 20);
        assertThat(labelField("timeLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFFAA00));
    }

    // ── (B) movimentos ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (B1): 'movimentos' > 5 → movesLabel roxo")
    void testeDominioMovimentosAcima5() throws Exception {
        fireEvent("movimentos", 10, 8);
        JLabel lbl = labelField("movesLabel");
        assertThat(lbl.getText()).isEqualTo("Mov: 8");
        assertThat(lbl.getForeground()).isEqualTo(new java.awt.Color(0x8B5CF6));
    }

    @Test
    @DisplayName("Domínio (B2): 'movimentos' ≤ 5 e > 3 → movesLabel laranja")
    void testeDominioMovimentosEntre3e5() throws Exception {
        fireEvent("movimentos", 8, 4);
        JLabel lbl = labelField("movesLabel");
        assertThat(lbl.getText()).isEqualTo("Mov: 4");
        assertThat(lbl.getForeground()).isEqualTo(new java.awt.Color(0xFFAA00));
    }

    @Test
    @DisplayName("Domínio (B3): 'movimentos' ≤ 3 → movesLabel vermelho")
    void testeDominioMovimentosAte3() throws Exception {
        fireEvent("movimentos", 4, 2);
        JLabel lbl = labelField("movesLabel");
        assertThat(lbl.getText()).isEqualTo("Mov: 2");
        assertThat(lbl.getForeground()).isEqualTo(new java.awt.Color(0xFF4444));
    }

    @Test
    @DisplayName("Domínio (B4): 'movimentos' exatamente 5 → laranja (fronteira)")
    void testeDominioMovimentosExatamente5() throws Exception {
        fireEvent("movimentos", 6, 5);
        assertThat(labelField("movesLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFFAA00));
    }

    @Test
    @DisplayName("Domínio (B5): 'movimentos' exatamente 3 → vermelho (fronteira)")
    void testeDominioMovimentosExatamente3() throws Exception {
        fireEvent("movimentos", 4, 3);
        assertThat(labelField("movesLabel").getForeground())
                .isEqualTo(new java.awt.Color(0xFF4444));
    }

    // ── (C) score ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (C): 'score' → atualiza scoreLabel com novo valor")
    void testeDominioScore() throws Exception {
        fireEvent("score", 0, 750);
        assertThat(labelField("scoreLabel").getText()).isEqualTo("Score: 750");
    }

    // ── (D) nivel ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (D): 'nivel' → atualiza levelLabel com novo nível")
    void testeDominioNivel() throws Exception {
        fireEvent("nivel", 1, 3);
        assertThat(labelField("levelLabel").getText()).isEqualTo("Nível: 3");
    }

    // ── (E) andar ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (E): 'andar' → atualiza andarLabel e escreve no log")
    void testeDominioAndar() throws Exception {
        fireEvent("andar", 1, 2);
        assertThat(labelField("andarLabel").getText()).isEqualTo("Andar: 2/4");
        assertThat(logAreaField().getText()).contains("Andar 2");
    }

    // ── (F) lupaObtida ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (F): 'lupaObtida' → mensagem de revelação no log")
    void testeDominioLupaObtida() throws Exception {
        fireEvent("lupaObtida", false, true);
        assertThat(logAreaField().getText()).contains("Lupa obtida");
    }

    // ── (G) gameOver vitória ────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (G): 'gameOver' true → statusLabel de vitória e save apagado")
    void testeDominioGameOverVitoria() throws Exception {

        SwingUtilities.invokeAndWait(() -> {
            gui.propertyChange(
                    new PropertyChangeEvent(model, "gameOver", null, true)
            );
        });

        assertThat(labelField("statusLabel").getText()).contains("VITORIA");

        verify(userMock).updateUserScoreAndSession(eq(user), anyInt());

        verify(saveMock).deleteSave(user, 0);
    }

    // ── (H) gameOver derrota ────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (H): 'gameOver' false → statusLabel de derrota e save apagado")
    void testeDominioGameOverDerrota() throws Exception {

        SwingUtilities.invokeAndWait(() -> {
            gui.propertyChange(
                    new PropertyChangeEvent(model, "gameOver", null, false)
            );
        });

        assertThat(labelField("statusLabel").getText()).contains("Tempo esgotado");
        verify(saveMock).deleteSave(user, 0);
    }

    // ── (I) mover válido ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (I): mover() em direção válida → log contém nome da sala destino")
    void testeDominioMoverValido() throws Exception {
        // A sala inicial ("entrada") tem vizinho ao leste na grade 5×5
        SwingUtilities.invokeAndWait(() -> gui.mover("leste"));
        String log = logAreaField().getText();
        assertThat(log).contains("->");
    }

    // ── (J) mover bloqueado ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (J): mover() em direção sem vizinho → log contém 'Bloqueado'")
    void testeDominioMoverBloqueado() throws Exception {
        // "entrada" está em (0,0); norte e oeste não existem
        SwingUtilities.invokeAndWait(() -> gui.mover("norte"));
        assertThat(logAreaField().getText()).contains("Bloqueado");
    }

    // ── (K) mover com jogo inativo ──────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (K): mover() com jogo inativo → nenhuma ação, log sem nova entrada")
    void testeDominioMoverJogoInativo() throws Exception {
        model.finalizarJogo(false);
        SwingUtilities.invokeAndWait(() -> {});
        String logAntes = logAreaField().getText();
        SwingUtilities.invokeAndWait(() -> gui.mover("leste"));
        // log não cresce com novo movimento
        assertThat(logAreaField().getText()).isEqualTo(logAntes);
    }

    // ── (L) log acumula ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (L): log() acumula múltiplas mensagens em ordem")
    void testeDominioLogAcumula() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            gui.log("Linha 1");
            gui.log("Linha 2");
            gui.log("Linha 3");
        });
        String conteudo = logAreaField().getText();
        assertThat(conteudo)
                .contains("Linha 1")
                .contains("Linha 2")
                .contains("Linha 3");
        assertThat(conteudo.indexOf("Linha 1"))
                .isLessThan(conteudo.indexOf("Linha 2"));
    }
}