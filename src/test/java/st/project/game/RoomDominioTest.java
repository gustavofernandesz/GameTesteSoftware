package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.Item;
import st.project.game.model.Room;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE DOMÍNIO: Room ────────────────────────────────────────────────
 *
 * Escopo: valida as regras de negócio da sala — estado inicial, bloqueio,
 * vizinhança, manipulação de itens e detecção de alçapão.
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

    // ── Alçapão (NOVO) ─────────────────────────────────────────────────────


    // ── Vizinhança ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: setVizinho e getVizinho retornam a mesma sala")
    void testeDominioAdicionarSalaVizinha() {
        Room vizinha = new Room("Sala Vizinha", 0, 1);
        room.setVizinho("leste", vizinha);
        assertThat(room.getVizinho("leste")).isEqualTo(vizinha);
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
    @DisplayName("Domínio: getItemPorTipo retorna o item correto")
    void testeDominioGetItemPorTipo() {
        Item chave = new Item("Chave", Item.Type.CHAVE, "Abre");
        room.adicionarItem(chave);
        assertThat(room.getItemPorTipo(Item.Type.CHAVE)).isEqualTo(chave);
    }


}
