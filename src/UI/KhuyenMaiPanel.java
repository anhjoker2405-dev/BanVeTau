package ui; 

import dao.KhuyenMai_Dao;
import entity.KhuyenMai;
import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class KhuyenMaiPanel extends JPanel {
	
    private JTextField txtMaKM, txtTenKM, txtGiamGia, txtMoTa;
    private JDateChooser dateNgayBatDau, dateNgayKetThuc;
    private JButton btnThem, btnLuu, btnXoa, btnLamMoi;
    private JTable tblKhuyenMai;
    private DefaultTableModel tableModel;
    private KhuyenMai_Dao khuyenMaiDAO; 

    public KhuyenMaiPanel() {
        khuyenMaiDAO = new KhuyenMai_Dao(); 
        
        setLayout(new BorderLayout());

        // 1. Panel Form Nhập Liệu 
        JPanel pnlForm = new JPanel(new GridLayout(3, 4, 10, 10)); 
        pnlForm.setBorder(BorderFactory.createTitledBorder("Thông tin khuyến mãi"));

        pnlForm.add(new JLabel("Mã khuyến mãi:"));
        txtMaKM = new JTextField();
        pnlForm.add(txtMaKM);

        pnlForm.add(new JLabel("Tên khuyến mãi:"));
        txtTenKM = new JTextField();
        pnlForm.add(txtTenKM);

        pnlForm.add(new JLabel("Giảm giá (giá trị):"));
        txtGiamGia = new JTextField();
        pnlForm.add(txtGiamGia);

        pnlForm.add(new JLabel("Mô tả:"));
        txtMoTa = new JTextField();
        pnlForm.add(txtMoTa);

        pnlForm.add(new JLabel("Ngày bắt đầu:"));
        dateNgayBatDau = new JDateChooser();
        dateNgayBatDau.setDateFormatString("dd/MM/yyyy HH:mm:ss"); 
        pnlForm.add(dateNgayBatDau);

        pnlForm.add(new JLabel("Ngày kết thúc:"));
        dateNgayKetThuc = new JDateChooser();
        dateNgayKetThuc.setDateFormatString("dd/MM/yyyy HH:mm:ss"); 
        pnlForm.add(dateNgayKetThuc);

        add(pnlForm, BorderLayout.NORTH);

        // 2. Panel Bảng
        String[] columnNames = {"Mã KM", "Tên KM", "Giảm Giá", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Mô Tả"};
        tableModel = new DefaultTableModel(columnNames, 0);
        tblKhuyenMai = new JTable(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(tblKhuyenMai);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Panel Nút 
        JPanel pnlButtons = new JPanel();
        
        btnThem = new JButton("Thêm");
        btnLuu = new JButton("Lưu");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm mới");
        
        pnlButtons.add(btnThem);
        pnlButtons.add(btnLuu);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);
        
        add(pnlButtons, BorderLayout.SOUTH);

        // 4. Tải dữ liệu ban đầu 
        loadDataToTable();

        // 5. Thêm sự kiện 
        addEvents(); 
    }

     //BƯỚC 4: VIẾT LOGIC CHO CÁC SỰ KIỆN
    private void addEvents() {
        
        // Sự kiện nút "Làm mới"
        btnLamMoi.addActionListener(e -> clearForm());
        
        // Sự kiện click chuột vào bảng
        tblKhuyenMai.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblKhuyenMai.getSelectedRow();
                if (row >= 0) {
                    fillForm(row);
                }
            }
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        btnThem.addActionListener(e -> themKhuyenMai());
        btnLuu.addActionListener(e -> capNhatKhuyenMai());
        btnXoa.addActionListener(e -> xoaKhuyenMai());
    }

    private void loadDataToTable() {
        // Xóa hết dữ liệu cũ trên bảng
        tableModel.setRowCount(0);
        
        List<KhuyenMai> ds = khuyenMaiDAO.getAllKhuyenMai();
        
        // Dùng vòng lặp để đưa dữ liệu từ List vào tableModel
        for (KhuyenMai km : ds) {
            Object[] row = {
                km.getMaKhuyenMai(),
                km.getTenKhuyenMai(),
                km.getGiamGia(),
                km.getNgayBatDau(), 
                km.getNgayKetThuc(), 
                km.getMoTa()
            };
            tableModel.addRow(row);
        }
    }
    
    private void clearForm() {
        txtMaKM.setText("");
        txtTenKM.setText("");
        txtGiamGia.setText("");
        txtMoTa.setText("");
        dateNgayBatDau.setDate(null);
        dateNgayKetThuc.setDate(null);
        txtMaKM.requestFocus(); // Focus vào ô nhập liệu đầu tiên
        tblKhuyenMai.clearSelection(); // Bỏ chọn trên bảng
    }

    // Lấy dữ liệu từ 1 dòng trên bảng và điền vào form
    private void fillForm(int row) {
        txtMaKM.setText(tableModel.getValueAt(row, 0).toString());
        txtTenKM.setText(tableModel.getValueAt(row, 1).toString());
        txtGiamGia.setText(tableModel.getValueAt(row, 2).toString());
        
        // Lấy LocalDateTime từ bảng
        LocalDateTime ngayBD_LDT = (LocalDateTime) tableModel.getValueAt(row, 3);
        LocalDateTime ngayKT_LDT = (LocalDateTime) tableModel.getValueAt(row, 4);

        // Chuyển LocalDateTime sang java.util.Date để set cho JDateChooser
        Date ngayBD_Date = Date.from(ngayBD_LDT.atZone(ZoneId.systemDefault()).toInstant());
        Date ngayKT_Date = Date.from(ngayKT_LDT.atZone(ZoneId.systemDefault()).toInstant()); 
        
        dateNgayBatDau.setDate(ngayBD_Date);
        dateNgayKetThuc.setDate(ngayKT_Date);
        
        if (tableModel.getValueAt(row, 5) != null) {
             txtMoTa.setText(tableModel.getValueAt(row, 5).toString());
        } else {
             txtMoTa.setText(""); 
        }
    }

    // Lấy dữ liệu từ form để tạo đối tượng KhuyenMai
    private KhuyenMai createKhuyenMaiFromForm() {
        String ma = txtMaKM.getText();
        String ten = txtTenKM.getText();
        BigDecimal giamGia = new BigDecimal(txtGiamGia.getText());
        String moTa = txtMoTa.getText();
        
        // Chuyển java.util.Date (từ JDateChooser) sang LocalDateTime (cho Entity)
        Date ngayBD_Date = dateNgayBatDau.getDate();
        Date ngayKT_Date = dateNgayKetThuc.getDate();
        
        // Validation (kiểm tra dữ liệu) 
        if (ma.isEmpty() || ten.isEmpty() || ngayBD_Date == null || ngayKT_Date == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin bắt buộc.");
        }
        
        LocalDateTime ngayBD_LDT = ngayBD_Date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime ngayKT_LDT = ngayKT_Date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        if (ngayKT_LDT.isBefore(ngayBD_LDT)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        }
        
        return new KhuyenMai(ma, ten, giamGia, ngayBD_LDT, ngayKT_LDT, moTa);
    }
    
    private void themKhuyenMai() {
        try {
            KhuyenMai km = createKhuyenMaiFromForm();
            
            // Gọi DAO để thêm
            if (khuyenMaiDAO.addKhuyenMai(km)) {
                JOptionPane.showMessageDialog(this, "Thêm khuyến mãi thành công!");
                loadDataToTable(); 
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại. Mã có thể đã tồn tại.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm: " + e.getMessage());
        }
    }
    
    private void capNhatKhuyenMai() {
        int row = tblKhuyenMai.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi trên bảng để cập nhật.");
            return;
        }
        
        try {
            KhuyenMai km = createKhuyenMaiFromForm();
            
            // Gọi DAO để cập nhật
            if (khuyenMaiDAO.updateKhuyenMai(km)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + e.getMessage());
        }
    }
    
    private void xoaKhuyenMai() {
        int row = tblKhuyenMai.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi trên bảng để xóa.");
            return;
        }
        
        // Hộp thoại xác nhận
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn xóa khuyến mãi này không?", 
                "Xác nhận xóa", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String maKM = tableModel.getValueAt(row, 0).toString();
            
            if (khuyenMaiDAO.deleteKhuyenMai(maKM)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadDataToTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại (có thể do lỗi khóa ngoại, ví dụ khuyến mãi đã được dùng).");
            }
        }
    }
}