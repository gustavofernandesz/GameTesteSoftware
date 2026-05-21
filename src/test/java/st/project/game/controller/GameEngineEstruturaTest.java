package st.project.game.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.GameModel;
import st.project.game.model.Mission;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES ESTRUTURAIS: GameEngine ─────────────────────────────────────────
 *
 * Foco:
 *  cobertura MC/DC das decisões internas.
 *
 * Decisões cobertas:
 *   (A) model.isJogoAtivo() == true  → reduzirTempo()
 *   (B) model.isJogoAtivo() == false → timer.stop()
 *   (C) moverJogador == false        → não finaliza jogo
 *   (D) missão concluída             → finalizarJogo(true)
 *   (E) movimentos <= 0              → finalizarJogo(false)
 *   (F) jogo não encerrado           → retomar timer
 *   (G) jogo encerrado               → não retoma
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("GameEngine – Testes Estruturais")
class GameEngineEstruturaTest {

    @Test
    @DisplayName("Estrutura (A): timer reduz tempo quando jogo está ativo")
    void testeEstruturaATimerReduceTempo() throws Exception {
        GameModel model = mock(GameModel.class);

        when(model.isJogoAtivo()).thenReturn(true);

        GameEngine engine = new GameEngine(model);

        Timer timer = obterTimer(engine);

        for (ActionListener listener : timer.getActionListeners()) {
            listener.actionPerformed(null);
        }

        verify(model, atLeastOnce()).reduzirTempo();
    }

    @Test
    @DisplayName("Estrutura (B): timer para quando jogo não está ativo")
    void testeEstruturaBTimerPara() throws Exception {
        GameModel model = mock(GameModel.class);

        when(model.isJogoAtivo()).thenReturn(false);

        GameEngine engine = new GameEngine(model);

        Timer timer = obterTimer(engine);

        for (ActionListener listener : timer.getActionListeners()) {
            listener.actionPerformed(null);
        }

        assertThat(timer.isRunning()).isFalse();
    }

    @Test
    @DisplayName("Estrutura (C): mover false não finaliza jogo")
    void testeEstruturaCMoverFalse() {
        GameModel model = mock(GameModel.class);

        when(model.moverJogador("OESTE")).thenReturn(false);

        GameEngine engine = new GameEngine(model);

        assertThat(engine.mover("OESTE")).isFalse();

        verify(model, never()).finalizarJogo(anyBoolean());
    }

    @Test
    @DisplayName("Estrutura (D): missão concluída finaliza jogo com vitória")
    void testeEstruturaDMissaoConcluida() {
        GameModel model = mock(GameModel.class);
        Mission mission = mock(Mission.class);

        when(model.moverJogador("NORTE")).thenReturn(true);
        when(model.getMissao()).thenReturn(mission);
        when(mission.isMissaoConcluida()).thenReturn(true);

        GameEngine engine = new GameEngine(model);

        engine.mover("NORTE");

        verify(model).finalizarJogo(true);
    }

    @Test
    @DisplayName("Estrutura (E): movimentos esgotados finaliza jogo com derrota")
    void testeEstruturaEMovimentosEsgotados() {
        GameModel model = mock(GameModel.class);
        Mission mission = mock(Mission.class);

        when(model.moverJogador("SUL")).thenReturn(true);
        when(model.getMissao()).thenReturn(mission);
        when(mission.isMissaoConcluida()).thenReturn(false);
        when(model.getMovimentosRestantes()).thenReturn(-1);

        GameEngine engine = new GameEngine(model);

        engine.mover("SUL");

        verify(model).finalizarJogo(false);
    }

    @Test
    @DisplayName("Estrutura (F): retomar reinicia timer se jogo não encerrado")
    void testeEstruturaFRetomar() throws Exception {
        GameModel model = mock(GameModel.class);

        GameEngine engine = new GameEngine(model);

        engine.pausar();
        engine.retomar();

        Timer timer = obterTimer(engine);

        assertThat(timer.isRunning()).isTrue();
    }

    @Test
    @DisplayName("Estrutura (G): retomar não reinicia timer se jogo encerrado")
    void testeEstruturaGRetomarJogoEncerrado() throws Exception {
        GameModel model = mock(GameModel.class);

        GameEngine engine = new GameEngine(model);

        engine.encerrarJogo();
        engine.retomar();

        Timer timer = obterTimer(engine);

        assertThat(timer.isRunning()).isFalse();
    }

    private Timer obterTimer(GameEngine engine) throws Exception {
        Field field = GameEngine.class.getDeclaredField("timer");
        field.setAccessible(true);
        return (Timer) field.get(engine);
    }
}