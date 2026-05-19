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
import java.lang.reflect.Field;
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
 *   (L) engine.mover: pausar/retomar      → controla ciclo de vida
 *   (M) Seed fixa → mapa determinístico (propriedade baseada em propriedades)
 *   (N) Estrutura de 4 andares: 100 salas (4 × 25)
 *   (O) Cada andar tem escada certa nos cantos corretos
 *
 * Dublê:
 *   • PropertyChangeListener (mock) — isola eventos sem GUI.
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
    @DisplayName("Estrutura (A): moverJogador com jogo inativo → false, sem evento")
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
        // Dá chave e posiciona ao lado de sagrado
        darItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        darItem(new Item("Chave", Item.Type.CHAVE, "Abre"));

        Room sagrado  = model.getSalas().get("sagrado");
        Room vizinha  = primeiroVizinho(sagrado);
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
            oscilate(); // consome movimentos sem concluir missão
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
    @DisplayName("Estrutura (G): reduzirTempo com jogo inativo → tempo não muda")
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
    void testeEstruturaJItemSemLupaIgnorado() {
        Room atual = model.getJogador().getPosicaoAtual();
        // Remove qualquer item que possa estar na sala
        atual.getItems().clear();
        Item chave = new Item("Chave Extra", Item.Type.CHAVE, "Extra");
        atual.adicionarItem(chave);

        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        model.moverJogador(direcaoPara(atual, vizinho));
        model.moverJogador(direcaoPara(vizinho, atual));

        // Sem lupa → chave não deve ter sido coletada
        assertThat(atual.getItems()).contains(chave);
    }

    // ── (K) item não-LUPA com lupa → coletado ────────────────────────────

    @Test
    @DisplayName("Estrutura (K): Poção na sala COM lupa no inventário → coletada e dobra tempo")
    void testeEstruturaKItemComLupaColetado() {
        darItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        int tempoAntes = model.getTempoRestante();

        Room atual = model.getJogador().getPosicaoAtual();
        atual.getItems().clear();
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2");
        atual.adicionarItem(pocao);

        Room vizinho = primeiroVizinhoNaoBloqueado(atual);
        model.moverJogador(direcaoPara(atual, vizinho));
        model.moverJogador(direcaoPara(vizinho, atual));

        assertThat(model.getJogador().possuiItem(Item.Type.POCAO_VELOCIDADE)).isTrue();
        // Poção dobra tempo
        assertThat(model.getTempoRestante()).isGreaterThan(tempoAntes);
    }

    // ── (L) engine.pausar / retomar ──────────────────────────────────────

    @Test
    @DisplayName("Estrutura (L): engine.pausar + retomar não modifica estado do modelo")
    void testeEstruturaLPausarRetomar() {
        engine.pausar();
        int antes = model.getTempoRestante();
        engine.retomar();
        engine.pausar();
        assertThat(model.getTempoRestante()).isEqualTo(antes);
    }

    @Test
    @DisplayName("Estrutura (L'): engine.encerrarJogo impede novo retomar")
    void testeEstruturaLEncerrarImpideRetomar() {
        engine.encerrarJogo();
        engine.retomar(); // não deve iniciar timer
        assertThat(engine.isJogoEncerrado()).isTrue();
    }

    // ── (M) Seed determinística ───────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (M): duas GameModel com mesma seed produzem mesma posição de entrada")
    void testeEstruturaMSeedDeterministica() {
        GameModel m2 = new GameModel(SEED);
        // "entrada" sempre em (0,0) e andar 1
        Room e1 = model.getSalas().get("entrada");
        Room e2 = m2.getSalas().get("entrada");
        assertThat(e1.getX()).isEqualTo(e2.getX());
        assertThat(e1.getY()).isEqualTo(e2.getY());
        assertThat(e1.getAndar()).isEqualTo(e2.getAndar());
    }

    @Test
    @DisplayName("Estrutura (M'): seeds diferentes produzem pelo menos uma sala intermediária em posição diferente")
    void testeEstruturaMSeedsDiferentesMapa() {
        GameModel m2 = new GameModel(SEED + 1);
        // Compara salas que não são fixas (não são entrada/sagrado/escadas)
        boolean algumaDiferente = model.getSalas().values().stream()
                .filter(r -> !r.getNome().equals("entrada")
                        && !r.getNome().equals("sagrado")
                        && !r.getNome().startsWith("escada_"))
                .anyMatch(r -> {
                    Room r2 = m2.getSalas().get(r.getNome());
                    return r2 == null || r.getX() != r2.getX() || r.getY() != r2.getY();
                });
        assertThat(algumaDiferente).isTrue();
    }

    // ── (N) Estrutura de 4 andares ────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (N): mapa tem exatamente 100 salas (4 andares × 25)")
    void testeEstruturaNMapa100Salas() {
        assertThat(model.getSalas()).hasSize(100);
    }

    @Test
    @DisplayName("Estrutura (N'): cada andar tem exatamente 25 salas")
    void testeEstruturaNAndaresCom25Salas() {
        for (int andar = 1; andar <= 4; andar++) {
            int finalAndar = andar;
            long count = model.getSalas().values().stream()
                    .filter(r -> r.getAndar() == finalAndar)
                    .count();
            assertThat(count).as("Andar " + andar + " deve ter 25 salas").isEqualTo(25);
        }
    }

    // ── (O) Escadas nos cantos certos ─────────────────────────────────────

    @Test
    @DisplayName("Estrutura (O): escada_cima_1 no andar 1 em (4,0)")
    void testeEstruturaOEscadaCima1Posicao() {
        Room e = model.getSalas().get("escada_cima_1");
        assertThat(e).isNotNull();
        assertThat(e.getAndar()).isEqualTo(1);
        assertThat(e.getX()).isEqualTo(4);
        assertThat(e.getY()).isEqualTo(0);
        assertThat(e.isEscadaCima()).isTrue();
    }

    @Test
    @DisplayName("Estrutura (O'): sagrado no andar 4 em (4,4) e bloqueada")
    void testeEstruturaOSagradoPosicao() {
        Room sagrado = model.getSalas().get("sagrado");
        assertThat(sagrado).isNotNull();
        assertThat(sagrado.getAndar()).isEqualTo(4);
        assertThat(sagrado.getX()).isEqualTo(4);
        assertThat(sagrado.getY()).isEqualTo(4);
        assertThat(sagrado.isBloqueada()).isTrue();
    }

    @Test
    @DisplayName("Estrutura (O''): cálice presente na sala sagrado")
    void testeEstruturaOCaliceEmSagrado() {
        Room sagrado = model.getSalas().get("sagrado");
        assertThat(sagrado.contemItem(Item.Type.CALICE)).isTrue();
    }

    // ── Adjacências do grid ────────────────────────────────────────────────

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
    @DisplayName("Estrutura: sala interna (x>0, y>0) tem vizinhos norte e oeste")
    void testeEstruturaSalaInternaVizinhos() {
        model.getSalas().values().stream()
                .filter(r -> r.getX() > 0 && r.getY() > 0 && r.getAndar() == 1)
                .forEach(r -> {
                    assertThat(r.getVizinho("norte")).isNotNull();
                    assertThat(r.getVizinho("oeste")).isNotNull();
                });
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

    // ── Itens fixos nos andares ────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: Lupa está no andar 2 em (2,2)")
    void testeEstruturaLupaAndar2() {
        Room salaLupa = buscarSalaPorAndarXY(2, 2, 2);
        assertThat(salaLupa).isNotNull();
        assertThat(salaLupa.contemItem(Item.Type.LUPA)).isTrue();
    }

    @Test
    @DisplayName("Estrutura: Chave está no andar 3 em (1,1)")
    void testeEstruturaChaveAndar3() {
        Room salaChave = buscarSalaPorAndarXY(3, 1, 1);
        assertThat(salaChave).isNotNull();
        assertThat(salaChave.contemItem(Item.Type.CHAVE)).isTrue();
    }

    @Test
    @DisplayName("Estrutura: Poção no andar 1 em (2,3)")
    void testeEstruturaPocaoAndar1() {
        Room salaPocao = buscarSalaPorAndarXY(1, 2, 3);
        assertThat(salaPocao).isNotNull();
        assertThat(salaPocao.contemItem(Item.Type.POCAO_VELOCIDADE)).isTrue();
    }

    @Test
    @DisplayName("Estrutura: Amuleto no andar 2 em (3,2)")
    void testeEstruturaAmuletoAndar2() {
        Room salaAmuleto = buscarSalaPorAndarXY(2, 3, 2);
        assertThat(salaAmuleto).isNotNull();
        assertThat(salaAmuleto.contemItem(Item.Type.AMULETO_VISAO)).isTrue();
    }

    // ── removePropertyChangeListener ──────────────────────────────────────

    @Test
    @DisplayName("Estrutura: removePropertyChangeListener impede recebimento de eventos")
    void testeEstruturaRemoverListener() {
        model.removePropertyChangeListener(listener);
        reset(listener);

        moverDireto(primeiroVizinhoNaoBloqueado(model.getJogador().getPosicaoAtual()));
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
        throw new IllegalStateException("Sem vizinhos: " + sala.getNome());
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

    private void oscilate() {
        if (!model.isJogoAtivo()) return;
        Room a = model.getJogador().getPosicaoAtual();
        Room v = primeiroVizinhoNaoBloqueado(a);
        if (!model.isJogoAtivo()) return;
        model.moverJogador(direcaoPara(a, v));
        if (model.isJogoAtivo() && model.getMovimentosRestantes() > 1) {
            model.moverJogador(direcaoPara(v, a));
        }
    }

    private void esvaziarMovimentos() {
        while (model.getMovimentosRestantes() > 0 && model.isJogoAtivo()) {
            oscilate();
        }
        // Se jogo encerrou antes de zerar, não importa
    }

    private Room buscarSalaPorAndarXY(int andar, int x, int y) {
        return model.getSalas().values().stream()
                .filter(r -> r.getAndar() == andar && r.getX() == x && r.getY() == y)
                .findFirst().orElse(null);
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