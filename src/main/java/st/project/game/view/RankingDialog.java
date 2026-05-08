package st.project.game.view;

import st.project.game.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankingDialog extends JDialog {
    public RankingDialog(List<User> users) {
        setTitle("Ranking de Jogadores");
        setModal(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0x0D0D1A));

        List<User> sorted = users.stream()
                .sorted(Comparator.comparingInt(User::getBestScore).reversed())
                .toList();

        String[] columns = {"Login", "Melhor Pontuação", "Sessões"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (User u : sorted) {
            model.addRow(new Object[]{u.getLogin(), u.getBestScore(), u.getTotalSessions()});
        }

        JTable table = new JTable(model);
        table.setEnabled(false);
        table.setBackground(new Color(0x1C1C38));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(0x3A3A60));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Fechar");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}