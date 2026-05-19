package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.controller.GameEngine;
import st.project.game.model.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE FRONTEIRA: GameEngine + GameModel ────────────────────────────
 *
 * Cobre: valores-limite e transições críticas do motor de jogo.
 *
 * Fronteiras testadas:
 *   T1. Tempo inicial (120s)
 *   T2. Tempo = 1  → após um reduzirTempo encerra
 *   T3. Tempo = 0  já encerrado (idempotência)
 *   T4. Tempo = 2  → um reduzirTempo ainda mantém jogo ativo (abaixo da fronteira)
 *   M1. Movimentos iniciais (200)
 *   M2. Movimentos = 1 → após mover encerra
 *   M3. Movimentos = 0 → mover retorna false imediatamente
 *   M4. Movimentos = 2 → após mover, jogo ainda ativo (abaixo da fronteira)
 *   N1. Nível = 1 com inventário vazio (inferior)
 *   N2. Nível = 1 só com chave (chave não conta)
 *   S1. Score inicial = 120*10 + 200*5 + 0*100 = 2200
 *   S2. Score: cada item acrescenta exatamente 100
 *   A1. Andar inicial = 1
 *   A2. Andar máximo = 4 (sagrado)
 *   P1. Poção: tempo × 2 (verificação do estado resultante real)
 *   P2. Amuleto: +3 movimentos exatos
 *   R1. Adicionar e remover listener — sem crash
 *   R2. Sem listener → sem NullPointerException
 *   B1. mover com direção em branco ("") → false
 *   B2. mover com null → false sem NullPointerException
 *
 * Dublê: PropertyChangeListener (mock) — dependência de saída do modelo.
 * ────────────────────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameEngine + GameModel – Testes de Fronteira")
class GameEngineFronteiraTest {

    private static final long SEED = 42L;

    private GameModel            model;
    private GameEngine           engine;
    private PropertyChangeListener listener;

    @BeforeEach
    void setUp() {
        model    = new GameModel(SEED);
        listener = mock(PropertyChangeListener.class);
        model.addPropertyChangeListener(listener);
        engine   = new GameEngine(model);
        engine.pausar();
    }

    // ── T1. Tempo inicial ─────────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (T1): tempoRestante inicial é 120")
    void testeFronteiraT1TempoInicial() {
        assertThat(model.getTempoRestante()).isEqualTo(120);
    }

    // ── T2. Tempo = 1 encerra em um tick ─────────────────────────────────

    @Test
    @DisplayName("Fronteira (T2): com tempo=1, um reduzirTempo encerra o jogo")
    void testeFronteiraT2TempoUmEncerra() {
        reduzirAte(1);
        assertThat(model.isJogoAtivo()).isTrue();

        model.reduzirTempo();

        assertThat(model.getTempoRestante()).isZero();
        assertThat(model.isJogoAtivo()).isFalse();
    }

    // ── T4. Tempo = 2 → jogo continua após 1 tick ─────────────────────────

    @Test
    @DisplayName("Fronteira (T4): com tempo=2, um reduzirTempo deixa jogo ativo (tempo=1)")
    void testeFronteiraT4TempoDoisAindaAtivo() {
        reduzirAte(2);

        model.reduzirTempo();

        assertThat(model.getTempoRestante()).isEqualTo(1);
        assertThat(model.isJogoAtivo()).isTrue();
    }

    // ── T3. finalizarJogo idempotente ─────────────────────────────────────

    @Test
    @DisplayName("Fronteira (T3): encerrar jogo quando já encerrado → não dispara gameOver novamente")
    void testeFronteiraT3FinalizarIdempotente() {
        model.finalizarJogo(false);
        reset(listener);

        model.finalizarJogo(false);

        verify(listener, never()).propertyChange(any());
    }

    // ── M1. Movimentos iniciais ───────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (M1): movimentosRestantes inicial é 200")
    void testeFronteiraM1MovimentosIniciais() {
        assertThat(model.getMovimentosRestantes()).isEqualTo(200);
    }

    // ── M2. Último movimento encerra ──────────────────────────────────────

    @Test
    @DisplayName("Fronteira (M2): com movimentos=1, o último mover encerra com derrota")
    void testeFronteiraM2UltimoMovimento() {
        consumirMovimentosAte(1);
        assertThat(model.getMovimentosRestantes()).isEqualTo(1);
        assertThat(model.isJogoAtivo()).isTrue();

        Room atual   = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoNaoBloqueadoNemEscada(atual);
        model.moverJogador(direcaoPara(atual, vizinho));

        assertThat(model.getMovimentosRestantes()).isZero();
        assertThat(model.isJogoAtivo()).isFalse();
        verificarGameOver(false);
    }

    // ── M4. Movimentos = 2 → jogo continua após 1 movimento ───────────────

