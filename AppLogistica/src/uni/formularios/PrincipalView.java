package uni.formularios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import uni.database.AccesoDB;
import uni.entity.EmpleadoTo;
import uni.entity.RolUsuario;
import uni.util.ConfiguracionSistema;
import uni.util.SesionUsuario;

/**
 * Ventana principal con JDesktopPane y menús Archivo, Procesos, Consultas,
 * Reportes, Utilitarios y Ayuda (misma organización que Principalview de
 * AppEmpresa). Cada opción se habilita según el rol del usuario en sesión.
 */
public class PrincipalView extends JFrame {

    private static PrincipalView actual;

    private final JDesktopPane desktopPane = new JDesktopPane() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(31, 78, 121), 0, getHeight(), new Color(12, 35, 58)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 40));
            g2.setFont(new Font("SansSerif", Font.BOLD, 54));
            g2.drawString("LogiRuta Perú", 40, getHeight() - 90);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
            g2.drawString("Plataforma de logística y seguimiento de envíos - AppLogistica", 44, getHeight() - 55);
        }
    };
    private final JLabel lblUsuario = new JLabel();
    private final JLabel lblIndicadores = new JLabel();

    public PrincipalView() {
        actual = this;
        initComponents();
        setBounds(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        setExtendedState(MAXIMIZED_BOTH);
    }

    public static PrincipalView getActual() {
        return actual;
    }

    private void initComponents() {
        EmpleadoTo u = SesionUsuario.getInstancia().getUsuario();
        setTitle("AppLogistica - LogiRuta Perú S.A.C.  |  " + u.getNombreCompleto() + " (" + u.getRol() + ")");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });

        JMenuBar menuBar = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        archivo.add(item("Clientes", "People.png", () -> abrir(new ClienteView()),
                RolUsuario.ADMINISTRADOR, RolUsuario.AGENTE_VENTANILLA));
        archivo.add(item("Empleados", "User.png", () -> abrir(new EmpleadoView()), RolUsuario.ADMINISTRADOR));
        archivo.addSeparator();
        archivo.add(item("Salir", "application-exit.png", this::salir, (RolUsuario[]) null));
        menuBar.add(archivo);

        JMenu procesos = new JMenu("Procesos");
        procesos.add(item("Registrar envío", "shopping_cart.png", () -> abrir(new EnvioView()),
                RolUsuario.ADMINISTRADOR, RolUsuario.AGENTE_VENTANILLA));
        procesos.add(item("Seguimiento y estados del envío", "Run.png", () -> abrir(new SeguimientoView(null)),
                (RolUsuario[]) null));
        procesos.add(item("Manifiesto de carga y despacho", "distributor-report.png",
                () -> abrir(new ManifiestoView()),
                RolUsuario.ADMINISTRADOR, RolUsuario.SUPERVISOR_RUTA, RolUsuario.OPERADOR_ALMACEN));
        menuBar.add(procesos);

        JMenu consultas = new JMenu("Consultas");
        consultas.add(item("Envíos por estado", "buscar.gif", () -> abrir(new ConsultaEnviosView()),
                (RolUsuario[]) null));
        menuBar.add(consultas);

        JMenu reportes = new JMenu("Reportes");
        reportes.add(item("Reportes estratégicos", "barchart.png", () -> abrir(new ReporteView()),
                RolUsuario.ADMINISTRADOR, RolUsuario.SUPERVISOR_RUTA));
        menuBar.add(reportes);

        JMenu utilitarios = new JMenu("Utilitarios");
        utilitarios.add(item("Alertas, notificaciones y bitácora", "Information.png",
                () -> abrir(new AlertasView()), RolUsuario.ADMINISTRADOR, RolUsuario.SUPERVISOR_RUTA));
        menuBar.add(utilitarios);

        JMenu ayuda = new JMenu("Ayuda");
        ayuda.add(item("Acerca de", "Globe1.png", this::acercaDe, (RolUsuario[]) null));
        menuBar.add(ayuda);
        setJMenuBar(menuBar);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.add(lblUsuario);
        barra.add(izq, BorderLayout.NORTH);
        barra.add(lblIndicadores, BorderLayout.SOUTH);
        lblIndicadores.setForeground(new Color(31, 78, 121));
        lblUsuario.setText("Usuario: " + u.getIdempleado() + " - " + u.getNombreCompleto() + "  |  Rol: "
                + u.getRol() + "  |  BD: " + AccesoDB.getInstancia().getUrl().replaceAll("\\?.*", "")
                + "  |  Integraciones: " + ConfiguracionSistema.getInstancia().getIntegraciones().getNombreFamilia());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(desktopPane, BorderLayout.CENTER);
        getContentPane().add(barra, BorderLayout.SOUTH);
        pack();

        // Indicadores en vivo (IndicadoresObserver): se refrescan cada 2 segundos
        new Timer(2000, e -> lblIndicadores.setText("Indicadores en vivo (Observer) - "
                + ConfiguracionSistema.getInstancia().getIndicadores().getResumen())).start();
        lblIndicadores.setText("Indicadores en vivo (Observer) - "
                + ConfiguracionSistema.getInstancia().getIndicadores().getResumen());
    }

    private JMenuItem item(String texto, String icono, Runnable accion, RolUsuario... roles) {
        JMenuItem mi = new JMenuItem(texto);
        URL url = getClass().getResource("/imagen/" + icono);
        if (url != null) {
            mi.setIcon(new ImageIcon(url));
        }
        if (roles != null) {
            Set<RolUsuario> permitidos = EnumSet.noneOf(RolUsuario.class);
            for (RolUsuario r : roles) {
                permitidos.add(r);
            }
            mi.setEnabled(permitidos.contains(SesionUsuario.getInstancia().getUsuario().getRol()));
        }
        mi.addActionListener(e -> accion.run());
        return mi;
    }

    /** Agrega un formulario interno al escritorio, centrado. */
    public void abrir(JInternalFrame f) {
        desktopPane.add(f);
        int x = Math.max(0, (desktopPane.getWidth() - f.getWidth()) / 2);
        int y = Math.max(0, (desktopPane.getHeight() - f.getHeight()) / 2);
        f.setLocation(x, y);
        f.setVisible(true);
        try {
            f.setSelected(true);
        } catch (PropertyVetoException ignorar) {
            // sin efecto
        }
    }

    private void acercaDe() {
        JOptionPane.showMessageDialog(this,
                "AppLogistica - Plataforma de logística y seguimiento de envíos\n"
                + "LogiRuta Perú S.A.C.\n\n"
                + "Curso: Diseño de Patrones - Proyecto Final (Avance 2: Capítulos I al VI)\n"
                + "Arquitectura en capas (molde AppEmpresa): controller, dao, database,\n"
                + "entity, formularios, reportes, service, util.\n\n"
                + "SOLID + Creacionales + Estructurales + Comportamiento\n"
                + "(Observer, State, Command, Memento).",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }

    private void salir() {
        if (JOptionPane.showConfirmDialog(this, "¿Desea salir del sistema?", "Salir",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                AccesoDB.getInstancia().cerrar();
            } catch (Exception ignorar) {
                // la aplicación se cierra igual
            }
            SesionUsuario.getInstancia().cerrar();
            System.exit(0);
        }
    }
}
