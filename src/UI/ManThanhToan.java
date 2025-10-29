package ui;

import dao.KhuyenMai_Dao;
import entity.KhuyenMai;
import entity.TicketSelection;
import entity.TrainInfo;
import util.AppEventBus;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Giao diện bước 3 – màn hình thanh toán.
 * Tách riêng khỏi BanVe để dễ bảo trì và tái sử dụng.
 */
public class ManThanhToan extends JPanel {
    
    public enum Mode {
        BOOKING,
        EXCHANGE
    }

    private final JLabel routeLabel = new JLabel("-");
    private final JLabel scheduleLabel = new JLabel(" ");

    private final JPanel ticketListPanel = new JPanel();
    private final JButton btnEditTickets = new JButton("Sửa vé đã chọn");

    private final JLabel customerNameLabel = new JLabel("-");
    private final JLabel customerPhoneLabel = new JLabel("-");
    private final JLabel customerIdLabel = new JLabel("-");

    private final JComboBox<KhuyenMai> promotionCombo = new JComboBox<>();
    private final JLabel vatValueLabel = new JLabel("10");
    private final JLabel totalAmountLabel = new JLabel("0 ₫");
    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final JButton btnBack = new JButton("Quay lại");
    private final JButton btnConfirm = new JButton("Xác nhận");
    
    private Mode mode = Mode.BOOKING;
    private ExchangeBreakdown exchangeBreakdown;

    private JLabel promotionLabel;
    private JLabel vatLabel;
    private JPanel exchangePanel;
    private JLabel exchangeOldFareLabel;
    private JLabel exchangeNewFareLabel;
    private JLabel exchangeFeeTitleLabel;
    private JLabel exchangeFeeLabel;
    private JLabel exchangeDifferenceLabel;
    private JLabel exchangeNoteLabel;

    private Runnable backAction;
    private Runnable editAction;
    private Runnable confirmAction;

    private List<TicketSelection> selections = new ArrayList<>();
    private TrainInfo trainInfo;

    private final NumberFormat currencyFormat;

    private int totalAmount = 0;
    private BigDecimal vatRate = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private String selectedPromotionId;

    public ManThanhToan() {
        // spinner removed
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);

