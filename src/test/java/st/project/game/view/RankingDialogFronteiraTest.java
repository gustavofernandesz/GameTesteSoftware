package st.project.game.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import st.project.game.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE FRONTEIRA: RankingDialog ────────────────────────────────────
 *
 * Cobre:
 *   - lista vazia
 *   - apenas um usuário
 *   - score zero
 *   - sessões zero
 *
 * Dublês:
 *   nenhum.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("RankingDialog – Testes de Fronteira")
class RankingDialogFronteiraTest {

    @Test
    @DisplayName("Fronteira: ranking vazio gera tabela sem linhas")
    void testeFronteiraRankingVazio() {

        RankingDialog dialog =
                new RankingDialog(Collections.emptyList());

        JTable table = findTable(dialog);

        assertThat(table.getRowCount()).isZero();

        dialog.dispose();
    }

    @Test
    @DisplayName("Fronteira: ranking com apenas 1 usuário")
    void testeFronteiraUmUsuario() {

        User user = new User("solo", "123", "a.png");

        RankingDialog dialog =
                new RankingDialog(List.of(user));

        JTable table = findTable(dialog);

        assertThat(table.getRowCount()).isEqualTo(1);

        dialog.dispose();
    }

    @Test
    @DisplayName("Fronteira: usuário com score zero")
    void testeFronteiraScoreZero() {

        User user = new User("zero", "123", "a.png");

        RankingDialog dialog =
                new RankingDialog(List.of(user));

        JTable table = findTable(dialog);

        assertThat(table.getValueAt(0, 1)).isEqualTo(0);

        dialog.dispose();
    }

    @Test
    @DisplayName("Fronteira: usuário com zero sessões")
    void testeFronteiraZeroSessoes() {

        User user = new User("novo", "123", "a.png");

        RankingDialog dialog =
                new RankingDialog(List.of(user));

        JTable table = findTable(dialog);

        assertThat(table.getValueAt(0, 2)).isEqualTo(0);

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
}