import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class NewsMonitoringSystem extends JFrame {
    private static final int MAX_ARTICLES = 5;
    private static final int REFRESH_INTERVAL = 5000; // במילישניות

    private List<Article> articles;
    private JTextField keywordField;
    private JTextPane articlePane;
    private HTMLDocument document;

    public NewsMonitoringSystem() {
        super("מערכת צפייה בחדשות בזמן אמת למילות מפתח מוגדרות על ידי המשתמש");
        articles = new ArrayList<>();
        setupUI();
        startMonitoring();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        keywordField = new JTextField(20);
        JButton searchButton = new JButton("חפש");

        articlePane = new JTextPane();
        articlePane.setContentType("text/html");
        articlePane.setEditable(false);
        document = (HTMLDocument) articlePane.getDocument();
        JScrollPane scrollPane = new JScrollPane(articlePane);

        inputPanel.add(new JLabel("מילות מפתח:"));
        inputPanel.add(keywordField);
        inputPanel.add(searchButton);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyword = keywordField.getText();
                if (!keyword.isEmpty()) {
                    performSearch(keyword);
                }
            }
        });

        pack();
        setVisible(true);
    }

    private void performSearch(String keyword) {
        for (String newsSite : getNewsSites()) {
            try {
                Document doc = Jsoup.connect(newsSite).get();
                Elements elements = doc.select("a"); // בחירת כל התגיות של אנקור

                for (Element element : elements) {
                    String title = element.text();
                    String link = element.absUrl("href");

                    if (!title.isEmpty() && title.toLowerCase().contains(keyword.toLowerCase())) {
                        Article article = new Article(newsSite, title, link);
                        updateArticles(article);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateArticlePane();
    }

    private void updateArticles(Article newArticle) {
        if (articles.size() >= MAX_ARTICLES) {
            articles.remove(articles.size() - 1); // הסרת הכתבה הישנה ביותר
        }
        articles.add(0, newArticle); // הוספת הכתבה החדשה בהתחלה
    }

    private void updateArticlePane() {
        try {
            StringBuilder htmlContent = new StringBuilder();
            for (Article article : articles) {
                htmlContent.append("<b>אתר:</b> ").append(article.getWebsite()).append("<br>")
                        .append("<b>כותרת הכתבה:</b> ").append(article.getTitle()).append("<br>")
                        .append("<b>קישור:</b> <a href=\"").append(article.getLink()).append("\">")
                        .append(article.getLink()).append("</a><br><br>");
            }
            articlePane.setText(htmlContent.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMonitoring() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String keyword = keywordField.getText();
                if (!keyword.isEmpty()) {
                    performSearch(keyword);
                }
            }
        }, 0, REFRESH_INTERVAL);
    }

    private List<String> getNewsSites() {
        List<String> newsSites = new ArrayList<>();
        // הוספת כתובות ה־URL של אתרי החדשות שברצונך לצפות בהם
        newsSites.add("https://www.walla.co.il"); // דוגמה לאתר 1
        newsSites.add("https://www.ynet.co.il/home/0,7340,L-8,00.html"); // דוגמה לאתר 2
        newsSites.add("https://hamal.co.il/main"); // דוגמה לאתר 3
//        newsSites.add("https://www.example4.com/news"); // דוגמה לאתר 4
//        newsSites.add("https://www.example5.com/news"); // דוגמה לאתר 5
        return newsSites;
    }

    private class Article {
        private String website;
        private String title;
        private String link;

        public Article(String website, String title, String link) {
            this.website = website;
            this.title = title;
            this.link = link;
        }

        public String getWebsite() {
            return website;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NewsMonitoringSystem();
            }
        });
    }
}
