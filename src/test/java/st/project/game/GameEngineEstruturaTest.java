package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.controller.GameEngine;
import st.project.game.model.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES ESTRUTURAIS: GameEngine + GameModel ─────────────────────────────
 *
 * Foco: cobertura MC/DC das decisões internas do motor e do modelo.
 *
 * Decisões MC/DC cobertas:
 *   (A) moverJogador: !jogoAtivo          → false (jogo já encerrado)
 *   (B) moverJogador: movimentos <= 0     → false (sem movimentos)
 *   (C) moverJogador: destino == null     → false (sem vizinho)
 *   (D) moverJogador: moveu && missao ok  → finalizarJogo(true)
 *   (E) moverJogador: moveu && mov == 0   → finalizarJogo(false)
 *   (F) moverJogador: moveu && continua   → decrementa e dispara eventos
 *   (G) reduzirTempo: !jogoAtivo          → retorna sem decrementar
 *   (H) reduzirTempo: tempo <= 0          → finalizarJogo(false)
 *   (I) coletarItens: LUPA presente       → sempre coletável
 *   (J) coletarItens: outro item sem lupa → ignorado (invisível)
 *   (K) coletarItens: outro item com lupa → coletado e efeito aplicado
 *   (L) engine.encerrarJogo: retomar após encerrado → timer não reinicia
 *   (M) Seed fixa → mapa determinístico
 *   (N) Estrutura de 4 andares: 100 salas (4 × 25)
 *   (O) Cada andar tem escada nos cantos corretos
 *   (P) Alçapão: mover para sagrado sem chave → alçapão + posição na entrada
 *   (Q) Alçapão: evento "alcapao" disparado
 *
 * Dublê: PropertyChangeListener (mock) — dependência de saída do modelo.
 * ────────────────────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameEngine + GameModel – Testes Estruturais (MC/DC)")
class GameEngineEstruturaTest {

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

    // ── (A) moverJogador quando jogo inativo ──────────────────────────────

    @Test
    @DisplayName("Estrutura (A): moverJogador com jogo inativo → false, sem evento de movimentos")
    void testeEstruturaAMoverComJogoInativo() {
        model.finalizarJogo(false);
        reset(listener);

        boolean moveu = model.moverJogador("leste");

        assertThat(moveu).isFalse();
        verify(listener, never()).propertyChange(argThat(e -> "movimentos".equals(e.getPropertyName())));
    }

    // ── (B) movimentos <= 0 ───────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (B): moverJogador com movimentos=0 → false")
    void testeEstruturaBMoverSemMovimentos() {
        esvaziarMovimentos();
        boolean moveu = model.moverJogador("leste");
        assertThat(moveu).isFalse();
    }

    // ── (C) destino null ──────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (C): mover em direção sem vizinho → false")
    void testeEstruturaCMoverSemVizinho() {
        // Entrada está em (0,0) — sem norte nem oeste
        assertThat(model.moverJogador("norte")).isFalse();
    }

    // ── (D) missão concluída encerra com vitória ──────────────────────────

    @Test
    @DisplayName("Estrutura (D): ao concluir missão após movimento → finalizarJogo(true)")
    void testeEstruturaDMissaoConcluida() {
        darItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        darItem(new Item("Chave", Item.Type.CHAVE, "Abre"));

        Room sagrado = model.getSalas().get("sagrado");
        Room vizinha = primeiroVizinho(sagrado);
        moverDireto(vizinha);

        model.moverJogador(direcaoPara(vizinha, sagrado));

        assertThat(model.isJogoAtivo()).isFalse();
        verificarGameOver(true);
    }

    // ── (E) movimentos zerados após movimento encerra com derrota ─────────

    @Test
    @DisplayName("Estrutura (E): último movimento quando movimentos=1 → finalizarJogo(false)")
    void testeEstruturaEUltimoMovimento() {
        while (model.getMovimentosRestantes() > 1) {
            oscilate();
        }
        assertThat(model.getMovimentosRestantes()).isEqualTo(1);

        Room atual   = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        model.moverJogador(direcaoPara(atual, vizinho));

        assertThat(model.isJogoAtivo()).isFalse();
        verificarGameOver(false);
    }

    // ── (F) movimento normal: eventos disparados ──────────────────────────

    @Test
    @DisplayName("Estrutura (F): movimento válido dispara eventos 'movimentos' e 'score'")
    void testeEstruturaFEventosMovimento() {
        Room atual   = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        model.moverJogador(direcaoPara(atual, vizinho));

        ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(listener, atLeastOnce()).propertyChange(captor.capture());

        List<String> nomes = captor.getAllValues().stream()
                .map(PropertyChangeEvent::getPropertyName).toList();
        assertThat(nomes).contains("movimentos", "score");
    }

