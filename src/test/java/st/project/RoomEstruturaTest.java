package st.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.Item;
import st.project.game.Room;

import static org.assertj.core.api.Assertions.assertThat;

public class RoomEstruturaTest {
    Room room = new Room("Sala de Teste", 0, 0);

    // isBloqueada

    @Test
    @DisplayName("Teste de Estrutura: Retorna False depois de ser bloqueada e desbloqueada")
    void testeEstruturaSalaBloqueadaDepoisDesbloqueada() {
        room.setBloqueada(true);
        room.setBloqueada(false);

        assertThat(room.isBloqueada()).isFalse();
    }

    // adicionarItem

    @Test
    @DisplayName("Teste de Estrutura: Não adiciona item nulo na sala")
    void testeEstruturaAdicaoDeItemNulo() {
        room.adicionarItem(null);

        assertThat(room.getItems()).isEmpty();
    }

    // removerItem

    @Test
    @DisplayName("Teste de Estrutura: Remoção de item no vetor vazio")
    void testeEstruturaRemocaoDeNuloEmVetorVazio() {
        assertThat(room.removerItem(null)).isEqualTo(null);
    }

    @Test
    @DisplayName("Teste de Estrutura: Remoção de item no vetor populado")
    void testeEstruturaRemocaoDeNuloEmVetorPopulado() {
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");

        room.adicionarItem(pocao);

        assertThat(room.removerItem(null)).isEqualTo(null);
    }

    // contemItem

    @Test
    @DisplayName("Teste de Estrutura: Retorna False com vetor vazio")
    void testeEstruturaVetorNaoContemNenhumItem() {
        assertThat(room.contemItem(Item.Type.POCAO_VELOCIDADE)).isFalse();
    }

    // getItemPorTipo

    @Test
    @DisplayName("Teste de Estrutura: Retorna null o vetor estiver vazio")
    void testeEstruturaGetItemPorTipoNaoPossuiNenhumItem() {
        assertThat(room.getItemPorTipo(Item.Type.POCAO_VELOCIDADE)).isEqualTo(null);
    }
}
