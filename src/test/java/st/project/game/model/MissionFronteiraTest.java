package st.project.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE FRONTEIRA: Mission ───────────────────────────────────────────
 *
 * Cobre: valores-limite e situações extremas do fluxo de missão.
 *
 * Decisões MC/DC testadas:
 *   (A) caliceColetado=false && possuiItem=false → nada muda
 *   (B) caliceColetado=false && possuiItem=true  → marca cálice, conclui
 *   (C) caliceColetado=true  (já coletado)       → estado preservado
 *
 * Dublê de teste: nenhum — classes de estado puras.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("Mission – Testes de Fronteira")
class MissionFronteiraTest {

    private Room    salaDoCalice;
    private Mission missao;

    @BeforeEach
    void setUp() {
        salaDoCalice = new Room("sagrado", 4, 4);
        missao       = new Mission(salaDoCalice);
    }

    // ── (A) Fronteira inferior: sem cálice ────────────────────────────────

    @Test
    @DisplayName("Fronteira (A): sem cálice → missão e cálice ambos false")
    void testeFronteiraSemCaliceTudoFalse() {
        Player jogador = new Player(salaDoCalice);
        missao.verificarProgresso(jogador);

        assertThat(missao.isCaliceColetado()).isFalse();
        assertThat(missao.isMissaoConcluida()).isFalse();
    }

    // ── (B) Fronteira: cálice obtido pela primeira vez ────────────────────

    @Test
    @DisplayName("Fronteira (B): ao receber cálice, isCaliceColetado e isMissaoConcluida ficam true")
    void testeFronteiraCaliceObtidoPrimeiraVez() {
        Player jogador = new Player(salaDoCalice);
        jogador.adicionarItem(new Item("Cálice Mágico", Item.Type.CALICE, "Missão"));

        missao.verificarProgresso(jogador);

        assertThat(missao.isCaliceColetado()).isTrue();
        assertThat(missao.isMissaoConcluida()).isTrue();
    }

    // ── (C) Missão já concluída permanece concluída após remoção ──────────

    @Test
    @DisplayName("Fronteira (C): missão permanece concluída mesmo após remoção do cálice")
    void testeFronteiraMissaoPermaneceConcluidaAposRemocaoCalice() {
        Player jogador = new Player(salaDoCalice);
        Item calice = new Item("Cálice Mágico", Item.Type.CALICE, "Missão");
        jogador.adicionarItem(calice);

        missao.verificarProgresso(jogador);        // conclui
        jogador.removerItem(calice);               // remove do inventário
        missao.verificarProgresso(jogador);        // reavalia

        assertThat(missao.isMissaoConcluida()).isTrue();
    }

    // ── Fronteira: inventário vazio ───────────────────────────────────────

    @Test
    @DisplayName("Fronteira: jogador com inventário vazio → calice não coletado")
    void testeFronteiraInventarioVazioNaoColeta() {
        Player jogador = new Player(salaDoCalice);
        missao.verificarProgresso(jogador);

        assertThat(missao.isCaliceColetado()).isFalse();
    }

    // ── Fronteira: apenas outros itens, sem cálice ────────────────────────

    @Test
    @DisplayName("Fronteira: poção e chave no inventário, mas sem cálice → não conclui")
    void testeFronteiraSoOutrosItensNaoConclui() {
        Player jogador = new Player(salaDoCalice);
        jogador.adicionarItem(new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2"));
        jogador.adicionarItem(new Item("Chave", Item.Type.CHAVE, "Abre"));

        missao.verificarProgresso(jogador);

        assertThat(missao.isMissaoConcluida()).isFalse();
    }

    // ── Fronteira: sala do cálice é referência correta ────────────────────

    @Test
    @DisplayName("Fronteira: getSalaCalice nunca é null após construção")
    void testeFronteiraGetSalaCaliceNaoNulo() {
        assertThat(missao.getSalaCalice()).isNotNull();
    }
}
