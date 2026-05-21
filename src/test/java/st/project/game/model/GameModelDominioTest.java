package st.project.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE DOMÍNIO: GameModel ───────────────────────────────────────────
 *
 * Escopo: regras de negócio do jogo — movimentação, score, missão,
 * itens, tempo e progressão.
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("GameModel – Testes de Domínio")
class GameModelDominioTest {

    private GameModel game;

    @BeforeEach
    void setUp() {
        game = new GameModel(123L);
    }

    @Test
    @DisplayName("Domínio: jogo inicia ativo")
    void testeDominioJogoIniciaAtivo() {
        assertThat(game.isJogoAtivo()).isTrue();
    }

    @Test
    @DisplayName("Domínio: jogador inicia na entrada")
    void testeDominioJogadorNaEntrada() {
        assertThat(game.getJogador().getPosicaoAtual().getNome())
                .isEqualTo("entrada");
    }

    @Test
    @DisplayName("Domínio: mapa possui 100 salas")
    void testeDominioMapaCom100Salas() {
        assertThat(game.getSalas()).hasSize(100);
    }

    @Test
    @DisplayName("Domínio: score inicial calculado corretamente")
    void testeDominioScoreInicial() {
        assertThat(game.getScore()).isEqualTo(2200);
    }

    @Test
    @DisplayName("Domínio: nível inicial é 1")
    void testeDominioNivelInicial() {
        assertThat(game.getNivel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Domínio: reduzirTempo decrementa o tempo")
    void testeDominioReduzirTempo() {
        int tempo = game.getTempoRestante();

        game.reduzirTempo();

        assertThat(game.getTempoRestante()).isEqualTo(tempo - 1);
    }

    @Test
    @DisplayName("Domínio: mover jogador para direção válida retorna true")
    void testeDominioMoverJogadorValido() {
        assertThat(game.moverJogador("leste")).isTrue();
    }

    @Test
    @DisplayName("Domínio: mover jogador para direção inválida retorna false")
    void testeDominioMoverJogadorInvalido() {
        assertThat(game.moverJogador("norte")).isFalse();
    }

    @Test
    @DisplayName("Domínio: finalizarJogo desativa o jogo")
    void testeDominioFinalizarJogo() {
        game.finalizarJogo(true);

        assertThat(game.isJogoAtivo()).isFalse();
    }

    @Test
    @DisplayName("Domínio: chave inicialmente não está ativa")
    void testeDominioChaveInicialmenteInativa() {
        assertThat(game.isChaveAtiva()).isFalse();
    }

    @Test
    @DisplayName("Domínio: lupa inicialmente invisível")
    void testeDominioLupaInicialmenteInvisivel() {
        assertThat(game.isChaveVisivel()).isFalse();
    }

    @Test
    @DisplayName("Domínio: sala da chave existe")
    void testeDominioSalaDaChaveExiste() {
        assertThat(game.getSalaDaChave()).isNotNull();
    }

    @Test
    @DisplayName("Domínio: missão existe")
    void testeDominioMissaoExiste() {
        assertThat(game.getMissao()).isNotNull();
    }

    @Test
    @DisplayName("Domínio: andar inicial é 1")
    void testeDominioAndarInicial() {
        assertThat(game.getAndarAtual()).isEqualTo(1);
    }

    @Test
    @DisplayName("Domínio: listener recebe evento de tempo")
    void testeDominioEventoTempo() {
        List<PropertyChangeEvent> eventos = new ArrayList<>();
        game.addPropertyChangeListener(eventos::add);

        game.reduzirTempo();

        assertThat(eventos)
                .extracting(PropertyChangeEvent::getPropertyName)
                .contains("tempo");
    }

    @Test
    @DisplayName("Domínio: remover listener impede novos eventos")
    void testeDominioRemoveListener() {
        List<PropertyChangeEvent> eventos = new ArrayList<>();

        var listener = (java.beans.PropertyChangeListener) eventos::add;

        game.addPropertyChangeListener(listener);
        game.removePropertyChangeListener(listener);

        game.reduzirTempo();

        assertThat(eventos).isEmpty();
    }
}