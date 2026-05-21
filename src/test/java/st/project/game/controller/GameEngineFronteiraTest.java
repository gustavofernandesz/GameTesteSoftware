package st.project.game.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.GameModel;
import st.project.game.model.Mission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE FRONTEIRA: GameEngine ────────────────────────────────────────
 *
 * Cobre:
 *  - movimentos restantes em 0
 *  - jogo encerrado
 *  - mover inválido
 *  - timer parado
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("GameEngine – Testes de Fronteira")
class GameEngineFronteiraTest {

    @Test
    @DisplayName("Fronteira: mover com direção inválida retorna false")
    void testeFronteiraMoverInvalido() {
        GameModel model = mock(GameModel.class);

        when(model.moverJogador(null)).thenReturn(false);

        GameEngine engine = new GameEngine(model);

        assertThat(engine.mover(null)).isFalse();
    }

    @Test
    @DisplayName("Fronteira: movimentos restantes exatamente 0 encerra jogo")
    void testeFronteiraMovimentosZero() {
        GameModel model = mock(GameModel.class);
        Mission mission = mock(Mission.class);

        when(model.moverJogador("LESTE")).thenReturn(true);
        when(model.getMissao()).thenReturn(mission);
        when(mission.isMissaoConcluida()).thenReturn(false);
        when(model.getMovimentosRestantes()).thenReturn(0);

        GameEngine engine = new GameEngine(model);

        engine.mover("LESTE");

        verify(model).finalizarJogo(false);
    }

    @Test
    @DisplayName("Fronteira: jogo inicia não encerrado")
    void testeFronteiraJogoNaoEncerradoInicialmente() {
        GameModel model = mock(GameModel.class);

        GameEngine engine = new GameEngine(model);

        assertThat(engine.isJogoEncerrado()).isFalse();
    }

    @Test
    @DisplayName("Fronteira: encerrarJogo múltiplas vezes mantém estado encerrado")
    void testeFronteiraEncerrarJogoMultiplasVezes() {
        GameModel model = mock(GameModel.class);

        GameEngine engine = new GameEngine(model);

        engine.encerrarJogo();
        engine.encerrarJogo();

        assertThat(engine.isJogoEncerrado()).isTrue();
    }
}