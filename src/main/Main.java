package main;

import ui.LoginFrame;
import javax.swing.*;
import javax.swing.border.Border;

public class Main {
    public static void main(String[] args) {

        try {
            //Dùng Look&Feel hệ thống để tránh viền "xám" của Nimbus
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            //Gỡ viền mặc định của ComboBox (toàn cục)
            Border empty = BorderFactory.createEmptyBorder();
            UIManager.put("ComboBox.border", empty);
            UIManager.put("ComboBox.editorBorder", empty);
            UIManager.put("ComboBox.padding", new javax.swing.plaf.InsetsUIResource(0, 0, 0, 0));
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

//package main;
//
//import ui.LoginFrame;
//import javax.swing.*;
//
//public class Main {
//    public static void main(String[] args) {
//        try {
//            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception ignored) {}
//
//        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
//        
//    }
//}
//
