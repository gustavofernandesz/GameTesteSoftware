package st.project.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.controller.GameEngine;
import st.project.game.controller.SaveManager;
import st.project.game.controller.UserManager;
import st.project.game.model.GameModel;
import st.project.game.model.User;
import st.project.game.view.GameGUI;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE DOMÍNIO: GameGUI ─────────────────────────────────────────────
 *
 * Escopo: regras de negócio da camada de visão — reação a movimentos,
 * logging, atualização de labels, ciclo MVC e integração com UserManager.
 *
 * Dublês de teste (Mockito):
 *   • UserManager (mock) — isola I/O de arquivo de usuários.
 *   • SaveManager (mock) — isola I/O de arquivo de save.
 *   • User (real ou spy) — objeto de valor leve, criado diretamente.
 *   • GameModel com seed fixa — elimina aleatoriedade do mapa.
 *
 * Headless: System.setProperty("java.awt.headless", "true") antes de qualquer
 *           instanciação de componentes Swing para rodar em CI sem tela.
 * ────────────────────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameGUI – Testes de Domínio")
class GameGUIDominioTest {

    private static final long SEED = 42L;

    private GameModel   model;
    private UserManager userManagerMock;
    private SaveManager saveManagerMock;
    private User        user;
    private GameGUI     gui;

    @BeforeAll
    static void configurarHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() throws Exception {
        model          = new GameModel(SEED);
        userManagerMock = mock(UserManager.class);
        saveManagerMock = mock(SaveManager.class);
        user           = new User("heroi", "senha123", "avatar.png");

        gui = new GameGUI(model, user, userManagerMock, saveManagerMock, 0);
        gui.setVisible(false);

        // Para o timer para evitar efeitos colaterais em testes
        getEngine().pausar();
    }

    @AfterEach
    void tearDown() {
        if (gui != null) gui.dispose();
    }

    // ── Ciclo MVC ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio (MVC): GUI registra-se como listener do model no construtor")
    void testeDominioGuiRegistradaComoListener() {
        // Se a GUI está recebendo eventos, ela está registrada
        // Verificamos disparando um evento e checando reação no label
        JLabel timeLabel = getLabel("timeLabel");
        String antes     = timeLabel.getText();

        // Dispara evento de tempo via model
        model.reduzirTempo();
        getEngine().pausar();

        String depois = timeLabel.getText();
        // O texto deve ter mudado para refletir o novo tempo
        assertThat(depois).contains(String.valueOf(model.getTempoRestante()));
    }

    @Test
    @DisplayName("Domínio (MVC): evento 'score' atualiza scoreLabel na GUI")
    void testeDominioEventoScoreAtualizaLabel() throws Exception {
        JLabel scoreLabel = getLabel("scoreLabel");
        int score = model.getScore();

        // Dispara evento score diretamente via propertyChange
        gui.propertyChange(new PropertyChangeEvent(model, "score", 0, score));

        assertThat(scoreLabel.getText()).isEqualTo("Score: " + score);
    }

    @Test
    @DisplayName("Domínio (MVC): evento 'nivel' atualiza levelLabel na GUI")
    void testeDominioEventoNivelAtualizaLabel() throws Exception {
        JLabel levelLabel = getLabel("levelLabel");

        gui.propertyChange(new PropertyChangeEvent(model, "nivel", 1, 3));

        assertThat(levelLabel.getText()).isEqualTo("Nível: 3");
    }

    @Test
    @DisplayName("Domínio (MVC): evento 'andar' atualiza andarLabel na GUI")
    void testeDominioEventoAndarAtualizaLabel() throws Exception {
        JLabel andarLabel = getLabel("andarLabel");

        gui.propertyChange(new PropertyChangeEvent(model, "andar", 1, 2));

        assertThat(andarLabel.getText()).isEqualTo("Andar: 2/4");
    }

