package uni.reportes;

/** ConcreteImplementor: CSV separado por ";" para abrir en Excel. */
public class FormatoCsv implements IFormatoReporte {

    private final StringBuilder sb = new StringBuilder();

    @Override
    public void iniciar(String titulo) {
        sb.setLength(0);
        sb.append("# ").append(titulo).append("\n");
    }

    @Override
    public void encabezado(String... columnas) {
        sb.append(String.join(";", columnas)).append("\n");
    }

    @Override
    public void fila(Object... valores) {
        for (int i = 0; i < valores.length; i++) {
            sb.append(i > 0 ? ";" : "").append(valores[i]);
        }
        sb.append("\n");
    }

    @Override
    public String finalizar() {
        return sb.toString();
    }
}
