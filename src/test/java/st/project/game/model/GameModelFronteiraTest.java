package st.project.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.io.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ─── TESTES DE FRONTEIRA: GameModel ────────────────────────────────────────
 *
 * Cobre estados limite de movimentação, tempo, score,
 * serialização e regras de bloqueio.
 * ────────────────────────────────────────────────────────────────────────────
 */
@DisplayName("GameModel – Testes de Fronteira")
class GameModelFronteiraTest {

    private GameModel game;

    @BeforeEach
    void setUp() {
        game = new GameModel(456L);
    }

    @Test
    @DisplayName("Fronteira: mover para direção inexistente retorna false")
    void testeFronteiraDirecaoInexistente() {
        assertThat(game.moverJogador("xyz")).isFalse();
    }

    @Test
    @DisplayName("Fronteira: mover para fora do mapa retorna false")
    void testeFronteiraForaMapa() {
        assertThat(game.moverJogador("oeste")).isFalse();
    }

    @Test
    @DisplayName("Fronteira: score permanece positivo")
    void testeFronteiraScorePositivo() {
        assertThat(game.getScore()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Fronteira: nível nunca menor que 1")
    void testeFronteiraNivelMinimo() {
        assertThat(game.getNivel()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Fronteira: reduzirTempo até game over")
    void testeFronteiraTempoZero() {
        for (int i = 0; i < 120; i++) {
            game.reduzirTempo();
        }

        assertThat(game.isJogoAtivo()).isFalse();
    }

    @Test
    @DisplayName("Fronteira: reduzirTempo com jogo inativo não altera tempo")
    void testeFronteiraReduzirTempoJogoInativo() {
        game.finalizarJogo(false);

        int tempo = game.getTempoRestante();

        game.reduzirTempo();

        assertThat(game.getTempoRestante()).isEqualTo(tempo);
    }

    @Test
    @DisplayName("Fronteira: finalizarJogo chamado duas vezes mantém estado")
    void testeFronteiraFinalizarJogoDuasVezes() {
        game.finalizarJogo(true);
        game.finalizarJogo(false);

        assertThat(game.isJogoAtivo()).isFalse();
    }

    @Test
    @DisplayName("Fronteira: serialização preserva estado")
    void testeFronteiraSerializacao() throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);

        out.writeObject(game);
        out.close();

        ObjectInputStream in =
                new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));

        GameModel desserializado = (GameModel) in.readObject();

        assertThat(desserializado.getScore()).isEqualTo(game.getScore());
        assertThat(desserializado.isJogoAtivo()).isTrue();
    }

    @Test
    @DisplayName("Fronteira: listener recriado após desserialização")
    void testeFronteiraListenerDesserializacao() throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(game);

        ObjectInputStream in =
                new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));

        GameModel desserializado = (GameModel) in.readObject();

        List<PropertyChangeEvent> eventos = new ArrayList<>();

        desserializado.addPropertyChangeListener(eventos::add);

        desserializado.reduzirTempo();

        assertThat(eventos).isNotEmpty();
    }
}