    // ── (G) reduzirTempo quando jogo inativo ─────────────────────────────

    @Test
    @DisplayName("Estrutura (G): reduzirTempo com jogo inativo → tempo não muda e sem evento")
    void testeEstruturaGReduzirTempoInativo() {
        model.finalizarJogo(false);
        int antes = model.getTempoRestante();
        reset(listener);

        model.reduzirTempo();

        assertThat(model.getTempoRestante()).isEqualTo(antes);
        verify(listener, never()).propertyChange(argThat(e -> "tempo".equals(e.getPropertyName())));
    }

    // ── (H) tempo zera → encerra ──────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (H): ao reduzir tempo até 0 → finalizarJogo(false)")
    void testeEstruturaHTempoZeraEncerra() {
        int t = model.getTempoRestante();
        for (int i = 0; i < t; i++) model.reduzirTempo();

        assertThat(model.isJogoAtivo()).isFalse();
        verificarGameOver(false);
    }

    // ── (I) LUPA sempre visível / coletável ───────────────────────────────

    @Test
    @DisplayName("Estrutura (I): LUPA na sala é coletada mesmo sem lupa no inventário")
    void testeEstruturaILupaSempreColetavel() {
        Room atual = model.getJogador().getPosicaoAtual();
        Item lupa  = new Item("Lupa", Item.Type.LUPA, "Revela");
        atual.adicionarItem(lupa);

        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        model.moverJogador(direcaoPara(atual, vizinho));
        model.moverJogador(direcaoPara(vizinho, atual));

        assertThat(model.getJogador().possuiItem(Item.Type.LUPA)).isTrue();
    }

    // ── (J) item não-LUPA sem lupa no inventário → ignorado ──────────────

    @Test
    @DisplayName("Estrutura (J): Chave na sala sem lupa no inventário → NÃO coletada")
    void testeEstruturaJChaveSemLupaNaoColetada() {
        Room atual = model.getJogador().getPosicaoAtual();
        Item chave = new Item("Chave", Item.Type.CHAVE, "Abre");
        atual.adicionarItem(chave);

        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        model.moverJogador(direcaoPara(atual, vizinho));
        model.moverJogador(direcaoPara(vizinho, atual));

        assertThat(model.getJogador().possuiItem(Item.Type.CHAVE)).isFalse();
        assertThat(atual.getItems()).contains(chave);
    }

    // ── (K) item não-LUPA com lupa no inventário → coletado ──────────────

    @Test
    @DisplayName("Estrutura (K): Chave na sala com lupa no inventário → coletada")
    void testeEstruturaKChaveComLupaColetada() {
        darItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        Room atual = model.getJogador().getPosicaoAtual();
        atual.adicionarItem(new Item("Chave", Item.Type.CHAVE, "Abre"));

        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        model.moverJogador(direcaoPara(atual, vizinho));
        model.moverJogador(direcaoPara(vizinho, atual));

        assertThat(model.getJogador().possuiItem(Item.Type.CHAVE)).isTrue();
    }

    // ── (L) engine encerrado não reinicia timer ───────────────────────────

    @Test
    @DisplayName("Estrutura (L): retomar após encerrarJogo → isJogoEncerrado permanece true")
    void testeEstruturaLRetomarAposEncerrar() {
        engine.encerrarJogo();
        assertThat(engine.isJogoEncerrado()).isTrue();

        engine.retomar(); // não deve reiniciar

        assertThat(engine.isJogoEncerrado()).isTrue();
    }

    // ── (M) Seed fixa → mapa determinístico ──────────────────────────────

    @Test
    @DisplayName("Estrutura (M): mesma seed produz mesmo mapa (salas e posições idênticas)")
    void testeEstruturaMMapaDeterministico() {
        GameModel model2 = new GameModel(SEED);

        Map<String, Room> salas1 = model.getSalas();
        Map<String, Room> salas2 = model2.getSalas();

        assertThat(salas1.keySet()).isEqualTo(salas2.keySet());
        for (String nome : salas1.keySet()) {
            Room r1 = salas1.get(nome);
            Room r2 = salas2.get(nome);
            assertThat(r1.getX()).isEqualTo(r2.getX());
            assertThat(r1.getY()).isEqualTo(r2.getY());
            assertThat(r1.getAndar()).isEqualTo(r2.getAndar());
        }
    }

