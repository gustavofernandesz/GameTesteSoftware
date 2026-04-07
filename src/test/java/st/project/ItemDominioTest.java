package st.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.Item;
import static org.assertj.core.api.Assertions.assertThat;

public class ItemDominioTest {
    Item item = new Item("Chave Encantada", Item.Type.CHAVE, "Abre a sala do cálice");

    @Test
    @DisplayName("Teste de Dominio: getNome retorna String com o nome do item")
    void testeDominioGetNome() {
        assertThat(item.getNome()).isEqualTo("Chave Encantada");
    }

    @Test
    @DisplayName("Teste de Dominio: getTipo retorna tipo do item")
    void testeDominioGetTipo() {
        assertThat(item.getTipo()).isEqualTo(Item.Type.CHAVE);
    }

    @Test
    @DisplayName("Teste de Dominio: getDescricao retorna String com a descricao do item")
    void testeDominioGetDescricao() {
        assertThat(item.getDescricao()).isEqualTo("Abre a sala do cálice");
    }

    // toString

    @Test
    @DisplayName("Teste de Dominio: toString retorna nome e descricao")
    void testeDominioToString() {
        assertThat(item.toString()).isEqualTo("Chave Encantada (Abre a sala do cálice)");
    }
}
