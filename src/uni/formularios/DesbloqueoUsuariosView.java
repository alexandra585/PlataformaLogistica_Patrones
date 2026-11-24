package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import uni.controller.EmpleadoController;
import uni.entity.BloqueoLoginTo;
import uni.util.ParametrosSeguridad;

/**
 * Utilitarios > Desbloquear usuarios (solo ADMINISTRADOR).
 * Muestra las cuentas con intentos fallidos o bloqueadas por
 * {@value ParametrosSeguridad#MAX_INTENTOS} contraseñas incorrectas y permite desbloquearlas.
 */
public class DesbloqueoUsuariosView extends FormularioBase {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final EmpleadoController controller = new EmpleadoController();
    private final DefaultTableModel modelo = modelo("Usuario", "Intentos fallidos", "Último fallo",
            "Bloqueado hasta", "Estado");
    private final JTable tbl = tabla(modelo);

    public DesbloqueoUsuariosView() {
        super("Seguridad del login", 760, 380);
        getContentPane().add(cabecera("Cuentas bloqueadas",
                "Se bloquean " + ParametrosSeguridad.MINUTOS_BLOQUEO + " minutos tras "
                + ParametrosSeguridad.MAX_INTENTOS + " contraseñas incorrectas seguidas"), BorderLayout.NORTH);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(boton("Desbloquear seleccionado", "edit.png", e -> desbloquear()));
        botones.add(boton("Actualizar", "update.png", e -> cargar()));
        botones.add(boton("Cerrar", "Cancel.png", e -> dispose()));
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(botones, BorderLayout.NORTH);
        centro.add(new JScrollPane(tbl), BorderLayout.CENTER);
        getContentPane().add(centro, BorderLayout.CENTER);
        cargar();
    }

    private void cargar() {
        try {
            modelo.setRowCount(0);
            for (BloqueoLoginTo b : controller.bloqueosListar()) {
                long seg = controller.bloqueoSegundosRestantes(b);
                String estado = seg > 0 ? "BLOQUEADO (" + ((seg + 59) / 60) + " min)"
                        : (b.getIntentosFallidos() > 0 ? "Con fallos" : "Libre");
                modelo.addRow(new Object[]{b.getUsuario(), b.getIntentosFallidos(),
                    b.getUltimoFallo() == null ? "" : b.getUltimoFallo().format(F),
                    b.getBloqueadoHasta() == null ? "" : b.getBloqueadoHasta().format(F), estado});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void desbloquear() {
        int f = tbl.getSelectedRow();
        if (f < 0) {
            info("Seleccione un usuario de la lista.");
            return;
        }
        String usuario = (String) modelo.getValueAt(tbl.convertRowIndexToModel(f), 0);
        try {
            controller.usuarioDesbloquear(usuario);
            info("Se desbloqueó la cuenta \"" + usuario + "\".");
            cargar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
