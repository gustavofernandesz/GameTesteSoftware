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

public class PlayerDominioTest {
    Room salaInicial = new Room("Sala Inicial", 0, 0);
    Player player = new Player(salaInicial);

    // getPosicaoAtual

    @Test
    @DisplayName("Teste de Dominio: Retorna a sala em que o player esta atualmente")
    void testeDominioGetPosicaoAtual() {
        assertThat(player.getPosicaoAtual()).isEqualTo(salaInicial);
    }

    // getInventario

    @Test
    @DisplayName("Teste de Dominio: Retorna os itens do inventario do player")
    void testeDominioGetInventario() {
        Item chave = new Item("Chave Encantada", Item.Type.CHAVE, "Abre a sala do cálice");
        List<Item> inv = new ArrayList<>();
        inv.add(chave);

        player.adicionarItem(chave);

        assertThat(player.getInventario()).isEqualTo(inv);
    }

    // getHistorico

    @Test
    @DisplayName("Teste de Dominio: Retorna uma stack das salas que o player passou")
    void testeDominioGetHistorico() {
        Stack<Room> hist = new Stack<>();
        hist.push(salaInicial);

        assertThat(player.getHistorico()).isEqualTo(hist);
    }

    // moverPara

    @Test
    @DisplayName("Teste de Dominio: Retorna True se player mover para outra sala")
    void testeDominioMoverPara() {
        Room segundaSala = new Room("Segunda Sala", 0, 1);
        assertThat(player.moverPara(segundaSala)).isTrue();
    }

    // adicionarItem

    @Test
    @DisplayName("Teste de Dominio: Item adicionado ao inventario do player")
    void testeDominioAdicionarItem() {
        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");

        player.adicionarItem(amuleto);
        assertThat(player.getInventario().contains(amuleto)).isTrue();
    }

    // removerItem

    @Test
    @DisplayName("Teste de Dominio: Retorna o item removido do inventario")
    void testeDominioRemoverItem() {
        Item amuleto = new Item("Amuleto de Visão", Item.Type.AMULETO_VISAO, "Revela localização do cálice");

        player.adicionarItem(amuleto);
        assertThat(player.getInventario().contains(amuleto)).isTrue();

        assertThat(player.removerItem(amuleto)).isEqualTo(amuleto);

        assertThat(player.getInventario().isEmpty()).isTrue();
    }

    // possuiItem

    @Test
    @DisplayName("Teste de Dominio: Retorna True se o jogador posuir o item procurado")
    void testeDominioPossuiItem() {
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");
        player.adicionarItem(pocao);
        assertThat(player.possuiItem(pocao.getTipo())).isTrue();
    }

    // usarItem

