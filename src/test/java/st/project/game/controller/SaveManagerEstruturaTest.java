package st.project.game.controller;

import org.junit.jupiter.api.*;
import st.project.game.model.GameModel;
import st.project.game.model.User;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES ESTRUTURAIS: SaveManager ────────────────────────────────────────
 *
 * Foco:
 *  cobertura MC/DC das decisões internas.
 *
 * Decisões cobertas:
 *   (A) diretório existe / não existe
 *   (B) arquivo existe → loadGame carrega
 *   (C) arquivo inexistente → loadGame retorna null
 *   (D) deleteIfExists true
 *   (E) deleteIfExists false
 *   (F) slot livre encontrado
 *   (G) nenhum slot livre
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("SaveManager – Testes Estruturais")
class SaveManagerEstruturaTest {

    private SaveManager saveManager;
    private User user;

    @BeforeEach
    void setUp() {
        saveManager = new SaveManager();
        user = new User("estruturaUser", "123", "avatar.png");

        saveManager.deleteAllSaves(user.getLogin());
    }

    @AfterEach
    void tearDown() {
        saveManager.deleteAllSaves(user.getLogin());
    }

    @Test
    @DisplayName("Estrutura (A): construtor cria diretório saves se não existir")
    void testeEstruturaACriaDiretorio() {
        File dir = new File("saves");

        assertThat(dir.exists()).isTrue();
    }

    @Test
    @DisplayName("Estrutura (B): loadGame com arquivo existente retorna GameModel")
    void testeEstruturaBLoadExistente() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 0);

        assertThat(saveManager.loadGame(user, 0)).isNotNull();
    }

    @Test
    @DisplayName("Estrutura (C): loadGame sem arquivo retorna null")
    void testeEstruturaCLoadInexistente() {
        assertThat(saveManager.loadGame(user, 0)).isNull();
    }

    @Test
    @DisplayName("Estrutura (D): deleteSave existente retorna true")
    void testeEstruturaDDeleteExistente() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 1);

        assertThat(saveManager.deleteSave(user, 1)).isTrue();
    }

    @Test
    @DisplayName("Estrutura (E): deleteSave inexistente retorna false")
    void testeEstruturaEDeleteInexistente() {
        assertThat(saveManager.deleteSave(user, 1)).isFalse();
    }

    @Test
    @DisplayName("Estrutura (F): getFreeSlot encontra slot disponível")
    void testeEstruturaFGetFreeSlot() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 0);

        assertThat(saveManager.getFreeSlot(user)).isEqualTo(1);
    }

    @Test
    @DisplayName("Estrutura (G): getFreeSlot retorna -1 sem slots livres")
    void testeEstruturaGSemSlotLivre() {
        GameModel model = mock(GameModel.class, withSettings().serializable());

        saveManager.saveGame(model, user, 0);
        saveManager.saveGame(model, user, 1);
        saveManager.saveGame(model, user, 2);

        assertThat(saveManager.getFreeSlot(user)).isEqualTo(-1);
    }

    @Test
    @DisplayName("Estrutura: getSaveFileName gera nome corretamente")
    void testeEstruturaGetSaveFileName() throws Exception {
        Method method = SaveManager.class.getDeclaredMethod(
                "getSaveFileName",
                String.class,
                int.class
        );

        method.setAccessible(true);

        String path = (String) method.invoke(saveManager, "abc", 2);

        assertThat(path)
                .contains("save_abc_2.ser")
                .contains("saves");
    }

    // ── saveGame IOException ───────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: saveGame lança RuntimeException em IOException")
    void testeEstruturaSaveGameIOException() throws Exception {

        SaveManager saveManager = new SaveManager();
        User user = new User("io_save_user", "123", "a.png");

        GameModel model = mock(GameModel.class, withSettings().serializable());

        Method method = SaveManager.class.getDeclaredMethod(
                "getSaveFileName",
                String.class,
                int.class
        );

        method.setAccessible(true);

        String path = (String) method.invoke(saveManager, user.getLogin(), 0);

        File target = new File(path);

        // garante limpeza total anterior
        if (target.exists()) {
            target.delete();
        }

        // cria DIRETÓRIO com o mesmo nome do arquivo esperado
        boolean created = target.mkdir();

        assertThat(created).isTrue();
        assertThat(target.isDirectory()).isTrue();

        assertThatThrownBy(() ->
                saveManager.saveGame(model, user, 0)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao salvar jogo");

        target.delete();
    }

// ── loadGame Exception ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: loadGame lança RuntimeException quando arquivo está corrompido")
    void testeEstruturaLoadGameException() throws Exception {
        SaveManager saveManager = new SaveManager();

        User user = new User("io_load_user", "123", "a.png");

        // cria arquivo inválido manualmente
        Method method = SaveManager.class.getDeclaredMethod(
                "getSaveFileName",
                String.class,
                int.class
        );

        method.setAccessible(true);

        String path = (String) method.invoke(saveManager, user.getLogin(), 0);

        try (FileWriter writer = new FileWriter(path)) {
            writer.write("arquivo corrompido");
        }

        assertThatThrownBy(() -> saveManager.loadGame(user, 0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao carregar jogo");

        new File(path).delete();
    }

// ── deleteSave IOException ─────────────────────────────────────────────────

    // ── deleteSave IOException ─────────────────────────────────────────────────

    @Test
    @DisplayName("Estrutura: deleteSave retorna false quando ocorre IOException")
    void testeEstruturaDeleteSaveIOException() throws Exception {
        SaveManager saveManager = new SaveManager();

        User user = new User("io_delete_user", "123", "a.png");

        Method method = SaveManager.class.getDeclaredMethod(
                "getSaveFileName",
                String.class,
                int.class
        );

        method.setAccessible(true);

        String path = (String) method.invoke(saveManager, user.getLogin(), 0);

        // cria diretório no lugar do arquivo
        File dir = new File(path);
        dir.mkdirs();

        // cria conteúdo dentro dele para impedir remoção
        File nested = new File(dir, "arquivo.txt");
        nested.createNewFile();

        assertThat(saveManager.deleteSave(user, 0)).isFalse();

        // limpeza manual
        nested.delete();
        dir.delete();
    }

    @Test
    @DisplayName("Estrutura: construtor cria diretório saves quando não existe")
    void testeEstruturaConstrutorCriaDiretorioQuandoNaoExiste() {

        File dir = new File("saves");

        // remove tudo antes
        if (dir.exists()) {
            File[] files = dir.listFiles();

            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }

            dir.delete();
        }

        assertThat(dir.exists()).isFalse();

        // executa construtor
        new SaveManager();

        // branch TRUE do if (!dir.exists())
        assertThat(dir.exists()).isTrue();
        assertThat(dir.isDirectory()).isTrue();
    }
}