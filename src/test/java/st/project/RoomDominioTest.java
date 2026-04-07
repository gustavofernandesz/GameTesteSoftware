package st.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.Item;
import st.project.game.Room;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoomDominioTest {
    Room room = new Room("Sala de Teste", 0, 0);

    // getNome

    @Test
    @DisplayName("Teste de Dominio: getNome retorna String com nome")
    void testeDominioGetNome() {
        assertThat(room.getNome()).isEqualTo("Sala de Teste");
    }

    // getX

    @Test
    @DisplayName("Teste de Dominio: getX retorna a posicao X da sala")
    void testeDominioGetX() {
        assertThat(room.getX()).isEqualTo(0);
    }

    // getY

    @Test
    @DisplayName("Teste de Dominio: getY retorna a posicao Y da sala")
    void testeDominioGetY() {
        assertThat(room.getY()).isEqualTo(0);
    }

    // isBloqueada

    @Test
    @DisplayName("Teste de Dominio: Retorna False pois sala é desbloqueada por padrao")
    void testeDominioSalaDesbloqueada() {
        assertThat(room.isBloqueada()).isFalse();
    }

    // setBloqueada

    @Test
    @DisplayName("Teste de Dominio: Bloquear sala altera seu estado")
    void testeDominioBloquearSala() {
        assertThat(room.isBloqueada()).isFalse();

        room.setBloqueada(true);

        assertThat(room.isBloqueada()).isTrue();
    }

    // setVizinho / getVizinho

    @Test
    @DisplayName("Teste de Dominio: Adicionar sala vizinha e retorna a sala vizinha quando buscado")
    void testeDominioAdicionarSalaVizinha() {
        Room salaVizinha = new Room("Sala Vizinha", 0, 1);

        room.setVizinho("leste", salaVizinha);

        assertThat(room.getVizinho("leste")).isEqualTo(salaVizinha);
    }

    // adicionarItem

    @Test
    @DisplayName("Teste de Dominio: Adição de item no vetor de items da sala")
    void testeDominioAdicaoDeItems() {
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");
        List<Item> items = new ArrayList<>();

        items.add(pocao);
        room.adicionarItem(pocao);

        assertThat(room.getItems()).isEqualTo(items);
    }

    // removerItem

    @Test
    @DisplayName("Teste de Dominio: Remoção de item no vetor da sala")
    void testeDominioRemocaoDeItems() {
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");

        room.adicionarItem(pocao);

        assertThat(room.removerItem(pocao)).isEqualTo(pocao);
    }

    // contemItem

    @Test
    @DisplayName("Teste de Dominio: Verficar se vetor contêm item retorna True")
    void testeDominioVetorContemItemBuscado() {
        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");

        room.adicionarItem(amuleto);

        assertThat(room.contemItem(amuleto.getTipo())).isTrue();
    }

    // getItemPorTipo

    @Test
    @DisplayName("Teste de Dominio: Verificar item buscado retorna o item caso possuir o tipo de item buscado no vetor")
    void testeDominioGetItemPorTipoPossuiItemBuscado() {
        Item chave = new Item("Chave Encantada", Item.Type.CHAVE, "Abre a sala do cálice");

        room.adicionarItem(chave);

        assertThat(room.getItemPorTipo(chave.getTipo())).isEqualTo(chave);
    }
}
