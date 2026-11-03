package uni.reportes;

/**
 * PATRÓN BRIDGE - Implementor.
 * Define CÓMO se dibuja un reporte (pantalla, CSV para Excel, PDF Jasper),
 * independiente de QUÉ información contiene.
 */
public interface IFormatoReporte {

    void iniciar(String titulo);

    void encabezado(String... columnas);

    void fila(Object... valores);

    String finalizar();
}
