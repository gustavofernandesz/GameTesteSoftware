package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.Item;
import st.project.game.model.Player;
import st.project.game.model.Room;

import java.util.List;
import java.util.Stack;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE DOMÍNIO: Player ──────────────────────────────────────────────
 *
 * Escopo: valida as regras de negócio — movimentação, inventário e histórico.
 * Dublê de teste: nenhum — Player é classe de estado pura.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("Player – Testes de Domínio")
class PlayerDominioTest {

    private Room salaInicial;
    private Player player;

    @BeforeEach
    void setUp() {
        salaInicial = new Room("Sala Inicial", 0, 0);
        player      = new Player(salaInicial);
    }

    @Test
    @DisplayName("Domínio: getPosicaoAtual retorna a sala inicial")
    void testeDominioGetPosicaoAtual() {
        assertThat(player.getPosicaoAtual()).isEqualTo(salaInicial);
    }

    @Test
    @DisplayName("Domínio: moverPara sala válida retorna true e atualiza posição")
    void testeDominioMoverParaValido() {
        Room destino = new Room("Segunda Sala", 0, 1);
        assertThat(player.moverPara(destino)).isTrue();
        assertThat(player.getPosicaoAtual()).isEqualTo(destino);
    }

    @Test
    @DisplayName("Domínio: moverPara sala bloqueada com chave retorna true")
    void testeDominioMoverParaBloqueadaComChave() {
        Room bloqueada = new Room("Sagrado", 4, 4);
        bloqueada.setBloqueada(true);
        player.adicionarItem(new Item("Chave", Item.Type.CHAVE, "Abre"));
        assertThat(player.moverPara(bloqueada)).isTrue();
    }

    @Test
    @DisplayName("Domínio: moverPara atualiza o histórico de salas")
    void testeDominioHistoricoAtualizado() {
        Room destino = new Room("Segunda Sala", 0, 1);
        player.moverPara(destino);
        assertThat(player.getHistorico()).contains(destino);
    }

    @Test
    @DisplayName("Domínio: adicionarItem insere item no inventário")
    void testeDominioAdicionarItem() {
        Item amuleto = new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3 mov");
        player.adicionarItem(amuleto);
        assertThat(player.getInventario()).contains(amuleto);
    }

    @Test
    @DisplayName("Domínio: removerItem extrai o item do inventário e o retorna")
    void testeDominioRemoverItem() {
        Item amuleto = new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3 mov");
        player.adicionarItem(amuleto);
        assertThat(player.removerItem(amuleto)).isEqualTo(amuleto);
        assertThat(player.getInventario()).isEmpty();
    }

    @Test
    @DisplayName("Domínio: possuiItem retorna true para tipo presente no inventário")
    void testeDominioPossuiItem() {
        player.adicionarItem(new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2"));
        assertThat(player.possuiItem(Item.Type.POCAO_VELOCIDADE)).isTrue();
    }

    @Test
    @DisplayName("Domínio: usarItem consumível remove o item do inventário")
    void testeDominioUsarItemConsumivel() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2");
        player.adicionarItem(pocao);
        player.usarItem(pocao);
        assertThat(player.getInventario()).isEmpty();
    }

    @Test
    @DisplayName("Domínio: usarItem não-consumível (Chave) mantém no inventário")
    void testeDominioUsarItemNaoConsumivel() {
        Item chave = new Item("Chave", Item.Type.CHAVE, "Abre");
        player.adicionarItem(chave);
        player.usarItem(chave);
        assertThat(player.getInventario()).contains(chave);
    }



}
