package st.project.game.controller;

import org.junit.jupiter.api.*;
import st.project.game.model.GameModel;
import st.project.game.model.User;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ─── TESTES DE INTEGRAÇÃO: SaveManager ──────────────────────────────────────
 * <p>
 * DEPENDÊNCIAS EXTERNAS
 * ─────────────────────
 *  • Sistema de arquivos (pasta "saves/") → dependência GERENCIADA
 *    → Usamos instância REAL (sem mock), conforme slide 11 da disciplina.
 *    → GameModel real é serializado/desserializado para garantir que o contrato
 *      de persistência seja exercitado de ponta a ponta.
 * <p>
 * ESTRUTURAS DE ACORDO (contratos que os testes verificam)
 * ─────────────────────────────────────────────────────────
 *  EA-1 saveGame → arquivo ser gerado no caminho correto
 *        (saves/save_<login>_<slot>.ser)
 * <p>
 *  EA-2 loadGame → desserializa o mesmo GameModel salvo por saveGame
 *        (round-trip: estado antes de salvar == estado após carregar)
 * <p>
 *  EA-3 listSlots → itera slots 0..MAX-1 e adiciona slot i se e somente se
 *        o arquivo correspondente existir
 *        Predicado: file.exists() → true | false  (teste de fronteira)
 * <p>
 *  EA-4 getFreeSlot → retorna o menor slot i tal que o arquivo NÃO existe;
 *        retorna −1 quando todos os slots estão ocupados
 *        Predicado: !file.exists() → true | false | todos=false (→ −1)
 * <p>
 *  EA-5 deleteSave → remove arquivo existente (retorna true) ou
 *        não faz nada se arquivo ausente (retorna false)
 *        Predicado: Files.deleteIfExists → true | false
 * <p>
 *  EA-6 deleteAllSaves (integração SaveManager ↔ UserManager)
 *        → UserManager.deleteUser() delega deleteAllSaves() ao SaveManager;
 *        após deleção do usuário nenhum arquivo de save deve existir
 * <p>
 * WORKFLOW (slide 19)
 * ────────────────────
 *  1. Estabelecer conexão/estado inicial com a dependência externa
 *  2. Confirmar estado inicial
 *  3. Executar operação
 *  4. Verificar saída
 *  5. Encerrar/limpar a dependência
 *  (passos 1 e 5 ficam na classe base SaveIntegrationTestBase)
 * <p>
 * NOTA SOBRE PREDICADOS E COBERTURA (slide 17)
 * ──────────────────────────────────────────────
 *  Cada predicado booleano do SaveManager é exercitado com seus dois valores
 *  (true/false). Onde há fronteira numérica (MAX_SAVES_PER_USER = 3), testamos
 *  os pontos on-point (valor = 3) e off-point (valor = 2 → ainda há vaga) e o
 *  caso de esgotamento total (valor = −1).
 * ─────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("SaveManager – Testes de Integração")
class SaveManagerIntegracaoTest extends SaveIntegracaoTestBase {

    // ─────────────────────────────────────────────────────────────────────────
    // EA-1 + EA-2 | Round-trip: saveGame → arquivo criado → loadGame
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Caminho feliz completo (slide 9, recomendação 1):
     * salvar um GameModel real, verificar arquivo criado, carregar de volta.
     * Exercita a integração real entre SaveManager e o sistema de arquivos com
     * serialização Java (Serializable).
     */
    @Test
    @DisplayName("EA-1/EA-2: salvar GameModel real e carregar round-trip – slot 0")
    void salvarECarregarGameModelRoundTrip() {
        // Arrange: GameModel real (não mock) — dependência gerenciada real
        GameModel modelOriginal = new GameModel(42L); // seed fixo → estado determinístico
        int score = modelOriginal.getScore();

        // Act: persiste no arquivo
        saveManager.saveGame(modelOriginal, testUser, 0);

        // Assert 1 (EA-1): arquivo ser gerado no caminho correto
        File arquivoSave = new File("saves/save_" + TEST_LOGIN + "_0.ser");
        assertThat(arquivoSave).exists()
                .describedAs("Arquivo .ser deve existir após saveGame");

        // Act: carrega do arquivo
        GameModel modelCarregado = saveManager.loadGame(testUser, 0);

        // Assert 2 (EA-2): objeto desserializado e score preservado
        assertThat(modelCarregado).isNotNull()
                .describedAs("loadGame deve retornar objeto não-nulo");
        assertThat(modelCarregado.getScore()).isEqualTo(score)
                .describedAs("Score deve ser idêntico após round-trip de serialização");
    }

