package st.project.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES ESTRUTURAIS: GameModel ─────────────────────────────────────────
 *
 * Foco: cobertura MC/DC das decisões internas.
 *
 * Decisões cobertas:
 *   (A) jogoAtivo false → moverJogador retorna false
 *   (B) movimentosRestantes <= 0 → false
 *   (C) destino == null → false
 *   (D) sala sagrado sem chave → queda no alçapão
 *   (E) mover válido → decrementa movimentos
 *   (F) escada_cima → teleporte
 *   (G) escada_baixo → teleporte
 *   (H) missão concluída → finalizarJogo(true)
 *   (I) movimentos zerados → finalizarJogo(false)
 *   (J) tempo <= 0 → finalizarJogo(false)
 *   (K) item invisível sem lupa → não coleta
 *   (L) item com lupa → coleta
 *   (M) aplicar efeito POCAO
 *   (N) aplicar efeito AMULETO
 *   (O) aplicar efeito LUPA
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("GameModel – Testes Estruturais")
class GameModelEstruturaTest {

    private GameModel game;

    @BeforeEach
    void setUp() {
        game = new GameModel(999L);
    }

    @Test
    @DisplayName("Estrutura (A): jogo inativo impede movimento")
    void testeEstruturaAJogoInativo() {
        game.finalizarJogo(false);

        assertThat(game.moverJogador("leste")).isFalse();
    }

    @Test
    @DisplayName("Estrutura (C): direção sem vizinho retorna false")
    void testeEstruturaCDirecaoSemVizinho() {
        assertThat(game.moverJogador("norte")).isFalse();
    }

    @Test
    @DisplayName("Estrutura (E): movimento válido reduz movimentos")
    void testeEstruturaEMovimentoValido() {
        int movimentos = game.getMovimentosRestantes();

        game.moverJogador("leste");

        assertThat(game.getMovimentosRestantes())
                .isEqualTo(movimentos - 1);
    }

    @Test
    @DisplayName("Estrutura: evento movimentos é disparado")
    void testeEstruturaEventoMovimentos() {

        List<PropertyChangeEvent> eventos = new ArrayList<>();

        game.addPropertyChangeListener(eventos::add);

        game.moverJogador("leste");

        assertThat(eventos)
                .extracting(PropertyChangeEvent::getPropertyName)
                .contains("movimentos");
    }

    @Test
    @DisplayName("Estrutura: evento score é disparado")
    void testeEstruturaEventoScore() {

        List<PropertyChangeEvent> eventos = new ArrayList<>();

        game.addPropertyChangeListener(eventos::add);

        game.reduzirTempo();

        assertThat(eventos)
                .extracting(PropertyChangeEvent::getPropertyName)
                .contains("score");
    }

    @Test
    @DisplayName("Estrutura: evento gameOver é disparado")
    void testeEstruturaEventoGameOver() {

        List<PropertyChangeEvent> eventos = new ArrayList<>();

        game.addPropertyChangeListener(eventos::add);

        game.finalizarJogo(false);

        assertThat(eventos)
                .extracting(PropertyChangeEvent::getPropertyName)
                .contains("gameOver");
    }

    @Test
    @DisplayName("Estrutura (J): tempo zerado finaliza jogo")
    void testeEstruturaJTempoZerado() {

        for (int i = 0; i < 120; i++) {
            game.reduzirTempo();
        }

        assertThat(game.isJogoAtivo()).isFalse();
    }

