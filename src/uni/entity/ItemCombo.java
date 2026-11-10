package uni.entity;

/** Elemento de los JComboBox (código oculto + texto visible). */
public class ItemCombo {

    private final String id;
    private final String texto;

    public ItemCombo(String id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    public String getId() { return id; }

    public String getTexto() { return texto; }

    @Override
    public String toString() { return texto; }
}
