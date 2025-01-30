import java.awt.Desktop;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class ShortLinkService {

    private static final Map<String, User> userMap = new HashMap<>();
    private static final Properties configSettings = new Properties();

    static {
        try (InputStream input = new FileInputStream("config.properties")) {
            configSettings.load(input);
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке настроек: " + e.getMessage());
        }
    }

    private static final int DEFAULT_LINK_LIFETIME = Integer.parseInt(configSettings.getProperty("defaultLifetimeHours", "24"));
    private static final int DEFAULT_MAX_CLICKS = Integer.parseInt(configSettings.getProperty("defaultMaxTransitions", "10"));

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Добро пожаловать в сервис сокращения ссылок!");

        while (true) {
            System.out.println("Выберите действие: 1 - Регистрация, 2 - Сократить ссылку, 3 - Редактировать, 4 - Удалить, 5 - Открыть ссылку, 0 - Выход");
            String choice = scanner.nextLine().toLowerCase();

            switch (choice) {
                case "1":
                    System.out.print("Введите имя пользователя для регистрации: ");
                    String username = scanner.nextLine();
                    User newUser = registerUser(username);
                    System.out.println("Регистрация успешна! Ваш UUID: " + newUser.getUuid());
                    break;

                case "2":
                    System.out.print("Введите ваш UUID: ");
                    String uuid = scanner.nextLine();
                    User user = userMap.get(uuid);
                    if (user == null) {
                        System.out.println("Ошибка: Неверный UUID.");
                        break;
                    }

                    System.out.print("Введите ссылку для сокращения: ");
                    String originalUrl = scanner.nextLine();

                    System.out.print("Введите максимальное число переходов (или Enter для значения по умолчанию): ");
                    String maxClicksInput = scanner.nextLine();
                    int maxClicks = maxClicksInput.isEmpty() ? DEFAULT_MAX_CLICKS : Integer.parseInt(maxClicksInput);

                    String shortUrl = user.createShortLink(originalUrl, maxClicks, DEFAULT_LINK_LIFETIME);
                    System.out.println("Ваша короткая ссылка: " + shortUrl);
                    break;

                case "3":
                    System.out.print("Введите ваш UUID: ");
                    uuid = scanner.nextLine();
                    user = userMap.get(uuid);
                    if (user == null) {
                        System.out.println("Ошибка: Неверный UUID.");
                        break;
                    }

                    System.out.print("Введите короткую ссылку для редактирования: ");
                    String shortUrlToUpdate = scanner.nextLine();

                    System.out.print("Введите новое максимальное количество переходов: ");
                    int newMaxClicks = Integer.parseInt(scanner.nextLine());

                    System.out.print("Введите новый срок действия (в часах): ");
                    int newLifetime = Integer.parseInt(scanner.nextLine());

                    boolean isUpdated = user.editShortLink(shortUrlToUpdate, newMaxClicks, newLifetime);
                    System.out.println(isUpdated ? "Ссылка успешно обновлена!" : "Ошибка при обновлении.");
                    break;

                case "4":
                    System.out.print("Введите ваш UUID: ");
                    uuid = scanner.nextLine();
                    user = userMap.get(uuid);
                    if (user == null) {
                        System.out.println("Ошибка: Неверный UUID.");
                        break;
                    }

                    System.out.print("Введите короткую ссылку для удаления: ");
                    String shortUrlToDelete = scanner.nextLine();

                    boolean isDeleted = user.deleteShortLink(shortUrlToDelete);
                    System.out.println(isDeleted ? "Ссылка удалена." : "Ошибка при удалении.");
                    break;

                case "5":
                    System.out.print("Введите короткую ссылку для открытия: ");
                    String shortUrlToOpen = scanner.nextLine();
                    openLink(shortUrlToOpen);
                    break;

                case "0":
                    System.out.println("Выход из программы. До свидания!");
                    return;

                default:
                    System.out.println("Ошибка: Неверный ввод. Попробуйте снова.");
            }
        }
    }

    private static User registerUser(String username) {
        User user = new User(username);
        userMap.put(user.getUuid(), user);
        return user;
    }

    public static String generateShortLink(String originalUrl) {
        try {
            URL url = new URL("https://clck.ru/--");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "text/plain");

            String requestBody = "url=" + originalUrl;
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes());
                os.flush();
            }

            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                if (scanner.hasNext()) {
                    return scanner.nextLine();
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при создании короткой ссылки: " + e.getMessage());
        }
        return null;
    }

    private static void openLink(String shortUrl) {
        for (User user : userMap.values()) {
            String originalUrl = user.getOriginalUrl(shortUrl);
            if (originalUrl != null) {
                try {
                    Desktop.getDesktop().browse(new URI(originalUrl));
                    System.out.println("Переход по ссылке: " + originalUrl);
                    return;
                } catch (Exception e) {
                    System.out.println("Ошибка при открытии ссылки: " + e.getMessage());
                }
            }
        }
        System.out.println("Ошибка: Ссылка не найдена или её срок действия истёк.");
    }
}
