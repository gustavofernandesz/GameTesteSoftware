package st.project.game;

public class Item {
    public enum Type { CHAVE, POCAO_VELOCIDADE, AMULETO_VISAO, CALICE }

    private String nome;
    private Type tipo;
    private String descricao;

    public Item(String nome, Type tipo, String descricao) {
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public String getNome() { return nome; }
    public Type getTipo() { return tipo; }
    public String getDescricao() { return descricao; }

    @Override
    public String toString() {
        return nome + " (" + descricao + ")";
    }
}