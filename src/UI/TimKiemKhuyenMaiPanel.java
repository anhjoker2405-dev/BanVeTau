package ui; // Hoặc package UI của bạn

// Import các lớp cần thiết
import dao.KhuyenMai_Dao; // Sửa tên này nếu file DAO của bạn khác
import entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class TimKiemKhuyenMaiPanel extends JPanel {

    private JTable tblKetQua;
    private DefaultTableModel tableModel;
    private KhuyenMai_Dao khuyenMaiDAO;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;

    public TimKiemKhuyenMaiPanel() {
        khuyenMaiDAO = new KhuyenMai_Dao(); 
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Panel Tìm kiếm 
        JPanel pnlSearch = new JPanel(new BorderLayout(5, 5));
        pnlSearch.setBorder(new TitledBorder("Bộ lọc tìm kiếm"));
        
        pnlSearch.add(new JLabel("Nhập (Mã KM, Tên KM, Mô tả):"), BorderLayout.WEST);
        txtSearch = new JTextField();
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        
        add(pnlSearch, BorderLayout.NORTH);

        // 2. Panel Kết quả
        String[] columnNames = {"Mã KM", "Tên KM", "Giảm Giá", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Mô Tả"};
        
        // Cài đặt table model (quan trọng: isCellEditable = false)
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        
        tblKetQua = new JTable(tableModel);
        tblKetQua.setRowHeight(25);
        tblKetQua.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Thêm bộ lọc Sorter vào bảng
        sorter = new TableRowSorter<>(tableModel);
        tblKetQua.setRowSorter(sorter);

        add(new JScrollPane(tblKetQua), BorderLayout.CENTER);

        // 3. Tải dữ liệu và Thêm sự kiện
        loadDataToTable();
        addEvents();
    }

    /**
     * Tải toàn bộ dữ liệu từ CSDL vào JTable
     */
    private void loadDataToTable() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        
        List<KhuyenMai> ds = khuyenMaiDAO.getAllKhuyenMai(); 
        
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

    /**
     * Thêm sự kiện cho ô tìm kiếm
     */
    private void addEvents() {
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });
    }

    /**
     * Lọc dữ liệu JTable dựa trên nội dung ô tìm kiếm
     */
    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            // Nếu ô tìm kiếm rỗng, hiển thị tất cả
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }
}