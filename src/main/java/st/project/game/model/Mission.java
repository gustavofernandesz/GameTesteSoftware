package st.project.game.model;

import java.io.Serial;
import java.io.Serializable;

public class Mission implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
        if (caliceColetado) {
            missaoConcluida = true;
        }
    }

    public boolean isMissaoConcluida() { return missaoConcluida; }
    public boolean isCaliceColetado() { return caliceColetado; }
    public Room getSalaCalice() { return salaCalice; }
}