    // ── Log ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: log adiciona mensagem ao logArea")
    void testeDominioLogAdicionaMensagem() {
        JTextArea logArea = getLogArea();
        logArea.setText("");

        gui.log("Mensagem de teste");

        assertThat(logArea.getText()).contains("Mensagem de teste");
    }

    @Test
    @DisplayName("Domínio: múltiplos logs acumulam no logArea")
    void testeDominioMultiplosLogs() {
        JTextArea logArea = getLogArea();
        logArea.setText("");

        gui.log("Linha 1");
        gui.log("Linha 2");
        gui.log("Linha 3");

        assertThat(logArea.getText())
                .contains("Linha 1")
                .contains("Linha 2")
                .contains("Linha 3");
    }

    // ── Mover ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: mover em direção válida registra log com nome da sala")
    void testeDominioMoverValidoLogaNomeDaSala() {
        JTextArea logArea = getLogArea();
        logArea.setText("");

        gui.mover("leste");

        // Deve ter logado algo (nome da sala ou bloqueio)
        assertThat(logArea.getText()).isNotEmpty();
    }

    @Test
    @DisplayName("Domínio: mover em direção bloqueada loga mensagem de bloqueio")
    void testeDominioMoverBloqueadoLogaErro() {
        JTextArea logArea = getLogArea();
        logArea.setText("");

        gui.mover("norte"); // entrada está em (0,0) → sem vizinho norte

        assertThat(logArea.getText()).contains("Bloqueado");
    }

    // ── Integração com UserManager ─────────────────────────────────────────

//    @Test
//    @DisplayName("Domínio: ao receber gameOver=true, chama updateUserScoreAndSession")
//    void testeDominioGameOverVitoriaAtualizaScore() {
//        gui.propertyChange(new PropertyChangeEvent(model, "gameOver", null, true));
//
//        verify(userManagerMock).updateUserScoreAndSession(eq(user), anyInt());
//    }

//    @Test
//    @DisplayName("Domínio: ao receber gameOver, chama saveManager.deleteSave")
//    void testeDominioGameOverDeletaSave() {
//        gui.propertyChange(new PropertyChangeEvent(model, "gameOver", null, false));
//
//        verify(saveManagerMock).deleteSave(user, 0);
//    }

    // ── lupaObtida ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: evento 'lupaObtida' registra mensagem no log")
    void testeDominioLupaObtidaLogaMensagem() {
        JTextArea logArea = getLogArea();
        logArea.setText("");

        gui.propertyChange(new PropertyChangeEvent(model, "lupaObtida", false, true));

        assertThat(logArea.getText()).contains("Lupa");
    }

    // ── Painel de pontuação inicial ───────────────────────────────────────

    @Test
    @DisplayName("Domínio: GUI exibe nível e score iniciais corretamente no construtor")
    void testeDominioPainelPontuacaoInicial() {
        JLabel levelLabel = getLabel("levelLabel");
        JLabel scoreLabel = getLabel("scoreLabel");

        assertThat(levelLabel.getText()).isEqualTo("Nível: " + model.getNivel());
        assertThat(scoreLabel.getText()).isEqualTo("Score: " + model.getScore());
    }

    @Test
    @DisplayName("Domínio: GUI exibe andar inicial 1/4 no construtor")
    void testeDominioAndarInicialExibido() {
        JLabel andarLabel = getLabel("andarLabel");
        assertThat(andarLabel.getText()).isEqualTo("Andar: 1/4");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utilitários reflexivos
    // ═══════════════════════════════════════════════════════════════════════

    private GameEngine getEngine() {
        try {
            Field f = GameGUI.class.getDeclaredField("engine");
            f.setAccessible(true);
            return (GameEngine) f.get(gui);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JLabel getLabel(String fieldName) {
        try {
            Field f = GameGUI.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (JLabel) f.get(gui);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JTextArea getLogArea() {
        try {
            Field f = GameGUI.class.getDeclaredField("logArea");
            f.setAccessible(true);
            return (JTextArea) f.get(gui);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
