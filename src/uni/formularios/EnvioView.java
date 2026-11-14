package uni.formularios;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import uni.controller.RegistroEnvioFacade;
import uni.dao.CatalogoDao;
import uni.entity.DireccionTo;
import uni.entity.EnvioTo;
import uni.entity.ItemCombo;
import uni.entity.TipoServicio;
import uni.service.ITarifa;
import uni.util.ConfiguracionSistema;
import uni.util.EnvioBuilder;
import uni.util.RegistroPlantillas;
import uni.util.SesionUsuario;

/**
 * Registro de envíos en ventanilla. - BUILDER: el EnvioTo se arma paso a paso
 * con EnvioBuilder. - DECORATOR: "Cotizar" muestra la tarifa con sus
 * adicionales. - FACADE: "Registrar" hace una sola llamada a
 * RegistroEnvioFacade. - PROTOTYPE: "Repetir último" clona el último envío
 * (clientes corporativos).
 */
public class EnvioView extends FormularioBase {

    private static final DireccionTo ORIGEN = new DireccionTo("Av. Javier Prado Este 1450",
            "San Isidro", "Lima", "Lima");
    private static final String PLANTILLA = "ULTIMO_ENVIO";

    private final RegistroEnvioFacade fachada = ConfiguracionSistema.getInstancia().getRegistroEnvio();
    private final RegistroPlantillas plantillas = ConfiguracionSistema.getInstancia().getPlantillas();

    /*private final JComboBox<ItemCombo> cboCliente = new JComboBox<>();*/
    private final JTextField txtRemitente = new JTextField(22);
    private final javax.swing.JButton btnBuscarCliente = new javax.swing.JButton("...");
    private String idClienteSeleccionado = null;
    private final JComboBox<TipoServicio> cboServicio = new JComboBox<>(TipoServicio.values());
    private final JTextField txtPeso = new JTextField("1.0", 6);
    private final JTextField txtValor = new JTextField("0", 8);
    private final JCheckBox chkSeguro = new JCheckBox("Seguro (2 % valor, mín. S/ 5)");
    private final JCheckBox chkFragil = new JCheckBox("Embalaje frágil (S/ 8.00)");
    private final JCheckBox chkFirma = new JCheckBox("Firma digital (S/ 4.50)");
    private final JTextField txtFechaProg = new JTextField(10);
    private final JComboBox<ItemCombo> cboTienda = new JComboBox<>();

    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtCelular = new JTextField(10);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtCalle = new JTextField(24);
    private final JTextField txtDistrito = new JTextField(14);
    private final JTextField txtProvincia = new JTextField("Lima", 14);
    private final JTextField txtDepartamento = new JTextField("Lima", 14);
    private final JComboBox<ItemCombo> cboZona = new JComboBox<>();

    private final JTextArea txtResultado = new JTextArea(7, 40);