    // ── (N) Estrutura de 4 andares ────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (N): cada andar tem exatamente 25 salas")
    void testeEstruturaNAndaresTem25Salas() {
        for (int andar = 1; andar <= 4; andar++) {
            final int a = andar;
            long count = model.getSalas().values().stream()
                    .filter(r -> r.getAndar() == a).count();
            assertThat(count).as("Andar %d deve ter 25 salas", a).isEqualTo(25);
        }
    }

    // ── (O) Escadas nos cantos certos ─────────────────────────────────────

    @Test
    @DisplayName("Estrutura (O): escada_cima_1 está no andar 1, col=4, row=0")
    void testeEstruturaOEscadaCima1Posicao() {
        Room escada = model.getSalas().get("escada_cima_1");
        assertThat(escada).isNotNull();
        assertThat(escada.getAndar()).isEqualTo(1);
        assertThat(escada.getX()).isEqualTo(4);
        assertThat(escada.getY()).isEqualTo(0);
        assertThat(escada.isEscadaCima()).isTrue();
    }

    @Test
    @DisplayName("Estrutura (O'): escada_baixo_2 está no andar 2, col=0, row=4")
    void testeEstruturaOEscadaBaixo2Posicao() {
        Room escada = model.getSalas().get("escada_baixo_2");
        assertThat(escada).isNotNull();
        assertThat(escada.getAndar()).isEqualTo(2);
        assertThat(escada.getX()).isEqualTo(0);
        assertThat(escada.getY()).isEqualTo(4);
        assertThat(escada.isEscadaBaixo()).isTrue();
    }

    // ── (P) Alçapão: jogador vai para entrada sem chave ───────────────────

    @Test
    @DisplayName("Estrutura (P): mover para sagrado sem chave → jogador vai para 'entrada'")
    void testeEstruturaPAlcapaoMoveparaEntrada() {
        Room sagrado = model.getSalas().get("sagrado");
        Room vizinha = primeiroVizinho(sagrado);
        moverDireto(vizinha);

        model.moverJogador(direcaoPara(vizinha, sagrado));

        assertThat(model.getJogador().getPosicaoAtual().getNome()).isEqualTo("entrada");
    }

    // ── (Q) Evento "alcapao" disparado ────────────────────────────────────

    @Test
    @DisplayName("Estrutura (Q): mover para sagrado sem chave dispara evento 'alcapao'")
    void testeEstruturaQAlcapaoDispara_Evento() {
        Room sagrado = model.getSalas().get("sagrado");
        Room vizinha = primeiroVizinho(sagrado);
        moverDireto(vizinha);

        model.moverJogador(direcaoPara(vizinha, sagrado));

        ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(listener, atLeastOnce()).propertyChange(captor.capture());
        boolean encontrado = captor.getAllValues().stream()
                .anyMatch(e -> "alcapao".equals(e.getPropertyName()));
        assertThat(encontrado).as("Esperava evento 'alcapao'").isTrue();
    }

    @Test
    @DisplayName("Estrutura: entrada (0,0) não tem vizinho norte nem oeste")
    void testeEstruturaEntradaSemNorteOeste() {
        Room entrada = model.getSalas().get("entrada");
        assertThat(entrada.getVizinho("norte")).isNull();
        assertThat(entrada.getVizinho("oeste")).isNull();
    }

    @Test
    @DisplayName("Estrutura: entrada (0,0) tem vizinho leste e sul")
    void testeEstruturaEntradaTemLesteESul() {
        Room entrada = model.getSalas().get("entrada");
        assertThat(entrada.getVizinho("leste")).isNotNull();
        assertThat(entrada.getVizinho("sul")).isNotNull();
    }

    @Test
    @DisplayName("Estrutura: borda norte (y=0) sem vizinho norte em qualquer andar")
    void testeEstruturaBordaNorteSemVizinho() {
        model.getSalas().values().stream()
                .filter(r -> r.getY() == 0)
                .forEach(r -> assertThat(r.getVizinho("norte")).isNull());
    }

    @Test
    @DisplayName("Estrutura: borda leste (x=4) sem vizinho leste em qualquer andar")
    void testeEstruturaBordaLesteSemVizinho() {
        model.getSalas().values().stream()
                .filter(r -> r.getX() == 4)
                .forEach(r -> assertThat(r.getVizinho("leste")).isNull());
    }

