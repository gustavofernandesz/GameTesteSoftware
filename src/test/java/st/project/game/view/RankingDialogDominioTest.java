package st.project.game.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE DOMÍNIO: RankingDialog ───────────────────────────────────────
 *
 * Escopo:
 *   - ordenação do ranking
 *   - renderização dos dados dos usuários
 *   - exibição correta das colunas
 *   - funcionamento do botão Fechar
 *
 * Dublês:
 *   nenhum — apenas objetos reais.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("RankingDialog – Testes de Domínio")
class RankingDialogDominioTest {

    @Test
    @DisplayName("Domínio: ranking é ordenado pela maior pontuação")
    void testeDominioRankingOrdenadoPorPontuacao() {

        User u1 = new User("alice", "123", "a.png");
        User u2 = new User("bob", "123", "b.png");
        User u3 = new User("carol", "123", "c.png");

        u1.updateScore(100);
        u2.updateScore(900);
        u3.updateScore(500);

        RankingDialog dialog =
                new RankingDialog(List.of(u1, u2, u3));

        JTable table = findTable(dialog);

        assertThat(table.getValueAt(0, 0)).isEqualTo("bob");
        assertThat(table.getValueAt(1, 0)).isEqualTo("carol");
        assertThat(table.getValueAt(2, 0)).isEqualTo("alice");

        dialog.dispose();
    }

    @Test
    @DisplayName("Domínio: tabela exibe login, score e sessões")
    void testeDominioTabelaExibeDadosUsuario() {

        User user = new User("player", "123", "avatar.png");
        user.updateScore(777);
        user.incrementSessions();
        user.incrementSessions();

        RankingDialog dialog =
                new RankingDialog(List.of(user));

        JTable table = findTable(dialog);

        assertThat(table.getValueAt(0, 0)).isEqualTo("player");
        assertThat(table.getValueAt(0, 1)).isEqualTo(777);
        assertThat(table.getValueAt(0, 2)).isEqualTo(2);

        dialog.dispose();
    }

    @Test
    @DisplayName("Domínio: botão fechar fecha o diálogo")
    void testeDominioBotaoFechar() {

        User user = new User("player", "123", "avatar.png");

        RankingDialog dialog =
                new RankingDialog(List.of(user));

        JButton closeBtn = findButton(dialog, "Fechar");

        closeBtn.doClick();

        assertThat(dialog.isDisplayable()).isFalse();
    }

    private JTable findTable(Container container) {

        for (Component component : container.getComponents()) {

            if (component instanceof JScrollPane scrollPane) {

                JViewport viewport = scrollPane.getViewport();

                if (viewport.getView() instanceof JTable table) {
                    return table;
                }
            }

            if (component instanceof Container child) {
                JTable table = findTable(child);

                if (table != null) {
                    return table;
                }
            }
        }

        return null;
    }

    private JButton findButton(Container container, String text) {

        for (Component component : container.getComponents()) {

            if (component instanceof JButton button &&
                    button.getText().equals(text)) {

                return button;
            }

            if (component instanceof Container child) {

                JButton result = findButton(child, text);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}