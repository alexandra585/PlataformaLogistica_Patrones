package uni.formularios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;
import uni.controller.EmpleadoController;

/**
 * Acceso al sistema (mismo comportamiento que LoginView de AppEmpresa:
 * valida con EmpleadoController y muestra una barra de progreso antes de
 * abrir la ventana principal). Clase principal del proyecto.
 */
public class LoginView extends JFrame {

    private Timer objTimer;
    private final JTextField txtusuario = new JTextField(14);
    private final JPasswordField txtpassword = new JPasswordField(14);
    private final JProgressBar jProgressBar1 = new JProgressBar();
    private final JLabel lblConectando = new JLabel("Conectando ...");

    public LoginView() {
        initComponents();
        setLocationRelativeTo(null);          // salga en el centro de la pantalla
    }

    private void initComponents() {
        setTitle("ACCESO AL SISTEMA - AppLogistica");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(new Color(31, 78, 121));
        cab.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel titulo = new JLabel("LogiRuta Perú S.A.C.");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JLabel sub = new JLabel("Plataforma de logística y seguimiento de envíos");
        sub.setForeground(new Color(200, 220, 240));
        cab.add(titulo, BorderLayout.NORTH);
        cab.add(sub, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        URL img = getClass().getResource("/imagen/login.png");
        JLabel icono = img == null ? new JLabel() : new JLabel(new ImageIcon(img));
        g.gridx = 0; g.gridy = 0; g.gridheight = 3;
        centro.add(icono, g);
        g.gridheight = 1; g.anchor = GridBagConstraints.WEST;
        g.gridx = 1; g.gridy = 0; centro.add(new JLabel("Usuario"), g);
        g.gridx = 2; centro.add(txtusuario, g);
        g.gridx = 1; g.gridy = 1; centro.add(new JLabel("Password"), g);
        g.gridx = 2; centro.add(txtpassword, g);

        JButton btnaceptar = new JButton("Aceptar");
        JButton btnsalir = new JButton("Salir");
        btnaceptar.addActionListener(e -> btnaceptarActionPerformed());
        btnsalir.addActionListener(e -> System.exit(0));
        txtpassword.addActionListener(e -> btnaceptarActionPerformed());
        JPanel botones = new JPanel();
        botones.add(btnaceptar);
        botones.add(btnsalir);
        g.gridx = 1; g.gridy = 2; g.gridwidth = 2; g.anchor = GridBagConstraints.EAST;
        centro.add(botones, g);

        JPanel pie = new JPanel(new BorderLayout(8, 0));
        pie.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));
        lblConectando.setFont(lblConectando.getFont().deriveFont(Font.BOLD));
        pie.add(lblConectando, BorderLayout.WEST);
        pie.add(jProgressBar1, BorderLayout.CENTER);
        JLabel ayuda = new JLabel("<html><small>Usuarios de prueba: admin/admin · rquispe, lmendoza, "
                + "jtorres, icardenas, mhuaman / 123456 (csalazar: de vacaciones)</small></html>");
        ayuda.setForeground(Color.GRAY);
        pie.add(ayuda, BorderLayout.SOUTH);
        lblConectando.setVisible(false);
        jProgressBar1.setVisible(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(cab, BorderLayout.NORTH);
        getContentPane().add(centro, BorderLayout.CENTER);
        getContentPane().add(pie, BorderLayout.SOUTH);
        pack();
    }

    private void btnaceptarActionPerformed() {
        EmpleadoController oUsuario = new EmpleadoController();
        try {
            if (oUsuario.EmpleadoValidar(txtusuario.getText().trim(), new String(txtpassword.getPassword()))) {
                objTimer = new Timer(15, new ClaseTime());
                objTimer.start();
                lblConectando.setVisible(true);
                jProgressBar1.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Acceso denegado...", "Login", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(this, "Acceso denegado: " + ex.getMessage(), "Login",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar con MySQL:\n" + ex.getMessage()
                    + "\n\nVerifique que el servicio MySQL esté iniciado, que ejecutó el script "
                    + "sql/01_instalacion_inicial.sql\ny los datos del archivo db.properties.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    public class ClaseTime implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            int a = jProgressBar1.getValue();
            if (a < 100) {
                jProgressBar1.setValue(a + 2);
            } else {
                objTimer.stop();
                dispose();
                new PrincipalView().setVisible(true);
            }
        }
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(LoginView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new LoginView().setVisible(true));
    }
}
