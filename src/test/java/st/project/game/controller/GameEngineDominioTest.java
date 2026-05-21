package st.project.game.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.GameModel;
import st.project.game.model.Mission;

import javax.swing.*;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE DOMÍNIO: GameEngine ──────────────────────────────────────────
 *
 * Escopo:
 *  - movimentação do jogador
 *  - encerramento de jogo
 *  - pausa e retomada
 *  - integração com GameModel
 *
 * Dublês:
 *  - Mockito para GameModel e Mission.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("GameEngine – Testes de Domínio")
class GameEngineDominioTest {

    @Test
    @DisplayName("Domínio: mover retorna true quando jogador se move")
    void testeDominioMoverRetornaTrue() {
        GameModel model = mock(GameModel.class);
        Mission mission = mock(Mission.class);

        when(model.moverJogador("NORTE")).thenReturn(true);
        when(model.getMissao()).thenReturn(mission);
        when(mission.isMissaoConcluida()).thenReturn(false);
        when(model.getMovimentosRestantes()).thenReturn(5);

        GameEngine engine = new GameEngine(model);

        assertThat(engine.mover("NORTE")).isTrue();

        verify(model).moverJogador("NORTE");
    }

    @Test
    @DisplayName("Domínio: mover conclui jogo quando missão é concluída")
    void testeDominioMoverConcluiMissao() {
        GameModel model = mock(GameModel.class);
        Mission mission = mock(Mission.class);

        when(model.moverJogador("LESTE")).thenReturn(true);
        when(model.getMissao()).thenReturn(mission);
        when(mission.isMissaoConcluida()).thenReturn(true);

        GameEngine engine = new GameEngine(model);

        engine.mover("LESTE");

        verify(model).finalizarJogo(true);
    }

    @Test
    @DisplayName("Domínio: mover encerra jogo quando movimentos acabam")
    void testeDominioMoverSemMovimentos() {
        GameModel model = mock(GameModel.class);
        Mission mission = mock(Mission.class);

        when(model.moverJogador("SUL")).thenReturn(true);
        when(model.getMissao()).thenReturn(mission);
        when(mission.isMissaoConcluida()).thenReturn(false);
        when(model.getMovimentosRestantes()).thenReturn(0);

        GameEngine engine = new GameEngine(model);

        engine.mover("SUL");

        verify(model).finalizarJogo(false);
    }

    @Test
    @DisplayName("Domínio: encerrarJogo marca jogo como encerrado")
    void testeDominioEncerrarJogo() {
        GameModel model = mock(GameModel.class);

        GameEngine engine = new GameEngine(model);

        engine.encerrarJogo();

        assertThat(engine.isJogoEncerrado()).isTrue();
    }

    @Test
    @DisplayName("Domínio: pausar interrompe o timer")
    void testeDominioPausar() throws Exception {
        GameModel model = mock(GameModel.class);

        GameEngine engine = new GameEngine(model);

        engine.pausar();

        Timer timer = obterTimer(engine);

        assertThat(timer.isRunning()).isFalse();
    }

    @Test
    @DisplayName("Domínio: retomar reinicia timer quando jogo não encerrado")
    void testeDominioRetomar() throws Exception {
        GameModel model = mock(GameModel.class);

        GameEngine engine = new GameEngine(model);

        engine.pausar();
        engine.retomar();

        Timer timer = obterTimer(engine);

        assertThat(timer.isRunning()).isTrue();
    }

    private Timer obterTimer(GameEngine engine) throws Exception {
        Field field = GameEngine.class.getDeclaredField("timer");
        field.setAccessible(true);
        return (Timer) field.get(engine);
    }
}