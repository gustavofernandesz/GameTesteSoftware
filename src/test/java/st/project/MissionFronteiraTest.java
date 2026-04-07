package st.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.Item;
import st.project.game.Mission;
import st.project.game.Player;
import st.project.game.Room;

import static org.assertj.core.api.Assertions.assertThat;

public class MissionFronteiraTest {
    Room salaDoCalice = new Room("Sala Do Calice", 0, 1);
    Mission missao = new Mission(salaDoCalice);

    // verificarProgresso

    @Test
    @DisplayName("Teste de Fronteira: Missao não concluida se o jogador não possui calice")
    void testeFronteiraJogadorNaoPossuiCalice() {
        Player jog = new Player(salaDoCalice);

        missao.verificarProgresso(jog);
        assertThat(missao.isMissaoConcluida()).isFalse();
    }

    // isCaliceColetado

    @Test
    @DisplayName("Teste de Fronteira: Se o jogador perder o calice depois de concluir a missao a missao continua concluida")
    void testeFronteiraCaliceJaFoiColetado() {
        Item calice = new Item("Cálice Mágico", Item.Type.CALICE, "O objeto da missão");
        Player jog = new Player(salaDoCalice);
        jog.adicionarItem(calice);

        missao.verificarProgresso(jog);
        assertThat(missao.isMissaoConcluida()).isTrue();

        jog.removerItem(calice);

        missao.verificarProgresso(jog);
        assertThat(missao.isMissaoConcluida()).isTrue();
    }

    @Test
    @DisplayName("Teste de Fronteira: Se o jogador não coletou o calice deve retornar False")
    void testeFronteiraCaliceNaoColetado() {
        Player jog = new Player(salaDoCalice);

        missao.verificarProgresso(jog);
        assertThat(missao.isCaliceColetado()).isFalse();
    }
}
