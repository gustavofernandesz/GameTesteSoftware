package st.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.Item;
import st.project.game.Player;
import st.project.game.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.assertj.core.api.Assertions.assertThat;

public class PlayerFronteiraTest {
    Room salaInicial = new Room("Sala Inicial", 0, 0);
    Player player = new Player(salaInicial);

    // getInventario

    @Test
    @DisplayName("Teste de Fronteira: Retorna a lista de inventario com apenas um item")
    void testeFronteiraInventarioPossuiUmItem() {
        Item chave = new Item("Chave Encantada", Item.Type.CHAVE, "Abre a sala do cálice");
        List<Item> inv = new ArrayList<>();
        inv.add(chave);

        player.adicionarItem(chave);

        assertThat(player.getInventario()).isEqualTo(inv);
    }

    @Test
    @DisplayName("Teste de Fronteira: Retorna a lista de inventario com mais de um item")
    void testeFronteiraInventarioPossuiMaisDeUmItem() {
        Item chave = new Item("Chave Encantada", Item.Type.CHAVE, "Abre a sala do cálice");
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");
        List<Item> inv = new ArrayList<>();
        inv.add(chave);
        inv.add(pocao);

        player.adicionarItem(chave);
        player.adicionarItem(pocao);

        assertThat(player.getInventario()).isEqualTo(inv);
    }

    @Test
    @DisplayName("Teste de Fronteira: Retorna a lista de inventario vazia")
    void testeFronteiraInventarioVazio() {
        List<Item> inv = new ArrayList<>();

        assertThat(player.getInventario().isEmpty()).isTrue();
        assertThat(player.getInventario()).isEqualTo(inv);
    }

    // getHistorico

    @Test
    @DisplayName("Teste de Fronteira: Retorna a stack com uma unica sala")
    void testeFronteiraHistoricoComUmaSala() {
        Stack<Room> hist = new Stack<>();
        hist.push(salaInicial);

        assertThat(player.getHistorico()).isEqualTo(hist);
    }

    @Test
    @DisplayName("Teste de Fronteira: Retorna a stack com mais de uma sala")
    void testeFronteiraHistoricoComMaisDeUmaSala() {
        Stack<Room> hist = new Stack<>();

        Room segundaSala = new Room("Segunda Sala", 0, 1);
        hist.push(salaInicial);
        hist.push(segundaSala);

        player.moverPara(segundaSala);

        assertThat(player.getHistorico()).isEqualTo(hist);
    }

    // moverPara

    @Test
    @DisplayName("Teste de Fronteira: Retorna False se tentar mover para uma sala nula")
    void testeFronteiraMoverParaSalaNula() {
        assertThat(player.moverPara(null)).isFalse();
    }

    @Test
    @DisplayName("Teste de Fronteira: Retorna False se tentar mover para uma sala bloqueada sem uma chave no inventario")
    void testeFronteiraMoverParaSalaBloqueadaSemChave() {
        Room segundaSala = new Room("Segunda Sala", 0, 1);
        segundaSala.setBloqueada(true);

        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");
        player.adicionarItem(amuleto);

        assertThat(player.moverPara(segundaSala)).isFalse();
    }

    @Test
    @DisplayName("Teste de Fronteira: Retorna True se tentar mover para uma sala bloqueada com a chave no inventario")
    void testeFronteiraMoverParaSalaBloqueadaComChave() {
        Room segundaSala = new Room("Segunda Sala", 0, 1);
        segundaSala.setBloqueada(true);

        Item chave = new Item("Chave Encantada", Item.Type.CHAVE, "Abre a sala do cálice");
        player.adicionarItem(chave);

        assertThat(player.moverPara(segundaSala)).isTrue();
    }

    // adicionarItem

    @Test
    @DisplayName("Teste de Fronteira: Não adiciona o objeto nulo no inventario")
    void testeFronteiraAdicionarItemNulo() {
        player.adicionarItem(null);
        assertThat(player.getInventario().contains(null)).isFalse();
    }

    // removerItem

    @Test
    @DisplayName("Teste de Fronteira: Retorna o item removido do inventario")
    void testeFronteiraRemoverItemExitente() {
        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");

        player.adicionarItem(amuleto);

        assertThat(player.removerItem(amuleto)).isEqualTo(amuleto);
    }

    @Test
    @DisplayName("Teste de Fronteira: Retorna null se tentar remover item que nao esta no inventario")
    void testeFronteiraRemoverItemExitenteQueNaoEstahNoInventario() {
        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");
        player.adicionarItem(amuleto);

        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");

        assertThat(player.removerItem(pocao)).isNull();
    }

    @Test
    @DisplayName("Teste de Fronteira: Retorna null se item removido for nulo")
    void testeFronteiraRemoverItemNuloComInventarioVazio() {
        assertThat(player.removerItem(null)).isNull();
    }

    // possuiItem

    @Test
    @DisplayName("Teste de Fronteira:  Retorna True se o jogador posuir o item procurado")
    void testeFronteiraPossuiItemExistenteNoInventario() {
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");
        player.adicionarItem(pocao);
        assertThat(player.possuiItem(pocao.getTipo())).isTrue();
    }

    @Test
    @DisplayName("Teste de Fronteira: Retorna False se o jogador nao possuir o item procurado")
    void testeFronteiraPossuiItemExistenteForaDoInventario() {
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");
        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");

        player.adicionarItem(pocao);

        assertThat(player.possuiItem(amuleto.getTipo())).isFalse();
    }

    @Test
    @DisplayName("Teste de Fronteira: Retorna False se o item procurado for nulo")
    void testeFronteiraPossuiItemComItemNuloComInventarioVazio() {
        assertThat(player.possuiItem(null)).isFalse();
    }

    // usarItem

    @Test
    @DisplayName("Teste de Fronteira: Pocao é usada e removida do inventario")
    void testeFronteiraUsarItemPocao() {
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");
        player.adicionarItem(pocao);
        player.usarItem(pocao);
        assertThat(player.getInventario().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Teste de Fronteira: Amuleto é usado e removido do inventario")
    void testeFronteiraUsarItemAmuleto() {
        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");
        player.adicionarItem(amuleto);
        player.usarItem(amuleto);
        assertThat(player.getInventario().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Teste de Fronteira: Nao remove nenhum item se item usado nao for consumivel")
    void testeFronteiraUsarItemNaoConsumivel() {
        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");
        Item chave = new Item("Chave Encantada", Item.Type.CHAVE, "Abre a sala do cálice");
        List<Item> inv = new ArrayList<>();

        player.adicionarItem(amuleto);
        player.adicionarItem(chave);
        inv.add(amuleto);
        inv.add(chave);

        player.usarItem(chave);
        assertThat(player.getInventario()).isEqualTo(inv);
    }
}
