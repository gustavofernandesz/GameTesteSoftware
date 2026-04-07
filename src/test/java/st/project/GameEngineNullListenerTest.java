package st.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.GameEngine;
import st.project.game.Item;
import st.project.game.Room;

import javax.swing.Timer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineNullListenerTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine(null);
    }

    @Test
    @DisplayName("Timer deve funcionar sem listener e não lançar exceção")
    void timerShouldWorkWithoutListener() throws Exception {
        Timer timer = getTimerFromEngine(engine);
        int tempoAntes = engine.getTempoRestante();
        fireTimerAction(timer);
        assertEquals(tempoAntes - 1, engine.getTempoRestante());
    }

    @Test
    @DisplayName("Mover jogador sem listener não deve lançar exceção e deve movimentar")
    void movePlayerWithoutListener() {
        Room entrada = engine.getSalas().get("entrada");
        Room vizinho = entrada.getVizinho("leste");
        assertNotNull(vizinho);
        assertTrue(engine.moverJogador("leste"));
        assertEquals(vizinho, engine.getJogador().getPosicaoAtual());
    }

    @Test
    @DisplayName("Coletar poção sem listener deve dobrar tempo sem exceção")
    void collectPotionWithoutListener() {
        Room atual = engine.getJogador().getPosicaoAtual();
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "Dobra");
        atual.adicionarItem(pocao);
        int tempoAntes = engine.getTempoRestante();
        engine.coletarItensSala();
        assertEquals(tempoAntes * 2, engine.getTempoRestante());
    }

    @Test
    @DisplayName("Coletar amuleto sem listener deve aumentar movimentos sem exceção")
    void collectAmuletWithoutListener() {
        Room atual = engine.getJogador().getPosicaoAtual();
        Item amuleto = new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3");
        atual.adicionarItem(amuleto);
        int movAntes = engine.getMovimentosRestantes();
        engine.coletarItensSala();
        assertEquals(movAntes + 3, engine.getMovimentosRestantes());
    }

    @Test
    @DisplayName("Encerrar jogo sem listener não deve lançar exceção")
    void endGameWithoutListener() {
        engine.setMovimentosRestantes(0);
        engine.moverJogador("leste");
        assertFalse(engine.isJogoAtivo());
    }

    @Test
    @DisplayName("Timer quando tempo zerar sem listener não deve lançar exceção")
    void timerExpiresWithoutListener() throws Exception {
        Timer timer = getTimerFromEngine(engine);
        for (int i = 0; i < 60; i++) {
            fireTimerAction(timer);
        }
        assertEquals(0, engine.getTempoRestante());
        assertFalse(engine.isJogoAtivo());
    }

    // Utilitários para manipular o Timer interno
    private Timer getTimerFromEngine(GameEngine engine) throws Exception {
        Field field = GameEngine.class.getDeclaredField("timer");
        field.setAccessible(true);
        return (Timer) field.get(engine);
    }

    private void fireTimerAction(Timer timer) throws Exception {
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