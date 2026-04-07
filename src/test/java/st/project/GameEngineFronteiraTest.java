package st.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.GameEngine;
import st.project.game.Item;
import st.project.game.Room;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.Timer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameEngineFronteiraTest {

    @Mock
    private GameEngine.TimerListener listenerMock;

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine(listenerMock);
    }

    @Test
    @DisplayName("Teste de Fronteira: Valor inicial de movimentos deve ser 7")
    void testeFronteiraMovimentosRestantesValorInicial() {
        assertEquals(7, engine.getMovimentosRestantes());
    }

    @Test
    @DisplayName("Teste de Fronteira: Quando movimentos chegam a zero, mover deve falhar e encerrar jogo")
    void testeFronteiraMovimentosRestantesZero() {
        // Reduzir para 0
        engine.setMovimentosRestantes(0);

        boolean moveu = engine.moverJogador("leste");

        assertFalse(moveu);
        verify(listenerMock).onJogoTerminado(false);
        assertFalse(engine.isJogoAtivo());
    }

    @Test
    @DisplayName("Teste de Fronteira: Valor inicial do tempo deve ser 60")
    void testeFronteiraTempoRestanteValorInicial() {
        assertEquals(60, engine.getTempoRestante());
    }

    @Test
    @DisplayName("Teste de Fronteira: Mudar o tempo restante para um valor maior que o inicial")
    void testeDominioSetTempoRestanteMaiorQueInicial() {
        engine.setTempoRestante(120);
        assertEquals(120, engine.getTempoRestante());
    }

    @Test
    @DisplayName("Teste de Fronteira: Mudar o tempo restante para um valor menor que o inicial")
    void testeDominioSetTempoRestanteMenorQueInicial() {
        engine.setTempoRestante(30);
        assertEquals(30, engine.getTempoRestante());

    }

    @Test
    @DisplayName("Teste de Fronteira: Mudar o tempo restante para zero")
    void testeDominioSetTempoRestanteParaZero() {
        engine.setTempoRestante(0);
        assertEquals(0, engine.getTempoRestante());
    }

    @Test
    @DisplayName("Teste de Fronteira: Quando tempo zerar, jogo deve ser encerrado")
    void testeFronteiraTempoRestanteZerar() throws Exception {
        // Simula a contagem regressiva do timer
        Timer timer = getTimerFromEngine(engine);
        // Dispara 60 vezes o actionPerformed
        for (int i = 0; i < 60; i++) {
            fireTimerAction(timer);
        }
        // Após 60 segundos, tempo deve ser 0 e jogo encerrado
        assertEquals(0, engine.getTempoRestante());
        assertFalse(engine.isJogoAtivo());
        verify(listenerMock, times(60)).onTempoAtualizado(anyInt());
        verify(listenerMock).onJogoTerminado(false);
    }

    @Test
    @DisplayName("Teste de Fronteira: Poção de velocidade dobra o tempo respeitando o limite")
    void testeFronteiraPocaoVelocidadeDobraTempo() {
        // Arrange: tempo inicial 60
        Room entrada = engine.getSalas().get("entrada");
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "Dobra");
        entrada.adicionarItem(pocao);

        // Act
        engine.coletarItensSala();

        // Assert: 60 * 2 = 120
        assertEquals(120, engine.getTempoRestante());
        verify(listenerMock).onTempoAtualizado(120);
    }

    @Test
    @DisplayName("Teste de Fronteira: Amuleto adiciona 3 movimentos e pode acumular acima de 7")
    void testeFronteiraAmuletoMovimentosAcumulaAcimaDe7() {
        engine.setMovimentosRestantes(5);
        Room atual = engine.getJogador().getPosicaoAtual();
        Item amuleto = new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3");
        atual.adicionarItem(amuleto);

        engine.coletarItensSala();

        assertEquals(8, engine.getMovimentosRestantes());
    }

    @Test
    @DisplayName("Teste de Fronteira: Tentar mover para borda deve falhar para direções inválidas")
    void testeFronteiraTentarMoverParaBorda() {
        // Usa a sala "entrada", que está na borda (0,0) e não é bloqueada
        Room borda = engine.getSalas().get("entrada");
        engine.getJogador().moverPara(borda);
        assertEquals(borda, engine.getJogador().getPosicaoAtual());

        // Tenta mover para direções que não existem a partir da entrada
        assertFalse(engine.moverJogador("norte"));
        assertEquals(borda, engine.getJogador().getPosicaoAtual());

        assertFalse(engine.moverJogador("oeste"));
        assertEquals(borda, engine.getJogador().getPosicaoAtual());

        // Opcional: testar também o movimento para direções válidas (leste e sul)
        assertTrue(engine.moverJogador("leste"));
    }

    // --- Utilitários para manipular o Timer interno ---
    private Timer getTimerFromEngine(GameEngine engine) throws Exception {
        Field field = GameEngine.class.getDeclaredField("timer");
        field.setAccessible(true);
        return (Timer) field.get(engine);
    }

    private void fireTimerAction(Timer timer) throws Exception {
        // O Timer do Swing tem uma lista de ActionListeners.
        // Podemos obter o primeiro e invocar actionPerformed.
        // Como é complicado, uma alternativa é usar reflection para obter o ActionListener
        // que foi adicionado e chamá-lo. Por simplicidade, consideramos que o Timer possui
        // um único listener e o obtemos através de uma chamada interna.
        // Abaixo uma forma genérica que pode funcionar, mas dependendo da implementação pode
        // precisar de ajustes.
        Class<?> timerClass = timer.getClass();
        Method getListeners = timerClass.getDeclaredMethod("getListeners", Class.class);
        getListeners.setAccessible(true);
        Object[] listeners = (Object[]) getListeners.invoke(timer, Class.forName("java.awt.event.ActionListener"));
        if (listeners.length > 0) {
            java.awt.event.ActionListener al = (java.awt.event.ActionListener) listeners[0];
            al.actionPerformed(new java.awt.event.ActionEvent(timer, 0, null));
        }
    }
}