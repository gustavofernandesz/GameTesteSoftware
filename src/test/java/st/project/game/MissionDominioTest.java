package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.Item;
import st.project.game.model.Mission;
import st.project.game.model.Player;
import st.project.game.model.Room;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE DOMÍNIO: Mission ─────────────────────────────────────────────
 *
 * Escopo: regras de negócio da missão — verificação de progresso, estado do
 * cálice e referência à sala-alvo.
 *
 * Dublê de teste: nenhum — Mission/Player/Room são classes de estado puras.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("Mission – Testes de Domínio")
class MissionDominioTest {

    private Room    salaDoCalice;
    private Mission missao;

    @BeforeEach
    void setUp() {
        salaDoCalice = new Room("sagrado", 4, 4);
        missao       = new Mission(salaDoCalice);
    }

    // ── verificarProgresso ─────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: missão concluída quando jogador possui cálice")
    void testeDominioMissaoConcluidaComCalice() {
        Player jogador = new Player(salaDoCalice);
        jogador.adicionarItem(new Item("Cálice Mágico", Item.Type.CALICE, "O objeto da missão"));

        missao.verificarProgresso(jogador);

        assertThat(missao.isMissaoConcluida()).isTrue();
    }

    @Test
    @DisplayName("Domínio: missão NÃO concluída quando jogador não possui cálice")
    void testeDominioMissaoNaoConcluidaSemCalice() {
        Player jogador = new Player(salaDoCalice);

        missao.verificarProgresso(jogador);

        assertThat(missao.isMissaoConcluida()).isFalse();
    }

    @Test
    @DisplayName("Domínio: missão com outros itens mas sem cálice permanece não concluída")
    void testeDominioMissaoNaoConcluidaComOutrosItens() {
        Player jogador = new Player(salaDoCalice);
        jogador.adicionarItem(new Item("Lupa",  Item.Type.LUPA,  "Revela itens"));
        jogador.adicionarItem(new Item("Chave", Item.Type.CHAVE, "Abre"));

        missao.verificarProgresso(jogador);

        assertThat(missao.isMissaoConcluida()).isFalse();
    }

    // ── isCaliceColetado ───────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: isCaliceColetado true após jogador obter cálice")
    void testeDominioCaliceColetado() {
        Player jogador = new Player(salaDoCalice);
        jogador.adicionarItem(new Item("Cálice Mágico", Item.Type.CALICE, "O objeto da missão"));

        missao.verificarProgresso(jogador);

        assertThat(missao.isCaliceColetado()).isTrue();
    }

    @Test
    @DisplayName("Domínio: isCaliceColetado false antes de qualquer progresso")
    void testeDominioCaliceNaoColetadoInicial() {
        assertThat(missao.isCaliceColetado()).isFalse();
    }

    // ── getSalaCalice ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: getSalaCalice retorna a sala informada na construção")
    void testeDominioGetSalaCalice() {
        assertThat(missao.getSalaCalice()).isEqualTo(salaDoCalice);
    }

    // ── Idempotência e persistência do estado ──────────────────────────────

    @Test
    @DisplayName("Domínio: verificarProgresso pode ser chamado múltiplas vezes sem mudar estado")
    void testeDominioVerificarProgressoIdempotente() {
        Player jogador = new Player(salaDoCalice);
        jogador.adicionarItem(new Item("Cálice Mágico", Item.Type.CALICE, "O objeto da missão"));

        missao.verificarProgresso(jogador);
        missao.verificarProgresso(jogador);

        assertThat(missao.isMissaoConcluida()).isTrue();
        assertThat(missao.isCaliceColetado()).isTrue();
    }

    @Test
    @DisplayName("Domínio: missão permanece concluída mesmo após remoção do cálice do inventário")
    void testeDominioMissaoPermaneceConcluidaAposRemocao() {
        Player jogador = new Player(salaDoCalice);
        Item calice = new Item("Cálice Mágico", Item.Type.CALICE, "O objeto da missão");
        jogador.adicionarItem(calice);

        missao.verificarProgresso(jogador); // conclui
        jogador.removerItem(calice);        // remove do inventário
        missao.verificarProgresso(jogador); // reavalia sem cálice

        // Estado persiste — missão continua concluída
        assertThat(missao.isMissaoConcluida()).isTrue();
    }

    @Test
    @DisplayName("Domínio: missão inicia com caliceColetado e missaoConcluida ambos false")
    void testeDominioEstadoInicialFalse() {
        assertThat(missao.isCaliceColetado()).isFalse();
        assertThat(missao.isMissaoConcluida()).isFalse();
    }
}