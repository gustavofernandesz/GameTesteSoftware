package st.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameGUIDominioTest {

    @Mock
    private GameEngine engineMock;
    private GameGUI gui;

    @BeforeEach
    void setUp() throws Exception {
        System.setProperty("java.awt.headless", "false");

        gui = new GameGUI();
        Field engineField = GameGUI.class.getDeclaredField("engine");
        engineField.setAccessible(true);
        engineField.set(gui, engineMock);

        // Configura mocks com lenient()
        Player jogadorMock = mock(Player.class);
        Room salaMock = mock(Room.class);
        lenient().when(salaMock.getNome()).thenReturn("Sala Teste");
        lenient().when(jogadorMock.getPosicaoAtual()).thenReturn(salaMock);
        lenient().when(jogadorMock.getInventario()).thenReturn(new ArrayList<>());
        lenient().when(jogadorMock.getHistorico()).thenReturn(new Stack<>());
        lenient().when(engineMock.getJogador()).thenReturn(jogadorMock);

        lenient().when(engineMock.getSalas()).thenReturn(new HashMap<>());

        Mission missaoMock = mock(Mission.class);
        lenient().when(engineMock.getMissao()).thenReturn(missaoMock);
        lenient().when(missaoMock.getSalaCalice()).thenReturn(mock(Room.class));

        lenient().when(engineMock.isJogoAtivo()).thenReturn(true);
        lenient().when(engineMock.moverJogador(anyString())).thenReturn(true);
        lenient().when(engineMock.isChaveAtiva()).thenReturn(false);

        gui.setVisible(false);
    }

    @Test
    @DisplayName("Domínio: mover deve chamar engine.moverJogador com direção correta quando jogo ativo")
    void testMoverChamaEngineQuandoAtivo() {
        invokeMover(gui, "norte");
        verify(engineMock, times(1)).moverJogador("norte");
    }

    @Test
    @DisplayName("Domínio: mover não chama engine quando jogo inativo")
    void testMoverNaoChamaEngineQuandoInativo() {
        when(engineMock.isJogoAtivo()).thenReturn(false);
        invokeMover(gui, "norte");
        verify(engineMock, never()).moverJogador(anyString());
    }

    @Test
    @DisplayName("Domínio: log adiciona mensagem ao logArea")
    void testLogAdicionaMensagem() throws Exception {
        Field logAreaField = GameGUI.class.getDeclaredField("logArea");
        logAreaField.setAccessible(true);
        JTextArea logArea = (JTextArea) logAreaField.get(gui);
        logArea.setText("");
        invokeLog(gui, "Mensagem de teste");
        assertTrue(logArea.getText().contains("Mensagem de teste"));
    }

    @Test
    @DisplayName("Domínio: timerListener atualiza timeLabel corretamente")
    void testOnTempoAtualizado() throws Exception {
        Field timeLabelField = GameGUI.class.getDeclaredField("timeLabel");
        timeLabelField.setAccessible(true);
        JLabel timeLabel = (JLabel) timeLabelField.get(gui);
        gui.onTempoAtualizado(45);
        assertEquals("Tempo: 45s", timeLabel.getText());
        assertNotEquals(Color.RED, timeLabel.getForeground());
    }

    @Test
    @DisplayName("Domínio: onMovimentoRealizado atualiza movesLabel com cor adequada")
    void testOnMovimentoRealizado() throws Exception {
        Field movesLabelField = GameGUI.class.getDeclaredField("movesLabel");
        movesLabelField.setAccessible(true);
        JLabel movesLabel = (JLabel) movesLabelField.get(gui);
        gui.onMovimentoRealizado(2);
        assertEquals("Mov: 2", movesLabel.getText());
        assertEquals(new Color(0xFF4444), movesLabel.getForeground());
    }

    private void invokeMover(GameGUI gui, String direcao) {
        try {
            Method moverMethod = GameGUI.class.getDeclaredMethod("mover", String.class);
            moverMethod.setAccessible(true);
            moverMethod.invoke(gui, direcao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeLog(GameGUI gui, String msg) {
        try {
            Method logMethod = GameGUI.class.getDeclaredMethod("log", String.class);
            logMethod.setAccessible(true);
            logMethod.invoke(gui, msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}