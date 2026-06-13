package st.project.game.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import st.project.game.model.User;

import java.io.File;

/**
 * ─── BASE DE INTEGRAÇÃO: SaveManager / UserManager ──────────────────────────
 * <p>
 * Estratégia (conforme Aniche 2022 e slides da disciplina):
 *  - O sistema de arquivos é uma dependência GERENCIADA (managed dependency):
 *    as interações são visíveis apenas internamente ao software.
 *    → Usamos instâncias REAIS (sem mocks) do sistema de arquivos.
 * <p>
 *  - Esta classe base centraliza a lógica de setup/teardown, liberando as
 *    subclasses para focar nos testes propriamente ditos.
 *    (Equivalente ao padrão SqlIntegrationTestBase do slide 30.)
 * <p>
 *  - @BeforeEach: garante diretório "saves/" existente e limpa saves do
 *    usuário de teste, evitando interferência entre execuções.
 *  - @AfterEach: remove saves residuais do usuário de teste.
 * <p>
 * Usuário de teste:
 *  - Login fixo "integration_test_user" isolado dos dados de produção.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public abstract class SaveIntegracaoTestBase {

    /** Login exclusivo dos testes de integração — não conflita com usuários reais. */
    protected static final String TEST_LOGIN = "integration_test_user";

    /** Diretório onde SaveManager persiste os arquivos ser. */
    private static final String SAVE_DIR = "saves";

    protected SaveManager saveManager;
    protected User testUser;

    /**
     * [Antes de cada teste]
     *  1. Garante que o diretório "saves/" existe.
     *  2. Instancia SaveManager (dependência gerenciada real).
     *  3. Cria o usuário de teste.
     *  4. Remove quaisquer saves residuais do teste anterior —
     *     equivalente ao "truncate table" do exemplo do slide 34.
     */
    @BeforeEach
    void setUpIntegration() {
        File saveDir = new File(SAVE_DIR);

        if (!saveDir.exists() && !saveDir.mkdirs()) {
            throw new IllegalStateException(
                    "Não foi possível criar o diretório de saves."
            );
        }

        saveManager = new SaveManager();

        saveManager.deleteAllSaves(TEST_LOGIN);

        testUser = new User(
                TEST_LOGIN,
                "testpass123",
                "test_avatar.png"
        );
    }

    /**
     * [Após cada teste]
     *  Remove todos os saves gerados pelo teste, mantendo o diretório limpo.
     */
    @AfterEach
    void tearDownIntegration() {
        saveManager.deleteAllSaves(TEST_LOGIN);
    }
}
