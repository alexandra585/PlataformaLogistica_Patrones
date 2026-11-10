package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import uni.controller.GestorComandos;
import uni.dao.CatalogoDao;
import uni.dao.EnvioDao;
import uni.dao.ManifiestoDao;
import uni.entity.AccionEnvio;
import uni.entity.EnvioTo;
import uni.entity.ItemCombo;
import uni.service.AgrupacionCarga;
import uni.service.ComandoCambiarEstado;
import uni.service.ISeguimientoService;
import uni.service.IUnidadCarga;
import uni.service.MacroComando;
import uni.service.PaqueteCarga;
import uni.util.ConfiguracionSistema;
import uni.util.SesionUsuario;

/**
 * Manifiesto de carga y despacho.
 *  - COMPOSITE: manifiesto > sacos/paletas > paquetes; peso y cantidad se
 *    calculan recursivamente sobre el árbol.
 *  - COMMAND (macro): el despacho del manifiesto es un solo comando que
 *    agrupa un ComandoCambiarEstado(DESPACHAR) por paquete (todo o nada).
 */
public class ManifiestoView extends FormularioBase {

    private final EnvioDao envioDao = new EnvioDao();
    private final ManifiestoDao manifiestoDao = new ManifiestoDao();
    private final ISeguimientoService servicio = ConfiguracionSistema.getInstancia().getSeguimiento();
    private final GestorComandos gestor = ConfiguracionSistema.getInstancia().getGestorComandos();

    private final JComboBox<ItemCombo> cboZona = new JComboBox<>();
    private final JComboBox<ItemCombo> cboVehiculo = new JComboBox<>();
    private final JComboBox<ItemCombo> cboRepartidor = new JComboBox<>();
    private final DefaultTableModel mDisponibles = modelo("Código", "Servicio", "Destinatario", "Distrito", "Peso kg", "Estado");
    private final JTable tblDisponibles = tabla(mDisponibles);
    private final List<EnvioTo> disponibles = new ArrayList<>();
    private final JTree arbol = new JTree();
    private final JLabel lblTotales = new JLabel();
    private AgrupacionCarga manifiesto;
    private int contadorSacos = 1;

    public ManifiestoView() {
        super("Manifiesto de carga y despacho", 1040, 620);
        getContentPane().add(cabecera("Manifiesto de carga",
                "Composite (manifiesto > sacos/paletas > paquetes) + MacroComando para el despacho"),
                BorderLayout.NORTH);
        try {
            CatalogoDao cat = new CatalogoDao();
            for (ItemCombo i : cat.zonas()) {
                cboZona.addItem(i);
            }
            for (ItemCombo i : cat.vehiculos()) {
                cboVehiculo.addItem(i);
            }
            for (ItemCombo i : cat.repartidores()) {
                cboRepartidor.addItem(i);
            }
        } catch (Exception ex) {
            error(ex);
        }

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Zona:"));
        filtros.add(cboZona);
        filtros.add(boton("Cargar envíos clasificados", "buscar.gif", e -> cargarDisponibles()));
        filtros.add(new JLabel("   Vehículo:"));
        filtros.add(cboVehiculo);
        filtros.add(new JLabel("Repartidor:"));
        filtros.add(cboRepartidor);

        JPanel izq = new JPanel(new BorderLayout());
        izq.setBorder(BorderFactory.createTitledBorder("Envíos disponibles (CLASIFICADO / REPROGRAMADO)"));
        izq.add(new JScrollPane(tblDisponibles), BorderLayout.CENTER);
        JPanel bIzq = new JPanel(new GridLayout(1, 3, 4, 4));
        bIzq.add(boton("Nuevo saco", "new.png", e -> nuevaAgrupacion("SACO", 0.4)));
        bIzq.add(boton("Nueva paleta", "new.png", e -> nuevaAgrupacion("PALETA", 12.0)));
        bIzq.add(boton("Agregar al nodo →", "Run.png", e -> agregarSeleccionados()));
        izq.add(bIzq, BorderLayout.SOUTH);

        JPanel der = new JPanel(new BorderLayout());
        der.setBorder(BorderFactory.createTitledBorder("Estructura del manifiesto (Composite)"));
        der.add(new JScrollPane(arbol), BorderLayout.CENTER);
        JPanel bDer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bDer.add(boton("Quitar nodo", "delete.png", e -> quitarNodo()));
        bDer.add(boton("Guardar y despachar", "save_all.png", e -> despachar()));
        bDer.add(lblTotales);
        der.add(bDer, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izq, der);
        split.setResizeWeight(0.55);
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(split, BorderLayout.CENTER);
        getContentPane().add(centro, BorderLayout.CENTER);
        nuevoManifiesto();
    }

    private void nuevoManifiesto() {
        try {
            manifiesto = new AgrupacionCarga(manifiestoDao.siguienteCodigo(), "MANIFIESTO", 0.0);
        } catch (Exception ex) {
            manifiesto = new AgrupacionCarga("MF-NUEVO", "MANIFIESTO", 0.0);
            error(ex);
        }
        contadorSacos = 1;
        refrescarArbol();
    }

