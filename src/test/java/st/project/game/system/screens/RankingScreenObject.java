package st.project.game.system.screens;

import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTableFixture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RankingScreenObject — Screen Object do diálogo de Ranking (RankingDialog).
 *
 * RankingDialog é um JDialog MODAL: setVisible(true) é chamado dentro do
 * próprio construtor (ver RankingDialog.java) e bloqueia a EDT até dispose().
 * Por isso este Screen Object recebe:
 *   - a janela "pai" (loginWindow ou menuWindow), de onde o diálogo foi
 *     aberto — herdada via BaseScreen, útil para robot()/contexto;
 *   - o DialogFixture já localizado pelo chamador através de
 *     window.dialog(timeout(...)), pois é o diálogo, e não a janela pai,
 *     que contém a tabela de ranking e o botão "Fechar".
 *
 * Componentes reais (RankingDialog.java):
 *   JTable      → tabela única, sem name, colunas
 *                 ["Login", "Melhor Pontuação", "Sessões"]
 *   closeButton → JButton "Fechar" (setName("closeButton"))
 */
public class RankingScreenObject extends BaseScreen {

    private final DialogFixture dialog;

    public RankingScreenObject(FrameFixture parentWindow, DialogFixture dialog) {
        super(parentWindow);
        this.dialog = dialog;
    }

    // ── Verificações ──────────────────────────────────────────────────────

    /** Verifica que o RankingDialog está aberto, visível e modal. */
    public RankingScreenObject verificarRankingAberto() {
        dialog.requireVisible();
        assertThat(dialog.target().isModal())
                .as("RankingDialog deve ser modal")
                .isTrue();
        assertThat(dialog.target().getTitle())
                .isEqualTo("Ranking de Jogadores");
        return this;
    }

    /** Verifica que a tabela de ranking está visível com as colunas esperadas. */
    public RankingScreenObject verificarConteudoVisivel() {
        JTableFixture tabela = dialog.table();
        tabela.requireVisible();

        String[] colunasEsperadas = {"Login", "Melhor Pontuação", "Sessões"};
        for (int i = 0; i < colunasEsperadas.length; i++) {
            assertThat(tabela.target().getColumnName(i))
                    .isEqualTo(colunasEsperadas[i]);
        }
        return this;
    }

    // ── Ações ─────────────────────────────────────────────────────────────

    /** Clica em "Fechar", encerrando o diálogo modal (dispose()). */
    public RankingScreenObject fechar() {
        JButtonFixture fecharBtn = dialog.button("closeButton");
        fecharBtn.requireVisible().requireEnabled().click();
        return this;
    }
}
