package st.project.game.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.Item;
import st.project.game.model.Player;
import st.project.game.model.Room;

import java.util.Stack;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE FRONTEIRA: Player ────────────────────────────────────────────
 *
 * Cobre: movimentação com null, sala bloqueada sem chave, inventário vazio,
 * remoção de item ausente, possuiItem com tipo null.
 * Dublê de teste: nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("Player – Testes de Fronteira")
class PlayerFronteiraTest {

    private Room salaInicial;
    private Player player;

    @BeforeEach
    void setUp() {
        salaInicial = new Room("Sala Inicial", 0, 0);
        player      = new Player(salaInicial);
    }

    @Test
    @DisplayName("Fronteira: moverPara(null) retorna false e mantém posição")
    void testeFronteiraMoverParaNull() {
        assertThat(player.moverPara(null)).isFalse();
        assertThat(player.getPosicaoAtual()).isEqualTo(salaInicial);
    }

    @Test
    @DisplayName("Fronteira: moverPara sala bloqueada sem chave retorna false")
    void testeFronteiraMoverParaBloqueadaSemChave() {
        Room bloqueada = new Room("Sagrado", 4, 4);
        bloqueada.setBloqueada(true);
        player.adicionarItem(new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3"));
        assertThat(player.moverPara(bloqueada)).isFalse();
    }

    @Test
    @DisplayName("Fronteira: getInventario retorna lista vazia ao iniciar")
    void testeFronteiraInventarioVazioInicial() {
        assertThat(player.getInventario()).isEmpty();
    }

    @Test
    @DisplayName("Fronteira: getHistorico contém apenas a sala inicial")
    void testeFronteiraHistoricoComUmaSala() {
        Stack<Room> esperado = new Stack<>();
        esperado.push(salaInicial);
        assertThat(player.getHistorico()).isEqualTo(esperado);
    }

    @Test
    @DisplayName("Fronteira: adicionarItem(null) não altera inventário")
    void testeFronteiraAdicionarItemNulo() {
        player.adicionarItem(null);
        assertThat(player.getInventario()).doesNotContain((Item) null);
    }

    @Test
    @DisplayName("Fronteira: removerItem ausente retorna null")
    void testeFronteiraRemoverItemAusente() {
        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2");
        assertThat(player.removerItem(pocao)).isNull();
    }

    @Test
    @DisplayName("Fronteira: possuiItem com tipo null retorna false")
    void testeFronteiraPossuiItemTipoNull() {
        assertThat(player.possuiItem(null)).isFalse();
    }

    @Test
    @DisplayName("Fronteira: possuiItem retorna false para tipo ausente no inventário")
    void testeFronteiraPossuiItemTipoAusente() {
        player.adicionarItem(new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2"));
        assertThat(player.possuiItem(Item.Type.CHAVE)).isFalse();
    }

    @Test
    @DisplayName("Fronteira: inventário com apenas 1 item — List de tamanho 1")
    void testeFronteiraInventarioUmItem() {
        Item chave = new Item("Chave", Item.Type.CHAVE, "Abre");
        player.adicionarItem(chave);
        assertThat(player.getInventario()).hasSize(1).containsExactly(chave);
    }
}
