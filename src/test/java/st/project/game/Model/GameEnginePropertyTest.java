package st.project.game.Model;

import net.jqwik.api.*;
import st.project.game.model.GameModel;
import st.project.game.model.Item;
import st.project.game.model.Room;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES BASEADOS EM PROPRIEDADE: GameModel ──────────────────────────────
 *
 * Foco: invariantes que devem valer para qualquer seed e qualquer sequência
 * de ações, sem depender de exemplos únicos do mapa.
 *
 * Propriedades testadas:
 *   P1. Mapa gerado sempre tem 100 salas com coordenadas válidas e vizinhança correta
 *   P2. Movimentos válidos nunca colocam o jogador fora dos limites (0–4, andares 1–4)
 *   P3. Movimento inválido preserva posição e contador de movimentos
 *   P4. Item não-LUPA nunca é coletado sem a lupa no inventário
 *   P5. Recursos únicos (LUPA, CHAVE, CALICE) não nascem duplicados no mapa
 *   P6. Score nunca é negativo enquanto o jogo está ativo
 *   P7. Nível nunca é menor que 1 (invariante de nível mínimo)
 *   P8. Andar sempre está entre 1 e 4 após qualquer sequência de movimentos
 *
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
class GameEnginePropertyTest {

    // ── P1. Mapa: 100 salas, coordenadas e vizinhança ─────────────────────

    @Property(tries = 100)
    @Label("P1: mapa gerado sempre tem 100 salas com coordenadas e vizinhança válidas")
    void mapaGeradoSempreRespeitaLimitesEVizinhanca(@ForAll long seed) {
        GameModel model = new GameModel(seed);

        assertThat(model.getSalas()).hasSize(100);
        assertThat(model.getSalas().values()).allSatisfy(sala -> {
            assertThat(sala.getX()).isBetween(0, 4);
            assertThat(sala.getY()).isBetween(0, 4);
            assertThat(sala.getAndar()).isBetween(1, 4);
            verificarVizinhoCardinal(sala, "norte",  0, -1);
            verificarVizinhoCardinal(sala, "sul",    0,  1);
            verificarVizinhoCardinal(sala, "leste",  1,  0);
            verificarVizinhoCardinal(sala, "oeste", -1,  0);
        });
    }

    // ── P2. Movimentos válidos nunca saem dos limites ─────────────────────

    @Property(tries = 80)
    @Label("P2: movimentos válidos nunca colocam o jogador fora dos limites")
    void movimentosValidosNuncaSaemDosLimites(@ForAll long seed,
                                              @ForAll("sequenciasDirecoes") List<String> direcoes) {
        GameModel model = new GameModel(seed);

        for (String direcao : direcoes) {
            model.moverJogador(direcao);
            Room atual = model.getJogador().getPosicaoAtual();
            assertThat(atual.getX()).isBetween(0, 4);
            assertThat(atual.getY()).isBetween(0, 4);
            assertThat(atual.getAndar()).isBetween(1, 4);
        }
    }

    // ── P3. Movimento inválido preserva estado essencial ──────────────────

    @Property(tries = 80)
    @Label("P3: movimento inválido preserva posição e contador de movimentos")
    void movimentoInvalidoPreservaEstadoEssencial(@ForAll long seed,
                                                  @ForAll("direcoesInvalidas") String direcaoInvalida) {
        GameModel model = new GameModel(seed);
        Room posicaoAntes = model.getJogador().getPosicaoAtual();
        int movimentosAntes = model.getMovimentosRestantes();

        boolean moveu = model.moverJogador(direcaoInvalida);

        assertThat(moveu).isFalse();
        assertThat(model.getJogador().getPosicaoAtual()).isEqualTo(posicaoAntes);
        assertThat(model.getMovimentosRestantes()).isEqualTo(movimentosAntes);
    }

    // ── P4. Item não-LUPA nunca coletado sem lupa ─────────────────────────

    @Property(tries = 80)
    @Label("P4: item não-LUPA nunca é coletado sem lupa no inventário")
    void itemNaoLupaNuncaEColetadoSemLupa(@ForAll long seed,
                                          @ForAll("tiposOcultos") Item.Type tipo) {
        GameModel model = new GameModel(seed);
        Room salaDoItem = salaComItem(model, tipo);
        Item item       = salaDoItem.getItemPorTipo(tipo);
        Room origem     = primeiroVizinhoDisponivel(salaDoItem);
        model.getJogador().moverPara(origem);

        boolean moveu = model.moverJogador(direcaoPara(origem, salaDoItem));

        assertThat(moveu).isTrue();
        assertThat(model.getJogador().possuiItem(tipo)).isFalse();
        assertThat(salaDoItem.getItems()).contains(item); // ainda na sala
    }

    // ── P5. Recursos únicos não nascem duplicados ─────────────────────────