    @Test
    @DisplayName("Fronteira (M4): com movimentos=2, um mover deixa jogo ativo (movimentos=1)")
    void testeFronteiraM4MovimentosDoisAindaAtivo() {
        consumirMovimentosAte(2);

        Room atual   = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoNaoBloqueadoNemEscada(atual);
        model.moverJogador(direcaoPara(atual, vizinho));

        assertThat(model.getMovimentosRestantes()).isEqualTo(1);
        assertThat(model.isJogoAtivo()).isTrue();
    }

    // ── M3. Zero movimentos antes de mover ────────────────────────────────

    @Test
    @DisplayName("Fronteira (M3): com movimentos=0, moverJogador retorna false sem evento")
    void testeFronteiraM3ZeroMovimentos() {
        consumirMovimentosAte(0);
        reset(listener);

        boolean moveu = model.moverJogador("leste");

        assertThat(moveu).isFalse();
        verify(listener, never()).propertyChange(any());
    }

    // ── N1 / N2. Nível com/sem chave ──────────────────────────────────────

    @Test
    @DisplayName("Fronteira (N1): nível com inventário vazio = 1 (limite inferior)")
    void testeFronteiraN1NivelMinimo() {
        assertThat(model.getNivel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fronteira (N2): somente chave no inventário → nível ainda é 1")
    void testeFronteiraN2SomenteChaveNivelUm() {
        model.getJogador().adicionarItem(new Item("Chave", Item.Type.CHAVE, "Abre"));
        assertThat(model.getNivel()).isEqualTo(1);
    }

    // ── S1. Score inicial ─────────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (S1): score inicial = 120×10 + 200×5 + 0×100 = 2200")
    void testeFronteiraS1ScoreInicial() {
        assertThat(model.getScore()).isEqualTo(2200);
    }

    // ── S2. Score aumenta com itens ──────────────────────────────────────

    @Test
    @DisplayName("Fronteira (S2): cada item no inventário acrescenta 100 ao score")
    void testeFronteiraS2ScoreComItem() {
        int antes = model.getScore();
        darItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        assertThat(model.getScore()).isEqualTo(antes + 100);
    }

    // ── A1. Andar inicial ─────────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (A1): andar inicial = 1 (entrada está no andar 1)")
    void testeFronteiraA1AndarInicial() {
        assertThat(model.getAndarAtual()).isEqualTo(1);
    }

    // ── A2. Andar 4 é o máximo ────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira (A2): sagrado está no andar 4 (limite superior)")
    void testeFronteiraA2AndarMaximo() {
        assertThat(model.getSalas().get("sagrado").getAndar()).isEqualTo(4);
    }

    // ── P1. Poção dobra tempo — verificação do estado resultante ──────────

    @Test
    @DisplayName("Fronteira (P1): coletar poção de velocidade dobra o tempo resultante")
    void testeFronteiraP1PocaoDobraTempoResultante() {
        darItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        Room atual = model.getJogador().getPosicaoAtual();
        atual.getItems().clear();
        atual.adicionarItem(new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2"));

        int tempoBefore = model.getTempoRestante();
        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        model.moverJogador(direcaoPara(atual, vizinho)); // -1 mov
        model.moverJogador(direcaoPara(vizinho, atual)); // -1 mov, coleta poção: tempo*2

        // Timer pausado → tempo só mudou pela poção
        assertThat(model.getTempoRestante()).isEqualTo(tempoBefore * 2);
    }

    // ── P2. Amuleto acrescenta exatamente 3 ──────────────────────────────

    @Test
    @DisplayName("Fronteira (P2): amuleto de movimentos acrescenta exatamente 3 ao contador")
    void testeFronteiraP2AmuletoAcrescenta3() {
        darItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        int movBefore = model.getMovimentosRestantes();

        Room atual = model.getJogador().getPosicaoAtual();
        atual.getItems().clear();
        atual.adicionarItem(new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3"));

        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        model.moverJogador(direcaoPara(atual, vizinho)); // -1
        model.moverJogador(direcaoPara(vizinho, atual)); // -1, coleta: +3

        assertThat(model.getMovimentosRestantes()).isEqualTo(movBefore - 2 + 3);
    }

    // ── R1. Adicionar/remover listener ────────────────────────────────────

    @Test
    @DisplayName("Fronteira (R1): remover listener e reaplicar → sem evento fantasma")
    void testeFronteiraR1ListenerRemoverEReadicionar() {
        model.removePropertyChangeListener(listener);
        reset(listener);

        model.reduzirTempo();
        verify(listener, never()).propertyChange(any());

        model.addPropertyChangeListener(listener);
        model.reduzirTempo();
        verify(listener, atLeastOnce()).propertyChange(any());
    }

    // ── R2. Sem listener — sem NullPointerException ────────────────────────

    @Test
    @DisplayName("Fronteira (R2): modelo sem listener não lança NullPointerException ao operar")
    void testeFronteiraR2SemListenerNaoExplode() {
        GameModel m = new GameModel(SEED);
        assertDoesNotThrow(() -> {
            Room atual   = m.getJogador().getPosicaoAtual();
            Room vizinho = primeiroVizinhoNaoBloqueado(atual);
            m.moverJogador(direcaoPara(atual, vizinho));
            m.reduzirTempo();
            m.finalizarJogo(false);
        });
    }

    // ── B1 / B2. Direções inválidas / null ────────────────────────────────

    @Test
    @DisplayName("Fronteira (B1): mover com string vazia retorna false")
    void testeFronteiraB1DirecaoVazia() {
        assertThat(model.moverJogador("")).isFalse();
    }

    @Test
    @DisplayName("Fronteira (B2): mover com null retorna false sem NullPointerException")
    void testeFronteiraB2DirecaoNull() {
        assertDoesNotThrow(() ->
                assertThat(model.moverJogador(null)).isFalse()
        );
    }

    // ── Propriedades paramétricas ─────────────────────────────────────────

    @ParameterizedTest(name = "score com {0} itens não-CHAVE = base + {0}×100")
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("Propriedade: score(n itens) = base + n×100")
    void testeFronteiraScorePropriedade(int nItens) {
        int base = model.getScore();
        for (int i = 0; i < nItens; i++) {
            darItem(new Item("Lupa" + i, Item.Type.LUPA, "Revela"));
        }
        assertThat(model.getScore()).isEqualTo(base + nItens * 100);
    }

    @ParameterizedTest(name = "nível com {0} itens não-CHAVE = 1 + {0}")
    @ValueSource(ints = {0, 1, 2, 3, 4})
    @DisplayName("Propriedade: nivel(n itens não-CHAVE) = 1 + n")
    void testeFronteiraNivelPropriedade(int nItens) {
        for (int i = 0; i < nItens; i++) {
            darItem(new Item("Lupa" + i, Item.Type.LUPA, "Revela"));
        }
        assertThat(model.getNivel()).isEqualTo(1 + nItens);
    }

    // ── Transição: evento 'nivel' ao mudar de nível ───────────────────────

    @Test
    @DisplayName("Fronteira: evento 'nivel' é disparado quando nível muda após movimento")
    void testeFronteiraEventoNivelDisparadoAoMudar() {
        // Coloca LUPA na sala vizinha para que nível suba ao coletar
        Room atual   = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        vizinho.adicionarItem(new Item("Lupa", Item.Type.LUPA, "Revela"));

        model.moverJogador(direcaoPara(atual, vizinho));

        verificarEventoNivel();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utilitários
    // ═══════════════════════════════════════════════════════════════════════

    private void darItem(Item item) {
        model.getJogador().adicionarItem(item);
    }

    private Room primeiroVizinhoNaoBloqueado(Room sala) {
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            Room v = sala.getVizinho(d);
            if (v != null && !v.isBloqueada()) return v;
        }
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            Room v = sala.getVizinho(d);
            if (v != null) return v;
        }
        throw new IllegalStateException("Sem vizinhos");
    }

    private Room primeiroVizinhoNaoBloqueadoNemEscada(Room sala) {
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            Room v = sala.getVizinho(d);
            if (v != null && !v.isBloqueada() && !v.isEscada()) return v;
        }
        return primeiroVizinhoNaoBloqueado(sala);
    }

    private String direcaoPara(Room origem, Room destino) {
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            if (destino.equals(origem.getVizinho(d))) return d;
        }
        throw new IllegalArgumentException("Não é vizinho");
    }

    private void reduzirAte(int alvo) {
        while (model.getTempoRestante() > alvo && model.isJogoAtivo()) {
            model.reduzirTempo();
        }
    }

    private void consumirMovimentosAte(int alvo) {
        while (model.getMovimentosRestantes() > alvo && model.isJogoAtivo()) {
            Room a = model.getJogador().getPosicaoAtual();
            Room v = primeiroVizinhoNaoBloqueadoNemEscada(a);
            model.moverJogador(direcaoPara(a, v));
            if (model.getMovimentosRestantes() > alvo && model.isJogoAtivo()) {
                model.moverJogador(direcaoPara(v, a));
            }
        }
    }

    private void verificarGameOver(boolean esperado) {
        ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(listener, atLeastOnce()).propertyChange(captor.capture());
        boolean encontrado = captor.getAllValues().stream()
                .filter(e -> "gameOver".equals(e.getPropertyName()))
                .anyMatch(e -> Boolean.valueOf(esperado).equals(e.getNewValue()));
        assertThat(encontrado).as("gameOver=" + esperado).isTrue();
    }

    private void verificarEventoNivel() {
        ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(listener, atLeastOnce()).propertyChange(captor.capture());
        boolean encontrado = captor.getAllValues().stream()
                .anyMatch(e -> "nivel".equals(e.getPropertyName()));
        assertThat(encontrado).as("Esperava evento 'nivel'").isTrue();
    }
}