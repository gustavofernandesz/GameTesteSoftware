package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.controller.GameEngine;
import st.project.game.model.GameModel;
import st.project.game.model.Item;
import st.project.game.model.Room;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

/**
 * ─── TESTES: GameEngine + GameModel sem PropertyChangeListener ───────────────
 *
 * Escopo: verificar que o motor de jogo funciona completamente sem nenhum
 * listener registrado — sem NullPointerException, sem estado corrompido.
 *
 * Dublê de teste: nenhum — testamos o comportamento real sem mock.
 * Aleatoriedade: seed fixa (42L) para mapa determinístico.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("GameEngine + GameModel – Sem PropertyChangeListener (null-safety)")
class GameEngineNullListenerTest {

    private static final long SEED = 42L;

    private GameModel  model;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        // Nenhum listener adicionado propositalmente
        model  = new GameModel(SEED);
        engine = new GameEngine(model);
        engine.pausar();
    }

    // ── Movimento sem listener ────────────────────────────────────────────

    @Test
    @DisplayName("NullSafety: mover em direção válida não lança exceção")
    void testeSemListenerMoverValido() {
        Room atual   = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        String dir   = direcaoPara(atual, vizinho);

        assertDoesNotThrow(() -> {
            boolean moveu = engine.mover(dir);
            assertThat(moveu).isTrue();
            assertThat(model.getJogador().getPosicaoAtual()).isEqualTo(vizinho);
        });
    }

    @Test
    @DisplayName("NullSafety: mover em direção inválida não lança exceção")
    void testeSemListenerMoverInvalido() {
        assertDoesNotThrow(() -> {
            boolean moveu = engine.mover("direcaoInexistente");
            assertThat(moveu).isFalse();
        });
    }

    // ── Tempo sem listener ────────────────────────────────────────────────

    @Test
    @DisplayName("NullSafety: reduzirTempo não lança exceção e decrementa normalmente")
    void testeSemListenerReduzirTempo() {
        int antes = model.getTempoRestante();

        assertDoesNotThrow(() -> model.reduzirTempo());

        assertThat(model.getTempoRestante()).isEqualTo(antes - 1);
    }

    @Test
    @DisplayName("NullSafety: reduzirTempo até zero encerra jogo sem exceção")
    void testeSemListenerTempoZeradoEncerra() {
        assertDoesNotThrow(() -> {
            int t = model.getTempoRestante();
            for (int i = 0; i < t && model.isJogoAtivo(); i++) {
                model.reduzirTempo();
            }
        });
        assertThat(model.isJogoAtivo()).isFalse();
    }

    // ── Coleta de itens sem listener ──────────────────────────────────────

    @Test
    @DisplayName("NullSafety: coletar LUPA sem listener não lança exceção")
    void testeSemListenerColetarLupa() {
        Room atual = model.getJogador().getPosicaoAtual();
        atual.getItems().clear();
        atual.adicionarItem(new Item("Lupa", Item.Type.LUPA, "Revela"));

        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        assertDoesNotThrow(() -> {
            model.moverJogador(direcaoPara(atual, vizinho));
            model.moverJogador(direcaoPara(vizinho, atual));
        });
        assertThat(model.getJogador().possuiItem(Item.Type.LUPA)).isTrue();
    }

    @Test
    @DisplayName("NullSafety: coletar poção sem listener dobra tempo sem exceção")
    void testeSemListenerColetarPocao() {
        model.getJogador().adicionarItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        Room atual = model.getJogador().getPosicaoAtual();
        atual.getItems().clear();
        atual.adicionarItem(new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2"));

        int tempoBefore = model.getTempoRestante();
        Room vizinho = primeiroVizinhoNaoBloqueado(atual);

        assertDoesNotThrow(() -> {
            model.moverJogador(direcaoPara(atual, vizinho));
            model.moverJogador(direcaoPara(vizinho, atual));
        });
        assertThat(model.getTempoRestante()).isGreaterThan(tempoBefore);
    }

    @Test
    @DisplayName("NullSafety: coletar amuleto sem listener acrescenta 3 movimentos sem exceção")
    void testeSemListenerColetarAmuleto() {
        model.getJogador().adicionarItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        Room atual = model.getJogador().getPosicaoAtual();
        atual.getItems().clear();
        atual.adicionarItem(new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3"));

        int movBefore = model.getMovimentosRestantes();
        Room vizinho = primeiroVizinhoNaoBloqueado(atual);

        assertDoesNotThrow(() -> {
            model.moverJogador(direcaoPara(atual, vizinho));
            model.moverJogador(direcaoPara(vizinho, atual));
        });
        // −2 movimentos + 3 do amuleto = movBefore + 1
        assertThat(model.getMovimentosRestantes()).isEqualTo(movBefore - 2 + 3);
    }

    // ── Encerramento sem listener ─────────────────────────────────────────

    @Test
    @DisplayName("NullSafety: finalizarJogo(true) sem listener não lança exceção")
    void testeSemListenerFinalizarJogoVitoria() {
        assertDoesNotThrow(() -> model.finalizarJogo(true));
        assertThat(model.isJogoAtivo()).isFalse();
    }

    @Test
    @DisplayName("NullSafety: finalizarJogo(false) sem listener não lança exceção")
    void testeSemListenerFinalizarJogoDerrota() {
        assertDoesNotThrow(() -> model.finalizarJogo(false));
        assertThat(model.isJogoAtivo()).isFalse();
    }

    @Test
    @DisplayName("NullSafety: esgotar movimentos sem listener encerra jogo sem exceção")
    void testeSemListenerMovimentosEsgotados() {
        // Força movimentos = 1 e faz o último movimento
        while (model.getMovimentosRestantes() > 1 && model.isJogoAtivo()) {
            Room a = model.getJogador().getPosicaoAtual();
            Room v = primeiroVizinhoNaoBloqueadoNemEscada(a);
            model.moverJogador(direcaoPara(a, v));
            if (model.getMovimentosRestantes() > 1 && model.isJogoAtivo()) {
                model.moverJogador(direcaoPara(v, a));
            }
        }
        if (model.isJogoAtivo()) {
            Room a = model.getJogador().getPosicaoAtual();
            Room v = primeiroVizinhoNaoBloqueadoNemEscada(a);
            assertDoesNotThrow(() -> model.moverJogador(direcaoPara(a, v)));
            assertThat(model.isJogoAtivo()).isFalse();
        }
    }

    // ── Ciclo engine sem listener ─────────────────────────────────────────

    @Test
    @DisplayName("NullSafety: pausar e retomar engine sem listener não lança exceção")
    void testeSemListenerPausarRetomar() {
        assertDoesNotThrow(() -> {
            engine.pausar();
            engine.retomar();
            engine.pausar();
        });
    }

    @Test
    @DisplayName("NullSafety: encerrarJogo sem listener não lança exceção")
    void testeSemListenerEncerrarJogo() {
        assertDoesNotThrow(() -> engine.encerrarJogo());
        assertThat(engine.isJogoEncerrado()).isTrue();
    }

    // ── Adicionar/remover listener em runtime ─────────────────────────────

    @Test
    @DisplayName("NullSafety: adicionar listener após construção e receber evento")
    void testeSemListenerAdicionarDepoisDaConstrucao() {
        java.beans.PropertyChangeListener l = mock(java.beans.PropertyChangeListener.class);
        model.addPropertyChangeListener(l);

        model.reduzirTempo();

        org.mockito.Mockito.verify(l, org.mockito.Mockito.atLeastOnce())
                .propertyChange(org.mockito.Mockito.any());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utilitários
    // ═══════════════════════════════════════════════════════════════════════

    private Room primeiroVizinhoNaoBloqueado(Room sala) {
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            Room v = sala.getVizinho(d);
            if (v != null && !v.isBloqueada()) return v;
        }
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            Room v = sala.getVizinho(d);
            if (v != null) return v;
        }
        throw new IllegalStateException("Sem vizinhos: " + sala.getNome());
    }

    private Room primeiroVizinhoNaoBloqueadoNemEscada(Room sala) {
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            Room v = sala.getVizinho(d);
            if (v != null && !v.isBloqueada() && !v.isEscada()) return v;
        }
        return primeiroVizinhoNaoBloqueado(sala); // fallback
    }

    private String direcaoPara(Room origem, Room destino) {
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            if (destino.equals(origem.getVizinho(d))) return d;
        }
        throw new IllegalArgumentException("Não é vizinho");
    }
}