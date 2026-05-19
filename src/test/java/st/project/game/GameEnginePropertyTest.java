package st.project.game;

import net.jqwik.api.*;
import st.project.game.model.GameModel;
import st.project.game.model.Item;
import st.project.game.model.Room;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES BASEADOS EM PROPRIEDADE: GameModel ──────────────────────────────
 *
 * Foco: invariantes que devem valer para vários seeds e várias sequências de
 * ações, sem depender de exemplos únicos do mapa.
 *
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("GameModel – Testes Baseados em Propriedade")
class GameEnginePropertyTest {

    @Property(tries = 100)
    @DisplayName("Propriedade: mapa gerado sempre respeita limites e vizinhança cardinal")
    void mapaGeradoSempreRespeitaLimitesEVizinhanca(@ForAll long seed) {
        GameModel model = new GameModel(seed);

        assertThat(model.getSalas()).hasSize(100);
        assertThat(model.getSalas().values()).allSatisfy(sala -> {
            assertThat(sala.getX()).isBetween(0, 4);
            assertThat(sala.getY()).isBetween(0, 4);
            assertThat(sala.getAndar()).isBetween(1, 4);
            verificarVizinhoCardinal(sala, "norte", 0, -1);
            verificarVizinhoCardinal(sala, "sul", 0, 1);
            verificarVizinhoCardinal(sala, "leste", 1, 0);
            verificarVizinhoCardinal(sala, "oeste", -1, 0);
        });
    }

    @Property(tries = 80)
    @DisplayName("Propriedade: movimentos válidos nunca deixam o jogador fora dos limites")
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

    @Property(tries = 80)
    @DisplayName("Propriedade: movimento inválido preserva posição e movimentos restantes")
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

    @Property(tries = 80)
    @DisplayName("Propriedade: item não-LUPA nunca é coletado sem a lupa")
    void itemNaoLupaNuncaEColetadoSemLupa(@ForAll long seed,
                                           @ForAll("tiposOcultos") Item.Type tipo) {
        GameModel model = new GameModel(seed);
        Room salaDoItem = salaComItem(model, tipo);
        Item item = salaDoItem.getItemPorTipo(tipo);
        Room origem = primeiroVizinhoDisponivel(salaDoItem);
        model.getJogador().moverPara(origem);

        boolean moveu = model.moverJogador(direcaoPara(origem, salaDoItem));

        assertThat(moveu).isTrue();
        assertThat(model.getJogador().possuiItem(tipo)).isFalse();
        assertThat(salaDoItem.getItems()).contains(item);
    }

    @Property(tries = 80)
    @DisplayName("Propriedade: recursos únicos da sessão não nascem duplicados no mapa")
    void recursosUnicosNaoNascemDuplicados(@ForAll long seed) {
        GameModel model = new GameModel(seed);

        assertThat(contarItensNoMapa(model, Item.Type.LUPA)).isEqualTo(1);
        assertThat(contarItensNoMapa(model, Item.Type.CHAVE)).isEqualTo(1);
        assertThat(contarItensNoMapa(model, Item.Type.CALICE)).isEqualTo(1);
    }

    @Provide
    Arbitrary<List<String>> sequenciasDirecoes() {
        return Arbitraries.of("norte", "sul", "leste", "oeste", "direcaoInexistente")
                .list()
                .ofMinSize(0)
                .ofMaxSize(40);
    }

    @Provide
    Arbitrary<String> direcoesInvalidas() {
        return Arbitraries.of("", "nordeste", "cima", "baixo", "direita", "esquerda", "direcaoInexistente");
    }

    @Provide
    Arbitrary<Item.Type> tiposOcultos() {
        return Arbitraries.of(Item.Type.CHAVE, Item.Type.POCAO_VELOCIDADE, Item.Type.AMULETO_VISAO);
    }

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