    @Property(tries = 100)
    @Label("P5: LUPA, CHAVE e CALICE aparecem exatamente uma vez no mapa")
    void recursosUnicosNaoNascemDuplicados(@ForAll long seed) {
        GameModel model = new GameModel(seed);

        assertThat(contarItensNoMapa(model, Item.Type.LUPA)).isEqualTo(1);
        assertThat(contarItensNoMapa(model, Item.Type.CHAVE)).isEqualTo(1);
        assertThat(contarItensNoMapa(model, Item.Type.CALICE)).isEqualTo(1);
    }

    // ── P6. Score nunca negativo com jogo ativo ───────────────────────────

    @Property(tries = 50)
    @Label("P6: score nunca é negativo enquanto o jogo está ativo")
    void scoreNuncaNegativoComJogoAtivo(@ForAll long seed,
                                        @ForAll("sequenciasDirecoes") List<String> direcoes) {
        GameModel model = new GameModel(seed);

        for (String direcao : direcoes) {
            if (!model.isJogoAtivo()) break;
            model.moverJogador(direcao);
            if (model.isJogoAtivo()) {
                assertThat(model.getScore()).isGreaterThanOrEqualTo(0);
            }
        }
    }

    // ── P7. Nível mínimo invariante ───────────────────────────────────────

    @Property(tries = 50)
    @Label("P7: nível nunca cai abaixo de 1 após qualquer sequência de movimentos")
    void nivelNuncaMenorQueUm(@ForAll long seed,
                              @ForAll("sequenciasDirecoes") List<String> direcoes) {
        GameModel model = new GameModel(seed);

        for (String direcao : direcoes) {
            if (!model.isJogoAtivo()) break;
            model.moverJogador(direcao);
            assertThat(model.getNivel()).isGreaterThanOrEqualTo(1);
        }
    }

    // ── P8. Andar sempre entre 1 e 4 ─────────────────────────────────────

    @Property(tries = 50)
    @Label("P8: andar do jogador sempre entre 1 e 4 após qualquer sequência")
    void andarSempreEntre1e4(@ForAll long seed,
                             @ForAll("sequenciasDirecoes") List<String> direcoes) {
        GameModel model = new GameModel(seed);

        for (String direcao : direcoes) {
            if (!model.isJogoAtivo()) break;
            model.moverJogador(direcao);
            assertThat(model.getAndarAtual()).isBetween(1, 4);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Providers de dados arbitrários
    // ═══════════════════════════════════════════════════════════════════════

    @Provide
    Arbitrary<List<String>> sequenciasDirecoes() {
        return Arbitraries.of("norte", "sul", "leste", "oeste", "direcaoInexistente")
                .list()
                .ofMinSize(0)
                .ofMaxSize(40);
    }

    @Provide
    Arbitrary<String> direcoesInvalidas() {
        return Arbitraries.of("", "nordeste", "cima", "baixo", "direita", "esquerda",
                "direcaoInexistente", "NORTE", "Sul");
    }

    @Provide
    Arbitrary<Item.Type> tiposOcultos() {
        // Tipos que não são LUPA — invisíveis sem ela
        return Arbitraries.of(Item.Type.CHAVE, Item.Type.POCAO_VELOCIDADE, Item.Type.AMULETO_VISAO);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utilitários
    // ═══════════════════════════════════════════════════════════════════════

    private static void verificarVizinhoCardinal(Room sala, String direcao, int dx, int dy) {
        Room vizinho = sala.getVizinho(direcao);
        if (vizinho == null) return;
        assertThat(vizinho.getAndar()).isEqualTo(sala.getAndar());
        assertThat(vizinho.getX()).isEqualTo(sala.getX() + dx);
        assertThat(vizinho.getY()).isEqualTo(sala.getY() + dy);
    }

    private static Room salaComItem(GameModel model, Item.Type tipo) {
        return model.getSalas().values().stream()
                .filter(sala -> sala.contemItem(tipo))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Item não encontrado: " + tipo));
    }

    private static Room primeiroVizinhoDisponivel(Room sala) {
        for (String dir : List.of("norte", "sul", "leste", "oeste")) {
            Room vizinho = sala.getVizinho(dir);
            if (vizinho != null) return vizinho;
        }
        throw new IllegalStateException("Sala sem vizinhos: " + sala.getNome());
    }

    private static String direcaoPara(Room origem, Room destino) {
        for (String dir : List.of("norte", "sul", "leste", "oeste")) {
            if (destino.equals(origem.getVizinho(dir))) return dir;
        }
        throw new IllegalArgumentException("Destino não é vizinho de origem");
    }

    private static long contarItensNoMapa(GameModel model, Item.Type tipo) {
        return model.getSalas().values().stream()
                .flatMap(sala -> sala.getItems().stream())
                .filter(item -> item.getTipo() == tipo)
                .count();
    }
}