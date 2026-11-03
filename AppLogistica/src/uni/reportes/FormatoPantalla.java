package uni.reportes;

/** ConcreteImplementor: tabla de texto para la vista InformeView / consola. */
public class FormatoPantalla implements IFormatoReporte {

    private final StringBuilder sb = new StringBuilder();

    @Override
    public void iniciar(String titulo) {
        sb.setLength(0);
        sb.append("==== ").append(titulo.toUpperCase()).append(" ====\n");
    }

    @Override
    public void encabezado(String... columnas) {
        for (String c : columnas) {
            sb.append(String.format("%-20s", c));
        }
        sb.append("\n").append("-".repeat(20 * columnas.length)).append("\n");
    }

    @Override
    public void fila(Object... valores) {
        for (Object v : valores) {
            sb.append(String.format("%-20s", v));
        }
        sb.append("\n");
    }

    @Override
    public String finalizar() {
        return sb.toString();
    }
}