    private void cargarDisponibles() {
        mDisponibles.setRowCount(0);
        disponibles.clear();
        try {
            int zona = Integer.parseInt(((ItemCombo) cboZona.getSelectedItem()).getId());
            List<String> enArbol = new ArrayList<>();
            manifiesto.recolectarCodigos(enArbol);
            for (EnvioTo e : envioDao.listarParaManifiesto(zona)) {
                if (!enArbol.contains(e.getCodigo())) {
                    disponibles.add(e);
                    mDisponibles.addRow(new Object[]{e.getCodigo(), e.getServicio(), e.getNombreDestinatario(),
                        e.getDestino() == null ? "-" : e.getDestino().getDistrito(), e.getPesoKg(), e.getEstado()});
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void nuevaAgrupacion(String tipo, double tara) {
        String pre = tipo.equals("SACO") ? "SC-" : "PL-";
        AgrupacionCarga a = new AgrupacionCarga(pre + manifiesto.getCodigo().substring(8) + "-" + contadorSacos++,
                tipo, tara);
        AgrupacionCarga destino = nodoSeleccionado();
        destino.agregar(a);
        refrescarArbol();
    }

    /** Agrupación seleccionada en el árbol (o el manifiesto si no hay selección). */
    private AgrupacionCarga nodoSeleccionado() {
        TreePath p = arbol.getSelectionPath();
        if (p != null) {
            Object u = ((DefaultMutableTreeNode) p.getLastPathComponent()).getUserObject();
            if (u instanceof AgrupacionCarga) {
                return (AgrupacionCarga) u;
            }
        }
        return manifiesto;
    }

    private void agregarSeleccionados() {
        int[] filas = tblDisponibles.getSelectedRows();
        if (filas.length == 0) {
            info("Seleccione uno o más envíos de la lista (Ctrl + clic).");
            return;
        }
        AgrupacionCarga destino = nodoSeleccionado();
        List<EnvioTo> agregados = new ArrayList<>();
        for (int f : filas) {
            EnvioTo e = disponibles.get(tblDisponibles.convertRowIndexToModel(f));
            destino.agregar(new PaqueteCarga(e));
            agregados.add(e);
        }
        for (EnvioTo e : agregados) {
            int i = disponibles.indexOf(e);
            disponibles.remove(i);
            mDisponibles.removeRow(i);
        }
        refrescarArbol();
    }

    private void quitarNodo() {
        TreePath p = arbol.getSelectionPath();
        if (p == null || p.getPathCount() < 2) {
            return;
        }
        Object hijo = ((DefaultMutableTreeNode) p.getLastPathComponent()).getUserObject();
        Object padre = ((DefaultMutableTreeNode) p.getParentPath().getLastPathComponent()).getUserObject();
        ((AgrupacionCarga) padre).quitar((IUnidadCarga) hijo);
        refrescarArbol();
        cargarDisponibles();
    }

    private void refrescarArbol() {
        DefaultMutableTreeNode raiz = nodo(manifiesto);
        arbol.setModel(new DefaultTreeModel(raiz));
        for (int i = 0; i < arbol.getRowCount(); i++) {
            arbol.expandRow(i);
        }
        lblTotales.setText(String.format("<html><b>%d paquete(s) · %.1f kg</b> (calculado por el Composite)</html>",
                manifiesto.contarPaquetes(), manifiesto.getPesoKg()));
    }

    private DefaultMutableTreeNode nodo(IUnidadCarga u) {
        DefaultMutableTreeNode n = new DefaultMutableTreeNode(u);
        if (u instanceof AgrupacionCarga) {
            for (IUnidadCarga h : ((AgrupacionCarga) u).getHijos()) {
                n.add(nodo(h));
            }
        }
        return n;
    }

    private void despachar() {
        if (manifiesto.contarPaquetes() == 0) {
            info("El manifiesto no tiene paquetes.");
            return;
        }
        ItemCombo veh = (ItemCombo) cboVehiculo.getSelectedItem();
        double capacidad = Double.parseDouble(veh.getTexto().replaceAll(".*\\(([0-9.]+) kg\\)", "$1"));
        if (manifiesto.getPesoKg() > capacidad) {
            info(String.format("El manifiesto pesa %.1f kg y supera la capacidad del vehículo (%.0f kg).",
                    manifiesto.getPesoKg(), capacidad));
            return;
        }
        String idrep = ((ItemCombo) cboRepartidor.getSelectedItem()).getId();
        if (JOptionPane.showConfirmDialog(this, "Se guardará el manifiesto " + manifiesto.getCodigo()
                + " y se despacharán " + manifiesto.contarPaquetes() + " paquete(s) con " + veh.getId()
                + ".\n¿Continuar?", "Despachar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int zona = Integer.parseInt(((ItemCombo) cboZona.getSelectedItem()).getId());
            // 1) MacroComando: un comando DESPACHAR por cada hoja del árbol (todo o nada)
            List<String> codigos = new ArrayList<>();
            manifiesto.recolectarCodigos(codigos);
            MacroComando macro = new MacroComando("Despachar " + manifiesto.getCodigo(), manifiesto.getCodigo());
            for (String c : codigos) {
                macro.agregar(new ComandoCambiarEstado(servicio, c, AccionEnvio.DESPACHAR,
                        String.valueOf(cboZona.getSelectedItem()),
                        "Salida a reparto (manifiesto " + manifiesto.getCodigo() + ", " + veh.getId() + ")"));
            }
            gestor.ejecutar(macro);
            // 2) Se guarda el árbol del Composite; si falla, se deshace el despacho
            int idruta;
            try {
                idruta = manifiestoDao.guardar(manifiesto, zona, veh.getId(),
                        SesionUsuario.getInstancia().getUsuario().getIdempleado(), idrep);
            } catch (Exception ex) {
                gestor.deshacer();
                throw ex;
            }
            info("Manifiesto " + manifiesto.getCodigo() + " despachado (ruta " + idruta + ").\n"
                    + codigos.size() + " envío(s) pasaron a EN_RUTA y quedaron asignados a " + idrep + ".");
            nuevoManifiesto();
            cargarDisponibles();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