        setOpaque(true);
        setBackground(new Color(0xF5F8FD));
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        configureComponents();
        loadPromotions();
        refreshUI();
        updateModeUI();
    }
    
    
    
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("BƯỚC 3 · THANH TOÁN");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(0x1E40A0));
        header.add(title, BorderLayout.NORTH);

        JPanel routePanel = new JPanel(new GridLayout(2, 1));
        routePanel.setBackground(Color.WHITE);
        routePanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xDDE3F0)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        routeLabel.setFont(routeLabel.getFont().deriveFont(Font.BOLD, 15f));
        routeLabel.setForeground(new Color(0x10306B));
        scheduleLabel.setForeground(new Color(0x4D638C));

        routePanel.add(routeLabel);
        routePanel.add(scheduleLabel);

        header.add(routePanel, BorderLayout.CENTER);
        return header;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setOpaque(false);

        body.add(buildTicketArea(), BorderLayout.CENTER);
        body.add(buildInfoArea(), BorderLayout.SOUTH);
        return body;
    }

    private JComponent buildTicketArea() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel lbl = new JLabel("Chi tiết vé đã chọn");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));
        lbl.setForeground(new Color(0x1E40A0));
        titlePanel.add(lbl, BorderLayout.WEST);

        styleLinkButton(btnEditTickets);
        titlePanel.add(btnEditTickets, BorderLayout.EAST);

        ticketListPanel.setOpaque(true);
        ticketListPanel.setBackground(Color.WHITE);
        ticketListPanel.setLayout(new BoxLayout(ticketListPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(ticketListPanel);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new LineBorder(new Color(0xDDE3F0)));
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        wrapper.add(titlePanel, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildInfoArea() {
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        info.add(buildCustomerBox());
        info.add(Box.createVerticalStrut(12));
        info.add(buildPaymentBox());
        return info;
    }

    private JComponent buildCustomerBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setOpaque(true);
        box.setBackground(Color.WHITE);
        box.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xDDE3F0)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        box.add(new JLabel("Khách hàng"), gc);
        gc.gridx = 1;
        customerNameLabel.setFont(customerNameLabel.getFont().deriveFont(Font.BOLD));
        box.add(customerNameLabel, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        box.add(new JLabel("Số điện thoại"), gc);
        gc.gridx = 1;
        box.add(customerPhoneLabel, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        box.add(new JLabel("CCCD"), gc);
        gc.gridx = 1;
        box.add(customerIdLabel, gc);

        return box;
    }

    private JComponent buildPaymentBox() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setOpaque(true);
        box.setBackground(Color.WHITE);
        box.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xDDE3F0)),
                new EmptyBorder(16, 18, 16, 18)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        promotionLabel = new JLabel("Khuyến mãi");
        box.add(promotionLabel, gc);
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        promotionCombo.setPreferredSize(new Dimension(220, 32));
        box.add(promotionCombo, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.fill = GridBagConstraints.NONE;
        vatLabel = new JLabel("VAT (%)");
        box.add(vatLabel, gc);
        gc.gridx = 1;
        vatValueLabel.setPreferredSize(new Dimension(80, 32));
        box.add(vatValueLabel, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        exchangePanel = buildExchangePanel();
        box.add(exchangePanel, gc);
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;

        gc.gridx = 0;
        gc.gridy = 3;
        box.add(new JLabel("Số tiền cần thanh toán"), gc);
        gc.gridx = 1;
        totalAmountLabel.setFont(totalAmountLabel.getFont().deriveFont(Font.BOLD, 18f));
        totalAmountLabel.setForeground(new Color(0xC62828));
        box.add(totalAmountLabel, gc);

        return box;
    }
    
    private JPanel buildExchangePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints egc = new GridBagConstraints();
        egc.insets = new Insets(4, 6, 4, 6);
        egc.anchor = GridBagConstraints.WEST;

        exchangeOldFareLabel = createValueLabel(new Color(0x102345));
        exchangeNewFareLabel = createValueLabel(new Color(0x102345));
        exchangeFeeLabel = createValueLabel(new Color(0xC62828));
        exchangeDifferenceLabel = createValueLabel(new Color(0xC62828));
        exchangeNoteLabel = new JLabel(" ");
        exchangeNoteLabel.setFont(exchangeNoteLabel.getFont().deriveFont(Font.ITALIC, 12f));
        exchangeNoteLabel.setForeground(new Color(0x6B7A99));

        exchangeFeeTitleLabel = new JLabel("Phụ phí đổi");

        addExchangeRow(panel, egc, 0, new JLabel("Giá vé cũ"), exchangeOldFareLabel);
        addExchangeRow(panel, egc, 1, new JLabel("Giá vé mới"), exchangeNewFareLabel);
        addExchangeRow(panel, egc, 2, exchangeFeeTitleLabel, exchangeFeeLabel);
        addExchangeRow(panel, egc, 3, new JLabel("Chênh lệch phải trả"), exchangeDifferenceLabel);

        egc.gridx = 0;
        egc.gridy = 4;
        egc.gridwidth = 2;
        egc.weightx = 1;
        panel.add(exchangeNoteLabel, egc);

        panel.setVisible(false);
        return panel;
    }

    private void addExchangeRow(JPanel panel, GridBagConstraints gc, int row, JLabel titleLabel, JLabel valueLabel) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 1;
        gc.weightx = 0;
        panel.add(titleLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(valueLabel, gc);
        gc.fill = GridBagConstraints.NONE;
    }

    private JLabel createValueLabel(Color color) {
        JLabel label = new JLabel("0 ₫");
        label.setForeground(color);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        styleSecondaryButton(btnBack);
        stylePrimaryButton(btnConfirm);

        footer.add(btnBack);
        footer.add(btnConfirm);
        return footer;
    }

    private void configureComponents() {
            promotionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhuyenMai km) {
                    BigDecimal percent = km.getGiamGia() != null
                            ? km.getGiamGia().multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    setText(km.getTenKhuyenMai() + " (" + percent.intValue() + "%)");
                } else if (value == null) {
                    setText("Không áp dụng");
                }
                return c;
            }
        });
        btnBack.addActionListener(e -> {
            if (backAction != null) {
                backAction.run();
            }
        });
        btnConfirm.addActionListener(e -> {
            if (confirmAction != null) {
                confirmAction.run();
            }
        });
        btnEditTickets.addActionListener(e -> {
            if (editAction != null) {
                editAction.run();
            }
        });

        promotionCombo.addActionListener(e -> recalcTotal());
        // removed spinner listener
    }

    private void loadPromotions() {
        if (mode == Mode.EXCHANGE) {
            promotionCombo.removeAllItems();
            promotionCombo.addItem(null);
            promotionCombo.setSelectedIndex(0);
            selectedPromotionId = null;
            return;
        }
        String previousSelectionId = selectedPromotionId;
        KhuyenMai currentSelection = (KhuyenMai) promotionCombo.getSelectedItem();
        if (currentSelection != null) {
            previousSelectionId = currentSelection.getMaKhuyenMai();
        }
        promotionCombo.removeAllItems();
        promotionCombo.addItem(null);
        
        KhuyenMai itemToSelect = null;
        try {
            List<KhuyenMai> all = new KhuyenMai_Dao().getAllKhuyenMai();
            for (KhuyenMai km : all) {
                promotionCombo.addItem(km);
                if (itemToSelect == null && previousSelectionId != null && previousSelectionId.equals(km.getMaKhuyenMai())) {
                    itemToSelect = km;
                }
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (itemToSelect != null) {
            promotionCombo.setSelectedItem(itemToSelect);
        } else {
            promotionCombo.setSelectedIndex(0);
        }
    }

    private void refreshUI() {
        renderTickets();
        updateCustomerInfo();
        updateExchangePanel();
        recalcTotal();
        updateTrainInfo();
    }

    private void renderTickets() {
        ticketListPanel.removeAll();
        if (selections.isEmpty()) {
            ticketListPanel.add(createEmptyMessage("Chưa có ghế nào được chọn."));
        } else {
            int index = 1;
            for (TicketSelection selection : selections) {
                ticketListPanel.add(createTicketCard(index++, selection));
                ticketListPanel.add(Box.createVerticalStrut(10));
            }
        }
        ticketListPanel.revalidate();
        ticketListPanel.repaint();
    }

    private Component createTicketCard(int index, TicketSelection selection) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xDDE3F0)),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel seatLabel = new JLabel(String.format("Vé %d · Toa %d · Ghế %d", index, selection.getCar(), selection.getSeatNumber()));
        seatLabel.setFont(seatLabel.getFont().deriveFont(Font.BOLD));
        head.add(seatLabel, BorderLayout.WEST);

        JLabel priceLabel = new JLabel(formatCurrency(selection.getBasePrice()));
        priceLabel.setForeground(new Color(0xC62828));
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD));
        head.add(priceLabel, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 6));
        grid.setOpaque(false);

        addInfoRow(grid, "Họ tên", safe(selection.getHoTen()));
        addInfoRow(grid, "Số điện thoại", safe(selection.getSoDienThoai()));
        addInfoRow(grid, "CCCD", safe(selection.getCccd()));
        addInfoRow(grid, "Năm sinh", safe(selection.getNamSinh()));
        addInfoRow(grid, "Loại vé", safe(selection.getTenLoaiVe()));

        card.add(grid, BorderLayout.CENTER);

        JButton btnEdit = new JButton("Sửa vé này");
        styleSecondaryButton(btnEdit);
        btnEdit.setForeground(new Color(0x1E40A0));
        btnEdit.addActionListener(e -> {
            if (editAction != null) {
                editAction.run();
            }
        });
        card.add(btnEdit, BorderLayout.SOUTH);

        return card;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lb = new JLabel(label + ":");
        lb.setForeground(new Color(0x4D638C));
        panel.add(lb);

        JLabel val = new JLabel(value);
        val.setForeground(new Color(0x102345));
        panel.add(val);
    }

    private Component createEmptyMessage(String message) {
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setForeground(new Color(0x6B7A99));
        label.setBorder(new EmptyBorder(20, 0, 20, 0));
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void updateCustomerInfo() {
        if (selections.isEmpty()) {
            customerNameLabel.setText("-");
            customerPhoneLabel.setText("-");
            customerIdLabel.setText("-");
            return;
        }
        TicketSelection first = selections.get(0);
        customerNameLabel.setText(safe(first.getHoTen()));
        customerPhoneLabel.setText(safe(first.getSoDienThoai()));
        customerIdLabel.setText(safe(first.getCccd()));
    }

    private void updateTrainInfo() {
        if (trainInfo == null) {
            routeLabel.setText("-");
            scheduleLabel.setText(" ");
            return;
        }
        routeLabel.setText(trainInfo.getRoute() != null ? trainInfo.getRoute() : "-");
        if (trainInfo.getDepart() != null || trainInfo.getArrive() != null) {
            String depart = trainInfo.getDepart() != null ? trainInfo.getDepart() : "?";
            String arrive = trainInfo.getArrive() != null ? trainInfo.getArrive() : "?";
            scheduleLabel.setText("Mã chuyến " + trainInfo.getCode() + " · " + depart + " → " + arrive);
        } else {
            scheduleLabel.setText("Mã chuyến " + trainInfo.getCode());
        }
    }

    private void recalcTotal() {
        if (mode == Mode.EXCHANGE) {
            vatRate = BigDecimal.ZERO;
            selectedPromotionId = null;
            if (exchangeBreakdown != null) {
                BigDecimal rounded = exchangeBreakdown.getTotalPayable().setScale(0, RoundingMode.HALF_UP);
                totalAmount = rounded.intValue();
                unitPrice = exchangeBreakdown.getNewFare() != null ? exchangeBreakdown.getNewFare() : BigDecimal.ZERO;
                totalAmountLabel.setText(formatCurrency(rounded));
            } else {
                totalAmount = 0;
                unitPrice = BigDecimal.ZERO;
                totalAmountLabel.setText(formatCurrency(BigDecimal.ZERO));
            }
            return;
        }
        BigDecimal subtotal = BigDecimal.ZERO;
        for (TicketSelection selection : selections) {
            subtotal = subtotal.add(selection.getBasePrice());
        }

        vatRate = VAT_RATE;
        KhuyenMai km = (KhuyenMai) promotionCombo.getSelectedItem();
        BigDecimal discountRate = (km != null && km.getGiamGia() != null) ? km.getGiamGia() : BigDecimal.ZERO;
        selectedPromotionId = km != null ? km.getMaKhuyenMai() : null;

        BigDecimal amountWithVat = subtotal.multiply(BigDecimal.ONE.add(vatRate));
        BigDecimal finalAmount = amountWithVat.multiply(BigDecimal.ONE.subtract(discountRate));
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        BigDecimal rounded = finalAmount.setScale(0, RoundingMode.HALF_UP);
        totalAmount = rounded.intValue();

        int quantity = Math.max(selections.size(), 1);
        unitPrice = rounded.divide(new BigDecimal(quantity), 0, RoundingMode.HALF_UP);

        totalAmountLabel.setText(formatCurrency(rounded));
    }
    
    private void updateExchangePanel() {
        boolean isExchange = mode == Mode.EXCHANGE;
        if (exchangePanel != null) {
            exchangePanel.setVisible(isExchange);
        }
        if (!isExchange) {
            return;
        }
        if (exchangeBreakdown == null) {
            exchangeOldFareLabel.setText(formatCurrency(BigDecimal.ZERO));
            exchangeNewFareLabel.setText(formatCurrency(BigDecimal.ZERO));
            exchangeFeeTitleLabel.setText("Phụ phí đổi");
            exchangeFeeLabel.setText(formatCurrency(BigDecimal.ZERO));
            exchangeDifferenceLabel.setText(formatCurrency(BigDecimal.ZERO));
            exchangeNoteLabel.setText(" ");
            return;
        }
        exchangeOldFareLabel.setText(formatCurrency(exchangeBreakdown.getOldFare()));
        exchangeNewFareLabel.setText(formatCurrency(exchangeBreakdown.getNewFare()));
        exchangeFeeTitleLabel.setText("Phụ phí đổi (" + exchangeBreakdown.getFeeRatePercentText() + ")");
        exchangeFeeLabel.setText(formatCurrency(exchangeBreakdown.getFeeAmount()));
        exchangeDifferenceLabel.setText(formatCurrency(exchangeBreakdown.getPayableDifference()));
        if (exchangeBreakdown.getDifference().signum() < 0) {
            exchangeNoteLabel.setText("Vé mới rẻ hơn " + formatCurrency(exchangeBreakdown.getDifference().abs()) + ". Khoản chênh lệch không hoàn lại.");
        } else if (exchangeBreakdown.getDifference().signum() > 0) {
            exchangeNoteLabel.setText("Vé mới cao hơn " + formatCurrency(exchangeBreakdown.getDifference()) + ".");
        } else {
            exchangeNoteLabel.setText(" ");
        }
    }

    private void styleLinkButton(JButton button) {
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setForeground(new Color(0x1E40A0));
        button.setBackground(new Color(0xE8F0FF));
        button.setBorder(new CompoundBorder(new LineBorder(new Color(0xB7C6F2)), new EmptyBorder(6, 12, 6, 12)));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(0x1E40A0));
        button.setBorder(new CompoundBorder(new LineBorder(new Color(0xB7C6F2)), new EmptyBorder(8, 18, 8, 18)));
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setForeground(Color.BLUE);
        button.setBackground(new Color(0x1E88E5));
        button.setBorder(new EmptyBorder(8, 22, 8, 22));
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }


    private static void setSpinnerEditorReadOnly(JSpinner spinner) {
        if (spinner != null && spinner.getEditor() instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor ed = (JSpinner.DefaultEditor) spinner.getEditor();
            ed.getTextField().setEditable(false);
            ed.getTextField().setFocusable(false);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return currencyFormat.format(amount) + " ₫";
    }

    // ==== Public API ======================================================

    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }
    
    public void setMode(Mode mode) {
        if (mode == null) {
            return;
        }
        this.mode = mode;
        if (mode == Mode.EXCHANGE) {
            promotionCombo.setSelectedItem(null);
            // removed set VAT to 0 when exchange
            selectedPromotionId = null;
        }
        updateModeUI();
        updateExchangePanel();
        recalcTotal();
    }

    public void setEditTicketsAction(Runnable editAction) {
        this.editAction = editAction;
    }

    public void setConfirmAction(Runnable confirmAction) {
        this.confirmAction = confirmAction;
    }
    
    public void setExchangeBreakdown(ExchangeBreakdown breakdown) {
        this.exchangeBreakdown = breakdown;
        updateExchangePanel();
        recalcTotal();
    }

    public ExchangeBreakdown getExchangeBreakdown() {
        return exchangeBreakdown;
    }

    public void setTrainInfo(TrainInfo trainInfo) {
        this.trainInfo = trainInfo;
        updateTrainInfo();
    }

    public void setSelections(List<TicketSelection> selections) {
        this.selections = selections != null ? new ArrayList<>(selections) : new ArrayList<>();
        loadPromotions();
        refreshUI();
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getVatRateDecimal() {
        return VAT_RATE;
    }

    public String getSelectedPromotionId() {
        return selectedPromotionId;
    }

    public BigDecimal getUnitPriceAfterAdjustments() {
        return unitPrice;
    }
    
        private void updateModeUI() {
        boolean isExchange = mode == Mode.EXCHANGE;
        if (promotionLabel != null) {
            promotionLabel.setVisible(!isExchange);
        }
        promotionCombo.setVisible(!isExchange);
        promotionCombo.setEnabled(!isExchange);
        if (vatLabel != null) {
            vatLabel.setVisible(!isExchange);
        }
        vatValueLabel.setVisible(!isExchange);
        // removed: spinner removed
        if (exchangePanel != null) {
            exchangePanel.setVisible(isExchange);
        }
    }

    public static class ExchangeBreakdown {
        private final BigDecimal oldFare;
        private final BigDecimal newFare;
        private final BigDecimal feeRate;
        private final BigDecimal feeAmount;
        private final BigDecimal difference;
        private final BigDecimal payableDifference;
        private final BigDecimal totalPayable;

        public ExchangeBreakdown(BigDecimal oldFare, BigDecimal newFare, BigDecimal feeRate,
                                  BigDecimal feeAmount, BigDecimal difference,
                                  BigDecimal payableDifference, BigDecimal totalPayable) {
            this.oldFare = oldFare != null ? oldFare : BigDecimal.ZERO;
            this.newFare = newFare != null ? newFare : BigDecimal.ZERO;
            this.feeRate = feeRate != null ? feeRate : BigDecimal.ZERO;
            this.feeAmount = feeAmount != null ? feeAmount : BigDecimal.ZERO;
            this.difference = difference != null ? difference : BigDecimal.ZERO;
            this.payableDifference = payableDifference != null ? payableDifference : BigDecimal.ZERO;
            this.totalPayable = totalPayable != null ? totalPayable : BigDecimal.ZERO;
        }

        public BigDecimal getOldFare() {
            return oldFare;
        }

        public BigDecimal getNewFare() {
            return newFare;
        }

        public BigDecimal getFeeRate() {
            return feeRate;
        }

        public String getFeeRatePercentText() {
            BigDecimal percent = feeRate.multiply(BigDecimal.valueOf(100));
            return percent.setScale(0, RoundingMode.HALF_UP).toPlainString() + "%";
        }

        public BigDecimal getFeeAmount() {
            return feeAmount;
        }

        public BigDecimal getDifference() {
            return difference;
        }

        public BigDecimal getPayableDifference() {
            return payableDifference;
        }

        public BigDecimal getTotalPayable() {
            return totalPayable;
        }
    }
}