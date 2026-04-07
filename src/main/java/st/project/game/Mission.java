package st.project.game;

public class Mission {
    private boolean caliceColetado;
    private final Room salaCalice;
    private boolean missaoConcluida;

    public Mission(Room salaCalice) {
        this.salaCalice = salaCalice;
        this.caliceColetado = false;
        this.missaoConcluida = false;
    }

    public void verificarProgresso(Player jogador) {
        if (!caliceColetado && jogador.possuiItem(Item.Type.CALICE)) {
            caliceColetado = true;
        }
        // Missão concluída quando coletar o cálice e estiver na sala final?
        // Vamos definir: missão concluída quando coletar o cálice.
        if (caliceColetado) {
            missaoConcluida = true;
        }
    }

    public boolean isMissaoConcluida() { return missaoConcluida; }
    public boolean isCaliceColetado() { return caliceColetado; }
    public Room getSalaCalice() { return salaCalice; }
}