    @Test
    @DisplayName("Teste de Dominio: Item consumivel é usado e removido do inventario")
    void testeDominioUsarItem() {
        Item pocao = new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante");
        player.adicionarItem(pocao);
        player.usarItem(pocao);
        assertThat(player.getInventario().isEmpty()).isTrue();
    }
}
//=======
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class PlayerDominioTest {
//
//// Teste de domínio
//// MOVIMENTO
//
//
//    @Test
//    @DisplayName("Teste de domínio: deve aprovar caso obedeça regras")
//    void testeDominioMoverSucesso() {
//        Room inicio = new Room("inicio", 0, 0);
//        Room destino = new Room("destino", 1, 0);
//        Player player = new Player(inicio);
//        assertThat(player.moverPara(destino)).isTrue();
//    }
//    @Test
//    @DisplayName("Teste de domínio: deve falhar caso destino esteja bloqueado")
//    void testeDominioMoverInsucesso() {
//        Room inicio = new Room("inicio", 0, 0);
//        Room destino = new Room("destino", 1, 0);
//        destino.setBloqueada(true);
//        Player player = new Player(inicio);
//
//
//        assertThat(player.moverPara(destino)).isFalse();
//    }
//
//    @Test
//    @DisplayName("Teste de domínio: deve falhar caso destino seja null")
//    void testeDominioMoverNullInsucesso() {
//
//        Room inicio = new Room("inicio", 0, 0);
//        Player player = new Player(inicio);
//
//        assertThat(player.moverPara(null)).isFalse();
//    }
//
//    @Test
//    @DisplayName("Teste de domínio: deve falhar caso destino seja null")
//    void testeDominioMoverComChavesucesso() {
//
//        Room inicio = new Room("inicio", 0, 0);
//        Room destino =  new Room("destino", 1, 0);
//        Player player = new Player(inicio);
//        destino.setBloqueada(true);
//        player.adicionarItem(new Item("chave", Item.Type.CHAVE, "chave"));
//
//        assertThat(player.moverPara(destino)).isTrue();
//    }
//    @Test
//    @DisplayName("Teste de domínio: deve atualizar o historico com o novo lugar visitado")
//    void testeDominioAtualizarHistoricoAoMover() {
//
//        Room inicio = new Room("inicio", 0, 0);
//        Room destino =  new Room("destino", 1, 0);
//        Player player = new Player(inicio);
//        player.moverPara(destino);
//        assertThat(player.getHistorico().contains(destino)).isTrue();
//    }
//
//
//
//    // ATUALIZAR INVENTÁRIO
//    @Test
//    @DisplayName("Teste de domínio: deve aprovar caso obedeça regras")
//    void testeDominioAdicionarItemSucesso() {
//        Room inicio = new Room("inicio", 0, 0);
//        Room destino = new Room("destino", 1, 0);
//        Player player = new Player(inicio);
//        Item item = new Item("chave", Item.Type.CHAVE, "chave");
//        player.adicionarItem(item);
//        assertThat(player.getInventario().contains(item)).isTrue();
//    }
//
//    @Test
//    @DisplayName("Teste de domínio: deve retornar verdadeiro caso possua item de um tipo")
//    void testeDominioPossuirItem() {
//        Room inicio = new Room("inicio", 0, 0);
//        Room destino = new Room("destino", 1, 0);
//        Player player = new Player(inicio);
//        Item item = new Item("chave", Item.Type.CHAVE, "chave");
//        player.adicionarItem(item);
//        assertThat(player.possuiItem(Item.Type.CHAVE)).isTrue();
//    }
//
//    @Test
//    @DisplayName("Teste de domínio: deve retornar verdadeiro caso possua item de um tipo")
//    void testeDominioNaoPossuirItem() {
//        Room inicio = new Room("inicio", 0, 0);
//        Room destino = new Room("destino", 1, 0);
//        Player player = new Player(inicio);
//        Item item = new Item("chave", Item.Type.CHAVE, "chave");
//        player.adicionarItem(item);
//        assertThat(player.possuiItem(null)).isFalse();
//    }
//
//    @Test
//    @DisplayName("Teste de domínio: deve retornar falso que possui item usadpo caso obedeça as regras para poder usar item")
//    void testeDominioUsarItem() {
//        Room inicio = new Room("inicio", 0, 0);
//        Room destino = new Room("destino", 1, 0);
//        Player player = new Player(inicio);
//        Item item = new Item("chave", Item.Type.AMULETO_VISAO, "descrição");
//        player.usarItem(item);
//        assertThat(player.getInventario().contains(item)).isFalse();
//    }
//
//    //public boolean possuiItem(Item.Type tipo) {
//        //return inventario.stream().anyMatch(i -> i.getTipo() == tipo);
//   // }
//
//
//
//}
//
//
//
////public boolean moverPara(Room destino) {
////        if (destino == null) return false;
////        if (destino.isBloqueada()) {
////            // verifica se possui chave
////            boolean temChave = inventario.stream().anyMatch(i -> i.getTipo() == Item.Type.CHAVE);
////            if (!temChave) return false;
////        }
////        this.posicaoAtual = destino;
////        historico.push(destino);
////        return true;
////    }
//>>>>>>> Stashed changes
