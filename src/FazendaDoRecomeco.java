import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** A compact pixel-art farming RPG with no external assets. */
@SuppressWarnings("serial")
public final class FazendaDoRecomeco extends JPanel {
    private static final int SCALE = 3;
    private static final int W = 320;
    private static final int H = 180;
    private static final int TILE = 16;
    private static final Color INK = new Color(39, 45, 44);
    private final Set<Integer> held = new HashSet<>();
    private final Plot[] plots = {
        new Plot(112, 72), new Plot(128, 72), new Plot(144, 72), new Plot(160, 72),
        new Plot(112, 88), new Plot(128, 88), new Plot(144, 88), new Plot(160, 88),
        new Plot(112, 104), new Plot(128, 104), new Plot(144, 104), new Plot(160, 104)
    };
    private int px = 61, py = 113, direction = 0, day = 1, wood = 8, seeds = 7, crops;
    private int houseLevel, barnLevel, ticks;
    private boolean chickens, sheep;
    private String message = "Uma fazenda esquecida... e um novo começo.";
    private int messageTime = 260;

    public FazendaDoRecomeco() {
        setPreferredSize(new Dimension(W * SCALE, H * SCALE));
        setFocusable(true);
        setBackground(new Color(123, 187, 104));
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                held.add(e.getKeyCode());
                if (e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_SPACE) interact();
                if (e.getKeyCode() == KeyEvent.VK_R) reset();
            }
            @Override public void keyReleased(KeyEvent e) { held.remove(e.getKeyCode()); }
        });
        new Timer(33, e -> update()).start();
    }

    private void update() {
        int dx = (down(KeyEvent.VK_D) || down(KeyEvent.VK_RIGHT) ? 1 : 0) - (down(KeyEvent.VK_A) || down(KeyEvent.VK_LEFT) ? 1 : 0);
        int dy = (down(KeyEvent.VK_S) || down(KeyEvent.VK_DOWN) ? 1 : 0) - (down(KeyEvent.VK_W) || down(KeyEvent.VK_UP) ? 1 : 0);
        if (dx != 0 || dy != 0) {
            if (Math.abs(dx) > Math.abs(dy)) direction = dx > 0 ? 1 : 3;
            else direction = dy > 0 ? 2 : 0;
            int nx = px + dx, ny = py + dy;
            if (nx > 12 && nx < W - 18 && ny > 38 && ny < H - 18 && !blocked(nx, ny)) { px = nx; py = ny; }
        }
        ticks++;
        if (ticks % 450 == 0) { day++; growCrops(); say("Dia " + day + ": a terra continua florescendo."); }
        if (messageTime > 0) messageTime--;
        repaint();
    }

    private boolean down(int key) { return held.contains(key); }
    private boolean blocked(int x, int y) {
        return (x > 23 && x < 90 && y > 44 && y < 89) || (houseLevel > 0 && x > 205 && x < 276 && y > 91 && y < 151);
    }
    private boolean near(int x, int y) { return Math.abs(px - x) < 18 && Math.abs(py - y) < 18; }
    private void say(String text) { message = text; messageTime = 220; }

    private void growCrops() { for (Plot p : plots) if (p.stage == 2) p.stage = 3; }
    private void reset() {
        px = 61; py = 113; direction = 0; day = 1; wood = 8; seeds = 7; crops = 0;
        houseLevel = barnLevel = 0; chickens = sheep = false;
        for (Plot p : plots) p.stage = 0;
        say("A terra sempre oferece outra chance.");
    }

    private void interact() {
        if (near(55, 52)) { buildHouse(); return; }
        if (near(262, 58)) { buildBarn(); return; }
        if (near(78, 145)) { fish(); return; }
        for (Plot p : plots) if (near(p.x + 8, p.y + 8)) { tend(p); return; }
        say("Nada para fazer aqui. Procure um lote, a oficina, o celeiro ou o lago.");
    }
    private void buildHouse() {
        if (houseLevel == 0 && wood >= 6) { wood -= 6; houseLevel = 1; say("Uma cabana simples. Agora existe um lar."); }
        else if (houseLevel == 1 && wood >= 10 && crops >= 3) { wood -= 10; crops -= 3; houseLevel = 2; say("A velha casa virou um lar acolhedor!"); }
        else say(houseLevel == 2 ? "Seu lar está completo." : "Oficina: 6 madeiras para construir a cabana.");
    }
    private void buildBarn() {
        if (barnLevel == 0 && wood >= 5) { wood -= 5; barnLevel = 1; chickens = true; say("O galinheiro ganhou vida. Cocoricó!"); }
        else if (barnLevel == 1 && wood >= 9 && crops >= 2) { wood -= 9; crops -= 2; barnLevel = 2; sheep = true; say("Um redil novo para as ovelhas. Bééé!"); }
        else say(barnLevel == 2 ? "Os animais parecem felizes." : "Celeiro: traga mais recursos para crescer.");
    }
    private void fish() { wood += 2; say("Você pescou materiais que boiavam no lago. +2 madeira"); }
    private void tend(Plot p) {
        if (p.stage == 0) { p.stage = 1; say("Você arou a terra. Use E novamente para plantar."); }
        else if (p.stage == 1 && seeds > 0) { p.stage = 2; seeds--; say("Sementes plantadas. Elas crescem com o passar dos dias."); }
        else if (p.stage == 2) say("Broto regado. Volte amanhã para colher.");
        else if (p.stage == 3) { p.stage = 0; crops++; wood++; seeds++; say("Colheita farta! +1 alimento, +1 madeira, +1 semente"); }
        else say("Você não tem sementes. Colha uma plantação madura primeiro.");
    }

    @Override protected void paintComponent(Graphics raw) {
        super.paintComponent(raw);
        Graphics2D g = (Graphics2D) raw.create();
        g.scale(SCALE, SCALE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        drawWorld(g); drawHud(g); g.dispose();
    }
    private void drawWorld(Graphics2D g) {
        g.setColor(new Color(116, 177, 91)); g.fillRect(0, 0, W, H);
        for (int y = 40; y < H; y += 16) for (int x = (y / 16 % 2) * 7; x < W; x += 29) grass(g, x, y);
        g.setColor(new Color(235, 186, 112)); g.fillRect(0, 30, W, 8); g.setColor(new Color(199, 146, 84)); g.fillRect(0, 35, W, 3);
        lake(g); ruinedWorkshop(g); farmPlots(g); barn(g); house(g); trees(g); player(g);
    }
    private void grass(Graphics2D g, int x, int y) { g.setColor(new Color(74, 141, 76)); g.fillRect(x, y + 6, 1, 3); g.fillRect(x + 2, y + 5, 1, 4); }
    private void lake(Graphics2D g) {
        g.setColor(new Color(46, 115, 151)); g.fillRoundRect(15, 126, 71, 40, 15, 15);
        g.setColor(new Color(89, 168, 191)); g.fillRect(25, 137, 18, 2); g.fillRect(53, 151, 21, 2); g.fillRect(34, 160, 13, 2);
        g.setColor(new Color(219, 191, 111)); for (int x = 18; x < 82; x += 8) g.fillRect(x, 164 - (x % 3), 5, 2);
    }
    private void ruinedWorkshop(Graphics2D g) {
        g.setColor(new Color(88, 66, 52)); g.fillRect(28, 54, 54, 32); g.setColor(new Color(62, 49, 43)); g.fillRect(23, 48, 65, 10);
        g.setColor(new Color(151, 102, 65)); g.fillRect(34, 62, 10, 23); g.fillRect(64, 62, 10, 23); g.setColor(INK); g.fillRect(49, 70, 12, 16);
        sign(g, 48, 92, "OFICINA");
    }
    private void farmPlots(Graphics2D g) { for (Plot p : plots) plot(g, p); }
    private void plot(Graphics2D g, Plot p) {
        g.setColor(p.stage == 0 ? new Color(124, 156, 75) : new Color(116, 70, 42)); g.fillRect(p.x, p.y, 14, 14);
        g.setColor(new Color(78, 53, 35)); if (p.stage > 0) { g.fillRect(p.x + 2, p.y + 4, 10, 1); g.fillRect(p.x + 1, p.y + 10, 11, 1); }
        if (p.stage == 2 || p.stage == 3) { g.setColor(p.stage == 3 ? new Color(238, 195, 63) : new Color(61, 134, 64)); g.fillRect(p.x + 6, p.y + 4, 2, 8); g.fillRect(p.x + 3, p.y + 6, 3, 2); g.fillRect(p.x + 8, p.y + 5, 3, 2); }
    }
    private void barn(Graphics2D g) {
        int x = 232, y = 48; g.setColor(new Color(144, 65, 49)); g.fillRect(x, y + 10, 54, 37); g.setColor(new Color(94, 53, 48)); g.fillRect(x - 4, y + 4, 62, 10);
        g.setColor(new Color(244, 211, 142)); g.fillRect(x + 22, y + 26, 10, 21); g.setColor(INK); g.fillRect(x + 26, y + 32, 2, 2); sign(g, 240, 93, "CELEIRO");
        if (chickens) chicken(g, 244, 105); if (chickens) chicken(g, 274, 113); if (sheep) sheep(g, 256, 128);
    }
    private void house(Graphics2D g) {
        if (houseLevel == 0) return;
        int x = 210, y = 108; g.setColor(new Color(205, 143, 77)); g.fillRect(x, y + 12, 58, 37); g.setColor(new Color(127, 61, 50)); g.fillRect(x - 5, y + 4, 68, 13);
        g.setColor(new Color(85, 58, 43)); g.fillRect(x + 23, y + 29, 12, 20); g.setColor(new Color(130, 203, 218)); g.fillRect(x + 7, y + 25, 10, 8); g.fillRect(x + 42, y + 25, 10, 8);
        if (houseLevel == 2) { g.setColor(new Color(240, 212, 106)); g.fillRect(x + 27, y + 31, 4, 4); g.setColor(new Color(190, 67, 50)); g.fillRect(x + 47, y - 4, 5, 12); }
    }
    private void trees(Graphics2D g) { tree(g, 93, 49); tree(g, 188, 55); tree(g, 294, 104); tree(g, 187, 151); }
    private void tree(Graphics2D g, int x, int y) { g.setColor(new Color(92, 63, 38)); g.fillRect(x + 6, y + 14, 5, 13); g.setColor(new Color(47, 113, 61)); g.fillRect(x, y + 4, 17, 15); g.fillRect(x + 3, y, 11, 23); g.setColor(new Color(67, 139, 66)); g.fillRect(x + 4, y + 2, 8, 7); }
    private void player(Graphics2D g) { g.setColor(INK); g.fillRect(px + 3, py + 2, 8, 8); g.setColor(new Color(239, 191, 141)); g.fillRect(px + 4, py + 3, 6, 6); g.setColor(new Color(75, 100, 164)); g.fillRect(px + 3, py + 10, 8, 5); g.setColor(new Color(67, 49, 40)); g.fillRect(px + 2, py, 10, 4); }
    private void chicken(Graphics2D g, int x, int y) { g.setColor(Color.WHITE); g.fillRect(x, y, 7, 6); g.setColor(new Color(225, 77, 47)); g.fillRect(x + 5, y - 2, 2, 2); g.setColor(new Color(239, 174, 45)); g.fillRect(x + 7, y + 2, 2, 2); }
    private void sheep(Graphics2D g, int x, int y) { g.setColor(new Color(239, 239, 222)); g.fillRect(x, y, 12, 8); g.setColor(INK); g.fillRect(x + 9, y + 2, 4, 5); g.fillRect(x + 2, y + 8, 2, 3); g.fillRect(x + 8, y + 8, 2, 3); }
    private void sign(Graphics2D g, int x, int y, String text) { g.setColor(new Color(92, 63, 38)); g.fillRect(x + 10, y, 2, 9); g.setColor(new Color(228, 205, 139)); g.fillRect(x, y, 23, 7); g.setColor(INK); g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 4)); g.drawString(text, x + 1, y + 5); }
    private void drawHud(Graphics2D g) {
        g.setColor(new Color(28, 42, 43)); g.fillRect(6, 5, 308, 20); g.setColor(new Color(246, 227, 171)); g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 7));
        g.drawString("FAZENDA DO RECOMECO", 12, 14); g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 6));
        g.drawString("DIA " + day + "   MADEIRA " + wood + "   SEMENTES " + seeds + "   COLHEITA " + crops, 12, 22);
        if (messageTime > 0) { g.setColor(new Color(28, 42, 43, 225)); g.fillRect(14, 157, 292, 16); g.setColor(new Color(255, 238, 185)); g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 6)); g.drawString(message, 20, 167); }
        g.setColor(new Color(31, 58, 43)); g.fillRect(230, 173, 85, 5); g.setColor(new Color(245, 231, 176)); g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 4)); g.drawString("WASD MOVER | E AGIR", 231, 177);
    }
    private static final class Plot { final int x, y; int stage; Plot(int x, int y) { this.x = x; this.y = y; } }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Fazenda do Recomeço"); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new FazendaDoRecomeco()); frame.setResizable(false); frame.pack(); frame.setLocationRelativeTo(null); frame.setVisible(true);
        });
    }
}