    @Test
    @DisplayName("Estrutura: removePropertyChangeListener impede recebimento de eventos")
    void testeEstruturaRemoverListener() {
        model.removePropertyChangeListener(listener);
        reset(listener);

        Room atual = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoNaoBloqueado(atual);

        model.moverJogador(direcaoPara(atual, vizinho));
        model.reduzirTempo();

        verify(listener, never()).propertyChange(any());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utilitários
    // ═══════════════════════════════════════════════════════════════════════

    private void darItem(Item item) {
        model.getJogador().adicionarItem(item);
    }

    private void moverDireto(Room destino) {
        model.getJogador().moverPara(destino);
    }

    private Room primeiroVizinho(Room sala) {
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            Room v = sala.getVizinho(d);
            if (v != null) return v;
        }
        throw new IllegalStateException("Sala sem vizinhos: " + sala.getNome());
    }

    private Room primeiroVizinhoNaoBloqueado(Room sala) {
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            Room v = sala.getVizinho(d);
            if (v != null && !v.isBloqueada()) return v;
        }
        return primeiroVizinho(sala);
    }

    private String direcaoPara(Room origem, Room destino) {
        for (String d : List.of("norte", "sul", "leste", "oeste")) {
            if (destino.equals(origem.getVizinho(d))) return d;
        }
        throw new IllegalArgumentException("Não é vizinho");
    }

    private void esvaziarMovimentos() {
        while (model.getMovimentosRestantes() > 0 && model.isJogoAtivo()) {
            Room a = model.getJogador().getPosicaoAtual();
            Room v = primeiroVizinhoNaoBloqueado(a);
            model.moverJogador(direcaoPara(a, v));
            if (model.getMovimentosRestantes() > 0 && model.isJogoAtivo()) {
                model.moverJogador(direcaoPara(v, a));
            }
        }
    }

    private void oscilate() {
        if (!model.isJogoAtivo() || model.getMovimentosRestantes() <= 1) return;
        Room a = model.getJogador().getPosicaoAtual();
        Room v = primeiroVizinhoNaoBloqueado(a);
        model.moverJogador(direcaoPara(a, v));
        if (model.isJogoAtivo() && model.getMovimentosRestantes() > 1) {
            model.moverJogador(direcaoPara(v, a));
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


}



//
//
//private void darItem(Item item) {
//    model.getJogador().adicionarItem(item);
//}
//
//private void moverDireto(Room destino) {
//    model.getJogador().moverPara(destino);
//}
//
//private Room primeiroVizinho(Room sala) {
//    for (String d : List.of("norte", "sul", "leste", "oeste")) {
//        Room v = sala.getVizinho(d);
//        if (v != null) return v;
//    }
//    throw new IllegalStateException("Sem vizinhos: " + sala.getNome());
//}
//
//private Room primeiroVizinhoNaoBloqueado(Room sala) {
//    for (String d : List.of("norte", "sul", "leste", "oeste")) {
//        Room v = sala.getVizinho(d);
//        if (v != null && !v.isBloqueada()) return v;
//    }
//    return primeiroVizinho(sala);
//}
//
//private String direcaoPara(Room origem, Room destino) {
//    for (String d : List.of("norte", "sul", "leste", "oeste")) {
//        if (destino.equals(origem.getVizinho(d))) return d;
//    }
//    throw new IllegalArgumentException("Não é vizinho");
//}
//
//private void oscilate() {
//    if (!model.isJogoAtivo()) return;
//    Room a = model.getJogador().getPosicaoAtual();
//    Room v = primeiroVizinhoNaoBloqueado(a);
//    if (!model.isJogoAtivo()) return;
//    model.moverJogador(direcaoPara(a, v));
//    if (model.isJogoAtivo() && model.getMovimentosRestantes() > 1) {
//        model.moverJogador(direcaoPara(v, a));
//    }
//}
//
//private void esvaziarMovimentos() {
//    while (model.getMovimentosRestantes() > 0 && model.isJogoAtivo()) {
//        oscilate();
//    }
//    // Se jogo encerrou antes de zerar, não importa
//}
//
//private Room buscarSalaPorAndarXY(int andar, int x, int y) {
//    return model.getSalas().values().stream()
//            .filter(r -> r.getAndar() == andar && r.getX() == x && r.getY() == y)
//            .findFirst().orElse(null);
//}
//
//private void verificarGameOver(boolean esperado) {
//    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
//    verify(listener, atLeastOnce()).propertyChange(captor.capture());
//    boolean encontrado = captor.getAllValues().stream()
//            .filter(e -> "gameOver".equals(e.getPropertyName()))
//            .anyMatch(e -> Boolean.valueOf(esperado).equals(e.getNewValue()));
//    assertThat(encontrado).as("gameOver=" + esperado).isTrue();
//}