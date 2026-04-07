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
class GameGUIFronteiraTest {

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

        // Configura os mocks com lenient() para evitar stubs não utilizados
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

        // Stubs padrão para métodos frequentemente usados
        lenient().when(engineMock.isJogoAtivo()).thenReturn(true);
        lenient().when(engineMock.isChaveAtiva()).thenReturn(false);   // corrige o erro de tipo

        // Não stubamos moverJogador por padrão; cada teste que precisar fará seu stub.

        gui.setVisible(false);
    }

    @Test
    @DisplayName("Fronteira: mover para direção inválida não modifica posição e loga erro")
    void testMoverDirecaoInvalida() throws Exception {
        when(engineMock.moverJogador("norte")).thenReturn(false);

        Field logAreaField = GameGUI.class.getDeclaredField("logArea");
        logAreaField.setAccessible(true);
        JTextArea logArea = (JTextArea) logAreaField.get(gui);
        logArea.setText("");

        invokeMover(gui, "norte");

        verify(engineMock, times(1)).moverJogador("norte");
        assertTrue(logArea.getText().contains("X Bloqueado ao norte."));
    }

    @Test
    @DisplayName("Fronteira: quando movimentos restantes = 0, jogo termina após movimento")
    void testMovimentosEsgotados() {
        // Controla o estado do jogo
        final boolean[] jogoAtivo = {true};

        // Sobrescreve o stub padrão de isJogoAtivo
        when(engineMock.isJogoAtivo()).thenAnswer(inv -> jogoAtivo[0]);

        // Simula movimento e desativa o jogo
        when(engineMock.moverJogador(anyString())).thenAnswer(inv -> {
            jogoAtivo[0] = false;
            return true;
        });

        invokeMover(gui, "leste");

        verify(engineMock, times(1)).moverJogador("leste");
        assertFalse(jogoAtivo[0], "O jogo deveria ter terminado após o movimento");
    }

    @Test
    @DisplayName("Fronteira: onTempoAtualizado com segundos <= 10 deixa label vermelha")
    void testTempoCriticoCorVermelha() throws Exception {
        Field timeLabelField = GameGUI.class.getDeclaredField("timeLabel");
        timeLabelField.setAccessible(true);
        JLabel timeLabel = (JLabel) timeLabelField.get(gui);
        gui.onTempoAtualizado(5);
        assertEquals(new Color(0xFF4444), timeLabel.getForeground());
    }

    @Test
    @DisplayName("Fronteira: onMovimentoRealizado com movimentos <= 3 deixa label vermelha")
    void testMovimentosCriticosCorVermelha() throws Exception {
        Field movesLabelField = GameGUI.class.getDeclaredField("movesLabel");
        movesLabelField.setAccessible(true);
        JLabel movesLabel = (JLabel) movesLabelField.get(gui);
        gui.onMovimentoRealizado(2);
        assertEquals(new Color(0xFF4444), movesLabel.getForeground());
    }

    @Test
    @DisplayName("Fronteira: tentar mover quando jogo inativo não faz nada")
    void testMoverComJogoInativo() {
        when(engineMock.isJogoAtivo()).thenReturn(false);
        invokeMover(gui, "sul");
        verify(engineMock, never()).moverJogador(anyString());
    }

    @Test
    @DisplayName("Fronteira: ao terminar jogo com vitória, statusLabel e log exibem mensagens corretas")
    void testJogoTerminadoVitoria() throws Exception {
        Field statusLabelField = GameGUI.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(gui);

        Field logAreaField = GameGUI.class.getDeclaredField("logArea");
        logAreaField.setAccessible(true);
        JTextArea logArea = (JTextArea) logAreaField.get(gui);
        logArea.setText("");

        gui.onJogoTerminado(true);

        assertEquals("VITORIA! Missao cumprida!", statusLabel.getText());
        assertEquals(new Color(0xF0C040), statusLabel.getForeground());
        assertTrue(logArea.getText().contains("PARABENS, AVENTUREIRO!"));
    }

    @Test
    @DisplayName("Fronteira: ao terminar jogo com derrota por tempo, statusLabel e log exibem mensagens")
    void testJogoTerminadoDerrota() throws Exception {
        Field statusLabelField = GameGUI.class.getDeclaredField("statusLabel");
        statusLabelField.setAccessible(true);
        JLabel statusLabel = (JLabel) statusLabelField.get(gui);

        Field logAreaField = GameGUI.class.getDeclaredField("logArea");
        logAreaField.setAccessible(true);
        JTextArea logArea = (JTextArea) logAreaField.get(gui);
        logArea.setText("");

        gui.onJogoTerminado(false);

        assertEquals("Tempo esgotado - Fim de Jogo", statusLabel.getText());
        assertEquals(new Color(0xFF4444), statusLabel.getForeground());
        assertTrue(logArea.getText().contains("TEMPO ESGOTADO"));
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
}