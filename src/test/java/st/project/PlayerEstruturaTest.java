package st.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.Item;
import st.project.game.Player;
import st.project.game.Room;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerEstruturaTest {
    Room salaInicial = new Room("Sala Inicial", 0, 0);
    Player player = new Player(salaInicial);

    // removerItem

    @Test
    @DisplayName("Teste de Estrutura: Retorna null se item removido for nulo e com o inventario vazio")
    void testeEstruturaRemoverItemNuloComInventarioVazio() {
        assertThat(player.removerItem(null)).isNull();
    }

    @Test
    @DisplayName("Teste de Estrutura: Retorna null se item removido for nulo e com itens inventario ")
    void testeEstruturaRemoverItemNuloComInventario() {
        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");

        player.adicionarItem(amuleto);

        assertThat(player.removerItem(null)).isNull();
    }

    // possuiItem

    @Test
    @DisplayName("Teste de Estrutura: Retorna False se o item procurado for nulo e jogador tem inventario vazio")
    void testeEstruturaPossuiItemComItemNuloComInventarioVazio() {
        assertThat(player.possuiItem(null)).isFalse();
    }

    @Test
    @DisplayName("Teste de Estrutura: Retorna False se o item procurado for nulo e jogador tem outros itens no inventario")
    void testeEstruturaPossuiItemComItemNuloComItenNoInventario() {
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");
        player.adicionarItem(pocao);
        assertThat(player.possuiItem(null)).isFalse();
    }
}
