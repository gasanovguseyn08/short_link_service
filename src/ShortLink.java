import java.time.LocalDateTime;

class UrlShortener {
    private final String originalUrl;
    private int maxVisits;
    private int visitCount;
    private LocalDateTime expiryDate;

    public UrlShortener(String originalUrl, int maxVisits, LocalDateTime expiryDate) {
        this.originalUrl = originalUrl;
        this.maxVisits = maxVisits;
        this.visitCount = 0;
        this.expiryDate = expiryDate;
    }

    public boolean registerVisit() {
        if (visitCount < maxVisits) {
            visitCount++;
            return true;
        }
        return false;
    }

    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void updateMaxVisits(int maxVisits) {
        this.maxVisits = maxVisits;
    }

    public void updateExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String retrieveOriginalUrl() {
        return originalUrl;
    }
}
