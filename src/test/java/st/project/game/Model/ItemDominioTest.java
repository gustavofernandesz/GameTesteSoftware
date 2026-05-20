package st.project.game.Model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.Item;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE DOMÍNIO: Item ────────────────────────────────────────────────
 *
 * Escopo: valida as regras de negócio da classe Item (construção, getters,
 * representação textual e classificação por tipo).
 *
 * Dublê de teste: nenhum — Item é uma classe de valor pura (sem dependências).
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("Item – Testes de Domínio")
class ItemDominioTest {

    // ── Fixture comum ──────────────────────────────────────────────────────
    private final Item chave = new Item("Chave Encantada", Item.Type.CHAVE,
            "Abre a sala do cálice");

    // ── getNome ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: getNome retorna o nome informado na construção")
    void testeDominioGetNome() {
        assertThat(chave.getNome()).isEqualTo("Chave Encantada");
    }

    // ── getTipo ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: getTipo retorna o tipo informado na construção")
    void testeDominioGetTipo() {
        assertThat(chave.getTipo()).isEqualTo(Item.Type.CHAVE);
    }

    // ── getDescricao ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: getDescricao retorna a descrição informada na construção")
    void testeDominioGetDescricao() {
        assertThat(chave.getDescricao()).isEqualTo("Abre a sala do cálice");
    }

    // ── toString ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: toString exibe nome e descrição no formato esperado")
    void testeDominioToString() {
        assertThat(chave.toString())
                .isEqualTo("Chave Encantada (Abre a sala do cálice)");
    }

    // ── Tipos disponíveis ──────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: todos os tipos esperados existem no enum")
    void testeDominioTiposExistem() {
        assertThat(Item.Type.values()).contains(
                Item.Type.CHAVE,
                Item.Type.POCAO_VELOCIDADE,
                Item.Type.AMULETO_VISAO,
                Item.Type.CALICE,
                Item.Type.LUPA

        );
    }


}
