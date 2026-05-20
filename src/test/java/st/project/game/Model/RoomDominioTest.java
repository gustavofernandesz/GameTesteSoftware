package st.project.game.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.Item;
import st.project.game.model.Room;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE DOMÍNIO: Room ────────────────────────────────────────────────
 *
 * Escopo: valida as regras de negócio da sala — estado inicial, bloqueio,
 * vizinhança, manipulação de itens e identificação de escadas.
 *
 * Dublê de teste: nenhum — Room é uma classe de estado pura.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("Room – Testes de Domínio")
class RoomDominioTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("Sala de Teste", 0, 0);
    }

    // ── Getters básicos ────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: getNome retorna o nome da sala")
    void testeDominioGetNome() {
        assertThat(room.getNome()).isEqualTo("Sala de Teste");
    }

    @Test
    @DisplayName("Domínio: getX retorna a coordenada X")
    void testeDominioGetX() {
        assertThat(room.getX()).isEqualTo(0);
    }

    @Test
    @DisplayName("Domínio: getY retorna a coordenada Y")
    void testeDominioGetY() {
        assertThat(room.getY()).isEqualTo(0);
    }

    @Test
    @DisplayName("Domínio: getAndar retorna o andar correto (construtor 4-param)")
    void testeDominioGetAndar() {
        Room r = new Room("x", 1, 2, 3);
        assertThat(r.getAndar()).isEqualTo(3);
    }

    @Test
    @DisplayName("Domínio: construtor 3-param usa andar 1 por padrão")
    void testeDominioAndarPadraoUm() {
        assertThat(room.getAndar()).isEqualTo(1);
    }

    // ── Bloqueio ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: sala nasce desbloqueada por padrão")
    void testeDominioSalaDesbloqueadaPorPadrao() {
        assertThat(room.isBloqueada()).isFalse();
    }

    @Test
    @DisplayName("Domínio: setBloqueada(true) bloqueia a sala")
    void testeDominioBloquearSala() {
        room.setBloqueada(true);
        assertThat(room.isBloqueada()).isTrue();
    }

    // ── Escadas ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: sala com nome 'escada_cima_1' é identificada como escada cima")
    void testeDominioIsEscadaCima() {
        Room escada = new Room("escada_cima_1", 4, 0, 1);
        assertThat(escada.isEscadaCima()).isTrue();
        assertThat(escada.isEscadaBaixo()).isFalse();
        assertThat(escada.isEscada()).isTrue();
    }

    @Test
    @DisplayName("Domínio: sala com nome 'escada_baixo_2' é identificada como escada baixo")
    void testeDominioIsEscadaBaixo() {
        Room escada = new Room("escada_baixo_2", 0, 4, 2);
        assertThat(escada.isEscadaBaixo()).isTrue();
        assertThat(escada.isEscadaCima()).isFalse();
        assertThat(escada.isEscada()).isTrue();
    }

    @Test
    @DisplayName("Domínio: sala comum não é identificada como escada")
    void testeDominioSalaComumNaoEscada() {
        assertThat(room.isEscada()).isFalse();
        assertThat(room.isEscadaCima()).isFalse();
        assertThat(room.isEscadaBaixo()).isFalse();
    }

    // ── Vizinhança ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: setVizinho e getVizinho retornam a mesma sala")
    void testeDominioAdicionarSalaVizinha() {
        Room vizinha = new Room("Sala Vizinha", 0, 1);
        room.setVizinho("leste", vizinha);
        assertThat(room.getVizinho("leste")).isEqualTo(vizinha);
    }

    @Test
    @DisplayName("Domínio: getVizinho para direção sem vizinho retorna null")
    void testeDominioVizinhoAusenteNull() {
        assertThat(room.getVizinho("norte")).isNull();
    }

    // ── Itens ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: adicionarItem e getItems refletem o item na sala")
    void testeDominioAdicionarItem() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "Dobra tempo");
        room.adicionarItem(pocao);
        assertThat(room.getItems()).containsExactly(pocao);
    }

    @Test
    @DisplayName("Domínio: removerItem retorna o item e o remove da lista")
    void testeDominioRemoverItem() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "Dobra tempo");
        room.adicionarItem(pocao);
        Item removido = room.removerItem(pocao);
        assertThat(removido).isEqualTo(pocao);
        assertThat(room.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Domínio: contemItem retorna true para tipo presente")
    void testeDominioContemItemPresente() {
        room.adicionarItem(new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3 mov"));
        assertThat(room.contemItem(Item.Type.AMULETO_VISAO)).isTrue();
    }

    @Test
    @DisplayName("Domínio: contemItem retorna false para tipo ausente")
    void testeDominioContemItemAusente() {
        assertThat(room.contemItem(Item.Type.CHAVE)).isFalse();
    }

    @Test
    @DisplayName("Domínio: getItemPorTipo retorna o item correto")
    void testeDominioGetItemPorTipo() {
        Item chave = new Item("Chave", Item.Type.CHAVE, "Abre");
        room.adicionarItem(chave);
        assertThat(room.getItemPorTipo(Item.Type.CHAVE)).isEqualTo(chave);
    }

    @Test
    @DisplayName("Domínio: getItemPorTipo retorna null para tipo ausente")
    void testeDominioGetItemPorTipoAusente() {
        assertThat(room.getItemPorTipo(Item.Type.CALICE)).isNull();
    }

    @Test
    @DisplayName("Domínio: sala começa sem itens")
    void testeDominioSalaSemItensInicial() {
        assertThat(room.getItems()).isEmpty();
    }
}