    public EnvioView() {
        super("Registrar Envío", 900, 640);
        getContentPane().add(cabecera("Registro de envíos",
                "Builder + Decorator (cotización) + Facade (registro) + Prototype (repetir envío)"),
                BorderLayout.NORTH);
        cargarCombos();

        //Panel auxiliar para juntar el texto de remitente y el boton [...]
        JPanel panelRemitente = new JPanel(new BorderLayout(4, 0));
        txtRemitente.setEditable(false);
        panelRemitente.add(txtRemitente, BorderLayout.CENTER);
        panelRemitente.add(btnBuscarCliente, BorderLayout.EAST);

        //Acción al hacer clic en los tres puntos [...]
        btnBuscarCliente.addActionListener(e -> {
            ClienteBuscarDialog modal = new ClienteBuscarDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this), true);
            modal.setVisible(true); //Abre la ventana flotante modal

            ItemCombo elegido = modal.getClienteSeleccionado();
            if (elegido != null) {
                idClienteSeleccionado = elegido.getId();
                txtRemitente.setText(elegido.getId() + " - " + elegido.toString());
            }
        });
        JPanel envio = formulario("Envío", "Remitente", panelRemitente, "Servicio", cboServicio,
                "Peso (kg)", txtPeso, "Valor declarado (S/)", txtValor, "", chkSeguro, "", chkFragil,
                "", chkFirma, "Fecha programada (aaaa-mm-dd)", txtFechaProg, "Tienda de recojo", cboTienda);
        JPanel destino = formulario("Destinatario y destino", "Nombre", txtNombre, "Celular (9 dígitos)",
                txtCelular, "Email", txtEmail, "Calle / Av. y número", txtCalle, "Distrito", txtDistrito,
                "Provincia", txtProvincia, "Departamento", txtDepartamento, "Zona de reparto", cboZona);
        JPanel columnas = new JPanel(new GridLayout(1, 2, 8, 0));
        columnas.add(envio);
        columnas.add(destino);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(boton("Cotizar", "Chart.png", e -> cotizar()));
        botones.add(boton("Registrar envío", "save_all.png", e -> registrar()));
        botones.add(boton("Repetir último (Prototype)", "update.png", e -> repetirUltimo()));
        botones.add(boton("Limpiar", "new.png", e -> limpiarDestino()));
        botones.add(boton("Cerrar", "Cancel.png", e -> dispose()));

        txtResultado.setEditable(false);
        txtResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtResultado.setText("Origen: Agencia Central Lima - " + ORIGEN + "\n");

        JPanel centro = new JPanel(new BorderLayout(4, 4));
        centro.add(columnas, BorderLayout.NORTH);
        centro.add(botones, BorderLayout.CENTER);
        centro.add(new JScrollPane(txtResultado), BorderLayout.SOUTH);
        getContentPane().add(centro, BorderLayout.CENTER);

        cboServicio.addActionListener(e -> ajustarPorServicio());
        cboServicio.setSelectedItem(TipoServicio.ESTANDAR);
        ajustarPorServicio();
    }

    private void cargarCombos() {
        try {
            CatalogoDao cat = new CatalogoDao();
            
            for (ItemCombo i : cat.tiendasRecojo()) {
                cboTienda.addItem(i);
            }
            for (ItemCombo i : cat.zonas()) {
                cboZona.addItem(i);
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    /**
     * Habilita solo los datos que pide cada tipo de servicio.
     */
    private void ajustarPorServicio() {
        TipoServicio t = (TipoServicio) cboServicio.getSelectedItem();
        boolean recojo = t == TipoServicio.RECOJO_TIENDA;
        txtFechaProg.setEnabled(t == TipoServicio.PROGRAMADO);
        if (t == TipoServicio.PROGRAMADO && txtFechaProg.getText().isEmpty()) {
            txtFechaProg.setText(LocalDate.now().plusDays(2).toString());
        }
        cboTienda.setEnabled(recojo);
        for (JTextField f : new JTextField[]{txtCalle, txtDistrito, txtProvincia, txtDepartamento}) {
            f.setEnabled(!recojo);
        }
        cboZona.setEnabled(!recojo);
        if (t == TipoServicio.INTERPROVINCIAL && cboZona.getItemCount() > 3) {
            cboZona.setSelectedIndex(3);
        }
    }

    /**
     * PATRÓN BUILDER: construcción paso a paso a partir del formulario.
     */
    private EnvioTo construir() {
        TipoServicio t = (TipoServicio) cboServicio.getSelectedItem();
        if (idClienteSeleccionado == null || idClienteSeleccionado.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un remitente haciendo clic en los tres puntos [...].");
        }
        EnvioBuilder b = new EnvioBuilder(idClienteSeleccionado, t)
                .desde(ORIGEN.clone())
                .destinatario(txtNombre.getText().trim(), txtCelular.getText().trim(),
                        txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim())
                .peso(numero(txtPeso, "peso"));
        if (t != TipoServicio.RECOJO_TIENDA) {
            b.hacia(new DireccionTo(txtCalle.getText().trim(), txtDistrito.getText().trim(),
                    txtProvincia.getText().trim(), txtDepartamento.getText().trim()));
        } else if (cboTienda.getSelectedItem() != null) {
            b.recogerEnTienda(((ItemCombo) cboTienda.getSelectedItem()).getId());
        }
        if (chkSeguro.isSelected()) {
            b.conSeguro(numero(txtValor, "valor declarado"));
        }
        if (chkFragil.isSelected()) {
            b.fragil();
        }
        if (chkFirma.isSelected()) {
            b.conFirmaDigital();
        }
        if (t == TipoServicio.PROGRAMADO) {
            try {
                b.programadoPara(LocalDate.parse(txtFechaProg.getText().trim()));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Fecha programada inválida (use aaaa-mm-dd).");
            }
        }
        EnvioTo e = b.build();                        // valida el objeto completo
        if (t != TipoServicio.RECOJO_TIENDA && e.getDestino().getCalle().isEmpty()) {
            throw new IllegalArgumentException("Ingrese la dirección de destino.");
        }
        if (t != TipoServicio.RECOJO_TIENDA && cboZona.getSelectedItem() != null) {
            e.setIdzonaDestino(Integer.valueOf(((ItemCombo) cboZona.getSelectedItem()).getId()));
        }
        return e;
    }

    private double numero(JTextField f, String nombre) {
        try {
            return Double.parseDouble(f.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor numérico inválido en " + nombre + ".");
        }
    }

    /**
     * PATRÓN DECORATOR: tarifa base envuelta por los adicionales elegidos.
     */
    private void cotizar() {
        try {
            EnvioTo e = construir();
            ITarifa t = fachada.cotizar(e);
            txtResultado.setText("COTIZACIÓN\n  " + t.getDescripcion() + "\n  Total: S/ "
                    + String.format("%.2f", t.getMonto()) + "\n  Entrega estimada: " + fachada.fechaEstimada(e)
                    + "\n");
        } catch (Exception ex) {
            error(ex);
        }
    }

    /**
     * PATRÓN FACADE: una sola llamada registra, geolocaliza, tarifa, guarda y
     * notifica.
     */
    private void registrar() {
        try {
            EnvioTo e = construir();
            String resumen = fachada.registrarEnvio(e, SesionUsuario.getInstancia().getUsuario().getIdempleado());
            plantillas.registrar(PLANTILLA, e);       // queda como prototipo para "Repetir último"
            txtResultado.setText("ENVÍO REGISTRADO\n  " + resumen.replace(" | ", "\n  ")
                    + (e.getDestino() == null ? "" : String.format("%n  Destino geolocalizado: (%.5f, %.5f)",
                    e.getDestino().getLatitud(), e.getDestino().getLongitud()))
                    + "\n  Notificación enviada al destinatario por "
                    + ConfiguracionSistema.getInstancia().getIntegraciones().crearNotificador().getCanal() + "\n");
            info("Envío " + e.getCodigo() + " registrado.\nCosto: S/ " + String.format("%.2f", e.getCosto()));
            limpiarDestino();
        } catch (Exception ex) {
            error(ex);
        }
    }

    /**
     * PATRÓN PROTOTYPE: copia profunda del último envío; solo cambia el
     * destinatario.
     */
    private void repetirUltimo() {
        try {
            EnvioTo copia = plantillas.obtener(PLANTILLA);
            idClienteSeleccionado = copia.getIdcliente();
            txtRemitente.setText(copia.getIdcliente() + " (Cargado por Prototype)");
            cboServicio.setSelectedItem(copia.getServicio());
            txtPeso.setText(String.valueOf(copia.getPesoKg()));
            txtValor.setText(String.valueOf(copia.getValorDeclarado()));
            chkSeguro.setSelected(copia.isConSeguro());
            chkFragil.setSelected(copia.isFragil());
            chkFirma.setSelected(copia.isRequiereFirma());
            if (copia.getDestino() != null) {
                txtDistrito.setText(copia.getDestino().getDistrito());
                txtProvincia.setText(copia.getDestino().getProvincia());
                txtDepartamento.setText(copia.getDestino().getDepartamento());
            }
            txtResultado.setText("Plantilla clonada (código=" + copia.getCodigo() + ", estado=" + copia.getEstado()
                    + ").\nComplete los datos del nuevo destinatario y registre.\n");
            txtNombre.requestFocus();
        } catch (IllegalArgumentException ex) {
            info("Aún no se registró ningún envío en esta sesión.");
        }
    }

    private void limpiarDestino() {
        for (JTextField f : new JTextField[]{txtNombre, txtCelular, txtEmail, txtCalle}) {
            f.setText("");
        }
    }
}
