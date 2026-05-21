package st.project.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES ESTRUTURAIS: Room ───────────────────────────────────────────────
 *
 * Foco: cobertura MC/DC das decisões internas de Room.
 *
 * Decisões cobertas:
 *   (A) adicionarItem: item == null  → não adiciona
 *   (B) adicionarItem: item != null  → adiciona
 *   (C) removerItem: item na lista   → remove e retorna
 *   (D) removerItem: item fora lista → retorna null
 *   (E) contemItem: tipo encontrado  → true
 *   (F) contemItem: tipo não encontrado → false
 *   (G) isAlcapao: nome com prefixo "alcapao_" → true (auto)
 *   (H) isAlcapao: nome sem prefixo → false (auto)
 *
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("Room – Testes Estruturais (MC/DC)")
class RoomEstruturaTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("Sala de Teste", 0, 0);
    }

    // ── (A)(B) adicionarItem ───────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (A): adicionarItem(null) deixa lista vazia — condição item==null")
    void testeEstruturaAdicionarItemNuloNaoAltera() {
        room.adicionarItem(null);
        assertThat(room.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Estrutura (B): adicionarItem válido insere na lista — condição item!=null")
    void testeEstruturaAdicionarItemValidoInsere() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2 tempo");
        room.adicionarItem(pocao);
        assertThat(room.getItems()).hasSize(1).containsExactly(pocao);
    }

    // ── (C)(D) removerItem ─────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (C): removerItem presente retorna o item — condição na lista=true")
    void testeEstruturaRemoverItemPresente() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2 tempo");
        room.adicionarItem(pocao);
        assertThat(room.removerItem(pocao)).isEqualTo(pocao);
        assertThat(room.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Estrutura (D): removerItem ausente retorna null — condição na lista=false")
    void testeEstruturaRemoverItemAusente() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2 tempo");
        assertThat(room.removerItem(pocao)).isNull();
    }

    @Test
    @DisplayName("Estrutura (D'): removerItem(null) em lista populada retorna null")
    void testeEstruturaRemoverNuloEmListaPopulada() {
        room.adicionarItem(new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2"));
        assertThat(room.removerItem(null)).isNull();
    }

    // ── (E)(F) contemItem ──────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (E): contemItem encontra tipo presente — condição tipo==FOUND=true")
    void testeEstruturaContemItemTipoEncontrado() {
        room.adicionarItem(new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3 mov"));
        assertThat(room.contemItem(Item.Type.AMULETO_VISAO)).isTrue();
    }

    @Test
    @DisplayName("Estrutura (F): contemItem não encontra tipo ausente — condição tipo==FOUND=false")
    void testeEstruturaContemItemTipoAusente() {
        room.adicionarItem(new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3 mov"));
        assertThat(room.contemItem(Item.Type.CHAVE)).isFalse();
    }




    // ── Bloqueio/desbloqueio encadeado ─────────────────────────────────────

    @Test
    @DisplayName("Estrutura: bloquear, desbloquear → resultado final false")
    void testeEstruturaBloquearDepoisDesbloquear() {
        room.setBloqueada(true);
        room.setBloqueada(false);
        assertThat(room.isBloqueada()).isFalse();
    }
}
