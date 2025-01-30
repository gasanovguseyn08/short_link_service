import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class User {
    private final String name;
    private final String userId;
    private final Map<String, ShortLink> linkStorage = new HashMap<>();

    public User(String name) {
        this.name = name;
        this.userId = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return userId;
    }

    public String generateShortLink(String originalUrl, int maxClicks, int lifetimeHours) {
        String shortUrl = ShortLinkService.generateShortLink(originalUrl);
        if (shortUrl == null) {
            System.err.println("Ошибка: не удалось создать короткую ссылку для " + originalUrl);
            return null;
        }

        LocalDateTime expiryTime = LocalDateTime.now().plusHours(lifetimeHours);
        linkStorage.put(shortUrl, new ShortLink(originalUrl, maxClicks, expiryTime));
        return shortUrl;
    }

    public boolean updateShortLink(String shortUrl, int updatedMaxClicks, int updatedLifetime) {
        ShortLink link = linkStorage.get(shortUrl);
        if (link != null && !link.isExpired()) {
            link.setMaxTransitions(updatedMaxClicks);
            link.setExpiration(LocalDateTime.now().plusHours(updatedLifetime));
            return true;
        }
        return false;
    }

    public boolean removeShortLink(String shortUrl) {
        return linkStorage.remove(shortUrl) != null;
    }

    public String retrieveOriginalUrl(String shortUrl) {
        ShortLink link = linkStorage.get(shortUrl);
        if (link != null && !link.isExpired() && link.incrementTransitionCount()) {
            return link.getLongUrl();
        }
        return null;
    }
}
