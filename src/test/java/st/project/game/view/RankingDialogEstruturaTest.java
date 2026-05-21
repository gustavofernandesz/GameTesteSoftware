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
 * ─── TESTES ESTRUTURAIS: RankingDialog ─────────────────────────────────────
 *
 * Foco:
 *   cobertura estrutural e MC/DC das decisões internas.
 *
 * Decisões cobertas:
 *
 *   (A) stream.sorted(reversed)
 *       → ordena descrescente
 *
 *   (B) loop for(User u : sorted)
 *       → executa
 *       → não executa
 *
 *   (C) botão Fechar
 *       → dispose()
 *
 *   (D) propriedades visuais da JTable
 *       → enabled false
 *       → cores aplicadas
 *
 * Dublês:
 *   nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("RankingDialog – Testes Estruturais (MC/DC)")
class RankingDialogEstruturaTest {

    // ── (A) ordenação ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (A): ordenação decrescente por bestScore")
    void testeEstruturaAOrdenacao() {

        User baixo = new User("baixo", "1", "a.png");
        User alto  = new User("alto",  "1", "b.png");

        baixo.updateScore(10);
        alto.updateScore(999);

        RankingDialog dialog =
                new RankingDialog(List.of(baixo, alto));

        JTable table = findTable(dialog);

        assertThat(table.getValueAt(0, 0)).isEqualTo("alto");
        assertThat(table.getValueAt(1, 0)).isEqualTo("baixo");

        dialog.dispose();
    }

    // ── (B1) loop executa ────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (B1): loop adiciona linhas corretamente")
    void testeEstruturaB1LoopAdicionaLinhas() {

        User u1 = new User("u1", "1", "a.png");
        User u2 = new User("u2", "1", "a.png");

        RankingDialog dialog =
                new RankingDialog(List.of(u1, u2));

        JTable table = findTable(dialog);

        assertThat(table.getRowCount()).isEqualTo(2);

        dialog.dispose();
    }

    // ── (B2) loop não executa ────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (B2): loop não executa com lista vazia")
    void testeEstruturaB2LoopNaoExecuta() {

        RankingDialog dialog =
                new RankingDialog(List.of());

        JTable table = findTable(dialog);

        assertThat(table.getRowCount()).isZero();

        dialog.dispose();
    }

    // ── (C) botão fechar ─────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (C): botão fechar executa dispose")
    void testeEstruturaCBotaoFechar() {

        User user = new User("u", "1", "a.png");

        RankingDialog dialog =
                new RankingDialog(List.of(user));

        JButton button = findButton(dialog, "Fechar");

        button.doClick();

        assertThat(dialog.isDisplayable()).isFalse();
    }

    // ── (D) propriedades JTable ──────────────────────────────────────────

    @Test
    @DisplayName("Estrutura (D): JTable possui propriedades visuais esperadas")
    void testeEstruturaDPropriedadesTabela() {

        User user = new User("u", "1", "a.png");

        RankingDialog dialog =
                new RankingDialog(List.of(user));

        JTable table = findTable(dialog);

        assertThat(table.isEnabled()).isFalse();
        assertThat(table.getForeground()).isEqualTo(Color.WHITE);

        dialog.dispose();
    }

    @Test
    @DisplayName("Estrutura: DefaultTableModel possui colunas esperadas")
    void testeEstruturaColunasTabela() {

        User user = new User("u", "1", "a.png");

        RankingDialog dialog =
                new RankingDialog(List.of(user));

        JTable table = findTable(dialog);

        DefaultTableModel model =
                (DefaultTableModel) table.getModel();

        assertThat(model.getColumnName(0)).isEqualTo("Login");
        assertThat(model.getColumnName(1))
                .isEqualTo("Melhor Pontuação");
        assertThat(model.getColumnName(2))
                .isEqualTo("Sessões");

        dialog.dispose();
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