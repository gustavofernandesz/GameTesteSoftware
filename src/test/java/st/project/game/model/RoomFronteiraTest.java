package st.project.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE FRONTEIRA: Room ──────────────────────────────────────────────
 *
 * Cobre: valores-limite e situações extremas na manipulação de Room.
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("Room – Testes de Fronteira")
class RoomFronteiraTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("Sala de Teste", 1, 2);
    }

    // ── Coordenadas ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira: getX retorna coordenada X não-zero")
    void testeFronteiraGetX() {
        assertThat(room.getX()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fronteira: getY retorna coordenada Y não-zero")
    void testeFronteiraGetY() {
        assertThat(room.getY()).isEqualTo(2);
    }

    // ── Bloqueio ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira: bloquear e desbloquear restaura o estado original")
    void testeFronteiraBloquearDesbloquear() {
        room.setBloqueada(true);
        assertThat(room.isBloqueada()).isTrue();
        room.setBloqueada(false);
        assertThat(room.isBloqueada()).isFalse();
    }

    // ── Vizinhança ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira: getVizinho para direção sem vizinho retorna null")
    void testeFronteiraSemVizinhoPorDirecao() {
        room.setVizinho("leste", new Room("Outra", 2, 2));
        assertThat(room.getVizinho("sul")).isNull();
    }

    @Test
    @DisplayName("Fronteira: getVizinho para sala sem nenhum vizinho retorna null")
    void testeFronteiraSemNenhumVizinho() {
        assertThat(room.getVizinho("leste")).isNull();
    }

    // ── Itens – limite inferior ────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira: adicionarItem(null) não adiciona nada")
    void testeFronteiraAdicionarItemNulo() {
        room.adicionarItem(null);
        assertThat(room.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Fronteira: removerItem em lista vazia retorna null")
    void testeFronteiraRemoverEmVetorVazio() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "Dobra tempo");
        assertThat(room.removerItem(pocao)).isNull();
    }

    @Test
    @DisplayName("Fronteira: removerItem(null) retorna null")
    void testeFronteiraRemoverItemNulo() {
        assertThat(room.removerItem(null)).isNull();
    }

    // ── Itens – lista vazia ────────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira: contemItem em sala vazia retorna false")
    void testeFronteiraContemItemSalaVazia() {
        assertThat(room.contemItem(Item.Type.POCAO_VELOCIDADE)).isFalse();
    }

    @Test
    @DisplayName("Fronteira: getItemPorTipo em sala vazia retorna null")
    void testeFronteiraGetItemPorTipoSalaVazia() {
        assertThat(room.getItemPorTipo(Item.Type.CHAVE)).isNull();
    }

    // ── Itens – tipo ausente ───────────────────────────────────────────────

    @Test
    @DisplayName("Fronteira: contemItem retorna false para tipo ausente na sala")
    void testeFronteiraContemItemTipoAusente() {
        room.adicionarItem(new Item("Chave", Item.Type.CHAVE, "Abre"));
        assertThat(room.contemItem(Item.Type.POCAO_VELOCIDADE)).isFalse();
    }

    @Test
    @DisplayName("Fronteira: getItemPorTipo retorna null para tipo ausente")
    void testeFronteiraGetItemPorTipoAusente() {
        room.adicionarItem(new Item("Chave", Item.Type.CHAVE, "Abre"));
        assertThat(room.getItemPorTipo(Item.Type.CALICE)).isNull();
    }


}
