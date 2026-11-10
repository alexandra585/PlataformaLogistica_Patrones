package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import uni.dao.AlertaDao;
import uni.dao.ComandoLogDao;
import uni.dao.NotificacionDao;
import uni.entity.AlertaTo;
import uni.entity.ComandoLogTo;

/**
 * Resultados persistidos de los patrones de comportamiento:
 * alertas (AlertaSupervisorObserver), notificaciones
 * (NotificacionDestinatarioObserver) y bitácora de comandos (Command).
 */
public class AlertasView extends FormularioBase {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final AlertaDao alertaDao = new AlertaDao();
    private final DefaultTableModel mAlertas = modelo("Id", "Fecha", "Envío", "Nivel", "Mensaje");
    private final JTable tblAlertas = tabla(mAlertas);
    private final DefaultTableModel mNotif = modelo("Fecha", "Envío", "Canal", "Destinatario", "Mensaje", "Enviado");
    private final DefaultTableModel mCmd = modelo("Id", "Fecha", "Envío", "Comando", "Descripción",
            "Estado previo", "Estado nuevo", "Empleado", "Deshecho");

    public AlertasView() {
        super("Alertas, notificaciones y bitácora", 1000, 520);
        getContentPane().add(cabecera("Centro de alertas",
                "Lo que registran los observadores (Observer) y el invocador de comandos (Command)"),
                BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        JPanel pa = new JPanel(new BorderLayout());
        pa.add(new JScrollPane(tblAlertas), BorderLayout.CENTER);
        JPanel ba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ba.add(boton("Marcar como atendida", "edit.png", e -> atender()));
        pa.add(ba, BorderLayout.SOUTH);
        tabs.addTab("Alertas pendientes", pa);
        tabs.addTab("Notificaciones enviadas", new JScrollPane(tabla(mNotif)));
        tabs.addTab("Bitácora de comandos", new JScrollPane(tabla(mCmd)));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(boton("Actualizar", "update.png", e -> cargar()));
        botones.add(boton("Cerrar", "Cancel.png", e -> dispose()));
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(botones, BorderLayout.NORTH);
        centro.add(tabs, BorderLayout.CENTER);
        getContentPane().add(centro, BorderLayout.CENTER);
        cargar();
    }

    private void cargar() {
        try {
            mAlertas.setRowCount(0);
            for (AlertaTo a : alertaDao.listarPendientes()) {
                mAlertas.addRow(new Object[]{a.getIdalerta(), a.getFecha().format(F), a.getCodigo(),
                    a.getNivel(), a.getMensaje()});
            }
            mNotif.setRowCount(0);
            for (Object[] n : new NotificacionDao().listarRecientes(200)) {
                mNotif.addRow(n);
            }
            mCmd.setRowCount(0);
            for (ComandoLogTo c : new ComandoLogDao().listarRecientes(200)) {
                mCmd.addRow(new Object[]{c.getIdcomando(), c.getFecha().format(F), c.getCodigo(), c.getComando(),
                    c.getDescripcion(), c.getEstadoPrevio(), c.getEstadoNuevo(), c.getIdempleado(),
                    c.isDeshecho() ? "Sí" : "No"});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void atender() {
        int f = tblAlertas.getSelectedRow();
        if (f < 0) {
            return;
        }
        try {
            alertaDao.marcarAtendida((Long) mAlertas.getValueAt(tblAlertas.convertRowIndexToModel(f), 0));
            cargar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
