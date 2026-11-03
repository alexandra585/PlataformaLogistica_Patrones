package uni.formularios;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Clase base de los formularios internos (JInternalFrame), igual que los
 * formularios de AppEmpresa que se abren dentro del JDesktopPane de la
 * ventana principal. Reúne utilidades comunes para no repetir código (DRY).
 */
public abstract class FormularioBase extends JInternalFrame {

    protected static final Color AZUL = new Color(31, 78, 121);
    protected static final Color AZUL_CLARO = new Color(222, 234, 246);

    protected FormularioBase(String titulo, int ancho, int alto) {
        super(titulo, true, true, true, true);
        setSize(ancho, alto);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(6, 6));
    }

    /** Franja azul con el título del formulario y el patrón que demuestra. */
    protected JPanel cabecera(String titulo, String subtitulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AZUL);
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel t = new JLabel(titulo);
        t.setForeground(Color.WHITE);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 16f));
        JLabel s = new JLabel(subtitulo);
        s.setForeground(new Color(200, 220, 240));
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    /** Panel de dos columnas: etiqueta + componente. */
    protected JPanel formulario(String tituloBorde, Object... pares) {
        JPanel p = new JPanel(new GridBagLayout());
        if (tituloBorde != null) {
            p.setBorder(BorderFactory.createTitledBorder(tituloBorde));
        }
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 5, 3, 5);
        g.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < pares.length; i += 2) {
            g.gridy = i / 2;
            g.gridx = 0;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            p.add(new JLabel(String.valueOf(pares[i])), g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            p.add((Component) pares[i + 1], g);
        }
        return p;
    }

    protected JButton boton(String texto, String icono, ActionListener accion) {
        JButton b = new JButton(texto);
        if (icono != null) {
            URL url = getClass().getResource("/imagen/" + icono);
            if (url != null) {
                b.setIcon(new ImageIcon(url));
            }
        }
        b.addActionListener(accion);
        return b;
    }

    protected DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    protected JTable tabla(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(22);
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    protected void info(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    protected void error(Exception ex) {
        String msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();
        int tipo = (ex instanceof SecurityException) ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE;
        String titulo = (ex instanceof SecurityException) ? "Acceso denegado (Proxy)"
                : (ex instanceof IllegalStateException) ? "Operación no permitida" : "Error";
        JOptionPane.showMessageDialog(this, msg, titulo, tipo);
    }

    protected static String texto(JComponent c) {
        if (c instanceof javax.swing.text.JTextComponent) {
            return ((javax.swing.text.JTextComponent) c).getText().trim();
        }
        return "";
    }
}