    @Test
    @DisplayName("Estrutura: mover para escada gera troca de andar")
    void testeEstruturaEscadaTrocaAndar() {

        while (game.getAndarAtual() == 1) {
            game.moverJogador("leste");
            if (game.getJogador().getPosicaoAtual().isEscadaCima()) {
                break;
            }
        }

        assertThat(game.getAndarAtual()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Estrutura: aplicar efeito da poção dobra o tempo")
    void testeEstruturaPocaoDobraTempo() {

        int tempoInicial = game.getTempoRestante();

        // adiciona lupa diretamente para liberar coleta
        game.getJogador().adicionarItem(
                new Item("Lupa", Item.Type.LUPA, "Revela")
        );

        // pega a sala da poção no andar 1 posição (2,3)
        Room salaPocao = null;

        for (Room r : game.getSalas().values()) {
            if (r.getAndar() == 1 && r.getX() == 2 && r.getY() == 3) {
                salaPocao = r;
                break;
            }
        }

        assertThat(salaPocao).isNotNull();

        // move jogador diretamente
        game.getJogador().moverPara(salaPocao);

        // força coleta dos itens da sala
        game.moverJogador("norte");
        game.moverJogador("sul");

        assertThat(game.getTempoRestante())
                .isGreaterThan(tempoInicial);
    }

    @Test
    @DisplayName("Estrutura: finalizarJogo evita evento duplicado")
    void testeEstruturaFinalizarJogoDuplicado() {

        List<PropertyChangeEvent> eventos = new ArrayList<>();

        game.addPropertyChangeListener(eventos::add);

        game.finalizarJogo(false);
        game.finalizarJogo(false);

        long total =
                eventos.stream()
                        .filter(e -> e.getPropertyName().equals("gameOver"))
                        .count();

        assertThat(total).isEqualTo(1);
    }

    @Test
    @DisplayName("Estrutura: getSalas retorna mapa preenchido")
    void testeEstruturaGetSalas() {
        assertThat(game.getSalas()).isNotEmpty();
    }

    @Test
    @DisplayName("Estrutura: getJogador retorna instância válida")
    void testeEstruturaGetJogador() {
        assertThat(game.getJogador()).isNotNull();
    }

    @Test
    @DisplayName("Estrutura: getMissao retorna instância válida")
    void testeEstruturaGetMissao() {
        assertThat(game.getMissao()).isNotNull();
    }

    @Test
    @DisplayName("Estrutura: reduzirTempo altera score")
    void testeEstruturaReducaoTempoAlteraScore() {

        int score = game.getScore();

        game.reduzirTempo();

        assertThat(game.getScore()).isLessThan(score);
    }

    @Test
    @DisplayName("Estrutura: entrar em sagrado sem chave ativa alçapão")
    void testeEstruturaAlcapaoSagradoSemChave() {

        List<PropertyChangeEvent> eventos = new ArrayList<>();
        game.addPropertyChangeListener(eventos::add);

        // pega sala vizinha do sagrado
        Room sagrado = game.getSalas().get("sagrado");
        Room vizinha = sagrado.getVizinho("oeste");

        // move jogador diretamente para a vizinha
        game.getJogador().moverPara(vizinha);

        int movimentosAntes = game.getMovimentosRestantes();

        // tenta entrar no sagrado sem chave
        boolean moveu = game.moverJogador("leste");

        assertThat(moveu).isTrue();

        // caiu no alçapão → voltou entrada
        assertThat(game.getJogador().getPosicaoAtual().getNome())
                .isEqualTo("entrada");

        assertThat(game.getMovimentosRestantes())
                .isEqualTo(movimentosAntes - 1);

        assertThat(eventos)
                .extracting(PropertyChangeEvent::getPropertyName)
                .contains("alcapao", "movimentos", "score");
    }

    @Test
    @DisplayName("Estrutura: escada baixo teleporta para andar inferior")
    void testeEstruturaEscadaBaixo() {

        // pega diretamente a escada_baixo_2
        Room escadaBaixo = game.getSalas().get("escada_baixo_2");

        // coloca jogador nela
        game.getJogador().moverPara(escadaBaixo);

        // move para uma sala vizinha e volta
        game.moverJogador("leste");

        // ao voltar para oeste, entra novamente na escada
        // e ativa o branch do teleporte para baixo
        game.moverJogador("oeste");

        // deve voltar para o andar 1
        assertThat(game.getAndarAtual()).isEqualTo(1);
    }

    @Test
    @DisplayName("Estrutura: missão concluída finaliza jogo com vitória")
    void testeEstruturaMissaoConcluida() {

        List<PropertyChangeEvent> eventos = new ArrayList<>();
        game.addPropertyChangeListener(eventos::add);

        // adiciona cálice diretamente
        game.getJogador().adicionarItem(
                new Item("Cálice", Item.Type.CALICE, "Missão")
        );

        // força verificação da missão
        game.moverJogador("leste");

        assertThat(game.isJogoAtivo()).isFalse();

        assertThat(eventos)
                .extracting(PropertyChangeEvent::getPropertyName)
                .contains("gameOver");
    }

    @Test
    @DisplayName("Estrutura: movimentos zerados finalizam jogo")
    void testeEstruturaMovimentosZerados() throws Exception {

        var field = GameModel.class.getDeclaredField("movimentosRestantes");
        field.setAccessible(true);

        field.setInt(game, 1);

        // movimento válido consome o último movimento
        game.moverJogador("leste");

        assertThat(game.getMovimentosRestantes()).isZero();
        assertThat(game.isJogoAtivo()).isFalse();
    }

    @Test
    @DisplayName("Estrutura: amuleto aumenta movimentos")
    void testeEstruturaAmuletoAumentaMovimentos() {

        int movimentosAntes = game.getMovimentosRestantes();

        // adiciona lupa para liberar coleta
        game.getJogador().adicionarItem(
                new Item("Lupa", Item.Type.LUPA, "Revela")
        );

        // pega sala do amuleto (andar 2, x=3, y=2)
        Room salaAmuleto = null;

        for (Room r : game.getSalas().values()) {
            if (r.getAndar() == 2 && r.getX() == 3 && r.getY() == 2) {
                salaAmuleto = r;
                break;
            }
        }

        assertThat(salaAmuleto).isNotNull();

        game.getJogador().moverPara(salaAmuleto);

        // força coleta
        game.moverJogador("norte");
        game.moverJogador("sul");

        assertThat(game.getMovimentosRestantes())
                .isGreaterThan(movimentosAntes);
    }

    @Test
    @DisplayName("Estrutura: coletar lupa dispara evento lupaObtida")
    void testeEstruturaEventoLupaObtida() {

        List<PropertyChangeEvent> eventos = new ArrayList<>();
        game.addPropertyChangeListener(eventos::add);

        // sala da lupa (andar 2, 2,2)
        Room salaLupa = null;

        for (Room r : game.getSalas().values()) {
            if (r.getAndar() == 2 && r.getX() == 2 && r.getY() == 2) {
                salaLupa = r;
                break;
            }
        }

        assertThat(salaLupa).isNotNull();

        game.getJogador().moverPara(salaLupa);

        // força coleta
        game.moverJogador("norte");
        game.moverJogador("sul");

        assertThat(eventos)
                .extracting(PropertyChangeEvent::getPropertyName)
                .contains("lupaObtida");
    }

    @Test
    @DisplayName("Estrutura: construtor default cria jogo válido")
    void testeEstruturaConstrutorDefault() {

        GameModel novo = new GameModel();

        assertThat(novo).isNotNull();
        assertThat(novo.getSalas()).hasSize(100);
        assertThat(novo.isJogoAtivo()).isTrue();
    }

    @Test
    @DisplayName("Estrutura: criarAndar usa nomes fallback quando lista vazia")
    void testeEstruturaFallbackNomeSala() throws Exception {

        GameModel model = new GameModel(1L);

        var metodo = GameModel.class.getDeclaredMethod(
                "criarAndar",
                int.class,
                String.class,
                String.class,
                String.class,
                String.class,
                List.class
        );

        metodo.setAccessible(true);

        metodo.invoke(
                model,
                5,
                null,
                null,
                null,
                null,
                new ArrayList<String>()
        );

        assertThat(model.getSalas())
                .containsKey("sala_5_0");
    }

    @Test
    @DisplayName("Estrutura: getEscadaNoAndar retorna null para andar inexistente")
    void testeEstruturaGetEscadaNoAndarNull() throws Exception {

        var metodo = GameModel.class.getDeclaredMethod(
                "getEscadaNoAndar",
                int.class,
                boolean.class
        );

        metodo.setAccessible(true);

        Object resultado = metodo.invoke(game, 99, true);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Estrutura: getRoomPorAndarXY retorna null para coordenada inexistente")
    void testeEstruturaGetRoomPorAndarXYNull() throws Exception {

        var metodo = GameModel.class.getDeclaredMethod(
                "getRoomPorAndarXY",
                int.class,
                int.class,
                int.class
        );

        metodo.setAccessible(true);

        Object resultado = metodo.invoke(game, 99, 2, 2);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("Estrutura: getNivel ignora chave no cálculo")
    void testeEstruturaNivelIgnoraChave() {

        game.getJogador().adicionarItem(
                new Item("Chave", Item.Type.CHAVE, "Abre")
        );

        assertThat(game.getNivel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Estrutura: moverJogador com movimentos zerados retorna false")
    void testeEstruturaMovimentosZero() throws Exception {

        var field = GameModel.class.getDeclaredField("movimentosRestantes");
        field.setAccessible(true);

        field.setInt(game, 0);

        assertThat(game.moverJogador("leste")).isFalse();
    }

    @Test
    @DisplayName("Estrutura: entrar em sagrado com chave não ativa alçapão")
    void testeEstruturaEntrarSagradoComChave() {

        game.getJogador().adicionarItem(
                new Item("Chave", Item.Type.CHAVE, "Abre")
        );

        Room sagrado = game.getSalas().get("sagrado");
        Room vizinha = sagrado.getVizinho("oeste");

        game.getJogador().moverPara(vizinha);

        boolean moveu = game.moverJogador("leste");

        assertThat(moveu).isTrue();

        assertThat(game.getJogador().getPosicaoAtual())
                .isEqualTo(sagrado);
    }

    @Test
    @DisplayName("Estrutura: movimento no mesmo andar não dispara troca de andar")
    void testeEstruturaMesmoAndarSemEventoAndar() {

        List<PropertyChangeEvent> eventos = new ArrayList<>();

        game.addPropertyChangeListener(eventos::add);

        game.moverJogador("leste");

        assertThat(eventos)
                .extracting(PropertyChangeEvent::getPropertyName)
                .doesNotContain("andar");
    }

    @Test
    @DisplayName("Estrutura: moverPara retorna false em sala bloqueada")
    void testeEstruturaMoveuFalse() {

        Room atual = game.getJogador().getPosicaoAtual();

        Room bloqueada = new Room("bloqueada", 1, 0);
        bloqueada.setBloqueada(true);

        atual.setVizinho("leste", bloqueada);

        assertThat(game.moverJogador("leste")).isFalse();
    }

    @Test
    @DisplayName("Estrutura: escada cima sem destino não teleporta")
    void testeEstruturaEscadaCimaSemDestino() {

        game.getSalas().remove("escada_baixo_2");

        while (game.getJogador().getPosicaoAtual().getX() < 4) {
            game.moverJogador("leste");
        }

        assertThat(game.getAndarAtual()).isEqualTo(1);
    }

    @Test
    @DisplayName("Estrutura: escada baixo sem destino não teleporta")
    void testeEstruturaEscadaBaixoSemDestino() {

        game.getSalas().remove("escada_cima_1");

        Room escada = game.getSalas().get("escada_baixo_2");

        game.getJogador().moverPara(escada);

        game.moverJogador("leste");
        game.moverJogador("oeste");

        assertThat(game.getAndarAtual()).isEqualTo(2);
    }

    @Test
    @DisplayName("Estrutura: item oculto sem lupa não é coletado")
    void testeEstruturaItemOcultoSemLupa() {

        Room sala = new Room("teste", 0, 0);

        Item pocao = new Item(
                "Poção",
                Item.Type.POCAO_VELOCIDADE,
                "x2"
        );

        sala.adicionarItem(pocao);

        game.getJogador().moverPara(sala);

        game.moverJogador("norte");

        assertThat(sala.getItems()).contains(pocao);
    }

    @Test
    @DisplayName("Estrutura: inicializarItens tolera salas opcionais inexistentes")
    void testeEstruturaInicializarItensSalasNull() throws Exception {

        GameModel model = new GameModel(1L);

        Room sagrado = model.getSalas().get("sagrado");

        model.getSalas().clear();
        model.getSalas().put("sagrado", sagrado);

        var metodo = GameModel.class.getDeclaredMethod("inicializarItens");

        metodo.setAccessible(true);

        metodo.invoke(model);

        assertThat(model.getSalas())
                .containsKey("sagrado");
    }

    @Test
    @DisplayName("Estrutura: alçapão sem mudança de andar não dispara evento andar")
    void testeEstruturaAlcapaoMesmoAndar() {

        Room entrada = game.getSalas().get("entrada");

        Room sagradoFake = new Room("sagrado", 1, 0, 1);

        entrada.setVizinho("leste", sagradoFake);

        List<PropertyChangeEvent> eventos = new ArrayList<>();

        game.addPropertyChangeListener(eventos::add);

        boolean moveu = game.moverJogador("leste");

        assertThat(moveu).isTrue();

        assertThat(eventos)
                .extracting(PropertyChangeEvent::getPropertyName)
                .contains("alcapao")
                .doesNotContain("andar");
    }

    @Test
    @DisplayName("Estrutura: aplicarEfeitoItem cobre case CHAVE")
    void testeEstruturaAplicarEfeitoChave() throws Exception {

        Item chave = new Item(
                "Chave",
                Item.Type.CHAVE,
                "Abre"
        );

        var metodo = GameModel.class.getDeclaredMethod(
                "aplicarEfeitoItem",
                Item.class
        );

        metodo.setAccessible(true);

        metodo.invoke(game, chave);

        assertThat(game.isJogoAtivo()).isTrue();
    }

    @Test
    @DisplayName("Estrutura: aplicarEfeitoItem cobre case CALICE")
    void testeEstruturaAplicarEfeitoCalice() throws Exception {

        Item calice = new Item(
                "Cálice",
                Item.Type.CALICE,
                "Missão"
        );

        var metodo = GameModel.class.getDeclaredMethod(
                "aplicarEfeitoItem",
                Item.class
        );

        metodo.setAccessible(true);

        metodo.invoke(game, calice);

        assertThat(game.isJogoAtivo()).isTrue();
    }
}