    /**
     * Verifica que o round-trip preserva o estado de jogoAtivo,
     * movimentosRestantes e tempoRestante do GameModel.
     */
    @Test
    @DisplayName("EA-2: estado interno do GameModel é preservado após round-trip – slot 1")
    void estadoGameModelPreservadoAposRoundTrip() {
        // Arrange
        GameModel modelOriginal = new GameModel(99L);
        boolean jogoAtivo        = modelOriginal.isJogoAtivo();
        int     movimentos        = modelOriginal.getMovimentosRestantes();
        int     tempo             = modelOriginal.getTempoRestante();

        // Act
        saveManager.saveGame(modelOriginal, testUser, 1);
        GameModel modelCarregado = saveManager.loadGame(testUser, 1);

        // Assert
        assertThat(modelCarregado.isJogoAtivo()).isEqualTo(jogoAtivo);
        assertThat(modelCarregado.getMovimentosRestantes()).isEqualTo(movimentos);
        assertThat(modelCarregado.getTempoRestante()).isEqualTo(tempo);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EA-2 | loadGame com arquivo ausente → retorna null
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Predicado: file.exists() → false
     * loadGame deve retornar null quando não há save no slot solicitado.
     */
    @Test
    @DisplayName("EA-2: loadGame retorna null quando slot não possui arquivo (predicado: exists=false)")
    void loadGameRetornaNullQuandoSlotVazio() {
        // Arrange: nenhum save criado (garantido pelo @BeforeEach)
        // Confirma estado inicial: arquivo realmente ausente
        assertThat(new File("saves/save_" + TEST_LOGIN + "_0.ser")).doesNotExist();

        // Act
        GameModel resultado = saveManager.loadGame(testUser, 0);

        // Assert
        assertThat(resultado).as("loadGame em slot vazio deve retornar null")
                .isNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EA-3  |  listSlots – predicado file.exists() → true / false
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Predicado file.exists() = true para slots 0 e 2, false para slot 1.
     * Verifica que listSlots só devolve os slots cujos arquivos existem.
     */
    @Test
    @DisplayName("EA-3: listSlots retorna exatamente os slots com arquivo existente (exists=true nos slots 0 e 2)")
    void listSlotsRetornaSomenteSlotsExistentes() {
        // Arrange
        GameModel model = new GameModel(7L);
        saveManager.saveGame(model, testUser, 0);   // slot 0: exists = true
        // slot 1: exists = false  (não salvo)
        saveManager.saveGame(model, testUser, 2);   // slot 2: exists = true

        // Confirma estado inicial
        assertThat(new File("saves/save_" + TEST_LOGIN + "_0.ser")).exists();
        assertThat(new File("saves/save_" + TEST_LOGIN + "_1.ser")).doesNotExist();
        assertThat(new File("saves/save_" + TEST_LOGIN + "_2.ser")).exists();

        // Act
        List<Integer> slots = saveManager.listSlots(testUser);

        // Assert: somente slots 0 e 2 retornados, exatamente nessa ordem
        assertThat(slots)
                .describedAs("listSlots deve retornar [0, 2] — slot 1 ausente ignorado")
                .containsExactly(0, 2);
    }

    /**
     * Predicado file.exists() = false para todos os slots.
     * listSlots deve retornar lista vazia.
     */
    @Test
    @DisplayName("EA-3: listSlots retorna lista vazia quando nenhum arquivo existe (exists=false para todos)")
    void listSlotsRetornaListaVaziaQuandoNaoHaSaves() {
        // Arrange: nenhum save (garantido pelo @BeforeEach)
        // Act
        List<Integer> slots = saveManager.listSlots(testUser);

        // Assert
        assertThat(slots).as("listSlots deve ser vazia quando não há saves")
                .isEmpty();
    }

    /**
     * Predicado file.exists() = true para todos os 3 slots.
     * listSlots deve retornar [0, 1, 2].
     */
    @Test
    @DisplayName("EA-3: listSlots retorna todos os 3 slots quando todos os arquivos existem (on-point: MAX_SAVES=3)")
    void listSlotsRetornaTodosOsSlotsOcupados() {
        // Arrange: ocupa todos os slots (on-point do MAX_SAVES_PER_USER = 3)
        GameModel model = new GameModel(13L);
        saveManager.saveGame(model, testUser, 0);
        saveManager.saveGame(model, testUser, 1);
        saveManager.saveGame(model, testUser, 2);

        // Act
        List<Integer> slots = saveManager.listSlots(testUser);

        // Assert
        assertThat(slots)
                .describedAs("listSlots deve retornar [0,1,2] com todos os slots ocupados")
                .containsExactly(0, 1, 2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EA-4  |  getFreeSlot – predicado !file.exists() → true / false / -1
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Predicado: !file.exists() = true no slot 0 (nenhum save criado).
     * getFreeSlot deve retornar 0.
     */
    @Test
    @DisplayName("EA-4: getFreeSlot retorna slot 0 quando todos os slots estão livres (!exists=true no slot 0)")
    void getFreeSlotRetornaZeroQuandoTodosLivres() {
        // Arrange: nenhum save (garantido pelo @BeforeEach)
        // Act
        int freeSlot = saveManager.getFreeSlot(testUser);
        // Assert
        assertThat(freeSlot).isEqualTo(0);
    }

    /**
     * Predicado !file.exists() = false para slots 0 e 1, true para slot 2.
     * getFreeSlot deve retornar 2 (primeiro livre).
     */
    @Test
    @DisplayName("EA-4: getFreeSlot retorna primeiro slot livre (off-point: slots 0 e 1 ocupados, slot 2 livre)")
    void getFreeSlotRetornaPrimeiroSlotLivre() {
        // Arrange: ocupa slots 0 e 1 (off-point: 2 de 3 ocupados → ainda há vaga)
        GameModel model = new GameModel(21L);
        saveManager.saveGame(model, testUser, 0);
        saveManager.saveGame(model, testUser, 1);

        // Act
        int freeSlot = saveManager.getFreeSlot(testUser);

        // Assert
        assertThat(freeSlot).isEqualTo(2);
    }

    /**
     * Predicado !file.exists() = false para todos os slots (on-point: MAX=3).
     * getFreeSlot deve retornar -1 — nenhum slot disponível.
     */
    @Test
    @DisplayName("EA-4: getFreeSlot retorna -1 quando todos os slots estão cheios (on-point: MAX_SAVES=3)")
    void getFreeSlotRetornaMenosUmQuandoTodosOcupados() {
        // Arrange: ocupa todos os 3 slots (on-point do MAX_SAVES_PER_USER = 3)
        GameModel model = new GameModel(33L);
        saveManager.saveGame(model, testUser, 0);
        saveManager.saveGame(model, testUser, 1);
        saveManager.saveGame(model, testUser, 2);

        // Act
        int freeSlot = saveManager.getFreeSlot(testUser);

        // Assert
        assertThat(freeSlot).isEqualTo(-1)
                .describedAs("getFreeSlot deve retornar -1 quando todos os 3 slots estão ocupados");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EA-5  |  deleteSave – predicado deleteIfExists → true / false
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Predicado deleteIfExists = true: arquivo existe antes da deleção.
     * deleteSave deve retornar true e o arquivo deve deixar de existir.
     */
    @Test
    @DisplayName("EA-5: deleteSave retorna true e remove arquivo quando save existe (deleteIfExists=true)")
    void deleteSaveRemoveArquivoExistente() {
        // Arrange: cria save real
        saveManager.saveGame(new GameModel(5L), testUser, 0);
        assertThat(new File("saves/save_" + TEST_LOGIN + "_0.ser")).exists();

        // Act
        boolean deletou = saveManager.deleteSave(testUser, 0);

        // Assert
        assertThat(deletou).isTrue();
        assertThat(new File("saves/save_" + TEST_LOGIN + "_0.ser")).doesNotExist();
    }

    /**
     * Predicado deleteIfExists = false: arquivo já ausente.
     * deleteSave deve retornar false sem lançar exceção.
     */
    @Test
    @DisplayName("EA-5: deleteSave retorna false quando arquivo não existe (deleteIfExists=false)")
    void deleteSaveRetornaFalseQuandoArquivoAusente() {
        // Arrange: nenhum save criado
        assertThat(new File("saves/save_" + TEST_LOGIN + "_2.ser")).doesNotExist();

        // Act & Assert: não lança exceção
        assertThatCode(() -> {
            boolean deletou = saveManager.deleteSave(testUser, 2);
            assertThat(deletou).isFalse();
        }).doesNotThrowAnyException();
    }

    /**
     * Ciclo completo: salvar → deletar → slot liberado.
     * Verifica que após deleteSave o slot volta a aparecer como livre.
     */
    @Test
    @DisplayName("EA-4/EA-5: slot deletado volta a aparecer como livre no getFreeSlot")
    void slotDeletadoVoltaASerLivre() {
        // Arrange: ocupa todos os slots
        GameModel model = new GameModel(55L);
        saveManager.saveGame(model, testUser, 0);
        saveManager.saveGame(model, testUser, 1);
        saveManager.saveGame(model, testUser, 2);
        assertThat(saveManager.getFreeSlot(testUser)).isEqualTo(-1);

        // Act: deleta o slot do meio
        saveManager.deleteSave(testUser, 1);

        // Assert: slot 1 agora é o primeiro livre
        assertThat(saveManager.getFreeSlot(testUser)).isEqualTo(1);
        assertThat(saveManager.listSlots(testUser)).containsExactly(0, 2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EA-6 | Integração SaveManager ↔ UserManager
    //         (caminho feliz mais longo — slide 9, recomendação 1)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Caminho feliz mais longo que percorre TODAS as dependências externas:
     *  UserManager (users.dat) ↔ SaveManager (saves/*.ser)
     * <p>
     * Cenário: registrar usuário → salvar partidas → deletar usuário →
     *          verificar que saves foram removidos em cascata.
     * <p>
     * Demonstra que UserManager.deleteUser() delega corretamente ao
     * SaveManager.deleteAllSaves() — integração entre dois componentes
     * que usam o sistema de arquivos.
     */
    @Test
    @DisplayName("EA-6: deleteUser em UserManager remove todos os saves do usuário em cascata")
    void deleteUserRemoveTodosOsSavesEmCascata() {
        // Arrange: registra um usuário temporário e cria saves reais para ele
        String loginTemp = "temp_integration_cascade";
        UserManager userManager = new UserManager();
        userManager.registerUser(loginTemp, "senha123", "avatar_temp.png");

        User userTemp = userManager.getUser(loginTemp);
        assertThat(userTemp).isNotNull();

        // Salva jogos nos 3 slots usando GameModel real
        GameModel model = new GameModel(77L);
        saveManager.saveGame(model, userTemp, 0);
        saveManager.saveGame(model, userTemp, 1);
        saveManager.saveGame(model, userTemp, 2);

        // Confirma que os saves existem antes da deleção
        assertThat(saveManager.listSlots(userTemp)).containsExactly(0, 1, 2);

        // Act: deleta o usuário — deve acionar deleteAllSaves internamente
        boolean deletado = userManager.deleteUser(loginTemp);

        // Assert 1: usuário removido com sucesso
        assertThat(deletado).isTrue();
        assertThat(userManager.getUser(loginTemp)).isNull();

        // Assert 2 (EA-6): nenhum arquivo de save residual
        assertThat(saveManager.listSlots(userTemp))
                .as("deleteUser deve remover todos os saves do usuário em cascata")
                .isEmpty();
        assertThat(new File("saves/save_" + loginTemp + "_0.ser")).doesNotExist();
        assertThat(new File("saves/save_" + loginTemp + "_1.ser")).doesNotExist();
        assertThat(new File("saves/save_" + loginTemp + "_2.ser")).doesNotExist();
    }

    /**
     * Verifica que o usuário "admin" não pode ser deletado
     * (proteção de invariante no UserManager).
     * Testa integração real com o arquivo users.dat.
     */
    @Test
    @DisplayName("EA-6: deleteUser recusa deletar admin – invariante do UserManager")
    void deleteUserRecusaRemoverAdmin() {
        // Arrange
        UserManager userManager = new UserManager();

        // Act
        boolean deletado = userManager.deleteUser("admin");

        // Assert
        assertThat(deletado).as("admin não deve ser deletável")
                .isFalse();
        assertThat(userManager.getUser("admin")).isNotNull();
    }

    /**
     * Verifica que deleteAllSaves remove corretamente saves de múltiplos slots
     * e que listSlots retorna vazia após a operação.
     */
    @Test
    @DisplayName("EA-6: deleteAllSaves remove todos os arquivos dos 3 slots existentes")
    void deleteAllSavesRemoveTodosOsArquivos() {
        // Arrange: preenche os 3 slots com GameModel real
        GameModel model = new GameModel(88L);
        saveManager.saveGame(model, testUser, 0);
        saveManager.saveGame(model, testUser, 1);
        saveManager.saveGame(model, testUser, 2);

        // Confirma que todos existem
        assertThat(saveManager.listSlots(testUser)).hasSize(3);

        // Act
        saveManager.deleteAllSaves(TEST_LOGIN);

        // Assert
        assertThat(saveManager.listSlots(testUser)).isEmpty();
        assertThat(new File("saves/save_" + TEST_LOGIN + "_0.ser")).doesNotExist();
        assertThat(new File("saves/save_" + TEST_LOGIN + "_1.ser")).doesNotExist();
        assertThat(new File("saves/save_" + TEST_LOGIN + "_2.ser")).doesNotExist();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SOBRESCRIÇÃO DE SAVE | Estrutura de acordo com múltiplas gravações
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verifica que salvar no mesmo slot sobrescreve o save anterior.
     * O loadGame deve retornar o estado do modelo mais recente.
     */
    @Test
    @DisplayName("EA-1/EA-2: sobrescrever save no mesmo slot preserva apenas o estado mais recente")
    void salvarNoMesmoSlotSobrescreveSaveAnterior() {
        // Arrange – V1: jogo encerrado (jogoAtivo = false)
        GameModel modelV1 = new GameModel(11L);
        modelV1.finalizarJogo(false);
        assertThat(modelV1.isJogoAtivo()).isFalse();
        saveManager.saveGame(modelV1, testUser, 0);

        // Act – V2: jogo ainda ativo (jogoAtivo = true), sobrescreve o slot
        GameModel modelV2 = new GameModel(22L);
        assertThat(modelV2.isJogoAtivo()).isTrue();
        saveManager.saveGame(modelV2, testUser, 0);

        GameModel carregado = saveManager.loadGame(testUser, 0);

        // Assert: estado carregado corresponde ao V2 (ativo), não ao V1 (encerrado)
        assertThat(carregado.isJogoAtivo()).isTrue()
                .describedAs("Slot sobrescrito deve retornar o estado do save mais recente (V2 ativo, não V1 encerrado)");
    }
}